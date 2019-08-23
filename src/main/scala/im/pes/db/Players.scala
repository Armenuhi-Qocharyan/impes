package im.pes.db

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import im.pes.Health
import im.pes.constants.{CommonConstants, Tables}
import im.pes.main.spark
import im.pes.utils.{BaseTable, DBUtils}
import spray.json.{DefaultJsonProtocol, RootJsonFormat, _}

case class Player(id: Int, name: String, teamId: Int, scoredGoals: Int, assists: Int, position: String, cost: Int,
                  age: Int, height: Int, weight: Int, skills: Int, gameIntelligence: Int, teamPlayer: Int,
                  physique: Int, redCardsCount: Int, yellowCardsCount: Int)

case class PartialPlayer(name: String, teamId: Int, position: String, age: Int, height: Int, weight: Int,
                         gameIntelligence: Int, teamPlayer: Int, physique: Int)

case class UpdatePlayer(name: Option[String], teamId: Option[Int], position: Option[String], age: Option[Int],
                        height: Option[Int], weight: Option[Int],
                        gameIntelligence: Option[Int], teamPlayer: Option[Int], physique: Option[Int]) extends BaseTable

trait PlayerJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val playerFormat: RootJsonFormat[Player] = jsonFormat16(Player)
}

object Players extends PlayerJsonSupport {

  private val playersConstants = Tables.Players

  def addPlayer(partialPlayer: PartialPlayer, userId: Int): ToResponseMarshallable = {
    if (Teams.checkTeam(partialPlayer.teamId, userId)) {
      addPlayer(partialPlayer.teamId, partialPlayer.name, partialPlayer.position, partialPlayer.age,
        partialPlayer.height, partialPlayer.weight, partialPlayer.gameIntelligence, partialPlayer.teamPlayer,
        partialPlayer.physique)
    } else {
      StatusCodes.Forbidden
    }
  }

  private def addPlayer(teamId: Int, name: String, position: String, age: Int, height: Int, weight: Int,
                        gameIntelligence: Int, teamPlayer: Int, physique: Int): ToResponseMarshallable = {
    val skill = calculateSkill(gameIntelligence, teamPlayer, physique)
    val data = spark.createDataFrame(
      Seq((DBUtils.getTable(playersConstants).count() + 1, name, teamId, 0, 0, position, calculateCost(skill,
        age), age, height, weight, skill, gameIntelligence, teamPlayer, physique, 0, 0)))
      .toDF(playersConstants.id, playersConstants.name, playersConstants.teamId, playersConstants.scoredGoals,
        playersConstants.assists, playersConstants.position, playersConstants.cost, playersConstants.age,
        playersConstants.height, playersConstants.weight, playersConstants.skills, playersConstants.gameIntelligence,
        playersConstants.teamPlayer, playersConstants.physique, playersConstants.redCardsCount,
        playersConstants.yellowCardsCount)
    DBUtils.addDataToTable(playersConstants.tableName, data)
    StatusCodes.OK
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

  def getPlayers(params: Map[String, String]): ToResponseMarshallable = {
    DBUtils.getTableData(playersConstants, params)
  }

  def getPlayer(id: Int): ToResponseMarshallable = {
    val player = DBUtils.getTableDataByPrimaryKey(playersConstants, id)
    if (null == player) {
      StatusCodes.NotFound
    } else {
      player
    }
  }

  def deletePlayer(id: Int, userId: Int): ToResponseMarshallable = {
    if (checkPlayer(id, userId)) {
      DBUtils.deleteDataFromTable(playersConstants.tableName, id)
      StatusCodes.NoContent
    } else {
      StatusCodes.Forbidden
    }
  }

  def updatePlayer(id: Int, updatePlayer: UpdatePlayer, userId: Int): ToResponseMarshallable = {
    if (checkPlayer(id, userId)) {
      DBUtils.updateDataInTable(id, updatePlayer, playersConstants)
      StatusCodes.NoContent
    } else {
      StatusCodes.Forbidden
    }
  }

  private def checkPlayer(id: Int, userId: Int): Boolean = {
    val teamsConstants = Tables.Teams
    val players = DBUtils.getTable(playersConstants).filter(s"${playersConstants.id} = $id").toJSON.collect()
    if (players.length == 0) {
      false
    } else {
      val player = players(0).parseJson.convertTo[Player]
      DBUtils.getTable(teamsConstants).filter(s"${teamsConstants.id} = ${player.teamId}")
        .filter(s"${teamsConstants.owner} = $userId").count() != 0
    }
  }


}
