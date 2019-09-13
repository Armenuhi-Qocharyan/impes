package im.pes.db

import com.github.dwickern.macros.NameOf.nameOf
import im.pes.constants.Tables
import im.pes.utils.DBUtils
import org.apache.spark.sql.types.{DataTypes, StructType}
import org.apache.spark.sql.{DataFrame, Row, functions}


object Teams {

  val teamsConstants: Tables.Teams.type = Tables.Teams
  val addTeamSchema: StructType = (new StructType)
    .add(nameOf(teamsConstants.name), DataTypes.StringType, nullable = false)
    .add(nameOf(teamsConstants.budget), DataTypes.IntegerType, nullable = false)
    .add(nameOf(teamsConstants.championship), DataTypes.StringType, nullable = false)
    .add(nameOf(teamsConstants.owner), DataTypes.IntegerType, nullable = false)
  val updateTeamSchema: StructType = (new StructType)
    .add(nameOf(teamsConstants.name), DataTypes.StringType)
    .add(nameOf(teamsConstants.budget), DataTypes.IntegerType)
    .add(nameOf(teamsConstants.championship), DataTypes.StringType)
    .add(nameOf(teamsConstants.owner), DataTypes.IntegerType)

  def getTeams(params: Map[String, String]): String = {
    DBUtils.getTableDataAsString(teamsConstants, params)
  }

  def getTeam(id: Int): String = {
    DBUtils.getTableDataAsStringByPrimaryKey(teamsConstants, id)
  }

  def addTeam(df: DataFrame): Unit = {
    val id = DBUtils.getTable(teamsConstants, rename = false).count() + 1
    DBUtils.addDataToTable(teamsConstants.tableName,
      DBUtils.renameColumnsToDBFormat(df, teamsConstants).withColumn(teamsConstants.id, functions.lit(id))
        .withColumn(teamsConstants.championsLeague, functions.lit(false))
        .withColumn(teamsConstants.isUsed, functions.lit(true)))
  }

  def updateTeam(id: Int, updateDf: DataFrame): Unit = {
    val df = DBUtils.renameColumnsToDBFormat(updateDf, teamsConstants)
    updateTeam(id, df.collect()(0).getValuesMap(df.columns))
  }

  def updateTeam(id: Int, updateData: Map[String, Any]): Unit = {
    DBUtils.updateDataInTable(id, updateData, teamsConstants.tableName)
  }

  def deleteTeam(id: Int): Unit = {
    DBUtils.deleteDataFromTable(teamsConstants.tableName, id)
  }

  def checkTeam(id: Int, userId: Int): Boolean = {
    !DBUtils.getTable(teamsConstants, rename = false).filter(s"${teamsConstants.id} = $id")
      .filter(s"${teamsConstants.owner} = $userId").isEmpty
  }

  def getTeamData(id: Int): Row = {
    DBUtils.getTableDataByPrimaryKey(teamsConstants, id)
  }

  def getUserTeam(userId: Int): Row = {
    val teamDf = DBUtils.getTable(teamsConstants, rename = false).filter(s"${teamsConstants.owner} = $userId")
    if (teamDf.isEmpty) null else teamDf.collect()(0)
  }

}
