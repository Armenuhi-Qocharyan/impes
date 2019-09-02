package im.pes.db

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import im.pes.Health
import im.pes.constants.{CommonConstants, Tables}
import im.pes.main.spark
import im.pes.utils.{BaseTable, DBUtils}
import spray.json._

case class User(id: Int, email: String, password: String, name: String, age: Int, budget: Int)

case class PartialUser(email: String, password: String, name: String, age: Int)

case class Login(email: String, password: String)

case class UpdateUser(email: Option[String], password: Option[String], name: Option[String],
                      age: Option[Int], budget: Option[Int]) extends BaseTable

trait UserJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val healthFormat: RootJsonFormat[Health] = jsonFormat2(Health)
  implicit val userFormat: RootJsonFormat[User] = jsonFormat6(User)
}

object Users extends UserJsonSupport {

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
    val data = spark
      .createDataFrame(Seq((DBUtils.getTable(usersConstants).count() + 1, email, password, age, name, 11 *
        CommonConstants.playerMinCost * 1.2)))
      .toDF(usersConstants.id, usersConstants.email, usersConstants.password, usersConstants.age, usersConstants.name,
        usersConstants.budget)
    DBUtils.addDataToTable(usersConstants.tableName, data)
  }

  def updateUser(id: Int, updateUser: UpdateUser): Unit = {
    DBUtils.updateDataInTable(id, updateUser, usersConstants)
  }

  def deleteUser(id: Int): Unit = {
    DBUtils.deleteDataFromTable(usersConstants.tableName, id)
  }

  def getUserData(id: Int): User = {
    val user = DBUtils.getTableDataByPrimaryKey(usersConstants, id)
    if (null == user) {
      null
    } else {
      user.parseJson.convertTo[User]
    }
  }

}
