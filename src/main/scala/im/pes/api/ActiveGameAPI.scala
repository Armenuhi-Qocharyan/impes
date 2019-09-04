package im.pes.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.pes.Health
import im.pes.constants.{ActivityTypes, CommonConstants, Paths, Tables}
import im.pes.db._
import im.pes.utils.DBUtils
import spray.json._

trait GameJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val activityFormat: RootJsonFormat[Activity] = jsonFormat1(Activity)
  implicit val runActivityFormat: RootJsonFormat[RunActivity] = jsonFormat1(RunActivity)
  implicit val stayActivityFormat: RootJsonFormat[StayActivity] = jsonFormat2(StayActivity)
  implicit val shotActivityFormat: RootJsonFormat[ShotActivity] = jsonFormat3(ShotActivity)
  implicit val passActivityFormat: RootJsonFormat[PassActivity] = jsonFormat3(PassActivity)
  implicit val tackleActivityFormat: RootJsonFormat[TackleActivity] = jsonFormat1(TackleActivity)
}

object ActiveGameAPI extends GameJsonSupport {

  def getRoute: Route =
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


  def addActivity(gameId: Int, playerId: Int, activity: String, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (!Players.checkPlayer(playerId, userId)) {
      return StatusCodes.Forbidden
    }
    if (!ActiveGames.playerDataExists(playerId)) {
      return StatusCodes.Forbidden
    }
    try {
      activity.parseJson.convertTo[Activity].activityType match {
        case ActivityTypes.run => activity.parseJson.convertTo[RunActivity]
        case ActivityTypes.stay => activity.parseJson.convertTo[StayActivity]
        case ActivityTypes.shot => activity.parseJson.convertTo[ShotActivity]
        case ActivityTypes.pass => activity.parseJson.convertTo[PassActivity]
        case ActivityTypes.tackle => activity.parseJson.convertTo[TackleActivity]
        case _ => return StatusCodes.BadRequest
      }
      ActiveGames.addActivity(playerId, activity)
      StatusCodes.NoContent
    } catch {
      case _: DeserializationException => StatusCodes.BadRequest
    }
  }

  def addActiveGamePlayerData(gameId: Int, playerId: Int, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
        if (!Players.checkPlayer(playerId, userId)) {
          return StatusCodes.Forbidden
        }
    if (ActiveGames.playerDataExists(playerId)) {
      return StatusCodes.Forbidden
    }
    ActiveGames.addActiveGamePlayerData(playerId, gameId)
    ActiveGames.addActivity(playerId, CommonConstants.stayActivity(0, 0))
    StatusCodes.NoContent
  }

  def updateSummary(gameId: Int, playerId: Int, summary: List[String], token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (!Players.checkPlayer(playerId, userId)) {
      return StatusCodes.Forbidden
    }
    val summaryConstants = Tables.Summary
    val summaryData = ActiveGames.getSummary(playerId)
    if (null == summaryData) {
      return StatusCodes.BadRequest
    }
    var data: Map[String, Any] = Map.empty
    for (key <- summary) {
      if (key.equals(summaryConstants.redCard)) {
        data = data + (key -> true)
      } else {
        try {
          val field = summaryData.getClass.getDeclaredField(key)
          field.setAccessible(true)
          data = data + (key -> (field.getInt(summaryData) + 1))
        } catch {
          case _:NoSuchFieldException =>
        }
      }
    }
    ActiveGames.updateSummary(playerId, data)
    StatusCodes.NoContent
  }

}