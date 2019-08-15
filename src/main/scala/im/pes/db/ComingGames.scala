package im.pes.db

import im.pes.constants.{CommonConstants, Tables}
import im.pes.main.{connectionProperties, spark}
import org.apache.spark.sql.SaveMode

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
    val comingGameId =
      spark.read.jdbc(CommonConstants.jdbcUrl, comingGamesConstants.tableName, connectionProperties).count() + 1
    val data = spark
      .createDataFrame(Seq((comingGameId, firstTeamId, secondTeamId, championship, championshipState, date)))
      .toDF(comingGamesConstants.id, comingGamesConstants.teamOne, comingGamesConstants.teamTwo,
        comingGamesConstants.championship, comingGamesConstants.championship_state, comingGamesConstants.date)
    data.write.mode(SaveMode.Append).jdbc(CommonConstants.jdbcUrl, comingGamesConstants.tableName, connectionProperties)
  }

}
