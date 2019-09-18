package im.pes.api

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.pes.constants.{CommonConstants, Paths}
import im.pes.db.ComingGames
import im.pes.db.ComingGames.{addComingGameSchema, updateComingGameSchema}
import im.pes.utils.DBUtils

object ComingGamesAPI {

  def getRoute: Route =
    path(Paths.comingGames) {
      get {
        parameterMap { params =>
          complete(getComingGames(params))
        }
      } ~
        post {
          entity(as[String]) { comingGame =>
            headerValueByName(CommonConstants.token) { token =>
              complete(addComingGame(comingGame, token))
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
              entity(as[String]) { comingGame =>
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

  def addComingGame(comingGame: String, token: String): ToResponseMarshallable = {
    if (DBUtils.isAdmin(DBUtils.getIdByToken(token))) {
      //TODO check fields values
      val comingGameDf = DBUtils.dataToDf(addComingGameSchema, comingGame)
      if (comingGameDf.first.anyNull) {
        StatusCodes.BadRequest
      } else {
        ComingGames.addComingGame(comingGameDf)
        StatusCodes.OK
      }
    } else {
      StatusCodes.Forbidden
    }
  }

  def updateComingGame(id: Int, updateComingGame: String, token: String): ToResponseMarshallable = {
    if (DBUtils.isAdmin(DBUtils.getIdByToken(token))) {
      //TODO check fields values
      val updateComingGameDf = DBUtils.dataToDf(updateComingGameSchema, updateComingGame)
        ComingGames.updateComingGame(id, updateComingGameDf)
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
