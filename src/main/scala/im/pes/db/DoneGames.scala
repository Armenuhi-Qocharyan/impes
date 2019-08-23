package im.pes.db

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import im.pes.constants.Tables
import im.pes.main.spark
import im.pes.utils.{BaseTable, DBUtils}

case class DoneGame(id: Int, firstTeamId: Int, secondTeamId: Int, championship: String, championshipState: String,
                    date: String)

case class PartialDoneGame(firstTeamId: Int, secondTeamId: Int, championship: String, championshipState: String,
                           date: String)

case class UpdateDoneGame(firstTeamId: Option[Int], secondTeamId: Option[Int], championship: Option[String], championshipState: Option[String],
                            date: Option[String]) extends BaseTable

object DoneGames {

  private val doneGamesConstants = Tables.DoneGames

  def addDoneGame(partialDoneGame: PartialDoneGame): ToResponseMarshallable = {
    addDoneGame(partialDoneGame.firstTeamId, partialDoneGame.secondTeamId, partialDoneGame.championship,
      partialDoneGame.championshipState, partialDoneGame.date)
  }

  private def addDoneGame(firstTeamId: Int, secondTeamId: Int, championship: String, championshipState: String,
                  date: String): ToResponseMarshallable = {
    val data = spark
      .createDataFrame(Seq((DBUtils.getTable(doneGamesConstants).count() +
        1, firstTeamId, secondTeamId, championship, championshipState, date)))
      .toDF(doneGamesConstants.id, doneGamesConstants.teamOne, doneGamesConstants.teamTwo,
        doneGamesConstants.championship, doneGamesConstants.championshipState, doneGamesConstants.date)
    DBUtils.addDataToTable(doneGamesConstants.tableName, data)
    StatusCodes.OK
  }

  def getDoneGames(params: Map[String, String]): ToResponseMarshallable = {
    DBUtils.getTableData(doneGamesConstants, params)
  }

  def getDoneGame(id: Int): ToResponseMarshallable = {
    val doneGame = DBUtils.getTableDataByPrimaryKey(doneGamesConstants, id)
    if (null == doneGame) {
      StatusCodes.NotFound
    } else {
      doneGame
    }
  }

}
