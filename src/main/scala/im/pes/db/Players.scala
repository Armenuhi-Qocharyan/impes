package im.pes.db

import im.pes.constants.{CommonConstants, Tables}
import im.pes.main.{connectionProperties, spark}
import org.apache.spark.sql.SaveMode

case class Player(id: Int, name: String, teamId: Int, scoredGoals: Int, assists: Int, position: String, cost: Int,
                  age: Int, height: Int, weight: Int, skills: Int, gameIntelligence: Int, teamPlayer: Int,
                  physique: Int, redCardsCount: Int, yellowCardsCount: Int)

case class PartialPlayer(name: String, teamId: Int, position: String, age: Int, height: Int, weight: Int,
                         gameIntelligence: Int, teamPlayer: Int, physique: Int)

object Players {

  private val playersConstants = Tables.Players

  def addPlayer(partialPlayer: PartialPlayer): Unit = {
    addPlayer(partialPlayer.teamId, partialPlayer.name, partialPlayer.position, partialPlayer.age, partialPlayer.height,
      partialPlayer.weight, partialPlayer.gameIntelligence, partialPlayer.teamPlayer, partialPlayer.physique)
  }

  def addPlayer(teamId: Int, name: String, position: String, age: Int, height: Int, weight: Int,
                gameIntelligence: Int, teamPlayer: Int, physique: Int): Unit = {
    val playerId = spark.read.jdbc(CommonConstants.jdbcUrl, playersConstants.tableName, connectionProperties).count() +
      1
    val skill = calculateSkill(gameIntelligence, teamPlayer, physique)
    val data = spark.createDataFrame(Seq((playerId, name, teamId, 0, 0, position, calculateCost(skill,
      age), age, height, weight, skill, gameIntelligence, teamPlayer, physique, 0, 0)))
      .toDF(playersConstants.id, playersConstants.name, playersConstants.team, playersConstants.scoredGoals,
        playersConstants.assists, playersConstants.position, playersConstants.cost, playersConstants.age,
        playersConstants.height, playersConstants.weight, playersConstants.skills, playersConstants.gameIntelligence,
        playersConstants.teamPlayer, playersConstants.physique, playersConstants.redCardsCount,
        playersConstants.yellowCardsCount)
    data.write.mode(SaveMode.Append).jdbc(CommonConstants.jdbcUrl, playersConstants.tableName, connectionProperties)
  }

  private def calculateSkill(game_intelligence: Int, team_player: Int, physique: Int): Int = {
    ((CommonConstants.kGameIntelligence * game_intelligence + CommonConstants.kTeamPlayer * team_player +
      CommonConstants.kPhysique * physique) /
      (CommonConstants.kGameIntelligence + CommonConstants.kTeamPlayer + CommonConstants.kPhysique)).toInt
  }

  private def calculateCost(skill: Int, age: Int): Int = {
    if (skill - CommonConstants.playerMinSkill < 100) {
      CommonConstants.playerMinCost + (CommonConstants.playerMaxAge - age) * 50 +
        (skill - CommonConstants.playerMinSkill) * 100
    } else if (CommonConstants.playerMaxSkill - skill < 100) {
      CommonConstants.playerMaxCost - (CommonConstants.playerMaxAge - age) * 50 -
        (CommonConstants.playerMaxSkill - skill) * 1000
    } else {
      skill * CommonConstants.playerMaxCost / CommonConstants.playerMaxSkill - age * 100
    }
  }

}
