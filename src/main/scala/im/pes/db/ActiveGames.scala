package im.pes.db

import com.github.dwickern.macros.NameOf.nameOf
import im.pes.constants.{ActivityTypes, CommonConstants, Tables}
import im.pes.main.spark.implicits._
import im.pes.main.stmt
import im.pes.utils.DBUtils
import org.apache.spark.sql.types.{ArrayType, DataTypes, StructType}
import org.apache.spark.sql.{DataFrame, Row, functions}

object ActiveGames {

  val activitiesConstants: Tables.Activities.type = Tables.Activities
  val activeGamesConstants: Tables.ActiveGames.type = Tables.ActiveGames
  val activeGamesPlayersDataConstants: Tables.ActiveGamesPlayersData.type = Tables.ActiveGamesPlayersData
  val summaryConstants: Tables.Summary.type = Tables.Summary
  val addActiveGameConstants: Tables.AddActiveGameData.type = Tables.AddActiveGameData

  val addActivitySchema: StructType = (new StructType)
    .add(nameOf(activitiesConstants.activityType), DataTypes.StringType, nullable = false)
  val addRunActivitySchema: StructType = addActivitySchema
    .add(nameOf(activitiesConstants.angle), DataTypes.IntegerType, nullable = false)
  val addStayActivitySchema: StructType = addActivitySchema
    .add(nameOf(activitiesConstants.x), DataTypes.IntegerType, nullable = false)
    .add(nameOf(activitiesConstants.y), DataTypes.IntegerType, nullable = false)
  val addShotActivitySchema: StructType = addActivitySchema
    .add(nameOf(activitiesConstants.firstAngle), DataTypes.IntegerType, nullable = false)
    .add(nameOf(activitiesConstants.secondAngle), DataTypes.IntegerType, nullable = false)
    .add(nameOf(activitiesConstants.power), DataTypes.IntegerType, nullable = false)
  val addPassActivitySchema: StructType = addActivitySchema
    .add(nameOf(activitiesConstants.firstAngle), DataTypes.IntegerType, nullable = false)
    .add(nameOf(activitiesConstants.secondAngle), DataTypes.IntegerType, nullable = false)
    .add(nameOf(activitiesConstants.power), DataTypes.IntegerType, nullable = false)
  val addTackleActivitySchema: StructType = addActivitySchema
    .add(nameOf(activitiesConstants.angle), DataTypes.IntegerType, nullable = false)

  val teamPlayerSchema: StructType = (new StructType)
    .add(nameOf(addActiveGameConstants.playerId), DataTypes.IntegerType, nullable = false)
    .add(nameOf(addActiveGameConstants.playerState), DataTypes.StringType, nullable = false)

  val addActiveGameSchema: StructType = (new StructType)
    .add(nameOf(addActiveGameConstants.firstTeamId), DataTypes.IntegerType, nullable = false)
    .add(nameOf(addActiveGameConstants.secondTeamId), DataTypes.IntegerType, nullable = false)
    .add(nameOf(addActiveGameConstants.firstTeamPlayers), new ArrayType(teamPlayerSchema, containsNull = false),
      nullable = false)
    .add(nameOf(addActiveGameConstants.secondTeamPlayers), new ArrayType(teamPlayerSchema, containsNull = false),
      nullable = false)
    .add(nameOf(addActiveGameConstants.championship), DataTypes.StringType, nullable = false)
    .add(nameOf(addActiveGameConstants.championshipState), DataTypes.StringType, nullable = false)

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

  def addActiveGame(df: DataFrame): Int = {
    val id = DBUtils.getTable(activeGamesConstants, rename = false).count() + 1
    DBUtils.addDataToTable(activeGamesConstants.tableName,
      DBUtils.renameColumnsToDBFormat(df, activeGamesConstants).withColumn(activeGamesConstants.id, functions.lit(id)))
    id.toInt
  }

  def addActiveGamePlayerData(gameId: Int, playerId: Int): Unit = {
    val id = DBUtils.getTable(activeGamesPlayersDataConstants, rename = false).count() + 1
    val data = Seq((id, gameId, playerId, CommonConstants.defaultSummaryJson))
      .toDF(activeGamesPlayersDataConstants.id, activeGamesPlayersDataConstants.gameId,
        activeGamesPlayersDataConstants.playerId, activeGamesPlayersDataConstants.summary)
    DBUtils.addDataToTable(activeGamesPlayersDataConstants.tableName, data)
  }

  def deleteActiveGame(id: Int): Unit = {
    DBUtils.deleteDataFromTable(activeGamesConstants.tableName, id)
    DBUtils.deleteDataFromTable(activeGamesPlayersDataConstants.tableName, activeGamesPlayersDataConstants.gameId, id)
    DBUtils.deleteDataFromTable(activitiesConstants.tableName, activitiesConstants.gameId, id)
  }

  def addStayActivity(gameId: Int, playerId: Int, x: Int, y: Int): Unit = {
    val df = Seq((ActivityTypes.stay, x, y)).toDF(activitiesConstants.activityType, activitiesConstants.x,
      activitiesConstants.y)
    addActivity(gameId, playerId, df)
  }

  def addActivity(gameId: Int, playerId: Int, activityDf: DataFrame): Unit = {
    val id = DBUtils.getTable(activitiesConstants, rename = false).count() + 1
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

}
