package im.pes.api

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.pes.constants.Paths
import im.pes.db.Statistics


object StatisticsAPI {

  def getRoute: Route =
    path(Paths.teamsStatistics) {
      get {
        parameterMap { params =>
          complete(getTeamsStatistics(params))
        }
      }
    } ~
      path(Paths.playersStatistics) {
        get {
          parameterMap { params =>
            complete(getPlayersStatistics(params))
          }
        }
      } ~
      path(Paths.teamsStatistics / IntNumber) { id =>
        get {
          rejectEmptyResponse {
            complete(getTeamStatistics(id))
          }
        }
      } ~
      path(Paths.playersStatistics / IntNumber) { id =>
        get {
          rejectEmptyResponse {
            complete(getPlayerStatistics(id))
          }
        }
      }

  def getTeamsStatistics(params: Map[String, String]): ToResponseMarshallable = {
    Statistics.getTeamsStatistics(params)
  }

  def getTeamStatistics(id: Int): ToResponseMarshallable = {
    Statistics.getTeamStatistics(id)
  }

  def getPlayersStatistics(params: Map[String, String]): ToResponseMarshallable = {
    Statistics.getPlayersStatistics(params)
  }

  def getPlayerStatistics(id: Int): ToResponseMarshallable = {
    Statistics.getPlayerStatistics(id)
  }

}
