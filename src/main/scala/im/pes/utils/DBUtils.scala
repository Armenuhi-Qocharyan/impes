package im.pes.utils

import im.pes.constants.{CommonConstants, Tables}
import im.pes.main.{connectionProperties, spark, stmt}
import org.apache.spark.sql.{DataFrame, SaveMode}
import org.mindrot.jbcrypt.BCrypt

trait BaseTable

object DBUtils {

  def getTableDataByPrimaryKey(table: Tables.Table, value: Int): String = {
    getTableDataByPrimaryKey(table, value, Seq())
  }

  def getTableDataByPrimaryKey(table: Tables.Table, value: Int, dropColumns: Seq[String]): String = {
    try {
      getTable(table).filter(s"${Tables.primaryKey} = $value").drop(dropColumns: _*).toJSON.collect()(0)
    } catch {
      case _: ArrayIndexOutOfBoundsException => null
    }
  }

  def getIdByToken(token: String): Int = {
    try {
      getTable(Tables.Sessions).filter(s"${Tables.Sessions.token} = '$token'").select(Tables.Sessions.userId).collect()(0)
        .getInt(0)
    } catch {
      case _: ArrayIndexOutOfBoundsException => -1
    }
  }

  def getSessionId(userId: Int): Int = {
    try {
      getTable(Tables.Sessions).filter(s"${Tables.Sessions.userId} = '$userId'").select(Tables.Sessions.id).collect()(0)
        .getInt(0)
    } catch {
      case _: ArrayIndexOutOfBoundsException => -1
    }
  }

  def getTableData(table: Tables.Table, params: Map[String, String]): String = {
    getTableData(table, params, Seq())
  }

  def getTableData(table: Tables.Table, params: Map[String, String], dropColumns: Seq[String]): String = {
    var df = getTable(table)
    for (param <- params) {
      df = df.filter(s"${param._1} = ${param._2}")
    }
    dataToJsonFormat(df.drop(dropColumns: _*))
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

  def getTable(table: Tables.Table): DataFrame = {
    getTable(table, Seq())
  }

  def getTable(table: Tables.Table, dropColumns: Seq[String]): DataFrame = {
    renameColumns(spark.read.jdbc(CommonConstants.jdbcUrl, table.tableName, connectionProperties).drop(dropColumns: _*),
      table)
  }

  private def renameColumns(dataFrame: DataFrame, table: Tables.Table): DataFrame = {
    var df = dataFrame
    for (field <- table.getClass.getDeclaredFields) {
      field.setAccessible(true)
      df = df.withColumnRenamed(field.get(table).toString, field.getName)
    }
    df
  }

  def addDataToTable(tableName: String, data: DataFrame): Unit = {
    data.write.mode(SaveMode.Append).jdbc(CommonConstants.jdbcUrl, tableName, connectionProperties)
  }

  def deleteDataFromTable(tableName: String, id: Int): Unit = {
    stmt.executeUpdate(CommonConstants.sqlDeleteFormat.format(tableName, id))
  }

  def updateDataInTable(id: Int, data: BaseTable, table: Tables.Table): Unit = {
    val builder = StringBuilder.newBuilder
    for (field <- data.getClass.getDeclaredFields) {
      field.setAccessible(true)
      val valueOption = field.get(data)
      if (None != valueOption) {
        val tableField = table.getClass.getDeclaredField(field.getName)
        tableField.setAccessible(true)
        val key = tableField.get(table)
        builder.append(key).append(" = ")
        val value = valueOption.asInstanceOf[Option[Any]].get
        if (value.isInstanceOf[String]) {
          if (key.equals(Tables.Users.password)) {
            builder.append(''').append(BCrypt.hashpw(value.toString, BCrypt.gensalt)).append(''')
          } else {
            builder.append(''').append(value).append(''')
          }
        } else {
          builder.append(value)
        }
        builder.append(", ")
      }
    }
    if (builder.length() > 0) {
      builder.setLength(builder.length() - 2)
      stmt.executeUpdate(CommonConstants.sqlUpdateFormat.format(table.tableName, builder.toString(), id))
    }
  }

  def isAdmin(userId: Int): Boolean = {
    CommonConstants.admins.contains(userId)
  }

}
