package im.pes.db

import im.pes.constants.{CommonConstants, Tables}
import im.pes.main.spark.implicits._
import im.pes.main.stmt
import im.pes.utils.DBUtils


object Sessions {

  private val sessionsConstants = Tables.Sessions

  def addSession(userId: Int, token: String): Unit = {
    val id = DBUtils.getSessionId(userId)
    if (id.isEmpty) {
      DBUtils.addDataToTable(sessionsConstants.tableName,
        Seq((userId, token)).toDF(sessionsConstants.userId, sessionsConstants.token))
    } else {
      DBUtils.updateDataInTableByPrimaryKey(id.get, Map(sessionsConstants.token -> token), sessionsConstants.tableName)
    }
  }

  def deleteSession(token: String): Unit = {
    stmt.executeUpdate(CommonConstants.sqlDeleteTokenQuery(token))
  }

}
