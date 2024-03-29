package im.pes.db

import com.github.dwickern.macros.NameOf.nameOf
import im.pes.constants.Tables
import im.pes.main.spark.implicits._
import im.pes.utils.DBUtils
import org.apache.spark.sql.types.{DataTypes, StructType}
import org.apache.spark.sql.{DataFrame, Row, functions}

object Transactions {

  val teamsTransactionsConstants: Tables.TeamsTransactions.type = Tables.TeamsTransactions
  val playersTransactionsConstants: Tables.PlayersTransactions.type = Tables.PlayersTransactions

  val addTeamTransactionSchema: StructType = (new StructType)
    .add(nameOf(teamsTransactionsConstants.teamId), DataTypes.IntegerType)
    .add(nameOf(teamsTransactionsConstants.price), DataTypes.IntegerType)
  val addPlayerTransactionSchema: StructType = (new StructType)
    .add(nameOf(playersTransactionsConstants.playerId), DataTypes.IntegerType)
    .add(nameOf(playersTransactionsConstants.price), DataTypes.IntegerType)

  def getTeamsTransactions(params: Map[String, String]): String = {
    DBUtils.getTableDataAsString(teamsTransactionsConstants, params)
  }

  def getPlayersTransactions(params: Map[String, String]): String = {
    DBUtils.getTableDataAsString(playersTransactionsConstants, params)
  }

  def addTeamTransaction(teamId: Int, price: Int): Unit = {
    addTeamTransaction(
      Seq((teamId, price)).toDF(teamsTransactionsConstants.teamId, teamsTransactionsConstants.price),
      rename = false)
  }

  def addTeamTransaction(df: DataFrame, rename: Boolean = true): Unit = {
    val id = DBUtils.getTable(teamsTransactionsConstants, rename = false).count + 1
    val addDf = if (rename) DBUtils.renameColumnsToDBFormat(df, teamsTransactionsConstants) else df
    DBUtils.addDataToTable(teamsTransactionsConstants.tableName,
      addDf.withColumn(teamsTransactionsConstants.id, functions.lit(id)))
  }

  def addPlayerTransaction(playerId: Int, price: Int): Unit = {
    addPlayerTransaction(
      Seq((playerId, price)).toDF(playersTransactionsConstants.playerId, playersTransactionsConstants.price),
      rename = false)
  }

  def addPlayerTransaction(df: DataFrame, rename: Boolean = true): Unit = {
    val id = DBUtils.getTable(playersTransactionsConstants, rename = false).count + 1
    val addDf = if (rename) DBUtils.renameColumnsToDBFormat(df, playersTransactionsConstants) else df
    DBUtils.addDataToTable(playersTransactionsConstants.tableName,
      addDf.withColumn(playersTransactionsConstants.id, functions.lit(id)))
  }

  def deleteTeamTransaction(id: Int): Unit = {
    DBUtils.deleteDataFromTable(teamsTransactionsConstants.tableName, id)
  }

  def deleteTeamTransactionByTeamId(teamId: Int): Unit = {
    DBUtils.deleteDataFromTable(teamsTransactionsConstants.tableName, teamsTransactionsConstants.teamId, teamId)
  }

  def deletePlayerTransaction(id: Int): Unit = {
    DBUtils.deleteDataFromTable(playersTransactionsConstants.tableName, id)
  }

  def deletePlayerTransactionByPlayerId(playerId: Int): Unit = {
    DBUtils.deleteDataFromTable(playersTransactionsConstants.tableName, playersTransactionsConstants.playerId, playerId)
  }

  def getTeamTransaction(id: Int): Option[Row] = {
    DBUtils.getTableDataByPrimaryKey(teamsTransactionsConstants, id)
  }

  def getPlayerTransaction(id: Int): Option[Row] = {
    DBUtils.getTableDataByPrimaryKey(playersTransactionsConstants, id)
  }

  def checkTeamTransaction(teamId: Int): Boolean = {
    !DBUtils.getTable(teamsTransactionsConstants, rename = false)
      .filter(s"${teamsTransactionsConstants.teamId} = $teamId").isEmpty
  }

  def checkPlayerTransaction(playerId: Int): Boolean = {
    !DBUtils.getTable(playersTransactionsConstants, rename = false)
      .filter(s"${playersTransactionsConstants.playerId} = $playerId").isEmpty
  }


}
