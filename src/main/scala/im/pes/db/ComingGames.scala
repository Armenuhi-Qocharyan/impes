package im.pes.db

import akka.actor.{Actor, Props}
import com.github.dwickern.macros.NameOf.nameOf
import im.pes.constants.Tables
import im.pes.utils.DBUtils
import org.apache.spark.sql.types.{DataTypes, StructType}
import org.apache.spark.sql.{DataFrame, functions}

object ComingGames {
  final case class GetComingGames(params: Map[String, String])
  final case class GetComingGame(id: Int)
  final case class AddComingGame(df: DataFrame)
  final case class UpdateComingGame(id: Int, df: DataFrame)
  final case class UpdateComingGameLocal(id: Int, updateData: Map[String, Any])
  final case class DeleteComingGame(id: Int)

  def props: Props = Props[ComingGames]

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
}


class ComingGames extends Actor {

  import ComingGames._

  def receive: Receive = {
    case msg: GetComingGames =>
      sender() ! getComingGames(msg.params)
    case msg: GetComingGame =>
      sender() ! getComingGame(msg.id)
    case msg: AddComingGame =>
      addComingGame(msg.df)
    case msg: UpdateComingGame =>
      updateComingGame(msg.id, msg.df)
    case msg: UpdateComingGameLocal =>
      updateComingGame(msg.id, msg.updateData)
    case msg: DeleteComingGame =>
      deleteComingGame(msg.id)
  }

  def getComingGames(params: Map[String, String]): String = {
    DBUtils.getTableDataAsString(comingGamesConstants, params)
  }

  def getComingGame(id: Int): Option[String] = {
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
