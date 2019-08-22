package im.pes.db

import im.pes.constants.Tables
import im.pes.main.spark
import im.pes.utils.{BaseTable, DBUtils}

case class User(id: Int, email: String, name: String, age: Int)

case class PartialUser(email: String, name: String, age: Int)

case class UpdateUser(email: Option[String], name: Option[String], age: Option[Int]) extends BaseTable

object Users {

  private val usersConstants = Tables.Users

  def addUser(partialUser: PartialUser): Unit = {
    addUser(partialUser.email, partialUser.name, partialUser.age)
  }

  def addUser(email: String, name: String, age: Int): Unit = {
    val data = spark.createDataFrame(Seq((DBUtils.getTable(usersConstants.tableName).count() + 1, email, age, name)))
      .toDF(usersConstants.id, usersConstants.email, usersConstants.age, usersConstants.name)
    DBUtils.addDataToTable(usersConstants.tableName, data)
  }

  def getUsers(params: Map[String, String]): String = {
    DBUtils.getTableData(usersConstants.tableName, params)
  }

  def getUser(id: Int): String = {
    DBUtils.getTableDataByPrimaryKey(usersConstants.tableName, id)
  }
  def deleteUser(id: Int): Unit = {
    DBUtils.deleteDataFromTable(usersConstants.tableName, id)
  }

  def updateUser(id: Int, updateUser: UpdateUser): Unit = {
    DBUtils.updateDataInTable(id, updateUser, usersConstants)
  }


}
