package im.pes

import scala.concurrent.Future

// Slick
import slick.driver.SQLiteDriver.api._

case class Player(id: Int, name: String, age: Int, username: String)

case class PartialPlayer(name: String, age: Int, username: String)
case class PlayersList(players: Seq[Player])

case class Players(tag: Tag) extends Table[Player](tag, "players") {
  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def name = column[String]("NAME")
  def age = column[Int]("AGE")
  def username = column[String]("username")
  def * = (id, name, age, username) <> (Player.tupled, Player.unapply)
}

object PlayersDAO extends TableQuery(new Players(_)) {

  /* Alternatively create a `val players = TableQuery[Players]` instead
   * of using `this` */

  val db = Database.forURL("jdbc:sqlite:./impes.db")

  /* Query for all people in the db */
  def allPlayers: Future[Seq[Player]] = {
    println("------------1---------------")
    print(this.result)
    println("------------2---------------")
    db.run(this.result)
  }

  /* Query for a single player */
  def singlePlayer(id: Int): Future[Option[Player]] = {
    db.run(this.filter(_.id === id).result.headOption)
  }

  /* Adding new player to DB. We need to get the auto incrementing id back
   * with this fancy trick:
   * http://stackoverflow.com/questions/31443505/slick-3-0-insert-and-then-get-auto-increment-value/31448129#31448129
   */
  def addPlayer(name: String, age: Int, username: String): Future[Player] = {
    db.run(this returning this.map(_.id) into ((player, id) => player.copy(id = id)) += Player(0, name, age, username))
  }

  /* Delete player by id */
  def deletePlayer(id: Int) = {
    db.run(this.filter(_.id === id).delete)
  }
}

