package im.pes.db

import com.github.dwickern.macros.NameOf.nameOf
import im.pes.constants.Tables
import im.pes.utils.DBUtils
import org.apache.spark.sql.types.{DataTypes, StructType}
import org.apache.spark.sql.{DataFrame, Row, functions}

object Users {

  val usersConstants: Tables.Users.type = Tables.Users
  val addUserSchema: StructType = (new StructType)
    .add(nameOf(usersConstants.email), DataTypes.StringType, nullable = false)
    .add(nameOf(usersConstants.password), DataTypes.StringType, nullable = false)
    .add(nameOf(usersConstants.age), DataTypes.IntegerType, nullable = false)
    .add(nameOf(usersConstants.name), DataTypes.StringType, nullable = false)
    .add(nameOf(usersConstants.budget), DataTypes.IntegerType, nullable = false)
  val loginSchema: StructType = (new StructType)
    .add(nameOf(usersConstants.email), DataTypes.StringType, nullable = false)
    .add(nameOf(usersConstants.password), DataTypes.StringType, nullable = false)
  val updateUserSchema: StructType = (new StructType)
    .add(nameOf(usersConstants.email), DataTypes.StringType)
    .add(nameOf(usersConstants.password), DataTypes.StringType)
    .add(nameOf(usersConstants.age), DataTypes.IntegerType)
    .add(nameOf(usersConstants.name), DataTypes.StringType)
    .add(nameOf(usersConstants.budget), DataTypes.IntegerType)

  def getUsers(params: Map[String, String]): String = {
    DBUtils.getTableDataAsString(usersConstants, params, Seq(usersConstants.password))
  }

  def getUser(id: Int): String = {
    DBUtils.getTableDataAsStringByPrimaryKey(usersConstants, id, Seq(usersConstants.password))
  }

  def addUser(df: DataFrame): Unit = {
    val id = DBUtils.getTable(usersConstants, rename = false).count() + 1
    DBUtils.addDataToTable(usersConstants.tableName,
      DBUtils.renameColumnsToDBFormat(df, usersConstants).withColumn(usersConstants.id, functions.lit(id)))
  }

  def updateUser(id: Int, updateDf: DataFrame): Unit = {
    val df = DBUtils.renameColumnsToDBFormat(updateDf, usersConstants)
    updateUser(id, df.collect()(0).getValuesMap(df.columns))
  }

  def updateUser(id: Int, updateData: Map[String, Any]): Unit = {
    DBUtils.updateDataInTable(id, updateData, usersConstants.tableName)
  }

  def deleteUser(id: Int): Unit = {
    DBUtils.deleteDataFromTable(usersConstants.tableName, id)
  }

  def getUserData(id: Int): Row = {
    DBUtils.getTableDataByPrimaryKey(usersConstants, id)
  }

}
