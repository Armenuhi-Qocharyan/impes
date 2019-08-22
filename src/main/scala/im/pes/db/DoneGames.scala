package im.pes.db

import im.pes.constants.Tables
import im.pes.main.spark
import im.pes.utils.{BaseTable, DBUtils}

case class DoneGame(id: Int, firstTeamId: Int, secondTeamId: Int, championship: String, championshipState: String,
                    date: String)

case class PartialDoneGame(firstTeamId: Int, secondTeamId: Int, championship: String, championshipState: String,
                           date: String)

case class UpdateDoneGame(firstTeamId: Option[Int], secondTeamId: Option[Int], championship: Option[String], championshipState: Option[String],
                            date: Option[String]) extends BaseTable

object DoneGames {

  private val doneGamesConstants = Tables.DoneGames

  def addDoneGame(partialDoneGame: PartialDoneGame): Unit = {
    addDoneGame(partialDoneGame.firstTeamId, partialDoneGame.secondTeamId, partialDoneGame.championship,
      partialDoneGame.championshipState, partialDoneGame.date)
  }

  def addDoneGame(firstTeamId: Int, secondTeamId: Int, championship: String, championshipState: String,
                  date: String): Unit = {
    val data = spark
      .createDataFrame(Seq((DBUtils.getTable(doneGamesConstants.tableName).count() +
        1, firstTeamId, secondTeamId, championship, championshipState, date)))
      .toDF(doneGamesConstants.id, doneGamesConstants.teamOne, doneGamesConstants.teamTwo,
        doneGamesConstants.championship, doneGamesConstants.championshipState, doneGamesConstants.date)
    DBUtils.addDataToTable(doneGamesConstants.tableName, data)
  }

  def getDoneGames(params: Map[String, String]): String = {
    DBUtils.getTableData(doneGamesConstants.tableName, params)
  }

  def getDoneGame(id: Int): String = {
    DBUtils.getTableDataByPrimaryKey(doneGamesConstants.tableName, id)
  }

  def deleteDoneGame(id: Int): Unit = {
    DBUtils.deleteDataFromTable(doneGamesConstants.tableName, id)
  }

  def updateDoneGame(id: Int, updateDoneGame: UpdateDoneGame): Unit = {
    DBUtils.updateDataInTable(id, updateDoneGame, doneGamesConstants)
  }

}
