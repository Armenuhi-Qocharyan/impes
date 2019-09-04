package im.pes.db

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import im.pes.Health
import im.pes.constants.Tables
import im.pes.main.spark
import im.pes.utils.DBUtils
import spray.json._

case class TeamTransactionHistory(id: Int, teamId: Int, sUserId: Int, bUserId: Int, price: Int, date: String)

case class PlayerTransactionHistory(id: Int, playerId: Int, sTeamId: Int, bTeamId: Int, price: Int, date: String)

trait TransactionsHistoryJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val teamTransactionHistoryFormat: RootJsonFormat[TeamTransactionHistory] = jsonFormat6(
    TeamTransactionHistory)
  implicit val playerTransactionHistoryFormat: RootJsonFormat[PlayerTransactionHistory] = jsonFormat6(
    PlayerTransactionHistory)
}

object TransactionsHistory extends TransactionsHistoryJsonSupport {

  private val teamsTransactionsHistoryConstants = Tables.TeamsTransactionsHistory
  private val playersTransactionsHistoryConstants = Tables.PlayersTransactionsHistory

  def getTeamsTransactionsHistory(params: Map[String, String]): String = {
    DBUtils.getTableData(teamsTransactionsHistoryConstants, params)
  }

  def getPlayersTransactionsHistory(params: Map[String, String]): String = {
    DBUtils.getTableData(playersTransactionsHistoryConstants, params)
  }

  def addTeamTransactionHistory(teamId: Int, sUserId: Int, bUserId: Int, price: Int, date: String): Unit = {
    val data = spark
      .createDataFrame(
        Seq((DBUtils.getTable(teamsTransactionsHistoryConstants).count() + 1, teamId, sUserId, bUserId, price, date)))
      .toDF(teamsTransactionsHistoryConstants.id, teamsTransactionsHistoryConstants.teamId,
        teamsTransactionsHistoryConstants.sUserId, teamsTransactionsHistoryConstants.bUserId,
        teamsTransactionsHistoryConstants.price, teamsTransactionsHistoryConstants.date)
    DBUtils.addDataToTable(playersTransactionsHistoryConstants.tableName, data)
  }

  def addPlayerTransactionHistory(playerId: Int, sTeamId: Int, bTeamId: Int, price: Int, date: String): Unit = {
    val data = spark
      .createDataFrame(Seq(
        (DBUtils.getTable(playersTransactionsHistoryConstants).count() + 1, playerId, sTeamId, bTeamId, price, date)))
      .toDF(playersTransactionsHistoryConstants.id, playersTransactionsHistoryConstants.playerId,
        playersTransactionsHistoryConstants.sTeamId, playersTransactionsHistoryConstants.bTeamId,
        playersTransactionsHistoryConstants.price, playersTransactionsHistoryConstants.date)
    DBUtils.addDataToTable(playersTransactionsHistoryConstants.tableName, data)
  }

}
