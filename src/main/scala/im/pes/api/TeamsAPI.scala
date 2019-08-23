package im.pes.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.pes.Health
import im.pes.constants.Paths
import im.pes.db.{PartialTeam, Teams, UpdateTeam}
import im.pes.utils.APIUtils
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait TeamJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val partialTeamFormat: RootJsonFormat[PartialTeam] = jsonFormat3(PartialTeam)
  implicit val updateTeamFormat: RootJsonFormat[UpdateTeam] = jsonFormat3(UpdateTeam)
}

object TeamsAPI extends TeamJsonSupport {

  def getRoute: Route =
      get {
        path(Paths.teams) {
          parameterMap { params =>
            complete(Teams.getTeams(params))
          }
        } ~
          path(Paths.teams / IntNumber) { id =>
            complete(Teams.getTeam(id))
          }
      } ~
        headerValueByName("Token") { token =>
          val userId = APIUtils.validateToken(token)
          post {
            path(Paths.teams) {
              entity(as[PartialTeam]) { team =>
                complete(Teams.addTeam(team, userId))
              }
            }
          } ~
            delete {
              path(Paths.teams / IntNumber) { id =>
                complete(Teams.deleteTeam(id, userId))
              }
            } ~
            put {
              path(Paths.teams / IntNumber) { id =>
                entity(as[UpdateTeam]) { team =>
                  complete(Teams.updateTeam(id, team, userId))
                }
              }
            }
        }
}
