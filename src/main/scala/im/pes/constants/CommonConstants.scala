package im.pes.constants

case object ActivityTypes {
  val run = "run"
  val stay = "stay"
  val shot = "shot"
  val pass = "pass"
  val tackle = "tackle"
}

case object UserRoles {
  val user = "user"
  val admin = "admin"
}

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

  val sqlDeleteQuery: (String, String, Int) => String = (tableName: String, searchKey: String, searchValue: Int) =>
    s"DELETE FROM $tableName WHERE $searchKey = $searchValue"
  val sqlDeleteByPrimaryKeyQuery: (String, Int) => String = (tableName: String, searchValue: Int) =>
    sqlDeleteQuery(tableName, Tables.primaryKey, searchValue)
  val sqlDeleteTokenQuery: String => String = (searchValue: String) =>
    s"DELETE FROM ${Tables.Sessions.tableName} WHERE ${Tables.Sessions.token} = '$searchValue'"
  val sqlUpdateQuery: (String, String, String, Int) => String = (tableName: String, data: String, searchKey: String, searchValue: Int) =>
    s"UPDATE $tableName SET $data WHERE $searchKey = $searchValue"
  val sqlUpdateByPrimaryKeyQuery: (String, String, Int) => String = (tableName: String, data: String, searchValue: Int) =>
    sqlUpdateQuery(tableName, data, Tables.primaryKey, searchValue)
  val sqlUpdateAppendToJsonArrayQuery: (String, String, String, String, Int) => String =
    (tableName: String, key: String, data: String, searchKey: String, searchValue: Int) =>
      sqlUpdateQuery(tableName, s"$key = JSON_ARRAY_APPEND($key, '$$', CAST('$data' as JSON))", searchKey, searchValue)
  val sqlUpdateReplaceJsonQuery: (String, String, String, String, Int) => String =
    (tableName: String, key: String, replaceData: String, searchKey: String, searchValue: Int) =>
      sqlUpdateQuery(tableName, s" $key = JSON_REPLACE($key$replaceData)", searchKey, searchValue)
  val jsonExtractFormat: (String, String) => String = (data: String, key: String) => s"JSON_EXTRACT($data, '$$.$key')"

  val token = "Token"

  val defaultSummaryJson = s"""{\"${Tables.Summary.goals}\": 0, \"${Tables.Summary.donePasses}\": 0, \"${
      Tables.Summary.smartPasses
    }\": 0, \"${Tables.Summary.passes}\": 0, \"${Tables.Summary.doneShots}\": 0, \"${
      Tables.Summary.shots
    }\": 0, \"${Tables.Summary.doneTackles}\": 0, \"${
      Tables.Summary.tackles
    }\": 0, \"${Tables.Summary.dribblingCount}\": 0, \"${Tables.Summary.hooks}\": 0, \"${Tables.Summary.ballLosses}\": 0, \"${
      Tables.Summary.aerialsWon
    }\": 0, \"${
      Tables.Summary.assists
    }\": 0, \"${Tables.Summary.falls}\": 0, \"${Tables.Summary.mileage}\": 0, \"${
      Tables.Summary.yellowCards
    }\": 0, \"${Tables.Summary.redCard}\": false}"""

}
