package im.pes.api

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.pes.constants.{CommonConstants, Paths}
import im.pes.db.Users
import im.pes.db.Users.{addUserSchema, updateUserSchema, updateUserWithRoleSchema}
import im.pes.utils.DBUtils

object UsersAPI {

  def getRoute: Route =
    path(Paths.users) {
      get {
        parameterMap { params =>
          complete(getUsers(params))
        }
      } ~
        post {
          entity(as[String]) { user =>
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
              entity(as[String]) { user =>
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

  def addUser(user: String): ToResponseMarshallable = {
    val userDf = DBUtils.dataToDf(addUserSchema, user)
    if (userDf.first.anyNull) {
      StatusCodes.BadRequest
    } else {
      //TODO check fields values
      Users.addUser(userDf)
      StatusCodes.OK
    }
  }

  def updateUser(id: Int, updateUser: String, token: String): ToResponseMarshallable = {
    val userId = DBUtils.getIdByToken(token)
    if (DBUtils.isAdmin(userId)) {
      val updateUserDf = DBUtils.dataToDf(updateUserWithRoleSchema, updateUser)
      //TODO check fields values
      Users.updateUser(id, updateUserDf)
      StatusCodes.NoContent
    } else if (userId == id) {
      val updateUserDf = DBUtils.dataToDf(updateUserSchema, updateUser)
      //TODO check what user may update
      //TODO check fields values
      Users.updateUser(id, updateUserDf)
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