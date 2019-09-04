package im.pes.db

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import im.pes.Health
import im.pes.constants.{CommonConstants, Tables}
import im.pes.main.{spark, stmt}
import im.pes.utils.DBUtils
import spray.json._


case class TeamPlayer(playerId: Int, playerState: String)

case class ActiveGame(id: Int, firstTeamId: Int, secondTeamId: Int, firstTeamPlayers: List[TeamPlayer],
                      secondTeamPlayers: List[TeamPlayer], championship: String, championshipState: String)

case class PartialActiveGame(firstTeamId: Int, secondTeamId: Int, firstTeamPlayers: List[TeamPlayer],
                             secondTeamPlayers: List[TeamPlayer], championship: String, championshipState: String)

case class ActiveGamePlayerData(id: Int, gameId: Int, playerId: Int, summary: String, activities: String)

case class Summary(goals: Int, donePasses: Int, smartPasses: Int, passes: Int, doneShots: Int, shots: Int,
                   doneTackles: Int, tackles: Int, dribblingCount: Int, hooks: Int, ballLosses: Int, aerialsWon: Int,
                   assists: Int, falls: Int, mileage: Int, yellowCards: Int, redCard: Boolean)

case class Activity(activityType: String)

case class RunActivity(angle: Int)

case class StayActivity(x: Int, y: Int)

case class ShotActivity(firstAngle: Int, secondAngle: Int, power: Int)

case class PassActivity(firstAngle: Int, secondAngle: Int, power: Int)

case class TackleActivity(angle: Int)

trait PlayerDataJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val summaryFormat: RootJsonFormat[Summary] = jsonFormat17(Summary)
  implicit val playerDataFormat: RootJsonFormat[ActiveGamePlayerData] = jsonFormat5(ActiveGamePlayerData)

}

object ActiveGames extends PlayerDataJsonSupport {

  private val activeGamesConstants = Tables.ActiveGames
  private val activeGamesPlayersDataConstants = Tables.ActiveGamesPlayersData
  private val activitiesConstants = Tables.Activities
  private val summaryConstants = Tables.Summary

  def getActiveGames(params: Map[String, String]): String = {
    DBUtils.getTableData(activeGamesConstants, params)
  }

  def getActiveGame(id: Int): String = {
    DBUtils.getTableDataByPrimaryKey(activeGamesConstants, id)
  }

  def addActiveGame(partialActiveGame: PartialActiveGame): Int = {
    addActiveGame(partialActiveGame.firstTeamId, partialActiveGame.secondTeamId, partialActiveGame.championship,
      partialActiveGame.championshipState)
  }

  private def addActiveGame(firstTeamId: Int, secondTeamId: Int, championship: String,
                            championshipState: String): Int = {
    val id = DBUtils.getTable(activeGamesConstants).count() +  1
    val data = spark
      .createDataFrame(Seq((id, firstTeamId, secondTeamId, championship, championshipState)))
      .toDF(activeGamesConstants.id, activeGamesConstants.firstTeamId, activeGamesConstants.secondTeamId,
        activeGamesConstants.championship, activeGamesConstants.championshipState)
    DBUtils.addDataToTable(activeGamesConstants.tableName, data)
    id.toInt
  }

  def addActiveGamePlayerData(gameId: Int, playerId: Int): Unit = {
    val data = spark
      .createDataFrame(Seq(
        (DBUtils.getTable(activeGamesPlayersDataConstants).count() + 1, gameId, playerId, CommonConstants
          .defaultSummaryJson, "[]")))
      .toDF(activeGamesPlayersDataConstants.id, activeGamesPlayersDataConstants.gameId,
        activeGamesPlayersDataConstants.playerId, activeGamesPlayersDataConstants.summary,
        activeGamesPlayersDataConstants.activities)
    DBUtils.addDataToTable(activeGamesPlayersDataConstants.tableName, data)
  }

  def deleteActiveGame(id: Int): Unit = {
    DBUtils.deleteDataFromTable(activeGamesConstants.tableName, id)
  }

  def addActivity(playerId: Int, activity: String): Unit = {
    println(CommonConstants.sqlUpdateAppendToJsonArrayQuery(activeGamesPlayersDataConstants.tableName,
      activeGamesPlayersDataConstants.activities, activity, activeGamesPlayersDataConstants.playerId, playerId))
    stmt.executeUpdate(CommonConstants.sqlUpdateAppendToJsonArrayQuery(activeGamesPlayersDataConstants.tableName,
      activeGamesPlayersDataConstants.activities, activity, activeGamesPlayersDataConstants.playerId, playerId))
  }

  def updateSummary(playerId: Int, data: Map[String, Any]): Unit = {
    val builder = StringBuilder.newBuilder
    for (keyValue <- data) {
      builder.append(", '$.").append(keyValue._1).append("', ").append(keyValue._2)
    }
    stmt.executeUpdate(CommonConstants.sqlUpdateReplaceJsonQuery(activeGamesPlayersDataConstants.tableName,
      activeGamesPlayersDataConstants.summary, builder.toString(), activeGamesPlayersDataConstants.playerId, playerId))
  }

  def getSummary(playerId: Int): Summary = {
    val playerData = try {
      DBUtils.getTable(activeGamesPlayersDataConstants)
        .filter(s"${activeGamesPlayersDataConstants.playerId} = $playerId").toJSON.collect()(0)
    } catch {
      case _: ArrayIndexOutOfBoundsException => null
    }
    playerData.parseJson.convertTo[ActiveGamePlayerData].
      summary.parseJson.convertTo[Summary]
  }

  def playerDataExists(playerId: Int): Boolean = {
    try {
      DBUtils.getTable(activeGamesPlayersDataConstants)
        .filter(s"${activeGamesPlayersDataConstants.playerId} = $playerId").toJSON.collect()(0)
      true
    } catch {
      case _: ArrayIndexOutOfBoundsException => false
    }
  }

}
