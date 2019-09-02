package im.pes.db

import im.pes.constants.{CommonConstants, Tables}
import im.pes.main.{spark, stmt}
import im.pes.utils.{BaseTable, DBUtils}

case class UpdateSession(token: Option[String]) extends BaseTable

object Sessions {

  private val sessionsConstants = Tables.Sessions

  def addSession(userId: Int, token: String): Unit = {
    val id: Int = DBUtils.getSessionId(userId)
    if (-1 == id) {
      val data = spark
        .createDataFrame(Seq((userId, token))).toDF(sessionsConstants.userId, sessionsConstants.token)
      DBUtils.addDataToTable(sessionsConstants.tableName, data)
    } else {
      DBUtils.updateDataInTable(id, UpdateSession(Option(token)), sessionsConstants)
    }
  }

  def deleteSession(token: String): Unit = {
    stmt.executeUpdate(CommonConstants.sqlDeleteTokenFormat.format(sessionsConstants.tableName, token))
  }

}
