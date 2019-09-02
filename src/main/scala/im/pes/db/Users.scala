package im.pes.db

import im.pes.constants.Tables
import im.pes.main.spark
import im.pes.utils.{BaseTable, DBUtils}

case class User(id: Int, email: String, password: String, name: String, age: Int)

case class PartialUser(email: String, password: String, name: String, age: Int)

case class Login(email: String, password: String)

case class UpdateUser(email: Option[String], password: Option[String], name: Option[String],
                      age: Option[Int]) extends BaseTable

object Users {

  private val usersConstants = Tables.Users

  def getUsers(params: Map[String, String]): String = {
    DBUtils.getTableData(usersConstants, params, Seq(usersConstants.password))
  }

  def getUser(id: Int): String = {
    DBUtils.getTableDataByPrimaryKey(usersConstants, id, Seq(usersConstants.password))
  }

  def addUser(partialUser: PartialUser): Unit = {
    addUser(partialUser.email, partialUser.password, partialUser.name, partialUser.age)
  }


  private def addUser(email: String, password: String, name: String, age: Int): Unit = {
    //TODO generate token?
    val data = spark
      .createDataFrame(Seq((DBUtils.getTable(usersConstants).count() + 1, email, password, age, name)))
      .toDF(usersConstants.id, usersConstants.email, usersConstants.password, usersConstants.age, usersConstants.name)
    DBUtils.addDataToTable(usersConstants.tableName, data)
  }

  def updateUser(id: Int, updateUser: UpdateUser): Unit = {
    DBUtils.updateDataInTable(id, updateUser, usersConstants)
  }

  def deleteUser(id: Int): Unit = {
    DBUtils.deleteDataFromTable(usersConstants.tableName, id)
  }

}
