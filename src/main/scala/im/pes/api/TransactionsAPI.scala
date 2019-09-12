package im.pes.api

import java.time.LocalDate

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.dwickern.macros.NameOf.nameOf
import im.pes.constants.{CommonConstants, Paths, Tables}
import im.pes.db.{Players, Teams, Transactions, TransactionsHistory, Users}
import im.pes.db.Transactions.{addPlayerTransactionSchema, addTeamTransactionSchema, playersTransactionsConstants, teamsTransactionsConstants}
import im.pes.utils.DBUtils

object TransactionsAPI {

  def getRoute: Route =
    path(Paths.teamsTransactions) {
      get {
        parameterMap { params =>
          complete(getTeamsTransactions(params))
        }
      } ~
        post {
          headerValueByName(CommonConstants.token) { token =>
            entity(as[String]) { teamTransaction =>
              complete(addTeamTransaction(teamTransaction, token))
            }
          }
        }
    } ~
      path(Paths.teamsTransactions / IntNumber) { teamTransactionId =>
        post {
          headerValueByName(CommonConstants.token) { token =>
            complete(buyTeam(teamTransactionId, token))
          }
        } ~
          delete {
            headerValueByName(CommonConstants.token) { token =>
              complete(deleteTeamTransaction(teamTransactionId, token))
            }
          }
      } ~
      path(Paths.playersTransactions) {
        get {
          parameterMap { params =>
            complete(getPlayersTransactions(params))
          }
        } ~
          post {
            headerValueByName(CommonConstants.token) { token =>
              entity(as[String]) { playerTransaction =>
                complete(addPlayerTransaction(playerTransaction, token))
              }
            }
          }
      } ~
      path(Paths.playersTransactions / IntNumber) { playerTransactionId =>
        post {
          headerValueByName(CommonConstants.token) { token =>
            complete(buyPlayer(playerTransactionId, token))
          }
        } ~
          delete {
            headerValueByName(CommonConstants.token) { token =>
              complete(deletePlayerTransaction(playerTransactionId, token))
            }
          }
      }

  def getTeamsTransactions(params: Map[String, String]): ToResponseMarshallable = {
    Transactions.getTeamsTransactions(params)
  }

  def addTeamTransaction(teamTransaction: String, token: String): ToResponseMarshallable = {
    val teamTransactionDf =
      try {
        DBUtils.dataToDf(addTeamTransactionSchema, teamTransaction)
      } catch {
        case _: NullPointerException => return StatusCodes.BadRequest
      }
    val teamTransactionData = teamTransactionDf.collect()(0)
    val teamId = teamTransactionData.getAs[Int](nameOf(teamsTransactionsConstants.teamId))
    if (Transactions.checkTeamTransaction(teamId)) {
      return StatusCodes.BadRequest
    }
    val userId = DBUtils.getIdByToken(token)
    val team = Teams.getTeamData(teamId)
    if (null != team && team.getAs[Int](Tables.Teams.owner) == userId) {
      val teamBudget = team.getAs[Int](Tables.Teams.budget)
      val priceDiff: Float =
        (teamTransactionData.getAs[Int](nameOf(teamsTransactionsConstants.price)) - teamBudget).toFloat / teamBudget
      if (priceDiff > 0.1 || priceDiff < -0.1) {
        return StatusCodes.BadRequest
      }
      Transactions.addTeamTransaction(teamTransactionDf)
      StatusCodes.OK
    } else {
      StatusCodes.Forbidden
    }
  }

  def buyTeam(teamTransactionId: Int, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    val user = Users.getUserData(userId)
    if (null == user) {
      return StatusCodes.Forbidden
    }
    val teamTransaction = Transactions.getTeamTransaction(teamTransactionId)
    val userBudget = user.getAs[Int](Tables.Users.budget)
    val teamPrice = teamTransaction.getAs[Int](Tables.TeamsTransactions.price)
    if (userBudget < teamPrice) {
      return StatusCodes.BadRequest
    }
    val teamId = teamTransaction.getAs[Int](Tables.TeamsTransactions.teamId)
    val sellerId = Teams.getTeamData(teamId).getAs[Int](Tables.Teams.owner)
    val seller = Users.getUserData(sellerId)
    Users.updateUser(sellerId, Map(Tables.Users.budget -> (seller.getAs[Int](Tables.Users.budget) + teamPrice)))
    Users.updateUser(userId, Map(Tables.Users.budget -> (userBudget - teamPrice)))
    Teams.updateTeam(teamId, Map(Tables.Teams.owner -> userId))
    TransactionsHistory.addTeamTransactionHistory(teamId, sellerId, userId, teamPrice, LocalDate.now().toString)
    Transactions.deleteTeamTransaction(teamTransactionId)
    StatusCodes.NoContent
  }

  def deleteTeamTransaction(id: Int, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    val teamTransaction = Transactions.getTeamTransaction(id)
    if (null == teamTransaction) {
      return StatusCodes.BadRequest
    }
    if (Teams.checkTeam(teamTransaction.getAs[Int](teamsTransactionsConstants.teamId), userId)) {
      Transactions.deleteTeamTransaction(id)
      StatusCodes.NoContent
    } else {
      StatusCodes.Forbidden
    }
  }

  def getPlayersTransactions(params: Map[String, String]): ToResponseMarshallable = {
    Transactions.getPlayersTransactions(params)
  }

  def addPlayerTransaction(playerTransaction: String, token: String): ToResponseMarshallable = {
    val playerTransactionDf =
      try {
        DBUtils.dataToDf(addPlayerTransactionSchema, playerTransaction)
      } catch {
        case _: NullPointerException => return StatusCodes.BadRequest
      }
    val playerTransactionRow = playerTransactionDf.collect()(0)
    val playerId = playerTransactionRow.getAs[Int](nameOf(playersTransactionsConstants.playerId))
    if (Transactions.checkPlayerTransaction(playerId)) {
      return StatusCodes.BadRequest
    }
    val userId = DBUtils.getIdByToken(token)
    val player = Players.getPlayerData(playerId)
    if (Players.checkPlayer(playerId, userId)) {
      val playerCost = player.getAs[Int](Tables.Players.cost)
      val priceDiff: Float =
        (playerTransactionRow.getAs[Int](nameOf(teamsTransactionsConstants.price)) - playerCost).toFloat / playerCost
      if (priceDiff > 0.1 || priceDiff < -0.1) {
        return StatusCodes.BadRequest
      }
      Transactions.addPlayerTransaction(playerTransactionDf)
      StatusCodes.OK
    } else {
      StatusCodes.Forbidden
    }
  }

  def buyPlayer(playerTransactionId: Int, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (-1 == userId) {
      return StatusCodes.Forbidden
    }
    val playerTransaction = Transactions.getPlayerTransaction(playerTransactionId)
    val team = Teams.getUserTeam(userId)
    if (null == team) {
      return StatusCodes.BadRequest
    }
    val teamBudget = team.getAs[Int](Tables.Teams.budget)
    val playerPrice = playerTransaction.getAs[Int](Tables.PlayersTransactions.price)
    if (teamBudget < playerPrice) {
      return StatusCodes.BadRequest
    }
    val playerId = playerTransaction.getAs[Int](Tables.PlayersTransactions.playerId)
    val sellersTeamId = Players.getPlayerData(playerId).getAs[Int](Tables.Players.teamId)
    val sellersTeam = Teams.getTeamData(sellersTeamId)
    val teamId = team.getAs[Int](Tables.Teams.id)
    Teams.updateTeam(sellersTeamId,
      Map(Tables.Teams.budget -> (sellersTeam.getAs[Int](Tables.Teams.budget) + playerPrice)))
    Teams.updateTeam(teamId, Map(Tables.Teams.budget -> (teamBudget - playerPrice)))
    Players.updatePlayer(playerId, Map(Tables.Players.teamId -> teamId))
    TransactionsHistory
      .addPlayerTransactionHistory(playerId, sellersTeamId, teamId, playerPrice, LocalDate.now().toString)
    Transactions.deletePlayerTransaction(playerTransactionId)
    StatusCodes.NoContent
  }

  def deletePlayerTransaction(id: Int, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    val playerTransaction = Transactions.getPlayerTransaction(id)
    if (null == playerTransaction) {
      return StatusCodes.BadRequest
    }
    if (Players.checkPlayer(playerTransaction.getAs[Int](playersTransactionsConstants.playerId), userId)) {
      Transactions.deletePlayerTransaction(id)
      StatusCodes.NoContent
    } else {
      StatusCodes.Forbidden
    }
  }

}
