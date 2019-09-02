package im.pes.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.pes.Health
import im.pes.constants.{CommonConstants, Paths}
import im.pes.db._
import im.pes.utils.DBUtils
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait DoneGameJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val playerDataFormat: RootJsonFormat[PlayerData] = jsonFormat18(PlayerData)
  implicit val teamDataFormat: RootJsonFormat[TeamData] = jsonFormat9(TeamData)
  implicit val doneGameDataFormat: RootJsonFormat[DoneGameData] = jsonFormat5(DoneGameData)
  implicit val updateDoneGameFormat: RootJsonFormat[UpdateDoneGame] = jsonFormat5(UpdateDoneGame)
}

object DoneGamesAPI extends DoneGameJsonSupport {

  def getRoute: Route =
    path(Paths.doneGames) {
      get {
        parameterMap { params =>
          complete(getDoneGames(params))
        }
      } ~
        post {
          headerValueByName(CommonConstants.token) { token =>
            entity(as[DoneGameData]) { doneGame =>
              complete(addDoneGame(doneGame, token))
            }
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

  def addDoneGame(doneGame: DoneGameData, token: String): ToResponseMarshallable = {
    //TODO check fields values
    val userId = DBUtils.getIdByToken(token)
    if (DBUtils.isAdmin(userId) || Teams.checkTeam(doneGame.firstTeamData.id, userId) ||
      Teams.checkTeam(doneGame.secondTeamData.id, userId)) {
      return StatusCodes.Forbidden
    }
    val doneGameId = DoneGames.addDoneGame(doneGame)
    addTeamData(doneGame.firstTeamData, doneGameId)
    addTeamData(doneGame.secondTeamData, doneGameId)
    StatusCodes.OK
  }

  private def addTeamData(teamData: TeamData, doneGameId: Int): Unit = {
    Statistics.addTeamStatistics(teamData, doneGameId)
    for (playerData <- teamData.playersData) {
      Statistics.addPlayerStatistics(playerData, teamData.id, doneGameId)
      Players.updatePlayer(playerData)
    }
  }

}
