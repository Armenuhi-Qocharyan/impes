package im.pes.constants

object CommonConstants {

  val kGameIntelligence = 1
  val kTeamPlayer = 1.2
  val kPhysique = 1.7

  val playerMinCost = 2000
  val playerMaxCost = 400000
  val playerMinSkill = 300
  val playerMaxSkill = 1000
  val playerMinAge = 18
  val playerMaxAge = 50

  private val jdbcHostname = "localhost"
  private val jdbcPort = 3306
  private val jdbcDatabase = "pes"
  val driverClass = "com.mysql.cj.jdbc.Driver"
  val jdbcUrl = s"jdbc:mysql://$jdbcHostname:$jdbcPort/$jdbcDatabase"

  val routeHost = "127.0.0.1"
  val routePort = 8080

  val sqlDeleteFormat = s"DELETE FROM %s WHERE ${Tables.primaryKey} = %d"
  val sqlDeleteTokenFormat = s"DELETE FROM %s WHERE ${Tables.Sessions.token} = '%s'"
  val sqlUpdateFormat = s"UPDATE %s SET %s WHERE ${Tables.primaryKey} = %d"

  val admins: Seq[Int] = Seq(1)
  val defaultTeams: Seq[Int] = Seq(1)
  val defaultPlayers: Seq[Int] = Seq(1)

  val token = "Token"

}
