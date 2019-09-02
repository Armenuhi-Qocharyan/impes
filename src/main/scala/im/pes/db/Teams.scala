package im.pes.db

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import im.pes.Health
import im.pes.constants.Tables
import im.pes.main.spark
import im.pes.utils.DBUtils.getTable
import im.pes.utils.{BaseTable, DBUtils}
import spray.json._

case class Team(id: Int, name: String, budget: Int, championship: String, championsLeague: Boolean, isUsed: Boolean,
                standardStaff: Int, owner: Int)

case class PartialTeam(name: String, budget: Int, championship: String, owner: Int)

case class UpdateTeam(name: Option[String], budget: Option[Int], championship: Option[String],
                      owner: Option[Int]) extends BaseTable

trait TeamJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val teamFormat: RootJsonFormat[Team] = jsonFormat8(Team)
}

object Teams extends TeamJsonSupport {

  private val teamsConstants = Tables.Teams

  def getTeams(params: Map[String, String]): String = {
    DBUtils.getTableData(teamsConstants, params)
  }

  def getTeam(id: Int): String = {
    DBUtils.getTableDataByPrimaryKey(teamsConstants, id)
  }

  def addTeam(partialTeam: PartialTeam, userId: Int): Unit = {
    addTeam(partialTeam.owner, partialTeam.name, partialTeam.budget, partialTeam.championship)
  }

  private def addTeam(userId: Int, name: String, budget: Int, championship: String): Unit = {
    val data = spark
      .createDataFrame(
        Seq((DBUtils.getTable(teamsConstants).count() + 1, name, budget, championship, false, true, userId)))
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

  def getTeamData(id: Int): Team = {
    val team = DBUtils.getTableDataByPrimaryKey(teamsConstants, id)
    if (null == team) {
      null
    } else {
      team.parseJson.convertTo[Team]
    }
  }

  def getUserTeam(userId: Int): Team = {
    try {
      getTable(teamsConstants).filter(s"${teamsConstants.owner} = $userId").toJSON.collect()(0).parseJson
        .convertTo[Team]
    } catch {
      case _: ArrayIndexOutOfBoundsException => null
    }
  }

}
