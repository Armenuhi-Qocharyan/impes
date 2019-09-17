package im.pes.db

import com.github.dwickern.macros.NameOf.nameOf
import im.pes.constants.Tables
import im.pes.main.spark.implicits._
import im.pes.utils.DBUtils
import org.apache.spark.sql.types.{DataTypes, StructType}
import org.apache.spark.sql.{DataFrame, functions}

object TransactionsHistory {

  val teamsTransactionsHistoryConstants: Tables.TeamsTransactionsHistory.type = Tables.TeamsTransactionsHistory
  val playersTransactionsHistoryConstants: Tables.PlayersTransactionsHistory.type = Tables.PlayersTransactionsHistory
  val addTeamTransactionHistorySchema: StructType = (new StructType)
    .add(nameOf(teamsTransactionsHistoryConstants.teamId), DataTypes.IntegerType, nullable = false)
    .add(nameOf(teamsTransactionsHistoryConstants.sUserId), DataTypes.IntegerType, nullable = false)
    .add(nameOf(teamsTransactionsHistoryConstants.bUserId), DataTypes.IntegerType, nullable = false)
    .add(nameOf(teamsTransactionsHistoryConstants.price), DataTypes.IntegerType, nullable = false)
    .add(nameOf(teamsTransactionsHistoryConstants.date), DataTypes.StringType, nullable = false)
  val addPlayerTransactionHistorySchema: StructType = (new StructType)
    .add(nameOf(playersTransactionsHistoryConstants.playerId), DataTypes.IntegerType, nullable = false)
    .add(nameOf(playersTransactionsHistoryConstants.sTeamId), DataTypes.IntegerType, nullable = false)
    .add(nameOf(playersTransactionsHistoryConstants.bTeamId), DataTypes.IntegerType, nullable = false)
    .add(nameOf(playersTransactionsHistoryConstants.price), DataTypes.IntegerType, nullable = false)
    .add(nameOf(playersTransactionsHistoryConstants.date), DataTypes.StringType, nullable = false)

  def getTeamsTransactionsHistory(params: Map[String, String]): String = {
    DBUtils.getTableDataAsString(teamsTransactionsHistoryConstants, params)
  }

  def getPlayersTransactionsHistory(params: Map[String, String]): String = {
    DBUtils.getTableDataAsString(playersTransactionsHistoryConstants, params)
  }

  def addTeamTransactionHistory(teamId: Int, sUserId: Int, bUserId: Int, price: Int, date: String): Unit = {
    addTeamTransactionHistory(
      Seq((teamId, sUserId, bUserId, price, date)).toDF(teamsTransactionsHistoryConstants.teamId,
        teamsTransactionsHistoryConstants.sUserId, teamsTransactionsHistoryConstants.bUserId,
        teamsTransactionsHistoryConstants.price, teamsTransactionsHistoryConstants.date), rename = false)
  }

  def addTeamTransactionHistory(df: DataFrame, rename: Boolean = true): Unit = {
    val id = DBUtils.getTable(teamsTransactionsHistoryConstants, rename = false).count + 1
    val addDf = if (rename) DBUtils.renameColumnsToDBFormat(df, teamsTransactionsHistoryConstants) else df
    DBUtils.addDataToTable(teamsTransactionsHistoryConstants.tableName,
      addDf.withColumn(teamsTransactionsHistoryConstants.id, functions.lit(id)))
  }

  def addPlayerTransactionHistory(playerId: Int, sTeamId: Int, bTeamId: Int, price: Int, date: String): Unit = {
    addPlayerTransactionHistory(
      Seq((playerId, sTeamId, bTeamId, price, date)).toDF(playersTransactionsHistoryConstants.playerId,
        playersTransactionsHistoryConstants.sTeamId, playersTransactionsHistoryConstants.bTeamId,
        playersTransactionsHistoryConstants.price, playersTransactionsHistoryConstants.date), rename = false)
  }

  def addPlayerTransactionHistory(df: DataFrame, rename: Boolean = true): Unit = {
    val id = DBUtils.getTable(playersTransactionsHistoryConstants, rename = false).count + 1
    val addDf = if (rename) DBUtils.renameColumnsToDBFormat(df, playersTransactionsHistoryConstants) else df
    DBUtils.addDataToTable(playersTransactionsHistoryConstants.tableName,
      addDf.withColumn(playersTransactionsHistoryConstants.id, functions.lit(id)))
  }

}
