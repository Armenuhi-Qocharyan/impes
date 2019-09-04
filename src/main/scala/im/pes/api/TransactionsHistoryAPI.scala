package im.pes.api

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.pes.constants.Paths
import im.pes.db.TransactionsHistory

object TransactionsHistoryAPI {

  def getRoute: Route =
    path(Paths.teamsTransactions) {
      get {
        parameterMap { params =>
          complete(getTeamsTransactionsHistory(params))
        }
      }
    } ~
      path(Paths.playersTransactions) {
        get {
          parameterMap { params =>
            complete(getPlayersTransactionsHistory(params))
          }
        }
      }

  def getTeamsTransactionsHistory(params: Map[String, String]): ToResponseMarshallable = {
    TransactionsHistory.getTeamsTransactionsHistory(params)
  }

  def getPlayersTransactionsHistory(params: Map[String, String]): ToResponseMarshallable = {
    TransactionsHistory.getPlayersTransactionsHistory(params)
  }

}
