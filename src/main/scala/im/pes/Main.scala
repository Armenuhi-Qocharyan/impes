package im.pes

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import spray.json._

import scala.concurrent.duration._
import scala.io.StdIn

// Slick

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  System.out.print("start");
  implicit val healthFormat = jsonFormat2(Health)
  implicit val playerFormat = jsonFormat4(Player)
  implicit val partialPlayerFormat = jsonFormat3(PartialPlayer)
  implicit val playerLisFormat = jsonFormat1(PlayersList)
}

object Main extends App with JsonSupport {

  val host = "localhost"
  val port = 8080

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val timeout = Timeout(20.seconds)
  
  val requestHandler = system.actorOf(RequestHandler.props(), "requesthandler")
  
  val route: Route = {
    get {
      path("players") {
       onSuccess(PlayersDAO.allPlayers) {
         case players: Seq[Player] =>
           complete(PlayersList(players))
         case _ =>
           complete(StatusCodes.InternalServerError)
       }
      } ~ 
      path("players"/IntNumber) { id =>
        onSuccess(PlayersDAO.singlePlayer(id)) {
          case Some(player) => complete(player)
          case None => complete("No such player!")
        }
      }
    } ~
    post {
      path("players") {
        entity(as[PartialPlayer]) { player =>
          onSuccess(PlayersDAO.addPlayer(player.name, player.age, player.username)) {
            case player: Player =>
              complete(player)
            case _ =>
              complete(StatusCodes.InternalServerError)
          }
        }
      }
    } ~
    delete {
      path("players"/IntNumber) { id =>
        onSuccess(PlayersDAO.deletePlayer(id)) {
          case 1 => complete(s"Deleted player with id $id")
          case 0 => complete(s"No such player with id $id")
        }
      }
    }
  }

  val bindingFuture = Http().bindAndHandle(route, host, port)
  println(s"\nServer running on $host:$port\nhit RETURN to terminate")
  StdIn.readLine()

  bindingFuture.flatMap(_.unbind())
  system.terminate()
}

