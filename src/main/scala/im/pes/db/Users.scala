package im.pes.db

import im.pes.constants.{CommonConstants, Tables}
import im.pes.main.{connectionProperties, spark}
import org.apache.spark.sql.SaveMode


case class User(id: Int, email: String, name: String, age: Int)

case class PartialUser(email: String, name: String, age: Int)

object Users {

  private val usersConstants = Tables.Users

  def addUser(partialUser: PartialUser): Unit = {
    addUser(partialUser.email, partialUser.name, partialUser.age)
  }

  def addUser(email: String, name: String, age: Int): Unit = {
    val userId = spark.read.jdbc(CommonConstants.jdbcUrl, usersConstants.tableName, connectionProperties).count() + 1
    val data = spark.createDataFrame(Seq((userId, email, age, name)))
      .toDF(usersConstants.id, usersConstants.email, usersConstants.age, usersConstants.name)
    data.write.mode(SaveMode.Append).jdbc(CommonConstants.jdbcUrl, usersConstants.tableName, connectionProperties)
  }

}
