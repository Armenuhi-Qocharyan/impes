package im.pes.api

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.pes.constants.{Paths, Tables}
import im.pes.db.ActiveGames.{addActiveGameSchema, summarySchema}
import im.pes.db.{ActiveGames, DoneGames, Players, Statistics}
import im.pes.utils.DBUtils
import org.apache.spark.sql.Row


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
    val game = ActiveGames.getActiveGame(id)
    if (null == game) {
      StatusCodes.NotFound
    } else {
      game
    }
  }

  def addGame(addGame: String): ToResponseMarshallable = {
    val addGameDf = DBUtils.dataToDf(addActiveGameSchema, addGame)
    val addGameData = addGameDf.first
    if (addGameData.anyNull) {
      StatusCodes.BadRequest
    } else {
      ActiveGames.addActiveGame(addGameDf).toString
    }
  }

  def deleteGame(id: Int): ToResponseMarshallable = {
    val firstTeamData = scala.collection.mutable
      .Map(Tables.TeamsStatistics.goals -> 0, Tables.TeamsStatistics.possession -> 0,
        Tables.TeamsStatistics.yellowCards -> 0, Tables.TeamsStatistics.redCards -> 0,
        Tables.TeamsStatistics.falls -> 0, Tables.TeamsStatistics.shots -> 0, Tables.TeamsStatistics.aerialsWon -> 0)
    val secondTeamData = scala.collection.mutable
      .Map(Tables.TeamsStatistics.goals -> 0, Tables.TeamsStatistics.possession -> 0,
        Tables.TeamsStatistics.yellowCards -> 0, Tables.TeamsStatistics.redCards -> 0,
        Tables.TeamsStatistics.falls -> 0, Tables.TeamsStatistics.shots -> 0, Tables.TeamsStatistics.aerialsWon -> 0)
    val activeGameDf = ActiveGames.getGameDF(id)
    if (activeGameDf.isEmpty) {
      StatusCodes.BadRequest
    } else {
      val teamsIds = ActiveGames.getActiveGameTeamsIds(id)
      val doneGameId = DoneGames.addDoneGame(teamsIds.head, teamsIds(1), activeGameDf)
      for (gamePlayer <- ActiveGames.getGamePlayers(id)) {
        val playerId = gamePlayer.getAs[Int](Tables.ActiveGamesPlayersData.playerId)
        val teamId = Players.getPlayerTeamId(playerId)
        val summaryDf = DBUtils.dataToDf(summarySchema, gamePlayer.getAs[String](Tables.ActiveGamesPlayersData.summary))
        Statistics.addPlayerStatistics(playerId, teamId, doneGameId, summaryDf)
        val summaryData = summaryDf.first
        Players.updatePlayer(playerId, summaryData)
        if (teamId == teamsIds.head) collectTeamData(firstTeamData, summaryData) else collectTeamData(secondTeamData,
          summaryData)
      }
      ActiveGames.deleteActiveGame(id)
      DoneGames.updateDoneGame(doneGameId,
        Map(Tables.DoneGames.firstTeamGoals -> firstTeamData(Tables.TeamsStatistics.goals),
          Tables.DoneGames.secondTeamGoals -> secondTeamData(Tables.TeamsStatistics.goals)))
      Statistics.addTeamStatistics(teamsIds.head, doneGameId, firstTeamData)
      Statistics.addTeamStatistics(teamsIds(1), doneGameId, secondTeamData)
      StatusCodes.OK
    }
  }

  def collectTeamData(teamData: scala.collection.mutable.Map[String, Int], playerSummary: Row): Unit = {
    teamData(Tables.TeamsStatistics.goals) += playerSummary.getAs[Int](Tables.Summary.goals)
    //TODO calculate possession
    teamData(Tables.TeamsStatistics.yellowCards) += playerSummary.getAs[Int](Tables.Summary.yellowCards)
    if (playerSummary.getAs[Boolean](Tables.Summary.redCard)) teamData(Tables.TeamsStatistics.redCards) += 1
    teamData(Tables.TeamsStatistics.falls) += playerSummary.getAs[Int](Tables.Summary.falls)
    teamData(Tables.TeamsStatistics.shots) += playerSummary.getAs[Int](Tables.Summary.shots)
    teamData(Tables.TeamsStatistics.aerialsWon) += playerSummary.getAs[Int](Tables.Summary.aerialsWon)
  }

}