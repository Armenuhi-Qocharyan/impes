package im.pes.api

import java.util.UUID.randomUUID

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.pes.constants.{Paths, Tables}
import im.pes.db.Sessions
import im.pes.db.Users.loginSchema
import im.pes.utils.DBUtils
import org.mindrot.jbcrypt.BCrypt


object LoginAPI {

  def getRoute: Route =
    path(Paths.login) {
      post {
        entity(as[String]) { loginData =>
          complete(login(loginData))
        }
      }
    }

  def login(login: String): ToResponseMarshallable = {
    val loginData = try {
      DBUtils.dataToDf(loginSchema, login).collect()(0)
    } catch {
      case _: NullPointerException => return StatusCodes.BadRequest
    }
    val userData = try {
      DBUtils.getTable(Tables.Users, rename = false)
        .filter(s"${Tables.Users.email} = '${loginData.getAs[String](Tables.Users.email)}'").collect()(0)
    }
    catch {
      case _: ArrayIndexOutOfBoundsException => return StatusCodes.NotFound
    }
    if (BCrypt.checkpw(loginData.getAs[String](Tables.Users.password), userData.getAs[String](Tables.Users.password))) {
      val token = randomUUID.toString
      Sessions.addSession(userData.getAs[Int](Tables.Users.id), token)
      token
    } else {
      StatusCodes.Forbidden
    }
  }

}