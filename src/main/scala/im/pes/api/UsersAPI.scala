package im.pes.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, entity, path, post, _}
import akka.http.scaladsl.server.Route
import im.pes.Health
import im.pes.constants.Paths
import im.pes.db.{PartialUser, Users}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait UserJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val partialUserFormat: RootJsonFormat[PartialUser] = jsonFormat3(PartialUser)
}

object UsersAPI extends UserJsonSupport {

  def getRoute: Route = {
    post {
      path(Paths.users) {
        entity(as[PartialUser]) { user =>
          Users.addUser(user)
          complete(StatusCodes.OK)
        }
      }
    }
  }

}
