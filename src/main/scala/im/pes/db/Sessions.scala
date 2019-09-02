package im.pes.db

import im.pes.constants.{CommonConstants, Tables}
import im.pes.main.{spark, stmt}
import im.pes.utils.DBUtils

object Sessions {

  private val sessionsConstants = Tables.Sessions

  def addSession(userId: Int, token: String): Unit = {
    val data = spark
      .createDataFrame(Seq((DBUtils.getTable(sessionsConstants).count() + 1, userId, token)))
      .toDF(sessionsConstants.id, sessionsConstants.userId, sessionsConstants.token)
    DBUtils.addDataToTable(sessionsConstants.tableName, data)
  }

  def deleteSession(token: String): Unit = {
    stmt.executeUpdate(CommonConstants.sqlDeleteTokenFormat.format(sessionsConstants.tableName, token))
  }

}
