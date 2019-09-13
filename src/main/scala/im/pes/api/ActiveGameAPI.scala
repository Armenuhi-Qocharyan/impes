package im.pes.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.pes.constants.{ActivityTypes, CommonConstants, Paths}
import im.pes.db.ActiveGames._
import im.pes.db.{ActiveGames, Players}
import im.pes.utils.DBUtils
import spray.json.DefaultJsonProtocol


object ActiveGameAPI extends SprayJsonSupport with DefaultJsonProtocol {

  def getRoute: Route =
    path(Paths.games / IntNumber / Paths.playersData) { gameId =>
      get {
        complete(getActiveGamePlayersData(gameId))
      }
    } ~
      path(Paths.games / IntNumber / Paths.playersData / IntNumber) { (gameId, activityId) =>
        get {
          complete(getActiveGamePlayersActivities(gameId, activityId))
        }
      } ~
      path(Paths.games / IntNumber / IntNumber) { (gameId, playerId) =>
        post {
          headerValueByName(CommonConstants.token) { token =>
            entity(as[String]) { activity =>
              complete(addActivity(gameId, playerId, activity, token))
            }
          }
        }
      } ~
      path(Paths.games / IntNumber / Paths.playersData / IntNumber) { (gameId, playerId) =>
        post {
          headerValueByName(CommonConstants.token) { token =>
            complete(addActiveGamePlayerData(gameId, playerId, token))
          }
        }
      } ~
      path(Paths.games / IntNumber / Paths.summary / IntNumber) { (gameId, playerId) =>
        put {
          headerValueByName(CommonConstants.token) { token =>
            entity(as[List[String]]) { summary =>
              complete(updateSummary(gameId, playerId, summary, token))
            }
          }
        }
      }

  def getActiveGamePlayersData(gameId: Int): ToResponseMarshallable = {
    ActiveGames.getActiveGamePlayersData(gameId)
  }

  def getActiveGamePlayersActivities(gameId: Int, activityId: Int): ToResponseMarshallable = {
    ActiveGames.getActiveGamePlayersActivities(gameId, activityId)
  }

  def addActivity(gameId: Int, playerId: Int, activity: String, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (!Players.checkPlayer(playerId, userId)) {
      return StatusCodes.Forbidden
    }
    if (!ActiveGames.playerDataExists(playerId)) {
      return StatusCodes.BadRequest
    }
    try {
      val addActivity =
        DBUtils.dataToDf(addActivitySchema, activity).collect()(0)
          .getAs[String](activitiesConstants.activityType) match {
          case ActivityTypes.run => DBUtils.dataToDf(addRunActivitySchema, activity)
          case ActivityTypes.stay => DBUtils.dataToDf(addStayActivitySchema, activity)
          case ActivityTypes.shot => DBUtils.dataToDf(addShotActivitySchema, activity)
          case ActivityTypes.pass => DBUtils.dataToDf(addPassActivitySchema, activity)
          case ActivityTypes.tackle => DBUtils.dataToDf(addTackleActivitySchema, activity)
          case _ => return StatusCodes.BadRequest
        }
      ActiveGames.addActivity(gameId, playerId, addActivity)
      StatusCodes.NoContent
    } catch {
      case _: NullPointerException => StatusCodes.BadRequest
    }
  }

  def addActiveGamePlayerData(gameId: Int, playerId: Int, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (!Players.checkPlayer(playerId, userId)) {
      return StatusCodes.Forbidden
    }
    if (ActiveGames.playerDataExists(playerId)) {
      return StatusCodes.BadRequest
    }
    ActiveGames.addActiveGamePlayerData(playerId, gameId)
    ActiveGames.addStayActivity(gameId, playerId, 0, 0)
    StatusCodes.NoContent
  }

  def updateSummary(gameId: Int, playerId: Int, summary: List[String], token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (!Players.checkPlayer(playerId, userId)) {
      return StatusCodes.Forbidden
    }
    if (!ActiveGames.playerDataExists(playerId)) {
      return StatusCodes.BadRequest
    }
    ActiveGames.updateSummary(playerId, summary)
    StatusCodes.NoContent
  }

}