package im.pes.api

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.pes.constants.Paths
import im.pes.db.DoneGames

object DoneGamesAPI {

  def getRoute: Route =
    path(Paths.doneGames) {
      get {
        parameterMap { params =>
          complete(getDoneGames(params))
        }
      }
    } ~
      path(Paths.doneGames / IntNumber) { id =>
        get {
          rejectEmptyResponse {
            complete(getDoneGame(id))
          }
        }
      }

  def getDoneGames(params: Map[String, String]): ToResponseMarshallable = {
    DoneGames.getDoneGames(params)
  }

  def getDoneGame(id: Int): ToResponseMarshallable = {
    DoneGames.getDoneGame(id)
  }

}
