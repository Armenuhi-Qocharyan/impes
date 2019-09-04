package im.pes.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.pes.Health
import im.pes.constants.{CommonConstants, Paths}
import im.pes.db.{ActiveGames, PartialActiveGame, TeamPlayer}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait GamesJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val teamPlayersFormat: RootJsonFormat[TeamPlayer] = jsonFormat2(TeamPlayer)
  implicit val partialGameFormat: RootJsonFormat[PartialActiveGame] = jsonFormat6(PartialActiveGame)
}

object ActiveGamesAPI extends GamesJsonSupport {

  def getRoute: Route =
    path(Paths.games) {
      get {
        parameterMap { params =>
          complete(getGames(params))
        }
      } ~
        post {
          entity(as[PartialActiveGame]) { game =>
            complete(addGame(game))
          }
        }
    } ~
      path(Paths.games / IntNumber) { id =>
        get {
          complete(getGame(id))
        }
    }

  def getGames(params: Map[String, String]): ToResponseMarshallable = {
    ActiveGames.getActiveGames(params)
  }

  def getGame(id: Int): ToResponseMarshallable = {
    ActiveGames.getActiveGame(id)
    val game = ActiveGames.getActiveGame(id)
    if (null == game) {
      StatusCodes.NotFound
    } else {
      game
    }
  }

  def addGame(game: PartialActiveGame): ToResponseMarshallable = {
    val gameId = ActiveGames.addActiveGame(game)
    addTeamPlayers(gameId, game.firstTeamPlayers)
    addTeamPlayers(gameId, game.secondTeamPlayers)
    StatusCodes.OK
  }

  def addTeamPlayers(gameId: Int, teamPlayers: List[TeamPlayer]): Unit = {
    for (teamPlayer <- teamPlayers) {
      //TODO check player in team
      if (teamPlayer.playerState.equals("reserve")) {
        //TODO add player to reserve list
      } else {
        ActiveGames.addActiveGamePlayerData(gameId, teamPlayer.playerId)
        //TODO get x and y according to the player state
        ActiveGames.addActivity(teamPlayer.playerId, CommonConstants.stayActivity(0, 0))
      }
    }
  }

}