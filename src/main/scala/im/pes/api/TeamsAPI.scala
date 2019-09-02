package im.pes.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.pes.Health
import im.pes.constants.{CommonConstants, Paths}
import im.pes.db.{PartialTeam, Teams, UpdateTeam}
import im.pes.utils.DBUtils
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait TeamJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val partialTeamFormat: RootJsonFormat[PartialTeam] = jsonFormat3(PartialTeam)
  implicit val updateTeamFormat: RootJsonFormat[UpdateTeam] = jsonFormat3(UpdateTeam)
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
      Teams.addTeam(partialTeam, userId)
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
      Teams.updateTeam(id, updateTeam)
      StatusCodes.NoContent
    } else {
      StatusCodes.Forbidden
    }
  }

  def deleteTeam(id: Int, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (DBUtils.isAdmin(userId) || Teams.checkTeam(id, userId)) {
      Teams.deleteTeam(id)
      //TODO What happens with players? Or check that team doesn't have players
      StatusCodes.NoContent
    } else {
      StatusCodes.Forbidden
    }
  }

}
