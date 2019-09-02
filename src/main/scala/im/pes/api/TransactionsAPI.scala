package im.pes.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.pes.Health
import im.pes.constants.{CommonConstants, Paths}
import im.pes.db._
import im.pes.utils.DBUtils
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait TransactionsJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val partialTeamTransactionFormat: RootJsonFormat[PartialTeamTransaction] = jsonFormat2(
    PartialTeamTransaction)
  implicit val partialPlayerTransactionFormat: RootJsonFormat[PartialPlayerTransaction] = jsonFormat2(
    PartialPlayerTransaction)
}

object TransactionsAPI extends TransactionsJsonSupport {

  def getRoute: Route =
    path(Paths.teamsTransactions) {
      get {
        parameterMap { params =>
          complete(getTeamsTransactions(params))
        }
      } ~
        post {
          headerValueByName(CommonConstants.token) { token =>
            entity(as[PartialTeamTransaction]) { teamTransaction =>
              complete(addTeamTransaction(teamTransaction, token))
            }
          }
        }
    } ~
      path(Paths.buyTeam / IntNumber) { teamTransactionId =>
        post {
          headerValueByName(CommonConstants.token) { token =>
            complete(buyTeam(teamTransactionId, token))
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
              entity(as[PartialPlayerTransaction]) { playerTransaction =>
                complete(addPlayerTransaction(playerTransaction, token))
              }
            }
          }
      } ~
      path(Paths.buyPlayer / IntNumber) { playerTransactionId =>
        post {
          headerValueByName(CommonConstants.token) { token =>
            complete(buyPlayer(playerTransactionId, token))
          }
        }
      }

  def getTeamsTransactions(params: Map[String, String]): ToResponseMarshallable = {
    Transactions.getTeamsTransactions(params)
  }

  def addTeamTransaction(partialTeamTransaction: PartialTeamTransaction, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    // TODO check if team is already selling
    val team = Teams.getTeamData(partialTeamTransaction.teamId)
    if (null != team && team.owner == userId) {
      val priceDiff: Float = (partialTeamTransaction.price - team.budget).toFloat / team.budget
      if (priceDiff > 0.1 || priceDiff < -0.1) {
        return StatusCodes.BadRequest
      }
      Transactions.addTeamTransaction(partialTeamTransaction)
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
    if (user.budget < teamTransaction.price) {
      return StatusCodes.BadRequest
    }
    val seller = Users.getUserData(Teams.getTeamData(teamTransaction.teamId).owner)
    Users.updateUser(seller.id, UpdateUser(None, None, None, None, Option(seller.budget + teamTransaction.price)))
    Users.updateUser(userId, UpdateUser(None, None, None, None, Option(user.budget - teamTransaction.price)))
    Teams.updateTeam(teamTransaction.teamId, UpdateTeam(None, None, None, Option(userId)))
    //TODO add transaction history
    Transactions.deleteTeamTransaction(teamTransactionId)
    StatusCodes.NoContent
  }

  def getPlayersTransactions(params: Map[String, String]): ToResponseMarshallable = {
    Transactions.getPlayersTransactions(params)
  }

  def addPlayerTransaction(partialPlayerTransaction: PartialPlayerTransaction,
                           token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    // TODO check if player is already selling
    val player = Players.getPlayerData(partialPlayerTransaction.playerId)
    if (Players.checkPlayer(partialPlayerTransaction.playerId, userId)) {
      val priceDiff: Float = (partialPlayerTransaction.price - player.cost).toFloat / player.cost
      if (priceDiff > 0.1 || priceDiff < -0.1) {
        return StatusCodes.BadRequest
      }
      Transactions.addPlayerTransaction(partialPlayerTransaction)
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
    if (null == team || team.budget < playerTransaction.price) {
      return StatusCodes.BadRequest
    }
    val sellersTeam = Teams.getTeamData(Players.getPlayerData(playerTransaction.playerId).teamId)
    Teams.updateTeam(sellersTeam.id, UpdateTeam(None, Option(sellersTeam.budget + playerTransaction.price), None, None))
    Teams.updateTeam(team.id, UpdateTeam(None, Option(team.budget - playerTransaction.price), None, None))
    Players.updatePlayer(playerTransaction.playerId,
      UpdatePlayer(None, Option(team.id), None, None, None, None, None, None, None))
    //TODO add transaction history
    Transactions.deletePlayerTransaction(playerTransactionId)
    StatusCodes.NoContent
  }

}
