package im.pes.db

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import im.pes.constants.Tables
import im.pes.main.spark
import im.pes.utils.{BaseTable, DBUtils}

case class ComingGame(id: Int, firstTeamId: Int, secondTeamId: Int, championship: String, championshipState: String,
                      date: String)

case class PartialComingGame(firstTeamId: Int, secondTeamId: Int, championship: String, championshipState: String,
                             date: String)

case class UpdateComingGame(firstTeamId: Option[Int], secondTeamId: Option[Int], championship: Option[String], championshipState: Option[String],
                             date: Option[String]) extends BaseTable

object ComingGames {

  private val comingGamesConstants = Tables.ComingGames

  def addComingGame(partialComingGame: PartialComingGame, userId: Int): ToResponseMarshallable = {
    if (DBUtils.checkAdmin(userId)) {
    addComingGame(partialComingGame.firstTeamId, partialComingGame.secondTeamId, partialComingGame.championship,
      partialComingGame.championshipState, partialComingGame.date)
    } else {
      StatusCodes.Forbidden
    }
  }

  private def addComingGame(firstTeamId: Int, secondTeamId: Int, championship: String, championshipState: String,
                    date: String): ToResponseMarshallable = {
    val data = spark
      .createDataFrame(Seq((DBUtils.getTable(comingGamesConstants).count() +
        1, firstTeamId, secondTeamId, championship, championshipState, date)))
      .toDF(comingGamesConstants.id, comingGamesConstants.firstTeamId, comingGamesConstants.secondTeamId,
        comingGamesConstants.championship, comingGamesConstants.championshipState, comingGamesConstants.date)
    DBUtils.addDataToTable(comingGamesConstants.tableName, data)
    StatusCodes.OK
  }

  def getComingGames(params: Map[String, String]): ToResponseMarshallable = {
    DBUtils.getTableData(comingGamesConstants, params)
  }

  def getComingGame(id: Int): ToResponseMarshallable = {
    val comingGame = DBUtils.getTableDataByPrimaryKey(comingGamesConstants, id)
    if (null == comingGame) {
      StatusCodes.NotFound
    } else {
      comingGame
    }
  }

  def deleteComingGame(id: Int, userId: Int): ToResponseMarshallable = {
    if (DBUtils.checkAdmin(userId)) {
      DBUtils.deleteDataFromTable(comingGamesConstants.tableName, id)
      StatusCodes.OK
    } else {
      StatusCodes.Forbidden
    }
  }

  def updateComingGame(id: Int, updateComingGame: UpdateComingGame, userId: Int): ToResponseMarshallable = {
    if (DBUtils.checkAdmin(userId)) {
      DBUtils.updateDataInTable(id, updateComingGame, comingGamesConstants)
      StatusCodes.OK
    } else {
      StatusCodes.Forbidden
    }
  }

}
