package im.pes.db

import im.pes.constants.{CommonConstants, Tables}
import im.pes.main.{connectionProperties, spark}
import org.apache.spark.sql.SaveMode

case class Team(id: Int, name: String, budget: Int, championship: String, championsLeague: Boolean, isUsed: Boolean,
                standardStaff: Int, owner: Int)

case class PartialTeam(name: String, championship: String, owner: Int)

object Teams {

  private val teamsConstants = Tables.Teams

  def addTeam(partialTeam: PartialTeam): Unit = {
    addTeam(partialTeam.owner, partialTeam.name, partialTeam.championship)
  }

  def addTeam(userId: Int, name: String, championship: String): Unit = {
    val teamId = spark.read.jdbc(CommonConstants.jdbcUrl, teamsConstants.tableName, connectionProperties).count() + 1
    val data = spark
      .createDataFrame(Seq((teamId, name, 11 * CommonConstants.playerMinCost, championship, false, true, userId)))
      .toDF(teamsConstants.id, teamsConstants.name, teamsConstants.budget, teamsConstants.championship,
        teamsConstants.championsLeague, teamsConstants.isUsed, teamsConstants.owner)
    data.write.mode(SaveMode.Append).jdbc(CommonConstants.jdbcUrl, teamsConstants.tableName, connectionProperties)
  }

}
