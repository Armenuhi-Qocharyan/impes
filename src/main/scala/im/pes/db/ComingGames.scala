package im.pes.db

import im.pes.constants.Tables
import im.pes.main.spark
import im.pes.utils.{BaseTable, DBUtils}

case class ComingGame(id: Int, firstTeamId: Int, secondTeamId: Int, championship: String, championshipState: String,
                      date: String)

case class PartialComingGame(firstTeamId: Int, secondTeamId: Int, championship: String, championshipState: String,
                             date: String)

case class UpdateComingGame(firstTeamId: Option[Int], secondTeamId: Option[Int], championship: Option[String],
                            championshipState: Option[String], date: Option[String]) extends BaseTable

object ComingGames {

  private val comingGamesConstants = Tables.ComingGames

  def getComingGames(params: Map[String, String]): String = {
    DBUtils.getTableData(comingGamesConstants, params)
  }

  def getComingGame(id: Int): String = {
    DBUtils.getTableDataByPrimaryKey(comingGamesConstants, id)
  }

  def addComingGame(partialComingGame: PartialComingGame): Unit = {
    addComingGame(partialComingGame.firstTeamId, partialComingGame.secondTeamId, partialComingGame.championship,
      partialComingGame.championshipState, partialComingGame.date)

  }

  private def addComingGame(firstTeamId: Int, secondTeamId: Int, championship: String, championshipState: String,
                            date: String): Unit = {
    val data = spark
      .createDataFrame(Seq((DBUtils.getTable(comingGamesConstants).count() +
        1, firstTeamId, secondTeamId, championship, championshipState, date)))
      .toDF(comingGamesConstants.id, comingGamesConstants.firstTeamId, comingGamesConstants.secondTeamId,
        comingGamesConstants.championship, comingGamesConstants.championshipState, comingGamesConstants.date)
    DBUtils.addDataToTable(comingGamesConstants.tableName, data)
  }

  def updateComingGame(id: Int, updateComingGame: UpdateComingGame): Unit = {
    DBUtils.updateDataInTable(id, updateComingGame, comingGamesConstants)
  }

  def deleteComingGame(id: Int): Unit = {
    DBUtils.deleteDataFromTable(comingGamesConstants.tableName, id)
  }

}
