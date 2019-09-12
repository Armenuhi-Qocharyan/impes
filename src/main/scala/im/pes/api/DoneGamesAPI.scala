package im.pes.api

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
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
          complete(getDoneGame(id))
        }
      }

  def getDoneGames(params: Map[String, String]): ToResponseMarshallable = {
    DoneGames.getDoneGames(params)
  }

  def getDoneGame(id: Int): ToResponseMarshallable = {
    val doneGame = DoneGames.getDoneGame(id)
    if (null == doneGame) {
      StatusCodes.NotFound
    } else {
      doneGame
    }
  }

}
