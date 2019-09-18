package im.pes.db

import com.github.dwickern.macros.NameOf.nameOf
import im.pes.constants.{ActivityTypes, CommonConstants, Tables}
import im.pes.main.spark.implicits._
import im.pes.main.stmt
import im.pes.utils.DBUtils
import org.apache.spark.sql.types.{DataTypes, StructField, StructType}
import org.apache.spark.sql.{DataFrame, Row, functions}

object ActiveGames {

  val activitiesConstants: Tables.Activities.type = Tables.Activities
  val activeGamesConstants: Tables.ActiveGames.type = Tables.ActiveGames
  val activeGamesTeamsDataConstants: Tables.ActiveGamesTeamsData.type = Tables.ActiveGamesTeamsData
  val activeGamesPlayersDataConstants: Tables.ActiveGamesPlayersData.type = Tables.ActiveGamesPlayersData
  val activeGamesReservePlayersConstants: Tables.ActiveGamesReservePlayers.type = Tables.ActiveGamesReservePlayers
  val summaryConstants: Tables.Summary.type = Tables.Summary
  val playerState = "playerState"
  val firstTeamId = "firstTeamId"
  val secondTeamId = "secondTeamId"

  val addActivitySchema: StructType = (new StructType)
    .add(nameOf(activitiesConstants.activityType), DataTypes.StringType, nullable = false)
  val addRunActivitySchema: StructType = addActivitySchema
    .add(nameOf(activitiesConstants.angle), DataTypes.IntegerType)
  val addStayActivitySchema: StructType = addActivitySchema
    .add(nameOf(activitiesConstants.x), DataTypes.IntegerType)
    .add(nameOf(activitiesConstants.y), DataTypes.IntegerType)
  val addShotActivitySchema: StructType = addActivitySchema
    .add(nameOf(activitiesConstants.firstAngle), DataTypes.IntegerType)
    .add(nameOf(activitiesConstants.secondAngle), DataTypes.IntegerType)
    .add(nameOf(activitiesConstants.power), DataTypes.IntegerType)
  val addPassActivitySchema: StructType = addActivitySchema
    .add(nameOf(activitiesConstants.firstAngle), DataTypes.IntegerType)
    .add(nameOf(activitiesConstants.secondAngle), DataTypes.IntegerType)
    .add(nameOf(activitiesConstants.power), DataTypes.IntegerType)
  val addTackleActivitySchema: StructType = addActivitySchema
    .add(nameOf(activitiesConstants.angle), DataTypes.IntegerType)

  val addTeamPlayersSchema: StructType = StructType(
    Array(StructField(nameOf(activeGamesPlayersDataConstants.playerId), DataTypes.IntegerType),
      StructField(playerState, DataTypes.StringType)))

  val addActiveGameSchema: StructType = (new StructType)
    .add(firstTeamId, DataTypes.IntegerType)
    .add(secondTeamId, DataTypes.IntegerType)
    .add(nameOf(activeGamesConstants.championship), DataTypes.StringType)
    .add(nameOf(activeGamesConstants.championshipState), DataTypes.StringType)

  val summarySchema: StructType = (new StructType)
    .add(summaryConstants.goals, DataTypes.IntegerType, nullable = false)
    .add(summaryConstants.donePasses, DataTypes.IntegerType, nullable = false)
    .add(summaryConstants.smartPasses, DataTypes.IntegerType, nullable = false)
    .add(summaryConstants.passes, DataTypes.IntegerType, nullable = false)
    .add(summaryConstants.doneShots, DataTypes.IntegerType, nullable = false)
    .add(summaryConstants.shots, DataTypes.IntegerType, nullable = false)
    .add(summaryConstants.doneTackles, DataTypes.IntegerType, nullable = false)
    .add(summaryConstants.tackles, DataTypes.IntegerType, nullable = false)
    .add(summaryConstants.dribblingCount, DataTypes.IntegerType, nullable = false)
    .add(summaryConstants.hooks, DataTypes.IntegerType, nullable = false)
    .add(summaryConstants.ballLosses, DataTypes.IntegerType, nullable = false)
    .add(summaryConstants.aerialsWon, DataTypes.IntegerType, nullable = false)
    .add(summaryConstants.assists, DataTypes.IntegerType, nullable = false)
    .add(summaryConstants.falls, DataTypes.IntegerType, nullable = false)
    .add(summaryConstants.mileage, DataTypes.IntegerType, nullable = false)
    .add(summaryConstants.yellowCards, DataTypes.IntegerType, nullable = false)
    .add(summaryConstants.redCard, DataTypes.BooleanType, nullable = false)


  def getActiveGames(params: Map[String, String]): String = {
    DBUtils.getTableDataAsString(activeGamesConstants, params)
  }

  def getActiveGame(id: Int): String = {
    DBUtils.getTableDataAsStringByPrimaryKey(activeGamesConstants, id)
  }

  def getActiveGamePlayersData(gmeId: Int): String = {
    DBUtils.getTableDataAsString(activeGamesPlayersDataConstants,
      Map(activeGamesPlayersDataConstants.gameId -> gmeId.toString),
      Seq(activeGamesPlayersDataConstants.id, activeGamesPlayersDataConstants.gameId))
  }

  def getActiveGamePlayersActivities(gameId: Int, activityId: Int): String = {
    val df = DBUtils.getTable(activitiesConstants, rename = false)
      .filter(s"${activitiesConstants.gameId} = $gameId")
      .filter(s"${activitiesConstants.id} > $activityId")
    DBUtils.dataToJsonFormat(DBUtils.renameColumns(df.drop(activitiesConstants.gameId), activitiesConstants))
  }

  def addActiveGame(df: DataFrame, rename: Boolean = true): Int = {
    val id = DBUtils.getTable(activeGamesConstants, rename = false).count + 1
    val addDf = if (rename) DBUtils.renameColumnsToDBFormat(df, activeGamesConstants) else df
    DBUtils.addDataToTable(activeGamesConstants.tableName,
      addDf.drop(firstTeamId, secondTeamId).withColumn(activeGamesConstants.id, functions.lit(id))
        .withColumn(activeGamesConstants.startTimestamp, functions.lit(0)))
    val data = df.first()
    addActiveGameTeamData(id.toInt, data.getAs[Int](firstTeamId))
    addActiveGameTeamData(id.toInt, data.getAs[Int](secondTeamId))
    id.toInt
  }

  def addActiveGame(firstTeamId: Int, secondTeamId: Int, championship: String, championshipState: String): Int = {
    addActiveGame(Seq((firstTeamId, secondTeamId, championship, championshipState))
      .toDF(this.firstTeamId, this.secondTeamId, activeGamesConstants.championship,
        activeGamesConstants.championshipState), rename = false)
  }

  def addActiveGameTeamData(gameId: Int, teamId: Int): Unit = {
    val id = DBUtils.getTable(activeGamesTeamsDataConstants, rename = false).count + 1
    val data = Seq((id, gameId, teamId, false))
      .toDF(activeGamesTeamsDataConstants.id, activeGamesTeamsDataConstants.gameId,
        activeGamesTeamsDataConstants.teamId, activeGamesTeamsDataConstants.isReady)
    DBUtils.addDataToTable(activeGamesTeamsDataConstants.tableName, data)
  }

  def addActiveGamePlayerData(gameId: Int, playerId: Int): Unit = {
    val id = DBUtils.getTable(activeGamesPlayersDataConstants, rename = false).count + 1
    val data = Seq((id, gameId, playerId, true, CommonConstants.defaultSummaryJson))
      .toDF(activeGamesPlayersDataConstants.id, activeGamesPlayersDataConstants.gameId,
        activeGamesPlayersDataConstants.playerId, activeGamesPlayersDataConstants.active,
        activeGamesPlayersDataConstants.summary)
    DBUtils.addDataToTable(activeGamesPlayersDataConstants.tableName, data)
  }

  def addActiveGameReservePlayer(gameId: Int, playerId: Int): Unit = {
    val id = DBUtils.getTable(activeGamesReservePlayersConstants, rename = false).count + 1
    val data = Seq((id, gameId, playerId))
      .toDF(activeGamesReservePlayersConstants.id, activeGamesReservePlayersConstants.gameId,
        activeGamesReservePlayersConstants.playerId)
    DBUtils.addDataToTable(activeGamesReservePlayersConstants.tableName, data)
  }

  def updateActiveGame(id: Int, updateData: Map[String, Any]): Unit = {
    DBUtils.updateDataInTableByPrimaryKey(id, updateData, activeGamesConstants.tableName)
  }

  def updateActiveGameTeam(id: Int, updateData: Map[String, Any]): Unit = {
    DBUtils
      .updateDataInTable(activeGamesTeamsDataConstants.teamId, id, updateData, activeGamesTeamsDataConstants.tableName)
  }

  def deleteActiveGame(id: Int): Unit = {
    DBUtils.deleteDataFromTable(activeGamesConstants.tableName, id)
    DBUtils.deleteDataFromTable(activeGamesTeamsDataConstants.tableName, activeGamesTeamsDataConstants.gameId, id)
    DBUtils.deleteDataFromTable(activeGamesPlayersDataConstants.tableName, activeGamesPlayersDataConstants.gameId, id)
    DBUtils.deleteDataFromTable(activitiesConstants.tableName, activitiesConstants.gameId, id)
    DBUtils
      .deleteDataFromTable(activeGamesReservePlayersConstants.tableName, activeGamesReservePlayersConstants.gameId, id)
  }

  def deleteReservePlayer(playerId: Int): Unit = {
    DBUtils
      .deleteDataFromTable(activeGamesReservePlayersConstants.tableName, activeGamesReservePlayersConstants.playerId,
        playerId)
  }

  def addStayActivity(gameId: Int, playerId: Int, x: Int, y: Int): Unit = {
    val df = Seq((ActivityTypes.stay, x, y)).toDF(activitiesConstants.activityType, activitiesConstants.x,
      activitiesConstants.y)
    addActivity(gameId, playerId, df)
  }

  def addActivity(gameId: Int, playerId: Int, activityDf: DataFrame): Unit = {
    val id = DBUtils.getTable(activitiesConstants, rename = false).count + 1
    DBUtils.addDataToTable(activitiesConstants.tableName,
      DBUtils.renameColumnsToDBFormat(
        activityDf.withColumn(activitiesConstants.timestamp, functions.lit(System.currentTimeMillis()))
        .withColumn(activitiesConstants.gameId, functions.lit(gameId))
          .withColumn(activitiesConstants.playerId, functions.lit(playerId)),
        activitiesConstants).withColumn(activitiesConstants.id, functions.lit(id)))
  }

  def updateSummary(playerId: Int, data: List[String]): Unit = {
    val builder = StringBuilder.newBuilder
    for (key <- data) {
      try {
        summaryConstants.getClass.getDeclaredField(key)
        builder.append(", '$.").append(key).append("', ")
        if (key.equals(summaryConstants.redCard)) {
          builder.append(true)
        } else {
          builder.append(CommonConstants.jsonExtractFormat(activeGamesPlayersDataConstants.summary, key)).append(" + 1")
        }
      } catch {
        case _: NoSuchFieldException =>
      }
    }
    stmt.executeUpdate(CommonConstants.sqlUpdateReplaceJsonQuery(activeGamesPlayersDataConstants.tableName,
      activeGamesPlayersDataConstants.summary, builder.toString(), activeGamesPlayersDataConstants.playerId, playerId))
  }

  def deactivatePlayer(playerId: Int): Unit = {
    DBUtils.updateDataInTable(activeGamesPlayersDataConstants.playerId, playerId,
      Map(activeGamesPlayersDataConstants.active -> false), activeGamesPlayersDataConstants.tableName)
  }

  def getGameData(id: Int): Row = {
    DBUtils.getTableDataByPrimaryKey(activeGamesConstants, id)
  }

  def getGameDF(id: Int): DataFrame = {
    DBUtils.getTableDfByPrimaryKey(activeGamesConstants, id)
  }

  def getGamePlayers(gameId: Int): Array[Row] = {
    DBUtils.renameColumns(DBUtils.getTable(activeGamesPlayersDataConstants, rename = false)
      .filter(s"${activeGamesPlayersDataConstants.gameId} = $gameId"), activeGamesPlayersDataConstants).collect()
  }

  def playerDataExists(playerId: Int): Boolean = {
    !DBUtils.getTable(activeGamesPlayersDataConstants, rename = false)
      .filter(s"${activeGamesPlayersDataConstants.playerId} = $playerId").isEmpty
  }

  def isPlayerActive(playerId: Int): Boolean = {
    !DBUtils.getTable(activeGamesPlayersDataConstants, rename = false)
      .filter(s"${activeGamesPlayersDataConstants.playerId} = $playerId")
      .filter(s"${activeGamesPlayersDataConstants.active} = true").isEmpty
  }

  def checkReservePlayer(playerId: Int): Boolean = {
    !DBUtils.getTable(activeGamesReservePlayersConstants, rename = false)
      .filter(s"${activeGamesReservePlayersConstants.playerId} = $playerId").isEmpty
  }

  def getActiveGameTeamsIds(id: Int): Seq[Int] = {
    DBUtils.getTable(activeGamesTeamsDataConstants, rename = false).filter(s"${activeGamesTeamsDataConstants.gameId} = $id")
      .select(activeGamesTeamsDataConstants.teamId).as[Int].collect()
  }

  def isActiveGameTeamNotReady(gameId: Int, teamId: Int): Boolean = {
    !DBUtils.getTable(activeGamesTeamsDataConstants, rename = false)
      .filter(s"${activeGamesTeamsDataConstants.gameId} = $gameId")
      .filter(s"${activeGamesTeamsDataConstants.teamId} = $teamId")
      .filter(s"${activeGamesTeamsDataConstants.isReady} = false").isEmpty
  }

  def activeGameTeamsReady(gameId: Int): Boolean = {
    DBUtils.getTable(activeGamesTeamsDataConstants, rename = false)
      .filter(s"${activeGamesTeamsDataConstants.gameId} = $gameId")
      .filter(s"${activeGamesTeamsDataConstants.isReady} = true").count == 2
  }

}
