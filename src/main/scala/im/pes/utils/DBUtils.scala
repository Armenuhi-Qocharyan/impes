package im.pes.utils

import im.pes.constants.CommonConstants
import im.pes.main.{connectionProperties, spark}
import org.apache.spark.sql.{DataFrame, SaveMode}

object DBUtils {

  def getTable(tableName: String): DataFrame = {
    spark.read.jdbc(CommonConstants.jdbcUrl, tableName, connectionProperties)
  }

  def getTableData(tableName: String): String = {
    val builder = StringBuilder.newBuilder
    builder.append("[")
    for (row <- getTable(tableName).toJSON.collect()) {
      builder.append(row)
    }
    builder.append("]").toString()
  }

  def addDataToTable(tableName: String, data: DataFrame): Unit = {
    data.write.mode(SaveMode.Append).jdbc(CommonConstants.jdbcUrl, tableName, connectionProperties)
  }

}
