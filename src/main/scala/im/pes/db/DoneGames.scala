package im.pes.db

import im.pes.constants.Tables
import im.pes.main.spark
import im.pes.utils.DBUtils

case class DoneGame(id: Int, firstTeamId: Int, secondTeamId: Int, championship: String, championshipState: String,
                    date: String)

case class PartialDoneGame(firstTeamId: Int, secondTeamId: Int, championship: String, championshipState: String,
                           date: String)

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
        doneGamesConstants.championship, doneGamesConstants.championship_state, doneGamesConstants.date)
    DBUtils.addDataToTable(doneGamesConstants.tableName, data)
  }

  def getDoneGames: String = {
    DBUtils.getTableData(doneGamesConstants.tableName)
  }

  def getDoneGame(id: Int): String = {
    val doneGames = DBUtils.getTable(doneGamesConstants.tableName).filter(s"${doneGamesConstants.id} = $id").toJSON
      .collect()
    doneGames(0)
  }

}
