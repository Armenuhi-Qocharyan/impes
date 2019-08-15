package im.pes.db

import im.pes.constants.{CommonConstants, Tables}
import im.pes.main.spark
import im.pes.utils.DBUtils

case class Team(id: Int, name: String, budget: Int, championship: String, championsLeague: Boolean, isUsed: Boolean,
                standardStaff: Int, owner: Int)

case class PartialTeam(name: String, championship: String, owner: Int)

object Teams {

  private val teamsConstants = Tables.Teams

  def addTeam(partialTeam: PartialTeam): Unit = {
    addTeam(partialTeam.owner, partialTeam.name, partialTeam.championship)
  }

  def addTeam(userId: Int, name: String, championship: String): Unit = {
    val data = spark
      .createDataFrame(Seq((DBUtils.getTable(teamsConstants.tableName).count() + 1, name, 11 *
        CommonConstants.playerMinCost, championship, false, true, userId)))
      .toDF(teamsConstants.id, teamsConstants.name, teamsConstants.budget, teamsConstants.championship,
        teamsConstants.championsLeague, teamsConstants.isUsed, teamsConstants.owner)
    DBUtils.addDataToTable(teamsConstants.tableName, data)
  }

  def getTeams: String = {
    DBUtils.getTableData(teamsConstants.tableName)
  }

  def getTeam(id: Int): String = {
    val teams = DBUtils.getTable(teamsConstants.tableName).filter(s"${teamsConstants.id} = $id").toJSON.collect()
    teams(0)
  }

}
