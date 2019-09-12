package im.pes.utils

import im.pes.constants.{CommonConstants, Tables}
import im.pes.main.spark.implicits._
import im.pes.main.{connectionProperties, spark, stmt}
import org.apache.spark.sql.types.StructType
import org.apache.spark.sql.{DataFrame, Row, SaveMode}
import org.mindrot.jbcrypt.BCrypt

object DBUtils {


  def dataToDf(schema: StructType, data: String): DataFrame = {
    spark.read.schema(schema).json(Seq(data).toDS())
  }

  def getTableDataAsStringByPrimaryKey(table: Tables.Table, value: Int, dropColumns: Seq[String] = Nil): String = {
    try {
      getTable(table).filter(s"${Tables.primaryKey} = $value").drop(dropColumns: _*).toJSON.collect()(0)
    } catch {
      case _: ArrayIndexOutOfBoundsException => null
    }
  }

  def getTableDataByPrimaryKey(table: Tables.Table, value: Int, dropColumns: Seq[String] = Nil): Row = {
    try {
      getTable(table, rename = false).filter(s"${Tables.primaryKey} = $value").drop(dropColumns: _*).collect()(0)
    } catch {
      case _: ArrayIndexOutOfBoundsException => null
    }
  }

  def getTableDataByPrimaryKey(table: Tables.Table, value: Int, selectCol: String, selectCols: String*): Row = {
    try {
      getTable(table, rename = false).filter(s"${Tables.primaryKey} = $value").select(selectCol, selectCols: _*)
        .collect()(0)
    } catch {
      case _: ArrayIndexOutOfBoundsException => null
    }
  }

  def getTableDfByPrimaryKey(table: Tables.Table, value: Int, dropColumns: Seq[String] = Nil): DataFrame = {
      getTable(table, rename = false).filter(s"${Tables.primaryKey} = $value").drop(dropColumns: _*)
  }

  def getIdByToken(token: String): Int = {
    try {
      getTable(Tables.Sessions, rename = false).filter(s"${Tables.Sessions.token} = '$token'")
        .select(Tables.Sessions.userId).collect()(0).getInt(0)
    } catch {
      case _: ArrayIndexOutOfBoundsException => -1
    }
  }

  def getSessionId(userId: Int): Int = {
    try {
      getTable(Tables.Sessions, rename = false).filter(s"${Tables.Sessions.userId} = $userId")
        .select(Tables.Sessions.id).collect()(0).getInt(0)
    } catch {
      case _: ArrayIndexOutOfBoundsException => -1
    }
  }

  def getTableDataAsString(table: Tables.Table, params: Map[String, String],
                           dropColumns: Seq[String] = Seq()): String = {
    var df = getTable(table, rename = false)
    for (param <- params) {
      df = df.filter(s"${param._1} = ${param._2}")
    }
    dataToJsonFormat(renameColumns(df.drop(dropColumns: _*), table))
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

  def renameColumns(dataFrame: DataFrame, table: Tables.Table): DataFrame = {
    var df = dataFrame
    for (field <- table.getClass.getDeclaredFields) {
      field.setAccessible(true)
      df = df.withColumnRenamed(field.get(table).toString, field.getName)
    }
    df
  }

  private def dataToJsonFormat(dataFrame: DataFrame): String = {
    val builder = StringBuilder.newBuilder
    builder.append('[')
    for (row <- dataFrame.toJSON.collect()) {
      builder.append(row).append(',')
    }
    if (builder.length() > 1) {
      builder.setLength(builder.length() - 1)
    }
    builder.append(']').toString()
  }

  def addDataToTable(tableName: String, data: DataFrame): Unit = {
    data.write.mode(SaveMode.Append).jdbc(CommonConstants.jdbcUrl, tableName, connectionProperties)
  }

  def renameColumnsToDBFormat(dataFrame: DataFrame, table: Tables.Table): DataFrame = {
    var df = dataFrame
    for (column <- dataFrame.columns) {
      val field = table.getClass.getDeclaredField(column)
      field.setAccessible(true)
      df = df.withColumnRenamed(column, field.get(table).toString)
    }
    df
  }

  def deleteDataFromTable(tableName: String, id: Int): Unit = {
    stmt.executeUpdate(CommonConstants.sqlDeleteByPrimaryKeyQuery(tableName, id))
  }

  def deleteDataFromTable(tableName: String, key: String, id: Int): Unit = {
    stmt.executeUpdate(CommonConstants.sqlDeleteQuery(tableName, key, id))
  }

  def updateDataInTable(id: Int, data: Map[String, Any], tableName: String): Unit = {
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
      stmt.executeUpdate(CommonConstants.sqlUpdateQuery(tableName, builder.toString(), id))
    }
  }

  def isAdmin(userId: Int): Boolean = {
    CommonConstants.admins.contains(userId)
  }

}
