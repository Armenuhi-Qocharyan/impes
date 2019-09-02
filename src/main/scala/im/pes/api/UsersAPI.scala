package im.pes.api

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.pes.Health
import im.pes.constants.{CommonConstants, Paths}
import im.pes.db.{PartialUser, UpdateUser, Users}
import im.pes.utils.DBUtils
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait UserJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val partialUserFormat: RootJsonFormat[PartialUser] = jsonFormat4(PartialUser)
  implicit val updateUserFormat: RootJsonFormat[UpdateUser] = jsonFormat5(UpdateUser)
}

object UsersAPI extends UserJsonSupport {

  def getRoute: Route =
    path(Paths.users) {
      get {
        parameterMap { params =>
          complete(getUsers(params))
        }
      } ~
        post {
          entity(as[PartialUser]) { user =>
            complete(addUser(user))
          }
        }
    } ~
      path(Paths.users / IntNumber) { id =>
        get {
          complete(getUser(id))
        } ~
          put {
            headerValueByName(CommonConstants.token) { token =>
              entity(as[UpdateUser]) { user =>
                complete(updateUser(id, user, token))
              }
            }
          } ~
          delete {
            headerValueByName(CommonConstants.token) { token =>
              complete(deleteUser(id, token))
            }
          }
      }

  def getUsers(params: Map[String, String]): ToResponseMarshallable = {
    Users.getUsers(params)
  }

  def getUser(id: Int): ToResponseMarshallable = {
    val user = Users.getUser(id)
    if (null == user) {
      StatusCodes.NotFound
    } else {
      user
    }
  }

  def addUser(partialUser: PartialUser): ToResponseMarshallable = {
    //TODO check fields values
    Users.addUser(partialUser)
    StatusCodes.OK
  }

  def updateUser(id: Int, updateUser: UpdateUser, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (DBUtils.isAdmin(userId)) {
      //TODO check fields values
      Users.updateUser(id, updateUser)
      return StatusCodes.NoContent
    }
    if (userId == id) {
      //TODO check what user may update
      //TODO check fields values
      Users.updateUser(id, updateUser)
      StatusCodes.NoContent
    } else {
      StatusCodes.Forbidden
    }
  }

  def deleteUser(id: Int, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (DBUtils.isAdmin(userId) || userId == id) {
      Users.deleteUser(id)
      StatusCodes.NoContent
    } else {
      StatusCodes.Forbidden
    }
  }

}