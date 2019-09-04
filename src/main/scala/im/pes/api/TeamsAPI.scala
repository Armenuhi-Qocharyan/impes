package im.pes.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.pes.Health
import im.pes.constants.{CommonConstants, Paths}
import im.pes.db._
import im.pes.utils.DBUtils
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait TeamJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val partialTeamFormat: RootJsonFormat[PartialTeam] = jsonFormat4(PartialTeam)
  implicit val updateTeamFormat: RootJsonFormat[UpdateTeam] = jsonFormat4(UpdateTeam)
}

object TeamsAPI extends TeamJsonSupport {

  def getRoute: Route =
    path(Paths.teams) {
      get {
        parameterMap { params =>
          complete(getTeams(params))
        }
      } ~
        post {
          headerValueByName(CommonConstants.token) { token =>
            entity(as[PartialTeam]) { team =>
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
              entity(as[UpdateTeam]) { team =>
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

  def addTeam(partialTeam: PartialTeam, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (userId.equals(partialTeam.owner)) {
      //TODO check fields values
      val user = Users.getUserData(userId)
      if (null == user || partialTeam.budget > user.budget) {
        return StatusCodes.BadRequest
      }
      Teams.addTeam(partialTeam, userId)
      Users.updateUser(userId, UpdateUser(None, None, None, None, Option(user.budget - partialTeam.budget)))
      StatusCodes.OK
    } else {
      StatusCodes.Forbidden
    }
  }

  def updateTeam(id: Int, updateTeam: UpdateTeam, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (DBUtils.isAdmin(userId)) {
      //TODO check fields values
      Teams.updateTeam(id, updateTeam)
      return StatusCodes.NoContent
    }
    if (Teams.checkTeam(id, userId)) {
      //TODO check what user may update
      //TODO check fields values
      if (updateTeam.owner.isDefined && updateTeam.owner.get != userId) {
        StatusCodes.BadRequest
      }
      Teams.updateTeam(id, updateTeam)
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
        Teams.updateTeam(id, UpdateTeam(None, None, None, Option(CommonConstants.admins.head)))
        Transactions.addTeamTransaction(PartialTeamTransaction(id, team.budget))
      } else {
        for (player <- Players.getTeamPlayers(id)) {
          Players.deletePlayer(player.getInt(0), player.getInt(1))
        }
        Teams.deleteTeam(id)
      }
      val user = Users.getUserData(userId)
      Users.updateUser(userId,
        UpdateUser(None, None, None, None, Option(user.budget + 11 * CommonConstants.playerMinCost)))
      StatusCodes.NoContent
    } else {
      StatusCodes.Forbidden
    }
  }

}
