package im.pes.utils

import im.pes.constants.{CommonConstants, Tables, UserRoles}
import im.pes.main.spark.implicits._
import im.pes.main.{connectionProperties, spark, stmt}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Row, SaveMode}
import org.mindrot.jbcrypt.BCrypt

object DBUtils {

  def dataToDf(schema: StructType, data: String): DataFrame = {
    spark.read.schema(schema).json(Seq(data).toDS())
  }

  def getTableDataAsStringByPrimaryKey(table: Tables.Table, value: Int, dropColumns: Seq[String] = Nil): Option[String] = {
    val df = getTableDfByPrimaryKey(table, value, dropColumns)
    if (df.isEmpty) None else Option(renameColumns(df, table).toJSON.first)
}

  def getTableDataByPrimaryKey(table: Tables.Table, value: Int, dropColumns: Seq[String] = Nil): Option[Row] = {
    val df = getTableDfByPrimaryKey(table, value, dropColumns)
    if (df.isEmpty) None else  Option(df.first)
  }

  def getTableDataByPrimaryKey(table: Tables.Table, value: Int, selectCol: String, selectCols: String*): Option[Row] = {
    val df = getTableDfByPrimaryKey(table, value)
    if (df.isEmpty) None else Option(df.select(selectCol, selectCols: _*).first)
  }

  def getTableDfByPrimaryKey(table: Tables.Table, value: Int, dropColumns: Seq[String] = Nil): DataFrame = {
    getTable(table, rename = false).filter(s"${Tables.primaryKey} = $value").drop(dropColumns: _*)
  }

  def getTable(table: Tables.Table, dropColumns: Seq[String] = Nil, rename: Boolean = true): DataFrame = {
    if (rename) {
      renameColumns(
        spark.read.jdbc(CommonConstants.jdbcUrl, table.tableName, connectionProperties).drop(dropColumns: _*),
        table)
    } else {
      spark.read.jdbc(CommonConstants.jdbcUrl, table.tableName, connectionProperties).drop(dropColumns: _*)
    }
  }

  def checkDataExist(table: Tables.Table, id: Int): Boolean = {
    !getTableDfByPrimaryKey(table, id).isEmpty
  }

  def renameColumns(dataFrame: DataFrame, table: Tables.Table): DataFrame = {
    table.getClass.getDeclaredFields.foldLeft[DataFrame](dataFrame)((dataFrameWithRenamedColumn, field) => {
      field.setAccessible(true)
      dataFrameWithRenamedColumn.withColumnRenamed(field.get(table).toString, field.getName)
    })
  }

  def getIdByToken(token: String): Option[Int] = {
    val df = getTable(Tables.Sessions, rename = false).filter(s"${Tables.Sessions.token} = '$token'")
    if (df.isEmpty) None else Option(df.select(Tables.Sessions.userId).first.getInt(0))
  }

  def getSessionId(userId: Int): Option[Int] = {
    val df = getTable(Tables.Sessions, rename = false).filter(s"${Tables.Sessions.userId} = $userId")
    if (df.isEmpty) None else Option(df.select(Tables.Sessions.id).first.getInt(0))
  }

  def getTableDataAsString(table: Tables.Table, params: Map[String, String],
                           dropColumns: Seq[String] = Seq()): String = {
    val df = params
      .foldLeft[DataFrame](getTable(table, rename = false))((df, param) => df.filter(s"${param._1} = ${param._2}"))
    dataToJsonFormat(renameColumns(df.drop(dropColumns: _*), table))
  }

  def dataToJsonFormat(dataFrame: DataFrame): String = {
    dataFrame.toJSON.collect.mkString("[", "," , "]" )
  }

  def addDataToTable(tableName: String, data: DataFrame): Unit = {
    data.write.mode(SaveMode.Append).jdbc(CommonConstants.jdbcUrl, tableName, connectionProperties)
  }

  def renameColumnsToDBFormat(dataFrame: DataFrame, table: Tables.Table): DataFrame = {
    dataFrame.columns.foldLeft[DataFrame](dataFrame)((dataFrameWithRenamedColumn, column) => {
      val field = table.getClass.getDeclaredField(column)
      field.setAccessible(true)
      dataFrameWithRenamedColumn.withColumnRenamed(column, field.get(table).toString)
    })
  }

  def deleteDataFromTable(tableName: String, id: Int): Unit = {
    stmt.executeUpdate(CommonConstants.sqlDeleteByPrimaryKeyQuery(tableName, id))
  }

  def deleteDataFromTable(tableName: String, key: String, value: Int): Unit = {
    stmt.executeUpdate(CommonConstants.sqlDeleteQuery(tableName, key, value))
  }

  def updateDataInTableByPrimaryKey(id: Int, data: Map[String, Any], tableName: String): Unit = {
    updateDataInTable(Tables.primaryKey, id, data, tableName)
  }

  def updateDataInTable(searchKey: String, searchValue: Int, data: Map[String, Any], tableName: String): Unit = {
    val builder = StringBuilder.newBuilder
    for (keyValue <- data.filter(_._2 != null)) {
      builder.append(keyValue._1).append(" = ")
      if (keyValue._2.isInstanceOf[String]) {
        if (keyValue._1.equals(Tables.Users.password)) {
          builder.append(''').append(BCrypt.hashpw(keyValue._2.toString, BCrypt.gensalt)).append(''')
        } else {
          builder.append(''').append(keyValue._2.toString).append(''')
        }
      } else {
        builder.append(keyValue._2.toString)
      }
      builder.append(", ")
    }
    if (builder.length() > 0) {
      builder.setLength(builder.length() - 2)
      stmt.executeUpdate(CommonConstants.sqlUpdateQuery(tableName, builder.toString(), searchKey, searchValue))
    }
  }

  def isAdmin(userId: Int): Boolean = {
    !getTableDfByPrimaryKey(Tables.Users, userId).filter(s"${Tables.Users.role} = '${UserRoles.admin}'").isEmpty
  }

}
