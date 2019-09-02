package im.pes.api

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.pes.constants.{CommonConstants, Paths}
import im.pes.db.Sessions
import im.pes.utils.DBUtils

object LogoutAPI {

  def getRoute: Route =
    path(Paths.logout) {
      post {
        headerValueByName(CommonConstants.token) { token =>
          complete(logout(token))
        }
      }
    }

  def logout(token: String): ToResponseMarshallable = {
    if (-1 == DBUtils.getIdByToken(token)) {
      return StatusCodes.Unauthorized
    }
    Sessions.deleteSession(token)
    StatusCodes.NoContent
  }

}