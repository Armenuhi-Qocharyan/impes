package im.pes.api

import java.util.UUID.randomUUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import im.pes.Health
import im.pes.constants.{Paths, Tables}
import im.pes.db.{Login, Sessions, User}
import im.pes.utils.DBUtils
import org.mindrot.jbcrypt.BCrypt
import spray.json._

trait LoginJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val loginFormat: RootJsonFormat[Login] = jsonFormat2(Login)
  implicit val userFormat: RootJsonFormat[User] = jsonFormat6(User)
}

object LoginAPI extends LoginJsonSupport {

  def getRoute: Route =
    path(Paths.login) {
      post {
        entity(as[Login]) { user =>
          complete(login(user))
        }
      }
    }

  def login(login: Login): ToResponseMarshallable = {
    val users = DBUtils.getTable(Tables.Users).filter(s"${Tables.Users.email} = '${login.email}'").toJSON.collect()
    if (users.length == 0) {
      return StatusCodes.NotFound
    }
    val user = users(0).parseJson.convertTo[User]
    if (BCrypt.checkpw(login.password, user.password)) {
      val token = randomUUID.toString
      Sessions.addSession(user.id, token)
      token
    } else {
      StatusCodes.Forbidden
    }
  }

}