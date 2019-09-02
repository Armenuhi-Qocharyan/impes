package im.pes.db

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import im.pes.Health
import im.pes.constants.Tables
import im.pes.main.spark
import im.pes.utils.DBUtils
import spray.json.{DefaultJsonProtocol, RootJsonFormat, _}

case class TeamTransaction(id: Int, teamId: Int, price: Int)

case class PartialTeamTransaction(teamId: Int, price: Int)

case class PlayerTransaction(id: Int, playerId: Int, price: Int)

case class PartialPlayerTransaction(playerId: Int, price: Int)

trait TransactionsJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val teamTransactionFormat: RootJsonFormat[TeamTransaction] = jsonFormat3(TeamTransaction)
  implicit val playerTransactionFormat: RootJsonFormat[PlayerTransaction] = jsonFormat3(PlayerTransaction)
}

object Transactions extends TransactionsJsonSupport {

  private val teamsTransactionsConstants = Tables.TeamsTransactions
  private val playersTransactionsConstants = Tables.PlayersTransactions

  def getTeamsTransactions(params: Map[String, String]): String = {
    DBUtils.getTableData(teamsTransactionsConstants, params)
  }

  def getPlayersTransactions(params: Map[String, String]): String = {
    DBUtils.getTableData(playersTransactionsConstants, params)
  }

  def addTeamTransaction(partialSellTeam: PartialTeamTransaction): Unit = {
    addTeamTransaction(partialSellTeam.teamId, partialSellTeam.price)
  }

  def addTeamTransaction(teamId: Int, price: Int): Unit = {
    val data = spark
      .createDataFrame(Seq((DBUtils.getTable(teamsTransactionsConstants).count() + 1, teamId, price)))
      .toDF(teamsTransactionsConstants.id, teamsTransactionsConstants.teamId, teamsTransactionsConstants.price)
    DBUtils.addDataToTable(teamsTransactionsConstants.tableName, data)
  }

  def addPlayerTransaction(partialSellPlayer: PartialPlayerTransaction): Unit = {
    addPlayerTransaction(partialSellPlayer.playerId, partialSellPlayer.price)
  }

  def addPlayerTransaction(playerId: Int, price: Int): Unit = {
    val data = spark
      .createDataFrame(Seq((DBUtils.getTable(playersTransactionsConstants).count() + 1, playerId, price)))
      .toDF(playersTransactionsConstants.id, playersTransactionsConstants.playerId, playersTransactionsConstants.price)
    DBUtils.addDataToTable(playersTransactionsConstants.tableName, data)
  }

  def deleteTeamTransaction(id: Int): Unit = {
    DBUtils.deleteDataFromTable(teamsTransactionsConstants.tableName, id)
  }

  def deletePlayerTransaction(id: Int): Unit = {
    DBUtils.deleteDataFromTable(playersTransactionsConstants.tableName, id)
  }

  def getTeamTransaction(id: Int): TeamTransaction = {
    val teamTransaction = DBUtils.getTableDataByPrimaryKey(teamsTransactionsConstants, id)
    if (null == teamTransaction) {
      null
    } else {
      teamTransaction.parseJson.convertTo[TeamTransaction]
    }
  }

  def getPlayerTransaction(id: Int): PlayerTransaction = {
    val playerTransaction = DBUtils.getTableDataByPrimaryKey(playersTransactionsConstants, id)
    if (null == playerTransaction) {
      null
    } else {
      playerTransaction.parseJson.convertTo[PlayerTransaction]
    }
  }

}
