package im.pes.db

import im.pes.constants.{CommonConstants, Tables}
import im.pes.main.spark
import im.pes.utils.{BaseTable, DBUtils}

case class Team(id: Int, name: String, budget: Int, championship: String, championsLeague: Boolean, isUsed: Boolean,
                standardStaff: Int, owner: Int)

case class PartialTeam(name: String, championship: String, owner: Int)

case class UpdateTeam(name: Option[String], championship: Option[String], owner: Option[Int]) extends BaseTable

object Teams {

  private val teamsConstants = Tables.Teams

  def getTeams(params: Map[String, String]): String = {
    DBUtils.getTableData(teamsConstants, params)
  }

  def getTeam(id: Int): String = {
    DBUtils.getTableDataByPrimaryKey(teamsConstants, id)
  }

  def addTeam(partialTeam: PartialTeam, userId: Int): Unit = {
    addTeam(partialTeam.owner, partialTeam.name, partialTeam.championship)
  }

  private def addTeam(userId: Int, name: String, championship: String): Unit = {
    val data = spark
      .createDataFrame(Seq((DBUtils.getTable(teamsConstants).count() + 1, name, 11 *
        CommonConstants.playerMinCost, championship, false, true, userId)))
      .toDF(teamsConstants.id, teamsConstants.name, teamsConstants.budget, teamsConstants.championship,
        teamsConstants.championsLeague, teamsConstants.isUsed, teamsConstants.owner)
    DBUtils.addDataToTable(teamsConstants.tableName, data)
  }

  def updateTeam(id: Int, updateTeam: UpdateTeam): Unit = {
    DBUtils.updateDataInTable(id, updateTeam, teamsConstants)
  }

  def deleteTeam(id: Int): Unit = {
    DBUtils.deleteDataFromTable(teamsConstants.tableName, id)
  }

  def checkTeam(id: Int, userId: Int): Boolean = {
    DBUtils.getTable(teamsConstants).filter(s"${teamsConstants.id} = $id")
      .filter(s"${teamsConstants.owner} = $userId").count() != 0
  }

}
