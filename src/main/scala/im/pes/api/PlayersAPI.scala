package im.pes.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.pes.Health
import im.pes.constants.{CommonConstants, Paths}
import im.pes.db.{PartialPlayer, Players, Teams, UpdatePlayer}
import im.pes.utils.DBUtils
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait PlayerJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val partialPlayerFormat: RootJsonFormat[PartialPlayer] = jsonFormat9(PartialPlayer)
  implicit val updatePlayerFormat: RootJsonFormat[UpdatePlayer] = jsonFormat9(UpdatePlayer)
}

object PlayersAPI extends PlayerJsonSupport {

  def getRoute: Route =
    path(Paths.players) {
      get {
        parameterMap { params =>
          complete(getPlayers(params))
        }
      } ~
        post {
          headerValueByName(CommonConstants.token) { token =>
            entity(as[PartialPlayer]) { player =>
              complete(addPlayer(player, token))
            }
          }
        }
    } ~
      path(Paths.players / IntNumber) { id =>
        get {
          complete(getPlayer(id))
        } ~
          delete {
            headerValueByName(CommonConstants.token) { token =>
              complete(deletePlayer(id, token))
            }
          } ~
          put {
            headerValueByName(CommonConstants.token) { token =>
              entity(as[UpdatePlayer]) { player =>
                complete(updatePlayer(id, player, token))
              }
            }
          }
      }


  def getPlayers(params: Map[String, String]): ToResponseMarshallable = {
    Players.getPlayers(params)
  }

  def getPlayer(id: Int): ToResponseMarshallable = {
    val player = Players.getPlayer(id)
    if (null == player) {
      StatusCodes.NotFound
    } else {
      player
    }
  }

  def addPlayer(partialPlayer: PartialPlayer, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (DBUtils.isAdmin(userId)) {
      //TODO check fields values
      Players.addPlayer(partialPlayer)
      return StatusCodes.OK
    }
    if (Teams.checkTeam(partialPlayer.teamId, userId)) {
      //TODO check fields values
      Players.addPlayer(partialPlayer)
      StatusCodes.OK
    } else {
      StatusCodes.Forbidden
    }
  }

  def updatePlayer(id: Int, updatePlayer: UpdatePlayer, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (DBUtils.isAdmin(userId)) {
      //TODO check fields values
      Players.updatePlayer(id, updatePlayer)
      return StatusCodes.OK
    }
    if (Players.checkPlayer(id, userId)) {
      if (updatePlayer.teamId.isDefined && !Teams.checkTeam(updatePlayer.teamId.get, userId)) {
        return StatusCodes.Forbidden
      }
      //TODO check fields values
      //TODO check what user may update
      Players.updatePlayer(id, updatePlayer)
      StatusCodes.NoContent
    } else {
      StatusCodes.Forbidden
    }
  }

  def deletePlayer(id: Int, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (DBUtils.isAdmin(userId) || Players.checkPlayer(id, userId)) {
      Players.deletePlayer(id)
      StatusCodes.OK
    } else {
      StatusCodes.Forbidden
    }
  }

}
