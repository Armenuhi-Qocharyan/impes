package im.pes.db

import com.github.dwickern.macros.NameOf.nameOf
import im.pes.constants.Tables
import im.pes.main.spark.implicits._
import im.pes.utils.DBUtils
import org.apache.spark.sql.types.{DataTypes, StructType}
import org.apache.spark.sql.{DataFrame, functions}

object Lobbies {

  val lobbiesConstants: Tables.Lobbies.type = Tables.Lobbies
  val lobbiesTeamsConstants: Tables.LobbiesTeams.type = Tables.LobbiesTeams
  val addLobbySchema: StructType = (new StructType).add(nameOf(lobbiesConstants.owner), DataTypes.IntegerType)

  def getLobbies(params: Map[String, String]): String = {
    DBUtils.getTableDataAsString(lobbiesConstants, params)
  }

  def getLobby(id: Int): String = {
    DBUtils.getTableDataAsStringByPrimaryKey(lobbiesConstants, id)
  }

  def addLobby(df: DataFrame): Int = {
    val id = DBUtils.getTable(lobbiesConstants, rename = false).count + 1
    DBUtils.addDataToTable(lobbiesConstants.tableName,
      DBUtils.renameColumnsToDBFormat(df, lobbiesConstants).withColumn(lobbiesConstants.id, functions.lit(id))
        .withColumn(lobbiesConstants.gameId, functions.lit(0)))
    id.toInt
  }

  def addLobbyTeam(lobbyId: Int, teamId: Int): Unit = {
    val id = DBUtils.getTable(lobbiesTeamsConstants, rename = false).count + 1
    val data = Seq((id, lobbyId, teamId))
      .toDF(lobbiesTeamsConstants.id, lobbiesTeamsConstants.lobbyId, lobbiesTeamsConstants.teamId)
    DBUtils.addDataToTable(lobbiesTeamsConstants.tableName, data)
  }

  def updateLobby(id: Int, updateData: Map[String, Any]): Unit = {
    DBUtils.updateDataInTableByPrimaryKey(id, updateData, lobbiesConstants.tableName)
  }

  def deleteLobby(id: Int): Unit = {
    DBUtils.deleteDataFromTable(lobbiesConstants.tableName, id)
  }

  def deleteGameLobby(gameId: Int): Unit = {
    DBUtils.deleteDataFromTable(lobbiesConstants.tableName, lobbiesConstants.gameId, gameId)
  }

  def deleteLobbyTeam(teamId: Int): Unit = {
    DBUtils.deleteDataFromTable(lobbiesTeamsConstants.tableName, lobbiesTeamsConstants.teamId, teamId)
  }

  def checkLobby(id: Int, userId: Int): Boolean = {
    !DBUtils.getTableDfByPrimaryKey(lobbiesConstants, id).filter(s"${lobbiesConstants.owner} = $userId").isEmpty
  }

  def checkTeamInLobby(id: Int, teamId: Int): Boolean = {
    !DBUtils.getTable(lobbiesTeamsConstants, rename = false).filter(s"${lobbiesTeamsConstants.lobbyId} = $id")
      .filter(s"${lobbiesTeamsConstants.teamId} = $teamId").isEmpty
  }

  def isLobbyFull(id: Int): Boolean = {
    DBUtils.getTable(lobbiesTeamsConstants, rename = false).filter(s"${lobbiesTeamsConstants.lobbyId} = $id").count == 2
  }

  def isLobbyConfirmed(id: Int): Boolean = {
    !DBUtils.getTableDfByPrimaryKey(lobbiesConstants, id).filter(s"${lobbiesConstants.gameId} != 0").isEmpty
  }

  def getLobbyTeamsIds(id: Int): Seq[Int] = {
    DBUtils.getTable(lobbiesTeamsConstants, rename = false).filter(s"${lobbiesTeamsConstants.lobbyId} = $id")
      .select(lobbiesTeamsConstants.teamId).as[Int].collect()
  }

}
