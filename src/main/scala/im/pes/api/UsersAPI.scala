package im.pes.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.pes.Health
import im.pes.constants.Paths
import im.pes.db.{PartialUser, UpdateUser, Users}
import im.pes.utils.APIUtils
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait UserJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val partialUserFormat: RootJsonFormat[PartialUser] = jsonFormat3(PartialUser)
  implicit val updateUserFormat: RootJsonFormat[UpdateUser] = jsonFormat3(UpdateUser)
}

object UsersAPI extends UserJsonSupport {

  def getRoute: Route =
    get {
      path(Paths.users) {
        parameterMap { params =>
          complete(Users.getUsers(params))
        }
      } ~
        path(Paths.users / IntNumber) { id =>
          complete(Users.getUser(id))
        }
    } ~
      post {
        path(Paths.users) {
          entity(as[PartialUser]) { user =>
            complete(Users.addUser(user))
          }
        }
      } ~
      headerValueByName("Token") { token =>
        val userId = APIUtils.validateToken(token)
          delete {
            path(Paths.users / IntNumber) { id =>
              complete(Users.deleteUser(id, userId))
            }
          } ~
          put {
            path(Paths.users / IntNumber) { id =>
              entity(as[UpdateUser]) { user =>
                complete(Users.updateUser(id, user, userId))
              }
            }
          }
      }
}