package im.pes

import java.util.Properties

import org.apache.spark.sql.{SaveMode, SparkSession}

object main {

  private val kGameIntelligence = 1
  private val kTeamPlayer = 1.2
  private val kPhysique = 1.7

  private val playerMinCost = 2000
  private val playerMaxCost = 400000
  private val playerMinSkill = 300
  private val playerMaxSkill = 1000
  private val playerMinAge = 18
  private val playerMaxAge = 50

  private val jdbcHostname = "localhost"
  private val jdbcPort = 3306
  private val jdbcDatabase = "pes"
  private val jdbcUrl = s"jdbc:mysql://$jdbcHostname:$jdbcPort/$jdbcDatabase"
  private val driverClass = "com.mysql.cj.jdbc.Driver"
  private val connectionProperties = new Properties()
  connectionProperties.setProperty("Driver", driverClass)
  private val spark = SparkSession.builder().appName("Spark SQL").config("spark.master", "local").getOrCreate()

  def main(args: Array[String]) {
    connectionProperties.put("user", args(0))
    connectionProperties.put("password", args(1))
    spark.stop()
  }

  def addUser(email: String, name: String, age: Int): Unit = {
    val usersCount = spark.read.jdbc(jdbcUrl, "users", connectionProperties).count()
    val data = spark.createDataFrame(Seq((usersCount + 1, email, age, name)))
      .toDF("id", "email", "age", "name")
    data.write.mode(SaveMode.Append).jdbc(jdbcUrl, "users", connectionProperties)
  }

  def addTeam(userId: Int, name: String, championship: String): Unit = {
    val teamsCount = spark.read.jdbc(jdbcUrl, "teams", connectionProperties).count()
    val data = spark.createDataFrame(Seq((teamsCount + 1, name, 11000, championship, false, true)))
      .toDF("id", "name", "budget", "championship", "champions_league", "is_used")
    data.write.mode(SaveMode.Append).jdbc(jdbcUrl, "teams", connectionProperties)
  }

  def addPlayer(teamId: Int, name: String, position: String, age: Int, height: Int, weight: Int,
                gameIntelligence: Int, teamPlayer: Int, physique: Int): Unit = {
    val playersCount = spark.read.jdbc(jdbcUrl, "players", connectionProperties).count()
    val skill = calculateSkill(gameIntelligence, teamPlayer, physique)
    val data = spark.createDataFrame(Seq((playersCount + 1, name, teamId, 0, 0, position,
      calculateCost(skill, age), age, height, weight, skill, gameIntelligence, teamPlayer, physique, 0, 0)))
      .toDF("id", "name", "team", "scored_goals", "assists", "position", "cost", "age", "height", "weight",
        "skills", "game_intelligence", "team_player", "physique", "red_cards_count", "yellow_cards_count")
    data.write.mode(SaveMode.Append).jdbc(jdbcUrl, "players", connectionProperties)
  }

  private def calculateSkill(game_intelligence: Int, team_player: Int, physique: Int): Int = {
    ((kGameIntelligence * game_intelligence + kTeamPlayer * team_player + kPhysique * physique) /
      (kGameIntelligence + kTeamPlayer + kPhysique)).toInt
  }

  private def calculateCost(skill: Int, age: Int): Int = {
    if (skill - playerMinSkill < 100) {
      playerMinCost + (playerMaxAge - age) * 50 + (skill - playerMinSkill) * 100
    } else if (playerMaxSkill - skill < 100) {
      playerMaxCost - (playerMaxAge - age) * 50 - (playerMaxSkill - skill) * 1000
    } else {
      skill * playerMaxCost / playerMaxSkill - age * 100
    }
  }

}
