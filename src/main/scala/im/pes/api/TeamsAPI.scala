package im.pes.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, entity, path, post, _}
import akka.http.scaladsl.server.Route
import im.pes.Health
import im.pes.constants.Paths
import im.pes.db.{PartialTeam, Teams}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait TeamJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val partialTeamFormat: RootJsonFormat[PartialTeam] = jsonFormat3(PartialTeam)
}

object TeamsAPI extends TeamJsonSupport {

  def getRoute: Route = {
    post {
      path(Paths.teams) {
        entity(as[PartialTeam]) { team =>
          Teams.addTeam(team)
          complete(StatusCodes.OK)
        }
      }
    }
  }

}
