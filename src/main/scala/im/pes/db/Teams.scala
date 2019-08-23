package im.pes.db

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import im.pes.constants.{CommonConstants, Tables}
import im.pes.main.spark
import im.pes.utils.{BaseTable, DBUtils}

case class Team(id: Int, name: String, budget: Int, championship: String, championsLeague: Boolean, isUsed: Boolean,
                standardStaff: Int, owner: Int)

case class PartialTeam(name: String, championship: String, owner: Int)

case class UpdateTeam(name: Option[String], championship: Option[String], owner: Option[Int]) extends BaseTable

object Teams {

  private val teamsConstants = Tables.Teams

  def addTeam(partialTeam: PartialTeam, userId: Int): ToResponseMarshallable = {
    if (userId.equals(partialTeam.owner)) {
      addTeam(partialTeam.owner, partialTeam.name, partialTeam.championship)
    } else {
      StatusCodes.Forbidden
    }
  }

  private def addTeam(userId: Int, name: String, championship: String): ToResponseMarshallable = {
    val data = spark
      .createDataFrame(Seq((DBUtils.getTable(teamsConstants).count() + 1, name, 11 *
        CommonConstants.playerMinCost, championship, false, true, userId)))
      .toDF(teamsConstants.id, teamsConstants.name, teamsConstants.budget, teamsConstants.championship,
        teamsConstants.championsLeague, teamsConstants.isUsed, teamsConstants.owner)
    DBUtils.addDataToTable(teamsConstants.tableName, data)
    StatusCodes.OK
  }

  def getTeams(params: Map[String, String]): ToResponseMarshallable = {
    DBUtils.getTableData(teamsConstants, params)
  }

  def getTeam(id: Int): ToResponseMarshallable = {
    val team = DBUtils.getTableDataByPrimaryKey(teamsConstants, id)
    if (null == team) {
      StatusCodes.NotFound
    } else {
      team
    }
  }

  def deleteTeam(id: Int, userId: Int): ToResponseMarshallable = {
    if (checkTeam(id, userId)) {
      DBUtils.deleteDataFromTable(teamsConstants.tableName, id)
      StatusCodes.NoContent
    } else {
      StatusCodes.Forbidden
    }
  }

    def updateTeam(id: Int, updateTeam: UpdateTeam, userId: Int): ToResponseMarshallable = {
      if (checkTeam(id, userId)) {
        DBUtils.updateDataInTable(id, updateTeam, teamsConstants)
        StatusCodes.NoContent
      } else {
        StatusCodes.Forbidden
      }
    }

    def checkTeam(id: Int, userId: Int): Boolean = {
      DBUtils.getTable(teamsConstants).filter(s"${teamsConstants.id} = $id")
        .filter(s"${teamsConstants.owner} = $userId").count() != 0
    }

  }
