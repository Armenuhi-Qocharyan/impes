package im.pes.db

import com.github.dwickern.macros.NameOf.nameOf
import im.pes.constants.Tables
import im.pes.utils.DBUtils
import org.apache.spark.sql.types.{DataTypes, StructType}
import org.apache.spark.sql.{DataFrame, Row, functions}

object PlayersPositions {

  val playersPositionsConstants: Tables.PlayersPositions.type = Tables.PlayersPositions
  val addPlayerPositionSchema: StructType = (new StructType)
    .add(nameOf(playersPositionsConstants.position), DataTypes.IntegerType)
    .add(nameOf(playersPositionsConstants.x), DataTypes.IntegerType)
    .add(nameOf(playersPositionsConstants.y), DataTypes.StringType)
  val updatePlayerPositionSchema: StructType = addPlayerPositionSchema

  def getPlayersPositions(params: Map[String, String]): String = {
    DBUtils.getTableDataAsString(playersPositionsConstants, params,
      Seq(playersPositionsConstants.id, playersPositionsConstants.x, playersPositionsConstants.y))
  }

  def addPlayerPosition(df: DataFrame): Unit = {
    val id = DBUtils.getTable(playersPositionsConstants, rename = false).count + 1
    DBUtils.addDataToTable(playersPositionsConstants.tableName,
      DBUtils.renameColumnsToDBFormat(df, playersPositionsConstants)
        .withColumn(playersPositionsConstants.id, functions.lit(id)))
  }

  def getPlayerPositionCoordinates(position: String): Option[Row] = {
    val df = DBUtils.getTable(playersPositionsConstants, rename = false)
    if (df.isEmpty) None else Option(df.select(playersPositionsConstants.x, playersPositionsConstants.y).first)
  }

  def updatePlayerPosition(id: Int, updateDf: DataFrame): Unit = {
    val df = DBUtils.renameColumnsToDBFormat(updateDf, playersPositionsConstants)
    updatePlayerPosition(id, df.first.getValuesMap(df.columns))
  }

  def updatePlayerPosition(id: Int, updateData: Map[String, Any]): Unit = {
    DBUtils.updateDataInTableByPrimaryKey(id, updateData, playersPositionsConstants.tableName)
  }

  def deletePlayerPosition(id: Int): Unit = {
    DBUtils.deleteDataFromTable(playersPositionsConstants.tableName, id)
  }

}
