package im.pes.api

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.dwickern.macros.NameOf.nameOf
import im.pes.constants.{CommonConstants, Paths}
import im.pes.db.{Players, Teams, Transactions, Users}
import im.pes.db.Teams.{addTeamSchema, teamsConstants, updateTeamSchema}
import im.pes.db.Users.usersConstants
import im.pes.utils.DBUtils

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
          complete(getTeam(id))
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
    val team = Teams.getTeam(id)
    if (null == team) {
      StatusCodes.NotFound
    } else {
      team
    }
  }

  def addTeam(team: String, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    val teamDf =
      try {
        DBUtils.dataToDf(addTeamSchema, team)
      } catch {
        case _: NullPointerException => return StatusCodes.BadRequest
      }
    val teamData = teamDf.collect()(0)
    if (userId == teamData.getAs[Int](nameOf(teamsConstants.owner))) {
      //TODO check fields values
      val user = Users.getUserData(userId)
      val teamBudget = teamData.getAs[Int](nameOf(teamsConstants.budget))
      val userBudget = user.getAs[Int](usersConstants.budget)
      if (null == user || teamBudget > userBudget) {
        return StatusCodes.BadRequest
      }
      Teams.addTeam(teamDf)
      Users.updateUser(userId, Map(usersConstants.budget -> (userBudget - teamBudget)))
      StatusCodes.OK
    } else {
      StatusCodes.Forbidden
    }
  }

  def updateTeam(id: Int, updateTeam: String, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    val updateTeamDf = DBUtils.dataToDf(updateTeamSchema, updateTeam)
    if (DBUtils.isAdmin(userId)) {
      //TODO check fields values
      Teams.updateTeam(id, updateTeamDf)
      return StatusCodes.NoContent
    }
    if (Teams.checkTeam(id, userId)) {
      //TODO check what user may update
      //TODO check fields values
      val updateTeamData = updateTeamDf.collect()(0)
      val owner = Option(updateTeamData.getAs[Int](nameOf(teamsConstants.owner)))
      if (owner.isDefined && owner.get != userId) {
        return StatusCodes.BadRequest
      }
      Teams.updateTeam(id, updateTeamDf)
      StatusCodes.NoContent
    } else {
      StatusCodes.Forbidden
    }
  }

  def deleteTeam(id: Int, token: String): ToResponseMarshallable = {
    //TODO check championship state
    val userId = DBUtils.getIdByToken(token)
    if (DBUtils.isAdmin(userId)) {
      Teams.deleteTeam(id)
      for (player <- Players.getTeamPlayers(id)) {
        Players.deletePlayer(player.getInt(0), player.getInt(1))
      }
    }
    if (Teams.checkTeam(id, userId)) {
      if (CommonConstants.defaultTeams.contains(id)) {
        val team = Teams.getTeamData(id)
        Teams.updateTeam(id, Map(teamsConstants.owner -> CommonConstants.admins.head))
        Transactions.addTeamTransaction(id, team.getAs(teamsConstants.budget))
      } else {
        for (player <- Players.getTeamPlayers(id)) {
          Players.deletePlayer(player.getInt(0), player.getInt(1))
        }
        Teams.deleteTeam(id)
      }
      val user = Users.getUserData(userId)
      Users.updateUser(userId,
        Map(usersConstants.budget -> (user.getAs[Int](usersConstants.budget) + 11 * CommonConstants.playerMinCost)))
      StatusCodes.NoContent
    } else {
      StatusCodes.Forbidden
    }
  }

}
