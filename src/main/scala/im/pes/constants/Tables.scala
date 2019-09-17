package im.pes.constants

object Tables {

  trait Table {
    val tableName: String
  }

  val primaryKey: String = "id"

  case object Users extends Table {
    override val tableName = "users"
    val id = "id"
    val email = "email"
    val password = "password"
    val age = "age"
    val name = "name"
    val budget = "budget"
    val role = "role"
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
    val isDefault = "isDefault"
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
    val isDefault = "isDefault"
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

  case object TeamsTransactions extends Table {
    override val tableName = "teams_transactions"
    val id = "id"
    val teamId = "teamId"
    val price = "price"
  }

  case object PlayersTransactions extends Table {
    override val tableName = "players_transactions"
    val id = "id"
    val playerId = "playerId"
    val price = "price"
  }


  case object TeamsTransactionsHistory extends Table {
    override val tableName = "teams_transactions_history"
    val id = "id"
    val teamId = "teamId"
    val sUserId = "sUserId"
    val bUserId = "bUserId"
    val price = "price"
    val date = "date"
  }

  case object PlayersTransactionsHistory extends Table {
    override val tableName = "players_transactions_history"
    val id = "id"
    val playerId = "playerId"
    val sTeamId = "sTeamId"
    val bTeamId = "bTeamId"
    val price = "price"
    val date = "date"
  }

  case object ActiveGames extends Table {
    override val tableName = "active_games"
    val id = "id"
    val firstTeamId = "firstTeamId"
    val secondTeamId = "secondTeamId"
    val championship = "championship"
    val championshipState = "championshipState"
  }

  case object ActiveGamesPlayersData extends Table {
    override val tableName = "active_games_players_data"
    val id = "id"
    val gameId = "gameId"
    val playerId = "playerId"
    val active = "active"
    val summary = "summary"
  }

  case object ActiveGamesReservePlayers extends Table {
    override val tableName = "active_games_reserve_players"
    val id = "id"
    val gameId = "gameId"
    val playerId = "playerId"
  }

  case object Activities extends Table {
    override val tableName = "active_games_players_activities"
    val id = "id"
    val gameId = "gameId"
    val playerId = "playerId"
    val activityType = "activityType"
    val angle = "angle"
    val x = "x"
    val y = "y"
    val firstAngle = "firstAngle"
    val secondAngle = "secondAngle"
    val power = "power"
    val timestamp = "timestamp"
  }

  case object Summary {
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

  case object AddActiveGameData {
    val firstTeamId = "firstTeamId"
    val secondTeamId = "secondTeamId"
    val firstTeamPlayers = "firstTeamPlayers"
    val secondTeamPlayers = "secondTeamPlayers"
    val championship = "championship"
    val championshipState = "championshipState"
    val playerId = "playerId"
    val playerState = "playerState"
  }

}
