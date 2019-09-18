package im.pes

import java.sql.{Connection, DriverManager, Statement}
import java.util.Properties

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import im.pes.api._
import im.pes.constants.CommonConstants
import org.apache.spark.sql.SparkSession

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration._
import scala.io.StdIn

object main {

  val connectionProperties = new Properties()
  val spark: SparkSession = SparkSession.builder().appName("Spark SQL").config("spark.master", "local").getOrCreate()
  lazy val conn: Connection = DriverManager.getConnection(CommonConstants.jdbcUrl, connectionProperties)
  lazy val stmt: Statement = conn.createStatement()

  def main(args: Array[String]) {
    connectionProperties.setProperty("Driver", CommonConstants.driverClass)
    connectionProperties.put("user", args(0))
    connectionProperties.put("password", args(1))
    implicit val system: ActorSystem = ActorSystem()
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher
    implicit val timeout: Timeout = Timeout(20.seconds)
    val requestHandler = system.actorOf(RequestHandler.props(), "requesthandler")

    val route = UsersAPI.getRoute ~ TeamsAPI.getRoute ~ DoneGamesAPI.getRoute ~ ComingGamesAPI.getRoute ~
      PlayersAPI.getRoute ~ LoginAPI.getRoute ~ LogoutAPI.getRoute ~ StatisticsAPI.getRoute ~ TransactionsAPI.getRoute ~
      TransactionsHistoryAPI.getRoute ~ ActiveGamesAPI.getRoute ~ ActiveGameAPI.getRoute ~ LobbiesAPI.getRoute
    val bindingFuture = Http().bindAndHandle(route, CommonConstants.routeHost, CommonConstants.routePort)
    println(s"\nServer running on ${CommonConstants.routeHost}:${CommonConstants.routePort}\nhit RETURN to terminate")
    StdIn.readLine()

    bindingFuture.flatMap(_.unbind())
    spark.stop()
    conn.close()
    system.terminate()
  }

}
