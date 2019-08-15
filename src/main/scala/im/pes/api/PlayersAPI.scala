package im.pes.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, entity, path, post, _}
import akka.http.scaladsl.server.Route
import im.pes.Health
import im.pes.constants.Paths
import im.pes.db.{PartialPlayer, Players}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait PlayerJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val partialPlayerFormat: RootJsonFormat[PartialPlayer] = jsonFormat9(PartialPlayer)
}

object PlayersAPI extends PlayerJsonSupport {

  def getRoute: Route = {
    post {
      path(Paths.players) {
        entity(as[PartialPlayer]) { player =>
          Players.addPlayer(player)
          complete(StatusCodes.OK)
        }
      }
    }
  }

}
