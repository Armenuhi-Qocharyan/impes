package im.pes.api

import java.time.LocalDate

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.github.dwickern.macros.NameOf.nameOf
import im.pes.constants.{CommonConstants, Paths, Tables}
import im.pes.db.Transactions.{addPlayerTransactionSchema, addTeamTransactionSchema, playersTransactionsConstants, teamsTransactionsConstants}
import im.pes.db._
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
    val userId = DBUtils.getIdByToken(token).getOrElse(return StatusCodes.Unauthorized)
    val teamTransactionDf = DBUtils.dataToDf(addTeamTransactionSchema, teamTransaction)
    val teamTransactionData = teamTransactionDf.first
    if (teamTransactionData.anyNull) {
      return StatusCodes.BadRequest
    }
    val teamId = teamTransactionData.getAs[Int](nameOf(teamsTransactionsConstants.teamId))
    if (Transactions.checkTeamTransaction(teamId)) {
      return StatusCodes.BadRequest
    }
    val team = Teams.getTeamData(teamId)
    if (team.isDefined && team.get.getAs[Int](Tables.Teams.owner) == userId) {
      val teamPLayers = Players.getTeamPlayers(teamId)
      val playersCost = teamPLayers.foldLeft[Int](0)((playersCost, teamPlayer) => {
        if (Transactions.checkPlayerTransaction(teamPlayer.getInt(0))) {
          return StatusCodes.BadRequest
        }
        playersCost + teamPlayer.getInt(1)
      })
      val teamBudget = team.get.getAs[Int](Tables.Teams.budget) + playersCost
      val priceDiff: Float =
        (teamTransactionData.getAs[Int](nameOf(teamsTransactionsConstants.price)) - teamBudget).toFloat / teamBudget
      if (priceDiff > 0.1 || priceDiff < -0.1) {
        StatusCodes.BadRequest
      } else {
        Transactions.addTeamTransaction(teamTransactionDf)
        StatusCodes.OK
      }
    } else {
      StatusCodes.Forbidden
    }
  }

  def buyTeam(teamTransactionId: Int, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token).getOrElse(return StatusCodes.Unauthorized)
    val user = Users.getUserData(userId).getOrElse(return StatusCodes.Forbidden)
    if (Teams.getUserTeamId(userId).isDefined) {
      return StatusCodes.BadRequest
    }
    val teamTransaction = Transactions.getTeamTransaction(teamTransactionId).getOrElse(return StatusCodes.NotFound)
    val teamId = teamTransaction.getAs[Int](Tables.TeamsTransactions.teamId)
    if (ActiveGames.isTeamInGame(teamId) || Lobbies.isTeamInLobby(teamId)) {
      return StatusCodes.Conflict
    }
    val userBudget = user.getAs[Int](Tables.Users.budget)
    val teamPrice = teamTransaction.getAs[Int](Tables.TeamsTransactions.price)
    if (userBudget < teamPrice) {
      StatusCodes.BadRequest
    } else {
      val sellerId = Teams.getTeamData(teamId).get.getAs[Int](Tables.Teams.owner)
      if (sellerId == userId) {
        StatusCodes.Conflict
      } else {
        val seller = Users.getUserData(sellerId).get
        Users.updateUser(sellerId, Map(Tables.Users.budget -> (seller.getAs[Int](Tables.Users.budget) + teamPrice)))
        Users.updateUser(userId, Map(Tables.Users.budget -> (userBudget - teamPrice)))
        Teams.updateTeam(teamId, Map(Tables.Teams.owner -> userId))
        TransactionsHistory.addTeamTransactionHistory(teamId, sellerId, userId, teamPrice, LocalDate.now().toString)
        Transactions.deleteTeamTransaction(teamTransactionId)
        StatusCodes.NoContent
      }
    }
  }

  def deleteTeamTransaction(id: Int, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (userId.isEmpty) {
      StatusCodes.Unauthorized
    } else {
      val teamTransaction = Transactions.getTeamTransaction(id)
      if (teamTransaction.isEmpty) {
        StatusCodes.BadRequest
      } else if (Teams.checkTeam(teamTransaction.get.getAs[Int](teamsTransactionsConstants.teamId), userId.get)) {
        Transactions.deleteTeamTransaction(id)
        StatusCodes.NoContent
      } else {
        StatusCodes.Forbidden
      }
    }
  }

  def getPlayersTransactions(params: Map[String, String]): ToResponseMarshallable = {
    Transactions.getPlayersTransactions(params)
  }

  def addPlayerTransaction(playerTransaction: String, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token).getOrElse(return StatusCodes.Unauthorized)
    val playerTransactionDf = DBUtils.dataToDf(addPlayerTransactionSchema, playerTransaction)
    val playerTransactionData = playerTransactionDf.first
    if (playerTransactionData.anyNull) {
      return StatusCodes.BadRequest
    }
    val playerId = playerTransactionData.getAs[Int](nameOf(playersTransactionsConstants.playerId))
    if (Transactions.checkPlayerTransaction(playerId)) {
      return StatusCodes.BadRequest
    }
    if (Players.checkPlayer(playerId, userId)) {
      val player = Players.getPlayerData(playerId).get
      val teamId = player.getAs[Int](Tables.Players.teamId)
      if (Transactions.checkTeamTransaction(teamId)) {
        StatusCodes.BadRequest
      } else {
        val playerCost = player.getAs[Int](Tables.Players.cost)
        val priceDiff: Float =
          (playerTransactionData.getAs[Int](nameOf(teamsTransactionsConstants.price)) - playerCost).toFloat / playerCost
        if (priceDiff > 0.1 || priceDiff < -0.1) {
          StatusCodes.BadRequest
        } else {
          Transactions.addPlayerTransaction(playerTransactionDf)
          StatusCodes.OK
        }
      }
    } else {
      StatusCodes.Forbidden
    }
  }

  def buyPlayer(playerTransactionId: Int, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token).getOrElse(return StatusCodes.Unauthorized)
    val playerTransaction = Transactions.getPlayerTransaction(playerTransactionId).getOrElse(return StatusCodes.BadRequest)
    val playerId = playerTransaction.getAs[Int](Tables.PlayersTransactions.playerId)
    if (ActiveGames.isPlayerInGame(playerId)) {
      return StatusCodes.Conflict
    }
    val team = Teams.getUserTeam(userId).getOrElse(return StatusCodes.BadRequest)
    val teamBudget = team.getAs[Int](Tables.Teams.budget)
    val playerPrice = playerTransaction.getAs[Int](Tables.PlayersTransactions.price)
    if (teamBudget < playerPrice) {
      StatusCodes.BadRequest
    } else {
      val playerId = playerTransaction.getAs[Int](Tables.PlayersTransactions.playerId)
      val sellersTeamId = Players.getPlayerData(playerId).get.getAs[Int](Tables.Players.teamId)
      val sellersTeam = Teams.getTeamData(sellersTeamId).get
      val teamId = team.getAs[Int](Tables.Teams.id)
      if (teamId == sellersTeamId) {
        StatusCodes.BadRequest
      } else {
        Teams.updateTeam(sellersTeamId,
          Map(Tables.Teams.budget -> (sellersTeam.getAs[Int](Tables.Teams.budget) + playerPrice)))
        Teams.updateTeam(teamId, Map(Tables.Teams.budget -> (teamBudget - playerPrice)))
        Players.updatePlayer(playerId, Map(Tables.Players.teamId -> teamId))
        TransactionsHistory
          .addPlayerTransactionHistory(playerId, sellersTeamId, teamId, playerPrice, LocalDate.now().toString)
        Transactions.deletePlayerTransaction(playerTransactionId)
        StatusCodes.NoContent
      }
    }
  }

  def deletePlayerTransaction(id: Int, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (userId.isEmpty) {
      StatusCodes.Unauthorized
    } else {
      val playerTransaction = Transactions.getPlayerTransaction(id)
      if (playerTransaction.isEmpty) {
        StatusCodes.BadRequest
      } else if (Players
        .checkPlayer(playerTransaction.get.getAs[Int](playersTransactionsConstants.playerId), userId.get)) {
        Transactions.deletePlayerTransaction(id)
        StatusCodes.NoContent
      } else {
        StatusCodes.Forbidden
      }
    }
  }

}
