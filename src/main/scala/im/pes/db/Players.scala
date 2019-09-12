package im.pes.db

import com.github.dwickern.macros.NameOf.nameOf
import im.pes.constants.{CommonConstants, Tables}
import im.pes.main.spark.implicits._
import im.pes.utils.DBUtils
import org.apache.spark.sql.types.{DataTypes, StructType}
import org.apache.spark.sql.{DataFrame, Row, functions}

object Players {

  val playersConstants: Tables.Players.type = Tables.Players
  val addPlayerSchema: StructType = (new StructType)
    .add(nameOf(playersConstants.name), DataTypes.StringType, nullable = false)
    .add(nameOf(playersConstants.teamId), DataTypes.IntegerType, nullable = false)
    .add(nameOf(playersConstants.position), DataTypes.StringType, nullable = false)
    .add(nameOf(playersConstants.age), DataTypes.IntegerType, nullable = false)
    .add(nameOf(playersConstants.height), DataTypes.IntegerType, nullable = false)
    .add(nameOf(playersConstants.weight), DataTypes.IntegerType, nullable = false)
    .add(nameOf(playersConstants.gameIntelligence), DataTypes.IntegerType, nullable = false)
    .add(nameOf(playersConstants.teamPlayer), DataTypes.IntegerType, nullable = false)
    .add(nameOf(playersConstants.physique), DataTypes.IntegerType, nullable = false)
  val updatePlayerSchema: StructType = (new StructType)
    .add(nameOf(playersConstants.name), DataTypes.StringType)
    .add(nameOf(playersConstants.teamId), DataTypes.IntegerType)
    .add(nameOf(playersConstants.position), DataTypes.StringType)
    .add(nameOf(playersConstants.age), DataTypes.IntegerType)
    .add(nameOf(playersConstants.height), DataTypes.IntegerType)
    .add(nameOf(playersConstants.weight), DataTypes.IntegerType)
    .add(nameOf(playersConstants.gameIntelligence), DataTypes.IntegerType)
    .add(nameOf(playersConstants.teamPlayer), DataTypes.IntegerType)
    .add(nameOf(playersConstants.physique), DataTypes.IntegerType)


  def getPlayers(params: Map[String, String]): String = {
    DBUtils.getTableDataAsString(playersConstants, params)
  }

  def getPlayer(id: Int): String = {
    DBUtils.getTableDataAsStringByPrimaryKey(playersConstants, id)
  }

  def addPlayer(df: DataFrame, cost: Int, skills: Int): Unit = {
    val id = DBUtils.getTable(playersConstants, rename = false).count() + 1
    DBUtils.addDataToTable(playersConstants.tableName,
      DBUtils.renameColumnsToDBFormat(df, playersConstants).withColumn(playersConstants.id, functions.lit(id))
        .withColumn(playersConstants.cost, functions.lit(cost))
        .withColumn(playersConstants.skills, functions.lit(skills)))
  }

  def updatePlayer(playerId: Int, summaryData: Row): Unit = {
    val player = getPlayerData(playerId)
    val age = player.getAs[Int](playersConstants.age)
    val gameIntelligence = calculateGameIntelligence(player.getAs[Int](playersConstants.gameIntelligence), summaryData)
    val teamPlayer = calculateTeamPlayer(player.getAs[Int](playersConstants.teamPlayer), summaryData)
    val physique = calculatePhysique(player.getAs[Int](playersConstants.physique), summaryData)
    updatePlayer(playerId, Seq((gameIntelligence, teamPlayer, physique, age))
      .toDF(nameOf(playersConstants.gameIntelligence), nameOf(playersConstants.teamPlayer),
        nameOf(playersConstants.physique), nameOf(playersConstants.age)))
  }

  def updatePlayer(id: Int, updateDf: DataFrame): Unit = {
    val updateData = updateDf.collect()(0)
    if (Option(updateData.getAs[Int](nameOf(playersConstants.gameIntelligence))).isDefined ||
      Option(updateData.getAs[Int](nameOf(playersConstants.teamPlayer))).isDefined ||
      Option(updateData.getAs[Int](nameOf(playersConstants.physique))).isDefined ||
      Option(updateData.getAs[Int](nameOf(playersConstants.age))).isDefined) {
      val updatePlayerWithSkillsDf = getUpdatePlayerWithSkills(id, updateDf)
      updatePlayer(id, updatePlayerWithSkillsDf.collect()(0).getValuesMap(updatePlayerWithSkillsDf.columns))
    } else {
      val df = DBUtils.renameColumnsToDBFormat(updateDf, playersConstants)
      updatePlayer(id, df.collect()(0).getValuesMap(df.columns))
    }
  }

  private def getUpdatePlayerWithSkills(playerId: Int, updateDf: DataFrame): DataFrame = {
    val player = getPlayerData(playerId)
    val df = DBUtils.renameColumnsToDBFormat(updateDf, playersConstants)
    val updateData = df.collect()(0)
    val gameIntelligence = Option(updateData.getAs[Int](playersConstants.gameIntelligence))
      .getOrElse(player.getAs[Int](playersConstants.gameIntelligence))
    val teamPlayer = Option(updateData.getAs[Int](playersConstants.teamPlayer))
      .getOrElse(player.getAs[Int](playersConstants.teamPlayer))
    val physique = Option(updateData.getAs[Int](playersConstants.physique))
      .getOrElse(player.getAs[Int](playersConstants.physique))
    val age = Option(updateData.getAs[Int](playersConstants.age))
      .getOrElse(player.getAs[Int](playersConstants.age))
    val skills = calculateSkills(gameIntelligence, teamPlayer, physique)
    val cost = calculateCost(skills, age)
    df.withColumn(playersConstants.cost, functions.lit(cost)).withColumn(playersConstants.skills, functions.lit(skills))
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

  private def calculateGameIntelligence(gameIntelligence: Int, summaryData: Row): Int = {
    val k: Float = (1000 - gameIntelligence).toFloat / 1000
    val goals = summaryData.getAs[Int](Tables.Summary.goals)
    val ballLosses = summaryData.getAs[Int](Tables.Summary.ballLosses)
    val assists = summaryData.getAs[Int](Tables.Summary.assists)
    val smartPasses = summaryData.getAs[Int](Tables.Summary.smartPasses)
    val dribblingCount = summaryData.getAs[Int](Tables.Summary.dribblingCount)
    val hooks = summaryData.getAs[Int](Tables.Summary.hooks)
    val goalsPoints = if (goals > 4) 20 else goals * 5
    val ballLossesPoints = if (ballLosses > 20) 20 else ballLosses
    var passesPoints = 4 * assists + 2 * smartPasses
    passesPoints = if (passesPoints > 40) 20 else passesPoints
    val dribblingPoints = if (dribblingCount > 20) 10 else dribblingCount / 2
    val hooksPoints = if (hooks > 20) 10 else hooks / 2
    val diff = goalsPoints - ballLossesPoints + passesPoints + dribblingPoints + hooksPoints
    if (diff > 0) (gameIntelligence + k * diff).toInt else gameIntelligence
  }

  private def calculateTeamPlayer(teamPlayer: Int, summaryData: Row): Int = {
    val k: Float = (1000 - teamPlayer).toFloat / 1000
    val assists = summaryData.getAs[Int](Tables.Summary.assists)
    val smartPasses = summaryData.getAs[Int](Tables.Summary.smartPasses)
    val donePasses = summaryData.getAs[Int](Tables.Summary.donePasses)
    val passes = summaryData.getAs[Int](Tables.Summary.passes)
    val ballLosses = summaryData.getAs[Int](Tables.Summary.ballLosses)
    val shots = summaryData.getAs[Int](Tables.Summary.shots)
    val doneShots = summaryData.getAs[Int](Tables.Summary.doneShots)
    val dribblingCount = summaryData.getAs[Int](Tables.Summary.dribblingCount)
    var passesPoints = 2 * assists + smartPasses + donePasses - 2 * (passes - donePasses)
    passesPoints = if (passesPoints > 40) 40 else passesPoints
    val ballLossesPoints = if (ballLosses > 20) 20 else ballLosses
    var shotsPoints = shots - 2 * doneShots
    shotsPoints = if (shotsPoints < 0) 0 else shotsPoints
    val dribblingPoints = if (dribblingCount > 20) 10 else dribblingCount / 2
    val diff = passesPoints - ballLossesPoints - shotsPoints + dribblingPoints
    if (diff > 0) (teamPlayer + k * diff).toInt else teamPlayer
  }

  private def calculatePhysique(physique: Int, summaryData: Row): Int = {
    val k: Float = (1000 - physique).toFloat / 1000
    val mileage = summaryData.getAs[Int](Tables.Summary.mileage)
    val tackles = summaryData.getAs[Int](Tables.Summary.tackles)
    val ballLosses = summaryData.getAs[Int](Tables.Summary.ballLosses)
    val mileagePoints = if (mileage > 10000) 25 else mileage / 400
    val tacklesPoints = if (tackles > 50) 25 else tackles / 2
    val ballLossesPoints = if (ballLosses > 20) 20 else ballLosses
    val diff = mileagePoints + tacklesPoints - ballLossesPoints
    if (diff > 0) (physique + k * diff).toInt else physique
  }

  def deletePlayer(id: Int, cost: Int): Unit = {
    if (CommonConstants.defaultPlayers.contains(id)) {
      Players.updatePlayer(id,
        Map(playersConstants.teamId -> Teams.getUserTeam(CommonConstants.admins.head).getAs[Int](Tables.Teams.id)))
      Transactions.addPlayerTransaction(id, cost)
    } else {
      deletePlayer(id)
    }
  }

  def updatePlayer(id: Int, updateData: Map[String, Any]): Unit = {
    DBUtils.updateDataInTable(id, updateData, playersConstants.tableName)
  }

  def deletePlayer(id: Int): Unit = {
    DBUtils.deleteDataFromTable(playersConstants.tableName, id)
  }

  def checkPlayer(id: Int, userId: Int): Boolean = {
    val teamsConstants = Tables.Teams
    val player = getPlayerData(id)
    if (null == player) {
      false
    } else {
      DBUtils.getTable(teamsConstants, rename = false)
        .filter(s"${teamsConstants.id} = ${player.getAs[Int](playersConstants.teamId)}")
        .filter(s"${teamsConstants.owner} = $userId").count() != 0
    }
  }

  def getPlayerData(id: Int): Row = {
    DBUtils.getTableDataByPrimaryKey(playersConstants, id)
  }

  def getPlayerTeamId(id: Int): Int = {
    try {
      DBUtils.getTable(playersConstants, rename = false).filter(s"${Tables.primaryKey} = '$id'")
        .select(playersConstants.teamId).collect()(0).getInt(0)
    } catch {
      case _: ArrayIndexOutOfBoundsException => -1
    }
  }

  def getTeamPlayers(teamId: Int): Array[Row] = {
    DBUtils.getTable(playersConstants, rename = false).filter(s"${playersConstants.teamId} = $teamId")
      .select(playersConstants.id, playersConstants.cost).collect()
  }

}
