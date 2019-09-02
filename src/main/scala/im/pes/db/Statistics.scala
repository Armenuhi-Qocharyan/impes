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

  private val teamStatisticsConstants = Tables.TeamsStatistics
  private val playerStatisticsConstants = Tables.PlayersStatistics

  def getTeamsStatistics(params: Map[String, String]): String = {
    DBUtils.getTableData(teamStatisticsConstants, params)
  }

  def getTeamStatistics(id: Int): String = {
    DBUtils.getTableDataByPrimaryKey(teamStatisticsConstants, id)
  }

  def getPlayersStatistics(params: Map[String, String]): String = {
    DBUtils.getTableData(playerStatisticsConstants, params)
  }

  def getPlayerStatistics(id: Int): String = {
    DBUtils.getTableDataByPrimaryKey(playerStatisticsConstants, id)
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
      .toDF(teamStatisticsConstants.id, teamStatisticsConstants.teamId, teamStatisticsConstants.doneGameId,
        teamStatisticsConstants.goals, teamStatisticsConstants.possession, teamStatisticsConstants.yellowCards,
        teamStatisticsConstants.redCards, teamStatisticsConstants.falls, teamStatisticsConstants.shots,
        teamStatisticsConstants.aerialsWon)
    DBUtils.addDataToTable(teamStatisticsConstants.tableName, data)
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
      .toDF(playerStatisticsConstants.id, playerStatisticsConstants.playerId, playerStatisticsConstants.teamId,
        playerStatisticsConstants.doneGameId, playerStatisticsConstants.goals, playerStatisticsConstants.donePasses,
        playerStatisticsConstants.smartPasses, playerStatisticsConstants.passes, playerStatisticsConstants.doneShots,
        playerStatisticsConstants.shots, playerStatisticsConstants.doneTackles, playerStatisticsConstants.tackles,
        playerStatisticsConstants.dribblingCount, playerStatisticsConstants.hooks, playerStatisticsConstants.ballLosses,
        playerStatisticsConstants.aerialsWon, playerStatisticsConstants.assists, playerStatisticsConstants.falls,
        playerStatisticsConstants.mileage, playerStatisticsConstants.yellowCards, playerStatisticsConstants.redCard)
    DBUtils.addDataToTable(playerStatisticsConstants.tableName, data)
  }

}
