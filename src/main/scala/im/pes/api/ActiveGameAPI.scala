package im.pes.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.dwickern.macros.NameOf.nameOf
import im.pes.constants.{ActivityTypes, CommonConstants, Paths}
import im.pes.db.ActiveGames._
import im.pes.db.{ActiveGames, Lobbies, Players, PlayersPositions, Teams}
import im.pes.utils.DBUtils
import spray.json.DefaultJsonProtocol


object ActiveGameAPI extends SprayJsonSupport with DefaultJsonProtocol {

  def getRoute: Route =
    path(Paths.games / IntNumber / Paths.playersData) { gameId =>
      get {
        complete(getActiveGamePlayersData(gameId))
      } ~
      post {
        headerValueByName(CommonConstants.token) { token =>
          entity(as[String]) { players =>
            complete(addTeamPlayers(gameId, players, token))
          }
        }
      }
    } ~
      path(Paths.games / IntNumber / Paths.teamsData) { gameId =>
        get {
          complete(getActiveGameTeamsData(gameId))
        }
      } ~
      path(Paths.games / IntNumber / Paths.playersData / IntNumber) { (gameId, activityId) =>
        get {
          complete(getActiveGamePlayersActivities(gameId, activityId))
        }
      } ~
      path(Paths.games / IntNumber / IntNumber) { (gameId, playerId) =>
        post {
          headerValueByName(CommonConstants.token) { token =>
            entity(as[String]) { activity =>
              complete(addActivity(gameId, playerId, activity, token))
            }
          }
        }
      } ~
      path(Paths.games / IntNumber / Paths.replacePlayer / IntNumber / IntNumber) { (gameId, replacePlayerId, replaceToPlayerId) =>
        post {
          headerValueByName(CommonConstants.token) { token =>
            complete(replacePlayer(gameId, replacePlayerId, replaceToPlayerId, token))
          }
        }
      } ~
      path(Paths.games / IntNumber / Paths.summary / IntNumber) { (gameId, playerId) =>
        put {
          headerValueByName(CommonConstants.token) { token =>
            entity(as[List[String]]) { summary =>
              complete(updateSummary(gameId, playerId, summary, token))
            }
          }
        }
      }

  def getActiveGamePlayersData(gameId: Int): ToResponseMarshallable = {
    ActiveGames.getActiveGamePlayersData(gameId)
  }

  def getActiveGameTeamsData(gameId: Int): ToResponseMarshallable = {
    ActiveGames.getActiveGameTeamsData(gameId)
  }

  def getActiveGamePlayersActivities(gameId: Int, activityId: Int): ToResponseMarshallable = {
    ActiveGames.getActiveGamePlayersActivities(gameId, activityId)
  }

  def addActivity(gameId: Int, playerId: Int, activity: String, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (userId.isEmpty) {
      StatusCodes.Unauthorized
    } else if (!Players.checkPlayer(playerId, userId.get)) {
      StatusCodes.Forbidden
    } else if (!ActiveGames.isPlayerActive(playerId)) {
      StatusCodes.BadRequest
    } else {
        val addActivity =
          DBUtils.dataToDf(addActivitySchema, activity).first
            .getAs[String](activitiesConstants.activityType) match {
            case ActivityTypes.run => DBUtils.dataToDf(addRunActivitySchema, activity)
            case ActivityTypes.stay => DBUtils.dataToDf(addStayActivitySchema, activity)
            case ActivityTypes.shot => DBUtils.dataToDf(addShotActivitySchema, activity)
            case ActivityTypes.pass => DBUtils.dataToDf(addPassActivitySchema, activity)
            case ActivityTypes.tackle => DBUtils.dataToDf(addTackleActivitySchema, activity)
            case _ => return StatusCodes.BadRequest
          }
      if (addActivity.first.anyNull) {
        StatusCodes.BadRequest
      } else {
        ActiveGames.addActivity(gameId, playerId, addActivity)
        StatusCodes.NoContent
      }
    }
  }

  def addTeamPlayers(gameId: Int, teamPlayers: String, token: String): ToResponseMarshallable = {
    val teamPlayersDf = DBUtils.dataToDf(addTeamPlayersSchema, teamPlayers).na.drop()
    val teamId = Teams.getUserTeamId(DBUtils.getIdByToken(token).getOrElse(return StatusCodes.Unauthorized)).get
    val activeGameReadyTeamsCount = ActiveGames.getActiveGameReadyTeamsCount(gameId)
    val x = if (activeGameReadyTeamsCount == 0) 0 else 100
    if (ActiveGames.isActiveGameTeamNotReady(gameId, teamId)) {
      for (teamPlayer <- teamPlayersDf.collect()) {
        val playerId = teamPlayer.getAs[Int](nameOf(activeGamesPlayersDataConstants.playerId))
        val playerPosition = teamPlayer.getAs[String](ActiveGames.playerPosition)
        //TODO check player in team
        if (playerPosition.equals("reserve")) {
          ActiveGames.addActiveGameReservePlayer(gameId, playerId)
        } else {
          val coordinates = PlayersPositions.getPlayerPositionCoordinates(playerPosition)
          ActiveGames.addActiveGamePlayerData(gameId, playerId)
          if (coordinates.isDefined) {
            ActiveGames.addStayActivity(gameId, playerId, math.abs(coordinates.get.getInt(0) - x), coordinates.get.getInt(1))
          } else {
            ActiveGames.addStayActivity(gameId, playerId, 0, 0)
          }
        }
      }
      ActiveGames.updateActiveGameTeam(teamId, Map(activeGamesTeamsDataConstants.isReady -> true))
      if (activeGameReadyTeamsCount == 1) {
        Lobbies.deleteGameLobby(gameId)
        ActiveGames.updateActiveGame(gameId, Map(activeGamesConstants.startTimestamp -> System.currentTimeMillis()))
      }
      StatusCodes.OK
    } else {
      StatusCodes.BadRequest
    }
  }

  def replacePlayer(gameId: Int, replacePlayerId: Int, replaceToPlayerId: Int,
                    token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (userId.isEmpty) {
      StatusCodes.Unauthorized
    } else if (!Players.checkPlayer(replacePlayerId, userId.get) || !Players.checkPlayer(replaceToPlayerId, userId.get)) {
      StatusCodes.Forbidden
    } else if (!ActiveGames.isPlayerActive(replacePlayerId) || !ActiveGames.checkReservePlayer(replaceToPlayerId)) {
      StatusCodes.BadRequest
    } else {
      ActiveGames.addActiveGamePlayerData(gameId, replaceToPlayerId)
      ActiveGames.deleteReservePlayer(replaceToPlayerId)
      ActiveGames.deactivatePlayer(replacePlayerId)
      ActiveGames.addStayActivity(gameId, replaceToPlayerId, 0, 0)
      StatusCodes.NoContent
    }
  }

  def updateSummary(gameId: Int, playerId: Int, summary: List[String], token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (userId.isEmpty) {
      StatusCodes.Unauthorized
    } else if (!Players.checkPlayer(playerId, userId.get)) {
      StatusCodes.Forbidden
    } else if (!ActiveGames.isPlayerActive(playerId)) {
      StatusCodes.BadRequest
    } else {
      if (summary.contains(nameOf(summaryConstants.redCard))) {
        ActiveGames.deactivatePlayer(playerId)
      }
      ActiveGames.updateSummary(playerId, summary)
      StatusCodes.NoContent
    }
  }

}