package im.pes.constants

object Tables {

  trait Table {
    val tableName: String = null
  }

  val primaryKey: String = "id"

  case object Users extends Table {
    override val tableName = "users"
    val id = "id"
    val email = "email"
    val password = "password"
    val age = "age"
    val name = "name"
  }

  case object Sessions extends Table {
    override val tableName = "sessions"
    val id = "id"
    val userId = "userId"
    val token = "token"
  }

  case object Teams extends Table {
    override val tableName = "teams"
    val id = "id"
    val name = "name"
    val budget = "budget"
    val championship = "championship"
    val championsLeague = "champions_league"
    val isUsed = "is_used"
    val owner = "owner"
  }

  case object Players extends Table {
    override val tableName = "players"
    val id = "id"
    val name = "name"
    val teamId = "team"
    val championsLeague = "champions_league"
    val position = "position"
    val cost = "cost"
    val age = "age"
    val height = "height"
    val weight = "weight"
    val skills = "skills"
    val gameIntelligence = "game_intelligence"
    val teamPlayer = "team_player"
    val physique = "physique"
  }

  case object DoneGames extends Table {
    override val tableName = "done_games"
    val id = "id"
    val firstTeamId = "team_one"
    val secondTeamId = "team_two"
    val firstTeamGoals = "firstTeamGoals"
    val secondTeamGoals = "secondTeamGoals"
    val championship = "championship"
    val championshipState = "championship_state"
    val date = "date"
  }

  case object ComingGames extends Table {
    override val tableName = "comming_games"
    val id = "id"
    val firstTeamId = "team_one"
    val secondTeamId = "team_two"
    val championship = "championship"
    val championshipState = "championship_state"
    val date = "date"
  }

  case object TeamsStatistics extends Table {
    override val tableName = "teams_statistics"
    val id = "id"
    val teamId = "teamId"
    val doneGameId = "doneGameId"
    val goals = "goals"
    val possession = "possession"
    val yellowCards = "yellowCards"
    val redCards = "redCards"
    val falls = "falls"
    val shots = "shots"
    val aerialsWon = "aerialsWon"
  }

  case object PlayersStatistics extends Table {
    override val tableName = "players_statistics"
    val id = "id"
    val playerId = "playerId"
    val teamId = "teamId"
    val doneGameId = "doneGameId"
    val goals = "goals"
    val donePasses = "donePasses"
    val smartPasses = "smartPasses"
    val passes = "passes"
    val doneShots = "doneShots"
    val shots = "shots"
    val doneTackles = "doneTackles"
    val tackles = "tackles"
    val dribblingCount = "dribblingCount"
    val hooks = "hooks"
    val ballLosses = "ballLosses"
    val aerialsWon = "aerialsWon"
    val assists = "assists"
    val falls = "falls"
    val mileage = "mileage"
    val yellowCards = "yellowCards"
    val redCard = "redCard"
  }

}
