package im.pes.db

import im.pes.constants.Tables
import im.pes.main.spark
import im.pes.utils.{BaseTable, DBUtils}

case class DoneGame(id: Int, firstTeamId: Int, secondTeamId: Int, firstTeamGoals: Int, secondTeamGoals: Int,
                    championship: String, championshipState: String, date: String)

case class UpdateDoneGame(firstTeamId: Option[Int], secondTeamId: Option[Int], championship: Option[String],
                          championshipState: Option[String], date: Option[String]) extends BaseTable

case class PlayerData(id: Int, goals: Int, donePasses: Int, smartPasses: Int, passes: Int, doneShots: Int, shots: Int,
                      doneTackles: Int, tackles: Int, dribblingCount: Int, hooks: Int, ballLosses: Int, aerialsWon: Int,
                      assists: Int, falls: Int, mileage: Int, yellowCards: Int, redCard: Boolean)

case class TeamData(id: Int, goals: Int, possession: Int, yellowCards: Int, redCards: Int, falls: Int, shots: Int,
                    aerialsWon: Int, playersData: List[PlayerData])

case class DoneGameData(firstTeamData: TeamData, secondTeamData: TeamData, championship: String,
                        championshipState: String, date: String)

object DoneGames {

  private val doneGamesConstants = Tables.DoneGames

  def getDoneGames(params: Map[String, String]): String = {
    DBUtils.getTableData(doneGamesConstants, params)
  }

  def getDoneGame(id: Int): String = {
    DBUtils.getTableDataByPrimaryKey(doneGamesConstants, id)
  }

  def addDoneGame(doneGameData: DoneGameData): Int = {
    addDoneGame(doneGameData.firstTeamData.id, doneGameData.secondTeamData.id, doneGameData.firstTeamData.goals,
      doneGameData.secondTeamData.goals, doneGameData.championship, doneGameData.championshipState, doneGameData.date)
  }

  def addDoneGame(firstTeamId: Int, secondTeamId: Int, firstTeamGoals: Int, secondTeamGoals: Int, championship: String,
                  championshipState: String, date: String): Int = {
    val id = DBUtils.getTable(doneGamesConstants).count() + 1
    val data = spark
      .createDataFrame(
        Seq((id, firstTeamId, secondTeamId, firstTeamGoals, secondTeamGoals, championship, championshipState, date)))
      .toDF(doneGamesConstants.id, doneGamesConstants.firstTeamId, doneGamesConstants.secondTeamId,
        doneGamesConstants.firstTeamGoals, doneGamesConstants.secondTeamGoals, doneGamesConstants.championship,
        doneGamesConstants.championshipState, doneGamesConstants.date)
    DBUtils.addDataToTable(doneGamesConstants.tableName, data)
    id.toInt
  }

}
