package im.pes.db

import im.pes.constants.Tables
import im.pes.main.spark
import im.pes.utils.DBUtils

case class PlayerStatistics(id: Int, playerId: Int, teamId: Int, doneGameId: Int, goals: Int, donePasses: Int,
                            smartPasses: Int, passes: Int, doneShots: Int, shots: Int, doneTackles: Int, tackles: Int,
                            dribblingCount: Int, hooks: Int, ballLosses: Int, aerialsWon: Int, assists: Int, falls: Int,
                            mileage: Int, yellowCards: Int, redCard: Boolean)

case class TeamStatistics(id: Int, teamId: Int, doneGameId: Int, goals: Int, possession: Int, yellowCards: Int,
                          redCards: Int, falls: Int, shots: Int, aerialsWon: Int)

object Statistics {

  private val teamsStatisticsConstants = Tables.TeamsStatistics
  private val playersStatisticsConstants = Tables.PlayersStatistics

  def getTeamsStatistics(params: Map[String, String]): String = {
    DBUtils.getTableData(teamsStatisticsConstants, params)
  }

  def getTeamStatistics(id: Int): String = {
    DBUtils.getTableDataByPrimaryKey(teamsStatisticsConstants, id)
  }

  def getPlayersStatistics(params: Map[String, String]): String = {
    DBUtils.getTableData(playersStatisticsConstants, params)
  }

  def getPlayerStatistics(id: Int): String = {
    DBUtils.getTableDataByPrimaryKey(playersStatisticsConstants, id)
  }

  def addTeamStatistics(teamData: TeamData, doneGameId: Int): Unit = {
    addTeamStatistics(teamData.id, doneGameId, teamData.goals, teamData.possession, teamData.yellowCards,
      teamData.redCards, teamData.falls, teamData.shots, teamData.aerialsWon)
  }

  def addTeamStatistics(teamId: Int, doneGameId: Int, goals: Int, possession: Int, yellowCards: Int,
                        redCards: Int, falls: Int, shots: Int, aerialsWon: Int): Unit = {
    val data = spark
      .createDataFrame(Seq((
        1, teamId, doneGameId, goals, possession, yellowCards, redCards, falls, shots, aerialsWon)))
      .toDF(teamsStatisticsConstants.id, teamsStatisticsConstants.teamId, teamsStatisticsConstants.doneGameId,
        teamsStatisticsConstants.goals, teamsStatisticsConstants.possession, teamsStatisticsConstants.yellowCards,
        teamsStatisticsConstants.redCards, teamsStatisticsConstants.falls, teamsStatisticsConstants.shots,
        teamsStatisticsConstants.aerialsWon)
    DBUtils.addDataToTable(teamsStatisticsConstants.tableName, data)
  }

  def addPlayerStatistics(playerData: PlayerData, teamId: Int, doneGameId: Int): Unit = {
    addPlayerStatistics(playerData.id, teamId, doneGameId, playerData.goals, playerData.donePasses,
      playerData.smartPasses, playerData.passes, playerData.doneShots, playerData.shots, playerData.doneTackles,
      playerData.tackles, playerData.dribblingCount, playerData.hooks, playerData.ballLosses, playerData.aerialsWon,
      playerData.assists, playerData.falls, playerData.mileage, playerData.yellowCards, playerData.redCard)
  }

  def addPlayerStatistics(playerId: Int, teamId: Int, doneGameId: Int, goals: Int, donePasses: Int, smartPasses: Int,
                          passes: Int, doneShots: Int, shots: Int, doneTackles: Int, tackles: Int, dribblingCount: Int,
                          hooks: Int, ballLosses: Int, aerialsWon: Int, assists: Int, falls: Int, mileage: Int,
                          yellowCards: Int, redCard: Boolean): Unit = {
    val data = spark
      .createDataFrame(Seq((
        1, playerId, teamId, doneGameId, goals, donePasses, smartPasses, passes, doneShots, shots, doneTackles, tackles,
        dribblingCount, hooks, ballLosses, aerialsWon, assists, falls, mileage, yellowCards, redCard)))
      .toDF(playersStatisticsConstants.id, playersStatisticsConstants.playerId, playersStatisticsConstants.teamId,
        playersStatisticsConstants.doneGameId, playersStatisticsConstants.goals, playersStatisticsConstants.donePasses,
        playersStatisticsConstants.smartPasses, playersStatisticsConstants.passes, playersStatisticsConstants.doneShots,
        playersStatisticsConstants.shots, playersStatisticsConstants.doneTackles, playersStatisticsConstants.tackles,
        playersStatisticsConstants.dribblingCount, playersStatisticsConstants.hooks, playersStatisticsConstants.ballLosses,
        playersStatisticsConstants.aerialsWon, playersStatisticsConstants.assists, playersStatisticsConstants.falls,
        playersStatisticsConstants.mileage, playersStatisticsConstants.yellowCards, playersStatisticsConstants.redCard)
    DBUtils.addDataToTable(playersStatisticsConstants.tableName, data)
  }

}
