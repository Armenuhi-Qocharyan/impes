package im.pes.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.pes.Health
import im.pes.constants.{CommonConstants, Paths}
import im.pes.db.{ComingGames, PartialComingGame, UpdateComingGame}
import im.pes.utils.DBUtils
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait ComingGameJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val partialComingGameFormat: RootJsonFormat[PartialComingGame] = jsonFormat5(PartialComingGame)
  implicit val updateComingGameFormat: RootJsonFormat[UpdateComingGame] = jsonFormat5(UpdateComingGame)
}

object ComingGamesAPI extends ComingGameJsonSupport {

  def getRoute: Route =
    path(Paths.comingGames) {
      get {
        parameterMap { params =>
          complete(getComingGames(params))
        }
      } ~
        post {
          path(Paths.comingGames) {
            entity(as[PartialComingGame]) { comingGame =>
              headerValueByName(CommonConstants.token) { token =>
                complete(addComingGame(comingGame, token))
              }
            }
          }
        }
    } ~
      path(Paths.comingGames / IntNumber) { id =>
        get {
          complete(getComingGame(id))
        } ~
          put {
            headerValueByName(CommonConstants.token) { token =>
              entity(as[UpdateComingGame]) { comingGame =>
                complete(updateComingGame(id, comingGame, token))
              }
            }
          } ~
          delete {
            headerValueByName(CommonConstants.token) { token =>
              complete(deleteComingGame(id, token))
            }
          }
      }

  def getComingGames(params: Map[String, String]): ToResponseMarshallable = {
    ComingGames.getComingGames(params)
  }

  def getComingGame(id: Int): ToResponseMarshallable = {
    val comingGame = ComingGames.getComingGame(id)
    if (null == comingGame) {
      StatusCodes.NotFound
    } else {
      comingGame
    }
  }

  def addComingGame(partialComingGame: PartialComingGame, token: String): ToResponseMarshallable = {
    if (DBUtils.isAdmin(DBUtils.getIdByToken(token))) {
      //TODO check fields values
      ComingGames.addComingGame(partialComingGame)
      StatusCodes.OK
    } else {
      StatusCodes.Forbidden
    }
  }

  def updateComingGame(id: Int, updateComingGame: UpdateComingGame, token: String): ToResponseMarshallable = {
    if (DBUtils.isAdmin(DBUtils.getIdByToken(token))) {
      //TODO check fields values
      ComingGames.updateComingGame(id, updateComingGame)
      StatusCodes.OK
    } else {
      StatusCodes.Forbidden
    }
  }

  def deleteComingGame(id: Int, token: String): ToResponseMarshallable = {
    if (DBUtils.isAdmin(DBUtils.getIdByToken(token))) {
      ComingGames.deleteComingGame(id)
      StatusCodes.OK
    } else {
      StatusCodes.Forbidden
    }
  }

}
