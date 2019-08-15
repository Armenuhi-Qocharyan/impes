package im.pes.db

import im.pes.constants.{CommonConstants, Tables}
import im.pes.main.{connectionProperties, spark}
import org.apache.spark.sql.SaveMode

case class DoneGame(id: Int, firstTeamId: Int, secondTeamId: Int, championship: String, championshipState: String,
                    date: String)

case class PartialDoneGame(firstTeamId: Int, secondTeamId: Int, championship: String, championshipState: String,
                           date: String)

object DoneGames {

  private val doneGamesConstants = Tables.DoneGames

  def addDoneGame(partialDoneGame: PartialDoneGame): Unit = {
    addDoneGame(partialDoneGame.firstTeamId, partialDoneGame.secondTeamId, partialDoneGame.championship,
      partialDoneGame.championshipState, partialDoneGame.date)
  }

  def addDoneGame(firstTeamId: Int, secondTeamId: Int, championship: String, championshipState: String,
                  date: String): Unit = {
    val doneGameId =
      spark.read.jdbc(CommonConstants.jdbcUrl, doneGamesConstants.tableName, connectionProperties).count() + 1
    val data = spark
      .createDataFrame(Seq((doneGameId, firstTeamId, secondTeamId, championship, championshipState, date)))
      .toDF(doneGamesConstants.id, doneGamesConstants.teamOne, doneGamesConstants.teamTwo,
        doneGamesConstants.championship, doneGamesConstants.championship_state, doneGamesConstants.date)
    data.write.mode(SaveMode.Append).jdbc(CommonConstants.jdbcUrl, doneGamesConstants.tableName, connectionProperties)
  }

}
