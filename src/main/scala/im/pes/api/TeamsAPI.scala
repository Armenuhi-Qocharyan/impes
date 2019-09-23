package im.pes.api

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.dwickern.macros.NameOf.nameOf
import im.pes.constants.{CommonConstants, Paths}
import im.pes.db.{ActiveGames, Players, Teams, Transactions, Users}
import im.pes.db.Teams.{addTeamSchema, addTeamWithDefaultSchema, teamsConstants, updateTeamSchema, updateTeamWithDefaultSchema}
import im.pes.db.Users.usersConstants
import im.pes.utils.DBUtils
import org.apache.spark.sql.functions

object TeamsAPI {

  def getRoute: Route =
    path(Paths.teams) {
      get {
        parameterMap { params =>
          complete(getTeams(params))
        }
      } ~
        post {
          headerValueByName(CommonConstants.token) { token =>
            entity(as[String]) { team =>
              complete(addTeam(team, token))
            }
          }
        }
    } ~
      path(Paths.teams / IntNumber) { id =>
        get {
          rejectEmptyResponse {
            complete(getTeam(id))
          }
        } ~
          put {
            headerValueByName(CommonConstants.token) { token =>
              entity(as[String]) { team =>
                complete(updateTeam(id, team, token))
              }
            }
          } ~
          delete {
            headerValueByName(CommonConstants.token) { token =>
              complete(deleteTeam(id, token))
            }
          }
      }

  def getTeams(params: Map[String, String]): ToResponseMarshallable = {
    Teams.getTeams(params)
  }

  def getTeam(id: Int): ToResponseMarshallable = {
    Teams.getTeam(id)
  }

  def addTeam(team: String, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token).getOrElse(return StatusCodes.Unauthorized)
    val isAdmin = DBUtils.isAdmin(userId)
    if (!isAdmin && Teams.getUserTeamId(userId).isDefined) {
      return StatusCodes.BadRequest
    }
    val teamDf = if (isAdmin) {
      DBUtils.dataToDf(addTeamWithDefaultSchema, team)
    } else {
      DBUtils.dataToDf(addTeamSchema, team).withColumn(nameOf(teamsConstants.isDefault), functions.lit(false))
    }
    val teamData = teamDf.first
    if (teamData.anyNull) {
      StatusCodes.BadRequest
    } else if (userId == teamData.getAs[Int](nameOf(teamsConstants.owner))) {
      //TODO check fields values
      val user = Users.getUserData(userId).get
      val teamBudget = teamData.getAs[Int](nameOf(teamsConstants.budget))
      val userBudget = user.getAs[Int](usersConstants.budget)
      if (teamBudget > userBudget) {
        StatusCodes.BadRequest
      } else {
        Teams.addTeam(teamDf)
        Users.updateUser(userId, Map(usersConstants.budget -> (userBudget - teamBudget)))
        StatusCodes.OK
      }
    } else {
      StatusCodes.Forbidden
    }
  }

  def updateTeam(id: Int, updateTeam: String, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (userId.isEmpty) {
      StatusCodes.Unauthorized
    } else if (DBUtils.isAdmin(userId.get)) {
      val updateTeamDf = DBUtils.dataToDf(updateTeamWithDefaultSchema, updateTeam)
      //TODO check fields values
      Teams.updateTeam(id, updateTeamDf)
      StatusCodes.NoContent
    } else if (Teams.checkTeam(id, userId.get)) {
      val updateTeamDf = DBUtils.dataToDf(updateTeamSchema, updateTeam)
      //TODO check what user may update
      //TODO check fields values
      val updateTeamData = updateTeamDf.first
      val owner = Option(updateTeamData.getAs[Int](nameOf(teamsConstants.owner)))
      if (owner.isDefined && owner.get != userId.get) {
        StatusCodes.BadRequest
      } else {
        Teams.updateTeam(id, updateTeamDf)
        StatusCodes.NoContent
      }
    } else {
      StatusCodes.Forbidden
    }
  }

  def deleteTeam(id: Int, token: String): ToResponseMarshallable = {
    //TODO check championship state
    val userId = DBUtils.getIdByToken(token)
    if (userId.isEmpty) {
      StatusCodes.Unauthorized
    } else if (ActiveGames.isTeamInGame(id)) {
      StatusCodes.Conflict
    } else if (DBUtils.isAdmin(userId.get)) {
      for (player <- Players.getTeamPlayers(id)) {
        Players.deletePlayer(player.getInt(0), player.getInt(1))
      }
      Teams.deleteTeam(id)
      StatusCodes.NoContent
    } else if (Teams.checkTeam(id, userId.get)) {
      if (Teams.isDefaultTeam(id)) {
        val team = Teams.getTeamData(id).get
        Teams.updateTeam(id, Map(teamsConstants.owner -> Users.getAdminId))
        Transactions.deleteTeamTransactionByTeamId(id)
        Transactions.addTeamTransaction(id, team.getAs[Int](teamsConstants.budget))
      } else {
        for (player <- Players.getTeamPlayers(id)) {
          Players.deletePlayer(player.getInt(0), player.getInt(1))
        }
        Teams.deleteTeam(id)
      }
      val user = Users.getUserData(userId.get).get
      Users.updateUser(userId.get,
        Map(usersConstants.budget -> (user.getAs[Int](usersConstants.budget) + 11 * CommonConstants.playerMinCost)))
      StatusCodes.NoContent
    } else {
      StatusCodes.Forbidden
    }
  }

}
