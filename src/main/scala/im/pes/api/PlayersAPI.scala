package im.pes.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives.{as, complete, entity, path, post, _}
import akka.http.scaladsl.server.Route
import im.pes.Health
import im.pes.constants.Paths
import im.pes.db.{PartialPlayer, Players, UpdatePlayer}
import im.pes.utils.APIUtils
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait PlayerJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val partialPlayerFormat: RootJsonFormat[PartialPlayer] = jsonFormat9(PartialPlayer)
  implicit val updatePlayerFormat: RootJsonFormat[UpdatePlayer] = jsonFormat9(UpdatePlayer)
}

object PlayersAPI extends PlayerJsonSupport {

  def getRoute: Route =
    get {
      path(Paths.players) {
        parameterMap { params =>
          complete(Players.getPlayers(params))
        }
      } ~
        path(Paths.players / IntNumber) { id =>
          complete(Players.getPlayer(id))
        }
    } ~
    headerValueByName("Token") { token =>
      val userId = APIUtils.validateToken(token)
      post {
        path(Paths.players) {
          entity(as[PartialPlayer]) { player =>
            complete(Players.addPlayer(player, userId))
          }
        }
      } ~
        delete {
          path(Paths.players / IntNumber) { id =>
            complete(Players.deletePlayer(id, userId))
          }
        } ~
        put {
          path(Paths.players / IntNumber) { id =>
            entity(as[UpdatePlayer]) { player =>
              complete(Players.updatePlayer(id, player, userId))
            }
          }
        }
    }
}
