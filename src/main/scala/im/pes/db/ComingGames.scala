package im.pes.db

import com.github.dwickern.macros.NameOf.nameOf
import im.pes.constants.Tables
import im.pes.utils.DBUtils
import org.apache.spark.sql.types.{DataTypes, StructType}
import org.apache.spark.sql.{DataFrame, functions}


object ComingGames {

  val comingGamesConstants: Tables.ComingGames.type = Tables.ComingGames
  val addComingGameSchema: StructType = (new StructType)
    .add(nameOf(comingGamesConstants.firstTeamId), DataTypes.IntegerType)
    .add(nameOf(comingGamesConstants.secondTeamId), DataTypes.IntegerType)
    .add(nameOf(comingGamesConstants.championship), DataTypes.StringType)
    .add(nameOf(comingGamesConstants.championshipState), DataTypes.StringType)
    .add(nameOf(comingGamesConstants.date), DataTypes.StringType)
  val updateComingGameSchema: StructType = (new StructType)
    .add(nameOf(comingGamesConstants.firstTeamId), DataTypes.IntegerType)
    .add(nameOf(comingGamesConstants.secondTeamId), DataTypes.IntegerType)
    .add(nameOf(comingGamesConstants.championship), DataTypes.StringType)
    .add(nameOf(comingGamesConstants.championshipState), DataTypes.StringType)
    .add(nameOf(comingGamesConstants.date), DataTypes.StringType)


  def getComingGames(params: Map[String, String]): String = {
    DBUtils.getTableDataAsString(comingGamesConstants, params)
  }

  def getComingGame(id: Int): String = {
    DBUtils.getTableDataAsStringByPrimaryKey(comingGamesConstants, id)
  }

  def addComingGame(df: DataFrame): Unit = {
    val id = DBUtils.getTable(comingGamesConstants, rename = false).count + 1
    DBUtils.addDataToTable(comingGamesConstants.tableName,
      DBUtils.renameColumnsToDBFormat(df, comingGamesConstants).withColumn(comingGamesConstants.id, functions.lit(id)))
  }

  def updateComingGame(id: Int, updateDf: DataFrame): Unit = {
    val df = DBUtils.renameColumnsToDBFormat(updateDf, comingGamesConstants)
    updateComingGame(id, df.first.getValuesMap(df.columns))
  }

  def updateComingGame(id: Int, updateData: Map[String, Any]): Unit = {
    DBUtils.updateDataInTableByPrimaryKey(id, updateData, comingGamesConstants.tableName)
  }

  def deleteComingGame(id: Int): Unit = {
    DBUtils.deleteDataFromTable(comingGamesConstants.tableName, id)
  }

}
