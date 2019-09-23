package im.pes.api

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.pes.constants.{CommonConstants, Paths}
import im.pes.db.PlayersPositions
import im.pes.db.PlayersPositions.{addPlayerPositionSchema, updatePlayerPositionSchema}
import im.pes.utils.DBUtils

object PlayersPositionsAPI {

  def getRoute: Route =
    path(Paths.playersPositions) {
      get {
        parameterMap { params =>
          complete(getPlayersPositions(params))
        }
      } ~
        post {
          entity(as[String]) { playerPosition =>
            headerValueByName(CommonConstants.token) { token =>
              complete(addPlayerPosition(playerPosition, token))
            }
          }
        }
    } ~
      path(Paths.playersPositions / IntNumber) { id =>
          put {
            headerValueByName(CommonConstants.token) { token =>
              entity(as[String]) { playerPosition =>
                complete(updatePlayerPosition(id, playerPosition, token))
              }
            }
          } ~
          delete {
            headerValueByName(CommonConstants.token) { token =>
              complete(deletePlayerPosition(id, token))
            }
          }
      }

  def getPlayersPositions(params: Map[String, String]): ToResponseMarshallable = {
    PlayersPositions.getPlayersPositions(params)
  }

  def addPlayerPosition(playerPosition: String, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (userId.isEmpty) {
      StatusCodes.Unauthorized
    } else if (DBUtils.isAdmin(userId.get)) {
      //TODO check fields values
      val playerPositionDf = DBUtils.dataToDf(addPlayerPositionSchema, playerPosition)
      if (playerPositionDf.first.anyNull) {
        StatusCodes.BadRequest
      } else {
        PlayersPositions.addPlayerPosition(playerPositionDf)
        StatusCodes.OK
      }
    } else {
      StatusCodes.Forbidden
    }
  }

  def updatePlayerPosition(id: Int, updatePlayerPosition: String, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (userId.isEmpty) {
      StatusCodes.Unauthorized
    } else if (DBUtils.isAdmin(userId.get)) {
      //TODO check fields values
      val updatePlayerPositionDf = DBUtils.dataToDf(updatePlayerPositionSchema, updatePlayerPosition)
      PlayersPositions.updatePlayerPosition(id, updatePlayerPositionDf)
      StatusCodes.OK
    } else {
      StatusCodes.Forbidden
    }
  }

  def deletePlayerPosition(id: Int, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (userId.isEmpty) {
      StatusCodes.Unauthorized
    } else if (DBUtils.isAdmin(userId.get)) {
      PlayersPositions.deletePlayerPosition(id)
      StatusCodes.OK
    } else {
      StatusCodes.Forbidden
    }
  }

}
