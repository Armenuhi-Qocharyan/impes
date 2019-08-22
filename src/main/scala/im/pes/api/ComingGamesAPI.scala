package im.pes.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, entity, path, post, _}
import akka.http.scaladsl.server.Route
import im.pes.Health
import im.pes.constants.Paths
import im.pes.db.{ComingGames, PartialComingGame, UpdateComingGame}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait ComingGameJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val partialComingGameFormat: RootJsonFormat[PartialComingGame] = jsonFormat5(PartialComingGame)
  implicit val updateComingGameFormat: RootJsonFormat[UpdateComingGame] = jsonFormat5(UpdateComingGame)
}

object ComingGamesAPI extends ComingGameJsonSupport {

  def getRoute: Route =
    post {
      path(Paths.comingGames) {
        entity(as[PartialComingGame]) { comingGame =>
          ComingGames.addComingGame(comingGame)
          complete(StatusCodes.OK)
        }
      }
    } ~
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
      delete {
        path(Paths.comingGames / IntNumber) { id =>
          ComingGames.deleteComingGame(id)
          complete(StatusCodes.OK)
        }
      } ~
      put {
        path(Paths.comingGames / IntNumber) { id =>
          entity(as[UpdateComingGame]) { comingGame =>
            ComingGames.updateComingGame(id, comingGame)
            complete(StatusCodes.OK)
          }
        }
      }
}
