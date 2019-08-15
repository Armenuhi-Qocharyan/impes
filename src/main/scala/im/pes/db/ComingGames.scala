package im.pes.db

import im.pes.constants.Tables
import im.pes.main.spark
import im.pes.utils.DBUtils

case class ComingGame(id: Int, firstTeamId: Int, secondTeamId: Int, championship: String, championshipState: String,
                      date: String)

case class PartialComingGame(firstTeamId: Int, secondTeamId: Int, championship: String, championshipState: String,
                             date: String)

object ComingGames {

  private val comingGamesConstants = Tables.ComingGames

  def addComingGame(partialComingGame: PartialComingGame): Unit = {
    addComingGame(partialComingGame.firstTeamId, partialComingGame.secondTeamId, partialComingGame.championship,
      partialComingGame.championshipState, partialComingGame.date)
  }

  def addComingGame(firstTeamId: Int, secondTeamId: Int, championship: String, championshipState: String,
                    date: String): Unit = {
    val data = spark
      .createDataFrame(Seq((DBUtils.getTable(comingGamesConstants.tableName).count() +
        1, firstTeamId, secondTeamId, championship, championshipState, date)))
      .toDF(comingGamesConstants.id, comingGamesConstants.teamOne, comingGamesConstants.teamTwo,
        comingGamesConstants.championship, comingGamesConstants.championship_state, comingGamesConstants.date)
    DBUtils.addDataToTable(comingGamesConstants.tableName, data)
  }

  def getComingGames: String = {
    DBUtils.getTableData(comingGamesConstants.tableName)
  }

  def getComingGame(id: Int): String = {
    val comingGames = DBUtils.getTable(comingGamesConstants.tableName).filter(s"${comingGamesConstants.id} = $id")
      .toJSON.collect()
    comingGames(0)
  }

}
