package im.pes.db

import im.pes.constants.Tables
import im.pes.main.spark
import im.pes.utils.DBUtils


case class User(id: Int, email: String, name: String, age: Int)

case class PartialUser(email: String, name: String, age: Int)

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

  def getUsers: String = {
    DBUtils.getTableData(usersConstants.tableName)
  }

  def getUser(id: Int): String = {
    val users = DBUtils.getTable(usersConstants.tableName).filter(s"${usersConstants.id} = $id").toJSON.collect()
    users(0)
  }

}
