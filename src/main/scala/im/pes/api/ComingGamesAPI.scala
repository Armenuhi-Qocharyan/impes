package im.pes.api

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import im.pes.constants.{CommonConstants, Paths}
import im.pes.db.ComingGames._
import im.pes.main.{comingGamesActor, timeout}
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
          rejectEmptyResponse {
            complete(getComingGame(id))
          }
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
    (comingGamesActor ? GetComingGames(params)).mapTo[String]
  }

  def getComingGame(id: Int): ToResponseMarshallable = {
    (comingGamesActor ? GetComingGame(id)).mapTo[Option[String]]
  }

  def addComingGame(comingGame: String, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (userId.isEmpty) {
      StatusCodes.Unauthorized
    } else if (DBUtils.isAdmin(userId.get)) {
      //TODO check fields values
      val comingGameDf = DBUtils.dataToDf(addComingGameSchema, comingGame)
      if (comingGameDf.first.anyNull) {
        StatusCodes.BadRequest
      } else {
        comingGamesActor ! AddComingGame(comingGameDf)
        StatusCodes.OK
      }
    } else {
      StatusCodes.Forbidden
    }
  }

  def updateComingGame(id: Int, updateComingGame: String, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (userId.isEmpty) {
      StatusCodes.Unauthorized
    } else if (DBUtils.isAdmin(userId.get)) {
      //TODO check fields values
      val updateComingGameDf = DBUtils.dataToDf(updateComingGameSchema, updateComingGame)
      comingGamesActor ! UpdateComingGame(id, updateComingGameDf)
      StatusCodes.OK
    } else {
      StatusCodes.Forbidden
    }
  }

  def deleteComingGame(id: Int, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (userId.isEmpty) {
      StatusCodes.Unauthorized
    } else if (DBUtils.isAdmin(userId.get)) {
      comingGamesActor ! DeleteComingGame(id)
      StatusCodes.OK
    } else {
      StatusCodes.Forbidden
    }
  }

}
