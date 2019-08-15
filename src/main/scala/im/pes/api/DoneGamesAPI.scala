package im.pes.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, entity, path, post, _}
import akka.http.scaladsl.server.Route
import im.pes.Health
import im.pes.constants.Paths
import im.pes.db.{DoneGames, PartialDoneGame}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait DoneGameJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val partialDoneGameFormat: RootJsonFormat[PartialDoneGame] = jsonFormat5(PartialDoneGame)
}

object DoneGamesAPI extends DoneGameJsonSupport {

  def getRoute: Route = {
    post {
      path(Paths.doneGames) {
        entity(as[PartialDoneGame]) { doneGame =>
          DoneGames.addDoneGame(doneGame)
          complete(StatusCodes.OK)
        }
      }
    }
  }


}
