package im.pes.constants

object Tables {

  case object Users {
    val tableName = "users"
    val id = "id"
    val email = "email"
    val age = "age"
    val name = "name"
  }

  case object Teams {
    val tableName = "teams"
    val id = "id"
    val name = "name"
    val budget = "budget"
    val championship = "championship"
    val championsLeague = "champions_league"
    val isUsed = "is_used"
    val owner = "owner"
  }

  case object Players {
    val tableName = "players"
    val id = "id"
    val name = "name"
    val team = "team"
    val scoredGoals = "scored_goals"
    val championsLeague = "champions_league"
    val assists = "assists"
    val position = "position"
    val cost = "cost"
    val age = "age"
    val height = "height"
    val weight = "weight"
    val skills = "skills"
    val gameIntelligence = "game_intelligence"
    val teamPlayer = "team_player"
    val physique = "physique"
    val redCardsCount = "red_cards_count"
    val yellowCardsCount = "yellow_cards_count"
  }

  case object DoneGames {
    val tableName = "done_games"
    val id = "id"
    val teamOne = "team_one"
    val teamTwo = "team_two"
    val championship = "championship"
    val championship_state = "championship_state"
    val date = "date"
  }

  case object ComingGames {
    val tableName = "comming_games"
    val id = "id"
    val teamOne = "team_one"
    val teamTwo = "team_two"
    val championship = "championship"
    val championship_state = "championship_state"
    val date = "date"
  }

}
