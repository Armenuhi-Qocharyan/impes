package im.pes.db

import java.time.LocalDate

import im.pes.constants.Tables
import im.pes.utils.DBUtils
import org.apache.spark.sql.{DataFrame, functions}

object DoneGames {

  private val doneGamesConstants = Tables.DoneGames

  def getDoneGames(params: Map[String, String]): String = {
    DBUtils.getTableDataAsString(doneGamesConstants, params)
  }

  def getDoneGame(id: Int): Option[String] = {
    DBUtils.getTableDataAsStringByPrimaryKey(doneGamesConstants, id)
  }

  def addDoneGame(firstTeamId: Int, secondTeamId: Int, activeGameDf: DataFrame): Int = {
    val id = DBUtils.getTable(doneGamesConstants, rename = false).count + 1
    DBUtils.addDataToTable(doneGamesConstants.tableName,
      activeGameDf.drop(Tables.ActiveGames.startTimestamp)
        .withColumn(doneGamesConstants.firstTeamId, functions.lit(firstTeamId))
        .withColumn(doneGamesConstants.secondTeamId, functions.lit(secondTeamId))
        .withColumnRenamed(Tables.ActiveGames.championshipState, doneGamesConstants.championshipState)
        .withColumn(doneGamesConstants.id, functions.lit(id))
        .withColumn(doneGamesConstants.firstTeamGoals, functions.lit(0))
        .withColumn(doneGamesConstants.secondTeamGoals, functions.lit(0))
        .withColumn(doneGamesConstants.date, functions.lit(LocalDate.now().toString)))
    id.toInt
  }

  def updateDoneGame(id: Int, updateDf: DataFrame): Unit = {
    val df = DBUtils.renameColumnsToDBFormat(updateDf, doneGamesConstants)
    updateDoneGame(id, df.first.getValuesMap(df.columns))
  }

  def updateDoneGame(id: Int, updateData: Map[String, Any]): Unit = {
    DBUtils.updateDataInTableByPrimaryKey(id, updateData, doneGamesConstants.tableName)
  }


}
