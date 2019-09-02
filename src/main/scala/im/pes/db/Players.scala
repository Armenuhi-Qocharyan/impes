package im.pes.db

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import im.pes.Health
import im.pes.constants.{CommonConstants, Tables}
import im.pes.main.spark
import im.pes.utils.{BaseTable, DBUtils}
import spray.json._

case class Player(id: Int, name: String, teamId: Int, position: String, cost: Int, age: Int, height: Int, weight: Int,
                  skills: Int, gameIntelligence: Int, teamPlayer: Int, physique: Int)

case class PartialPlayer(name: String, teamId: Int, position: String, age: Int, height: Int, weight: Int,
                         gameIntelligence: Int, teamPlayer: Int, physique: Int)

case class UpdatePlayer(name: Option[String], teamId: Option[Int], position: Option[String], age: Option[Int],
                        height: Option[Int], weight: Option[Int], gameIntelligence: Option[Int],
                        teamPlayer: Option[Int], physique: Option[Int]) extends BaseTable

case class UpdatePlayerWithSkills(name: Option[String], teamId: Option[Int], position: Option[String],
                                  cost: Option[Int],
                                  age: Option[Int], height: Option[Int], weight: Option[Int], skills: Option[Int],
                                  gameIntelligence: Option[Int], teamPlayer: Option[Int],
                                  physique: Option[Int]) extends BaseTable

trait PlayerJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val playerFormat: RootJsonFormat[Player] = jsonFormat12(Player)
}

object Players extends PlayerJsonSupport {

  private val playersConstants = Tables.Players

  def getPlayers(params: Map[String, String]): String = {
    DBUtils.getTableData(playersConstants, params)
  }

  def getPlayer(id: Int): String = {
    DBUtils.getTableDataByPrimaryKey(playersConstants, id)
  }

  def addPlayer(partialPlayer: PartialPlayer, skills: Int, cost: Int): Unit = {
    addPlayer(partialPlayer.teamId, partialPlayer.name, partialPlayer.position, cost, partialPlayer.age,
      partialPlayer.height, partialPlayer.weight, skills, partialPlayer.gameIntelligence, partialPlayer.teamPlayer,
      partialPlayer.physique)
  }

  private def addPlayer(teamId: Int, name: String, position: String, cost: Int, age: Int, height: Int, weight: Int,
                        skills: Int, gameIntelligence: Int, teamPlayer: Int, physique: Int): Unit = {
    val data = spark.createDataFrame(
      Seq((DBUtils.getTable(playersConstants).count() +
        1, name, teamId, position, cost, age, height, weight, skills, gameIntelligence, teamPlayer, physique)))
      .toDF(playersConstants.id, playersConstants.name, playersConstants.teamId, playersConstants.position,
        playersConstants.cost, playersConstants.age, playersConstants.height, playersConstants.weight,
        playersConstants.skills, playersConstants.gameIntelligence, playersConstants.teamPlayer,
        playersConstants.physique)
    DBUtils.addDataToTable(playersConstants.tableName, data)
  }

  def updatePlayer(playerData: PlayerData): Unit = {
    val player = DBUtils.getTableDataByPrimaryKey(playersConstants, playerData.id).parseJson.convertTo[Player]
    val gameIntelligence = calculateGameIntelligence(player.gameIntelligence, playerData)
    val teamPlayer = calculateTeamPlayer(player.teamPlayer, playerData)
    val physique = calculatePhysique(player.physique, playerData)
    updatePlayer(playerData.id,
      UpdatePlayer(None, None, None, None, None, None, Option(gameIntelligence), Option(teamPlayer), Option(physique)))
  }

  def updatePlayer(id: Int, updatePlayer: UpdatePlayer): Unit = {
    if (updatePlayer.gameIntelligence.isDefined || updatePlayer.teamPlayer.isDefined ||
      updatePlayer.physique.isDefined) {
      DBUtils.updateDataInTable(id, getUpdatePlayerWithSkills(id, updatePlayer), playersConstants)
    } else {
      DBUtils.updateDataInTable(id, updatePlayer, playersConstants)
    }
  }

  private def getUpdatePlayerWithSkills(playerId: Int, updatePlayer: UpdatePlayer): UpdatePlayerWithSkills = {
    val player = DBUtils.getTableDataByPrimaryKey(playersConstants, playerId).parseJson.convertTo[Player]
    val gameIntelligence = if (updatePlayer.gameIntelligence.isDefined) updatePlayer.gameIntelligence.get else player
      .gameIntelligence
    val teamPlayer = if (updatePlayer.teamPlayer.isDefined) updatePlayer.teamPlayer.get else player.teamPlayer
    val physique = if (updatePlayer.physique.isDefined) updatePlayer.physique.get else player.physique
    val skills = calculateSkills(gameIntelligence, teamPlayer, physique)
    val age = if (updatePlayer.age.isDefined) updatePlayer.age.get else player.age
    val cost = calculateCost(skills, age)
    UpdatePlayerWithSkills(updatePlayer.name, updatePlayer.teamId, updatePlayer.position, Option(cost),
      updatePlayer.age,
      updatePlayer.height, updatePlayer.weight, Option(skills), updatePlayer.gameIntelligence, updatePlayer.teamPlayer,
      updatePlayer.physique)
  }

  def calculateSkills(game_intelligence: Int, team_player: Int, physique: Int): Int = {
    ((CommonConstants.kGameIntelligence * game_intelligence + CommonConstants.kTeamPlayer * team_player +
      CommonConstants.kPhysique * physique) /
      (CommonConstants.kGameIntelligence + CommonConstants.kTeamPlayer + CommonConstants.kPhysique)).toInt
  }

  def calculateCost(skill: Int, age: Int): Int = {
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

  private def calculateGameIntelligence(gameIntelligence: Int, playerData: PlayerData): Int = {
    val k: Float = (1000 - gameIntelligence).toFloat / 1000
    val goalsPoints = if (playerData.goals > 4) 20 else playerData.goals * 5
    val ballLossesPoints = if (playerData.ballLosses > 20) 20 else playerData.ballLosses
    var passesPoints = 4 * playerData.assists + 2 * playerData.smartPasses
    passesPoints = if (passesPoints > 40) 20 else passesPoints
    val dribblingPoints = if (playerData.dribblingCount > 20) 10 else playerData.dribblingCount / 2
    val hooksPoints = if (playerData.hooks > 20) 10 else playerData.hooks / 2
    val diff = goalsPoints - ballLossesPoints + passesPoints + dribblingPoints + hooksPoints
    if (diff > 0) {
      (gameIntelligence + k * diff).toInt
    } else {
      gameIntelligence
    }
  }

  private def calculateTeamPlayer(teamPlayer: Int, playerData: PlayerData): Int = {
    val k: Float = (1000 - teamPlayer).toFloat / 1000
    var passesPoints = 2 * playerData.assists + playerData.smartPasses + playerData.donePasses -
      2 * (playerData.passes - playerData.donePasses)
    passesPoints = if (passesPoints > 40) 40 else passesPoints
    val ballLossesPoints = if (playerData.ballLosses > 20) 20 else playerData.ballLosses
    var shotsPoints = playerData.shots - 2 * playerData.doneShots
    shotsPoints = if (shotsPoints < 0) 0 else shotsPoints
    val dribblingPoints = if (playerData.dribblingCount > 20) 10 else playerData.dribblingCount / 2
    val diff = passesPoints - ballLossesPoints - shotsPoints + dribblingPoints
    if (diff > 0) {
      (teamPlayer + k * diff).toInt
    } else {
      teamPlayer
    }
  }

  private def calculatePhysique(physique: Int, playerData: PlayerData): Int = {
    val k: Float = (1000 - physique).toFloat / 1000
    val mileagePoints = if (playerData.mileage > 10000) 25 else playerData.mileage / 400
    val tacklesPoints = if (playerData.tackles > 50) 25 else playerData.tackles / 2
    val ballLossesPoints = if (playerData.ballLosses > 20) 20 else playerData.ballLosses
    val diff = mileagePoints + tacklesPoints - ballLossesPoints
    if (diff > 0) {
      (physique + k * diff).toInt
    } else {
      physique
    }
  }

  def deletePlayer(id: Int): Unit = {
    DBUtils.deleteDataFromTable(playersConstants.tableName, id)
  }

  def checkPlayer(id: Int, userId: Int): Boolean = {
    val teamsConstants = Tables.Teams
    val player = DBUtils.getTableDataByPrimaryKey(playersConstants, id)
    if (null == player) {
      false
    } else {
      DBUtils.getTable(teamsConstants).filter(s"${teamsConstants.id} = ${player.parseJson.convertTo[Player].teamId}")
        .filter(s"${teamsConstants.owner} = $userId").count() != 0
    }
  }

  def getPlayerData(id: Int): Player = {
    val player = DBUtils.getTableDataByPrimaryKey(playersConstants, id)
    if (null == player) {
      null
    } else {
      player.parseJson.convertTo[Player]
    }
  }

}
