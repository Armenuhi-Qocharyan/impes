package im.pes.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives.{as, complete, entity, path, post, _}
import akka.http.scaladsl.server.Route
import im.pes.Health
import im.pes.constants.Paths
import im.pes.db.{ComingGames, PartialComingGame, UpdateComingGame}
import im.pes.utils.APIUtils
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait ComingGameJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val partialComingGameFormat: RootJsonFormat[PartialComingGame] = jsonFormat5(PartialComingGame)
  implicit val updateComingGameFormat: RootJsonFormat[UpdateComingGame] = jsonFormat5(UpdateComingGame)
}

object ComingGamesAPI extends ComingGameJsonSupport {

  def getRoute: Route =
    get {
      path(Paths.comingGames) {
        parameterMap { params =>
          complete(ComingGames.getComingGames(params))
        }
      } ~
        path(Paths.comingGames / IntNumber) { id =>
          complete(ComingGames.getComingGame(id))
        }
    } ~
      headerValueByName("Token") { token =>
        val userId = APIUtils.validateToken(token)
        post {
          path(Paths.comingGames) {
            entity(as[PartialComingGame]) { comingGame =>
              complete(ComingGames.addComingGame(comingGame, userId))
            }
          }
        } ~
          delete {
            path(Paths.comingGames / IntNumber) { id =>
              complete(ComingGames.deleteComingGame(id, userId))
            }
          } ~
          put {
            path(Paths.comingGames / IntNumber) { id =>
              entity(as[UpdateComingGame]) { comingGame =>
                complete(ComingGames.updateComingGame(id, comingGame, userId))
              }
            }
          }
      }
}
