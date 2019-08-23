package im.pes.db

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import im.pes.constants.Tables
import im.pes.main.spark
import im.pes.utils.{BaseTable, DBUtils}

case class User(id: Int, email: String, name: String, age: Int)

case class PartialUser(email: String, name: String, age: Int)

case class UpdateUser(email: Option[String], name: Option[String], age: Option[Int]) extends BaseTable

object Users {

  private val usersConstants = Tables.Users

  def addUser(partialUser: PartialUser): ToResponseMarshallable = {
    addUser(partialUser.email, partialUser.name, partialUser.age)
  }

  private def addUser(email: String, name: String, age: Int): ToResponseMarshallable = {
    val data = spark.createDataFrame(Seq((DBUtils.getTable(usersConstants).count() + 1, email, age, name)))
      .toDF(usersConstants.id, usersConstants.email, usersConstants.age, usersConstants.name)
    DBUtils.addDataToTable(usersConstants.tableName, data)
    StatusCodes.OK
  }

  def getUsers(params: Map[String, String]): ToResponseMarshallable = {
    DBUtils.getTableData(usersConstants, params)
  }

  def getUser(id: Int): ToResponseMarshallable = {
    val user = DBUtils.getTableDataByPrimaryKey(usersConstants, id)
    if (null == user) {
      StatusCodes.NotFound
    } else {
      user
    }
  }

  def deleteUser(id: Int, userId: Int): ToResponseMarshallable = {
    if (checkUser(id, userId)) {
      DBUtils.deleteDataFromTable(usersConstants.tableName, id)
      StatusCodes.NoContent
    } else {
      StatusCodes.Forbidden
    }
  }

  def updateUser(id: Int, updateUser: UpdateUser, userId: Int): ToResponseMarshallable = {
    if (checkUser(id, userId)) {
      DBUtils.updateDataInTable(id, updateUser, usersConstants)
      StatusCodes.NoContent
    } else {
      StatusCodes.Forbidden
    }
  }

  private def checkUser(id: Int, userId: Int): Boolean = id == userId

}
