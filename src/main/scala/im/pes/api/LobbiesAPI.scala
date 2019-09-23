package im.pes.api

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.dwickern.macros.NameOf.nameOf
import im.pes.constants.{CommonConstants, Paths}
import im.pes.db.Lobbies.{addLobbySchema, lobbiesConstants}
import im.pes.db.{ActiveGames, Lobbies, Teams}
import im.pes.utils.DBUtils

object LobbiesAPI {

  def getRoute: Route =
    path(Paths.lobbies) {
      get {
        parameterMap { params =>
          complete(getLobbies(params))
        }
      } ~
        post {
          entity(as[String]) { lobby =>
            headerValueByName(CommonConstants.token) { token =>
              complete(addLobby(lobby, token))
            }
          }
        }
    } ~
      path(Paths.lobbies / IntNumber) { id =>
        get {
          rejectEmptyResponse {
            complete(getLobby(id))
          }
        } ~
          post {
            headerValueByName(CommonConstants.token) { token =>
              complete(joinToLobby(id, token))
            }
          } ~
          delete {
            headerValueByName(CommonConstants.token) { token =>
              complete(deleteLobby(id, token))
            }
          }
      } ~
      path(Paths.lobbies / IntNumber / Paths.confirm) { id =>
          post {
            headerValueByName(CommonConstants.token) { token =>
              complete(confirmLobby(id, token))
            }
          }
      } ~
      path(Paths.lobbies / IntNumber / Paths.reject / IntNumber) { (id, teamId) =>
        post {
          headerValueByName(CommonConstants.token) { token =>
            complete(rejectLobbyTeam(id, teamId, token))
          }
        }
      } ~
      path(Paths.lobbies / IntNumber / Paths.leave) { id =>
        post {
          headerValueByName(CommonConstants.token) { token =>
            complete(leaveLobby(id, token))
          }
        }
      }

  def getLobbies(params: Map[String, String]): ToResponseMarshallable = {
    Lobbies.getLobbies(params)
  }

  def getLobby(id: Int): ToResponseMarshallable = {
    Lobbies.getLobby(id)
  }

  def addLobby(lobby: String, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (userId.isEmpty) {
      StatusCodes.Unauthorized
    } else {
      val teamId = Teams.getUserTeamId(userId.get)
      if (teamId.isEmpty) {
        StatusCodes.BadRequest
      } else {
        val lobbyDf = DBUtils.dataToDf(addLobbySchema, lobby)
        val lobbyData = lobbyDf.first
        if (lobbyData.anyNull) {
          StatusCodes.BadRequest
        } else if (lobbyData.getAs[Int](nameOf(lobbiesConstants.owner)) == userId.get) {
          val lobbyId = Lobbies.addLobby(lobbyDf)
          Lobbies.addLobbyTeam(lobbyId, teamId.get)
          StatusCodes.OK
        } else {
          StatusCodes.Forbidden
        }
      }
    }
  }

  def joinToLobby(id: Int, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (userId.isEmpty) {
      StatusCodes.Unauthorized
    } else if (Lobbies.isLobbyFull(id)) {
      StatusCodes.BadRequest
    } else {
      val teamId = Teams.getUserTeamId(userId.get)
      if (teamId.isEmpty) {
        StatusCodes.BadRequest
      } else {
        Lobbies.addLobbyTeam(id, teamId.get)
        StatusCodes.OK
      }
    }
  }

  def confirmLobby(id: Int, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (userId.isEmpty) {
      StatusCodes.Unauthorized
    } else if (Lobbies.checkLobby(id, userId.get) && Lobbies.isLobbyFull(id)) {
      val lobbyTeamsIds = Lobbies.getLobbyTeamsIds(id)
      val gameId = ActiveGames.addActiveGame(lobbyTeamsIds.head, lobbyTeamsIds(1))
      Lobbies.updateLobby(id, Map(lobbiesConstants.gameId -> gameId))
      Lobbies.deleteLobbyTeam(lobbyTeamsIds.head)
      Lobbies.deleteLobbyTeam(lobbyTeamsIds(1))
      StatusCodes.OK
    } else {
      StatusCodes.BadRequest
    }
  }

  def rejectLobbyTeam(id: Int, teamId: Int, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (userId.isEmpty) {
      StatusCodes.Unauthorized
    } else if (Lobbies.checkLobby(id, userId.get) && Lobbies.checkTeamInLobby(id, teamId) && !Lobbies.isLobbyConfirmed(id)) {
      Lobbies.deleteLobbyTeam(teamId)
      StatusCodes.OK
    } else {
      StatusCodes.BadRequest
    }
  }

  def leaveLobby(id: Int, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (userId.isEmpty) {
      StatusCodes.Unauthorized
    } else {
      val teamId = Teams.getUserTeamId(userId.get)
      if (teamId.isDefined && Lobbies.checkTeamInLobby(id, teamId.get) && !Lobbies.isLobbyConfirmed(id)) {
        Lobbies.deleteLobbyTeam(teamId.get)
        StatusCodes.OK
      } else {
        StatusCodes.BadRequest
      }
    }
  }

  def deleteLobby(id: Int, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (userId.isEmpty) {
      StatusCodes.Unauthorized
    } else if (Lobbies.checkLobby(id, userId.get)) {
      Lobbies.deleteLobby(id)
      StatusCodes.OK
    } else {
      StatusCodes.BadRequest
    }
  }

}
