package im.pes.api

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.dwickern.macros.NameOf.nameOf
import im.pes.constants.{CommonConstants, Paths}
import im.pes.db.Players._
import im.pes.db.Teams.teamsConstants
import im.pes.db.{ActiveGames, Players, Teams, Transactions}
import im.pes.utils.DBUtils
import org.apache.spark.sql.functions

object PlayersAPI {

  def getRoute: Route =
    path(Paths.players) {
      get {
        parameterMap { params =>
          complete(getPlayers(params))
        }
      } ~
        post {
          headerValueByName(CommonConstants.token) { token =>
            entity(as[String]) { player =>
              complete(addPlayer(player, token))
            }
          }
        }
    } ~
      path(Paths.players / IntNumber) { id =>
        get {
          rejectEmptyResponse {
            complete(getPlayer(id))
          }
        } ~
          delete {
            headerValueByName(CommonConstants.token) { token =>
              complete(deletePlayer(id, token))
            }
          } ~
          put {
            headerValueByName(CommonConstants.token) { token =>
              entity(as[String]) { player =>
                complete(updatePlayer(id, player, token))
              }
            }
          }
      }


  def getPlayers(params: Map[String, String]): ToResponseMarshallable = {
    Players.getPlayers(params)
  }

  def getPlayer(id: Int): ToResponseMarshallable = {
    Players.getPlayer(id)
  }

  def addPlayer(player: String, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token).getOrElse(return StatusCodes.Unauthorized)
    val playerDf = if (DBUtils.isAdmin(userId)) {
      DBUtils.dataToDf(addPlayerWithDefaultSchema, player)
    } else {
      DBUtils.dataToDf(addPlayerSchema, player).withColumn(nameOf(playersConstants.isDefault), functions.lit(false))
    }
    val playerData = playerDf.first
    if (playerData.anyNull) {
      StatusCodes.BadRequest
    }
    val teamId = playerData.getAs[Int](nameOf(playersConstants.teamId))
    if (Teams.checkTeam(teamId, userId)) {
      //TODO check fields values
      val teamData = Teams.getTeamData(teamId)
      val teamBudget = teamData.getOrElse(return StatusCodes.BadRequest).getAs[Int](nameOf(teamsConstants.budget))
      val skills = Players.calculateSkills(playerData.getAs[Int](nameOf(playersConstants.gameIntelligence)),
        playerData.getAs[Int](nameOf(playersConstants.teamPlayer)),
        playerData.getAs[Int](nameOf(playersConstants.physique)))
      val cost = Players.calculateCost(skills, playerData.getAs[Int](nameOf(playersConstants.age)))
      if (cost > teamBudget) return StatusCodes.BadRequest
      Players.addPlayer(playerDf, cost, skills)
      Teams.updateTeam(teamId, Map(teamsConstants.budget -> (teamBudget - cost)))
      StatusCodes.OK
    } else {
      StatusCodes.Forbidden
    }
  }

  def updatePlayer(id: Int, updatePlayer: String, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (userId.isEmpty) {
      StatusCodes.Unauthorized
    } else if (!Players.checkPlayerExist(id)) {
      StatusCodes.BadRequest
    } else if (DBUtils.isAdmin(userId.get)) {
      val updateTeamDf = DBUtils.dataToDf(updatePlayerWithDefaultSchema, updatePlayer)
      //TODO check fields values
      Players.updatePlayer(id, updateTeamDf)
      StatusCodes.OK
    } else if (Players.checkPlayer(id, userId.get)) {
      val updateTeamDf = DBUtils.dataToDf(updatePlayerSchema, updatePlayer)
      val teamId = Option(updateTeamDf.first.getAs[Int](nameOf(playersConstants.teamId)))
      //TODO check fields values
      //TODO check what user may update
      if (teamId.isDefined) {
        StatusCodes.BadRequest
      } else {
        Players.updatePlayer(id, updateTeamDf)
        StatusCodes.NoContent
      }
    } else {
      StatusCodes.Forbidden
    }
  }

  def deletePlayer(id: Int, token: String): ToResponseMarshallable = {
    //TODO check championship state
    val userId = DBUtils.getIdByToken(token)
    if (userId.isEmpty) {
      StatusCodes.Unauthorized
    } else if (ActiveGames.isPlayerInGame(id)) {
      StatusCodes.Conflict
    } else if (DBUtils.isAdmin(userId.get)) {
      Players.deletePlayer(id)
      StatusCodes.OK
    } else {
      val playerData = Players.getPlayerData(id).getOrElse(return StatusCodes.BadRequest)
      val teamData = Teams.getTeamData(playerData.getAs[Int](playersConstants.teamId))
      if (teamData.isEmpty) {
        Players.deletePlayer(id)
        StatusCodes.OK
      } else if (teamData.get.getAs[Int](teamsConstants.owner) == userId.get) {
        val teamId = teamData.get.getAs[Int](teamsConstants.id)
        if (Transactions.checkTeamTransaction(teamId)) {
          StatusCodes.Conflict
        } else {
          Teams.updateTeam(teamData.get.getAs[Int](teamsConstants.id),
            Map(teamsConstants.budget ->
              (teamData.get.getAs[Int](teamsConstants.budget) + CommonConstants.playerMinCost)))
          Players.deletePlayer(id, playerData.getAs[Int](playersConstants.cost))
          StatusCodes.OK
        }
      } else {
        StatusCodes.Forbidden
      }
    }
  }

}
