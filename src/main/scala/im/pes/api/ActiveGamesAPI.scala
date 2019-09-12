package im.pes.api

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.dwickern.macros.NameOf.nameOf
import im.pes.constants.{CommonConstants, Paths, Tables}
import im.pes.db.{ActiveGames, DoneGames, Players, Statistics}
import im.pes.db.ActiveGames.{activeGamesConstants, addActiveGameConstants, addActiveGameSchema, summarySchema}
import im.pes.utils.DBUtils
import org.apache.spark.sql.Row

import scala.collection.mutable

object ActiveGamesAPI {

  def getRoute: Route =
    path(Paths.games) {
      get {
        parameterMap { params =>
          complete(getGames(params))
        }
      } ~
        post {
          entity(as[String]) { game =>
            complete(addGame(game))
          }
        }
    } ~
      path(Paths.games / IntNumber) { id =>
        get {
          complete(getGame(id))
        } ~
          delete {
            complete(deleteGame(id))
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

  def addGame(addGame: String): ToResponseMarshallable = {
    val addGameDf =
      try {
        DBUtils.dataToDf(addActiveGameSchema, addGame)
      } catch {
        case _: NullPointerException => return StatusCodes.BadRequest
      }
    val gameId = ActiveGames.addActiveGame(
      addGameDf.drop(nameOf(addActiveGameConstants.firstTeamPlayers), nameOf(addActiveGameConstants.secondTeamPlayers)))
    val addGameData = addGameDf.collect()(0)
    addTeamPlayers(gameId,
      addGameData.getAs[mutable.WrappedArray[Row]](nameOf(addActiveGameConstants.firstTeamPlayers)))
    addTeamPlayers(gameId,
      addGameData.getAs[mutable.WrappedArray[Row]](nameOf(addActiveGameConstants.secondTeamPlayers)))
    StatusCodes.OK
  }

  def addTeamPlayers(gameId: Int, teamPlayers: mutable.WrappedArray[Row]): Unit = {
    for (teamPlayer <- teamPlayers) {
      //TODO check player in team
      if (teamPlayer.getAs[String](addActiveGameConstants.playerState).equals("reserve")) {
        //TODO add player to reserve list
      } else {
        val playerId = teamPlayer.getAs[Int](addActiveGameConstants.playerId)
        ActiveGames.addActiveGamePlayerData(gameId, playerId)
        //TODO get x and y according to the player state
        ActiveGames.addActivity(playerId, CommonConstants.stayActivity(0, 0))
      }
    }
  }

  def deleteGame(id: Int): ToResponseMarshallable = {
    val firstTeamData = mutable.Map(Tables.TeamsStatistics.goals -> 0, Tables.TeamsStatistics.possession -> 0,
        Tables.TeamsStatistics.yellowCards -> 0, Tables.TeamsStatistics.redCards -> 0,
        Tables.TeamsStatistics.falls -> 0, Tables.TeamsStatistics.shots -> 0, Tables.TeamsStatistics.aerialsWon -> 0)
    val secondTeamData = mutable.Map(Tables.TeamsStatistics.goals -> 0, Tables.TeamsStatistics.possession -> 0,
        Tables.TeamsStatistics.yellowCards -> 0, Tables.TeamsStatistics.redCards -> 0,
        Tables.TeamsStatistics.falls -> 0, Tables.TeamsStatistics.shots -> 0, Tables.TeamsStatistics.aerialsWon -> 0)
    val activeGameDf = ActiveGames.getGameDF(id)
    val activeGame = try {
      activeGameDf.collect()(0)
    } catch {
      case _: ArrayIndexOutOfBoundsException => return StatusCodes.BadRequest
    }
    val firstTeamId = activeGame.getAs[Int](activeGamesConstants.firstTeamId)
    val secondTeamId = activeGame.getAs[Int](activeGamesConstants.secondTeamId)
    val doneGameId = DoneGames.addDoneGame(activeGameDf)
    for (gamePlayer <- ActiveGames.getGamePlayers(id)) {
      val playerId = gamePlayer.getAs[Int](Tables.ActiveGamesPlayersData.playerId)
      val teamId = Players.getPlayerTeamId(playerId)
      val summaryDf = DBUtils.dataToDf(summarySchema, gamePlayer.getAs[String](Tables.ActiveGamesPlayersData.summary))
      Statistics.addPlayerStatistics(playerId, teamId, doneGameId, summaryDf)
      val summaryData = summaryDf.collect()(0)
      Players.updatePlayer(playerId, summaryData)
      if (teamId == firstTeamId) collectTeamData(firstTeamData, summaryData) else collectTeamData(secondTeamData,
        summaryData)
    }
    ActiveGames.deleteActiveGame(id)
    ActiveGames.deleteActiveGamePlayersData(id)
    DoneGames.updateDoneGame(doneGameId,
      Map(Tables.DoneGames.firstTeamGoals -> firstTeamData(Tables.TeamsStatistics.goals),
        Tables.DoneGames.secondTeamGoals -> secondTeamData(Tables.TeamsStatistics.goals)))
    Statistics.addTeamStatistics(firstTeamId, doneGameId, firstTeamData)
    Statistics.addTeamStatistics(secondTeamId, doneGameId, secondTeamData)
    StatusCodes.OK
  }

  def collectTeamData(teamData: mutable.Map[String, Int], playerSummary: Row): Unit = {
    teamData(Tables.TeamsStatistics.goals) += playerSummary.getAs[Int](Tables.Summary.goals)
    //TODO calculate possession
    teamData(Tables.TeamsStatistics.yellowCards) += playerSummary.getAs[Int](Tables.Summary.yellowCards)
    if (playerSummary.getAs[Boolean](Tables.Summary.redCard)) teamData(Tables.TeamsStatistics.redCards) += 1
    teamData(Tables.TeamsStatistics.falls) += playerSummary.getAs[Int](Tables.Summary.falls)
    teamData(Tables.TeamsStatistics.shots) += playerSummary.getAs[Int](Tables.Summary.shots)
    teamData(Tables.TeamsStatistics.aerialsWon) += playerSummary.getAs[Int](Tables.Summary.aerialsWon)
  }

}