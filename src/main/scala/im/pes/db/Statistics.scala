package im.pes.db

import im.pes.constants.Tables
import im.pes.main.spark.implicits._
import im.pes.utils.DBUtils
import org.apache.spark.sql.{DataFrame, functions}

object Statistics {

  private val teamsStatisticsConstants = Tables.TeamsStatistics
  private val playersStatisticsConstants = Tables.PlayersStatistics

  def getTeamsStatistics(params: Map[String, String]): String = {
    DBUtils.getTableDataAsString(teamsStatisticsConstants, params)
  }

  def getTeamStatistics(id: Int): String = {
    DBUtils.getTableDataAsStringByPrimaryKey(teamsStatisticsConstants, id)
  }

  def getPlayersStatistics(params: Map[String, String]): String = {
    DBUtils.getTableDataAsString(playersStatisticsConstants, params)
  }

  def getPlayerStatistics(id: Int): String = {
    DBUtils.getTableDataAsStringByPrimaryKey(playersStatisticsConstants, id)
  }

  def addTeamStatistics(teamId: Int, doneGameId: Int, teamData: scala.collection.mutable.Map[String, Int]): Unit = {
    val id = DBUtils.getTable(teamsStatisticsConstants, rename = false).count() + 1
    val data = Seq((id, teamId, doneGameId, teamData(teamsStatisticsConstants.goals), teamData(
      teamsStatisticsConstants.possession), teamData(teamsStatisticsConstants.yellowCards), teamData(
      teamsStatisticsConstants.redCards), teamData(teamsStatisticsConstants.falls), teamData(
      teamsStatisticsConstants.shots), teamData(teamsStatisticsConstants.aerialsWon)))
      .toDF(teamsStatisticsConstants.id, teamsStatisticsConstants.teamId, teamsStatisticsConstants.doneGameId,
        teamsStatisticsConstants.goals, teamsStatisticsConstants.possession, teamsStatisticsConstants.yellowCards,
        teamsStatisticsConstants.redCards, teamsStatisticsConstants.falls, teamsStatisticsConstants.shots,
        teamsStatisticsConstants.aerialsWon)
    DBUtils.addDataToTable(teamsStatisticsConstants.tableName, data)
  }

  def addPlayerStatistics(playerId: Int, teamId: Int, doneGameId: Int, df: DataFrame): Unit = {
    val id = DBUtils.getTable(playersStatisticsConstants, rename = false).count() + 1
    DBUtils.addDataToTable(playersStatisticsConstants.tableName,
      df.withColumn(playersStatisticsConstants.id, functions.lit(id))
        .withColumn(playersStatisticsConstants.playerId, functions.lit(playerId))
        .withColumn(playersStatisticsConstants.teamId, functions.lit(teamId))
        .withColumn(playersStatisticsConstants.doneGameId, functions.lit(doneGameId)))
  }

}
