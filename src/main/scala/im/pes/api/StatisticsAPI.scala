package im.pes.api

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.pes.constants.Paths
import im.pes.db._


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
          complete(getTeamStatistics(id))
        }
      } ~
      path(Paths.playersStatistics / IntNumber) { id =>
        get {
          complete(getPlayerStatistics(id))
        }
      }

  def getTeamsStatistics(params: Map[String, String]): ToResponseMarshallable = {
    Statistics.getTeamsStatistics(params)
  }

  def getTeamStatistics(id: Int): ToResponseMarshallable = {
    val teamStatistic = Statistics.getTeamStatistics(id)
    if (null == teamStatistic) {
      StatusCodes.NotFound
    } else {
      teamStatistic
    }
  }

  def getPlayersStatistics(params: Map[String, String]): ToResponseMarshallable = {
    Statistics.getPlayersStatistics(params)
  }

  def getPlayerStatistics(id: Int): ToResponseMarshallable = {
    val playerStatistic = Statistics.getPlayerStatistics(id)
    if (null == playerStatistic) {
      StatusCodes.NotFound
    } else {
      playerStatistic
    }
  }

}
