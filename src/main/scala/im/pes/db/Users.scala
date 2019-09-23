package im.pes.db

import com.github.dwickern.macros.NameOf.nameOf
import im.pes.constants.{CommonConstants, Tables, UserRoles}
import im.pes.utils.DBUtils
import org.apache.spark.sql.types.{DataTypes, StructType}
import org.apache.spark.sql.{DataFrame, Row, functions}
import org.mindrot.jbcrypt.BCrypt

object Users {

  val usersConstants: Tables.Users.type = Tables.Users
  val userRoles: UserRoles.type = UserRoles
  val addUserSchema: StructType = (new StructType)
    .add(nameOf(usersConstants.email), DataTypes.StringType)
    .add(nameOf(usersConstants.password), DataTypes.StringType)
    .add(nameOf(usersConstants.age), DataTypes.IntegerType)
    .add(nameOf(usersConstants.name), DataTypes.StringType)
  val loginSchema: StructType = (new StructType)
    .add(nameOf(usersConstants.email), DataTypes.StringType)
    .add(nameOf(usersConstants.password), DataTypes.StringType)
  val updateUserSchema: StructType = (new StructType)
    .add(nameOf(usersConstants.email), DataTypes.StringType)
    .add(nameOf(usersConstants.password), DataTypes.StringType)
    .add(nameOf(usersConstants.age), DataTypes.IntegerType)
    .add(nameOf(usersConstants.name), DataTypes.StringType)
    .add(nameOf(usersConstants.budget), DataTypes.IntegerType)
  val updateUserWithRoleSchema: StructType = updateUserSchema
    .add(nameOf(usersConstants.role), DataTypes.StringType)

  def getUsers(params: Map[String, String]): String = {
    DBUtils.getTableDataAsString(usersConstants, params, Seq(usersConstants.password))
  }

  def getUser(id: Int): Option[String] = {
    DBUtils.getTableDataAsStringByPrimaryKey(usersConstants, id, Seq(usersConstants.password))
  }

  def addUser(df: DataFrame): Unit = {
    val encrypt = functions.udf( (x: String) => BCrypt.hashpw(x, BCrypt.gensalt) )
    val id = DBUtils.getTable(usersConstants, rename = false).count + 1
    DBUtils.addDataToTable(usersConstants.tableName,
      DBUtils.renameColumnsToDBFormat(df, usersConstants).withColumn(usersConstants.id, functions.lit(id))
        .withColumn(usersConstants.password, encrypt(functions.col(usersConstants.password)))
        .withColumn(usersConstants.budget, functions.lit(14 * CommonConstants.playerMinCost))
        .withColumn(usersConstants.role, functions.lit(userRoles.user)))
  }

  def updateUser(id: Int, updateDf: DataFrame): Unit = {
    val df = DBUtils.renameColumnsToDBFormat(updateDf, usersConstants)
    updateUser(id, df.first.getValuesMap(df.columns))
  }

  def updateUser(id: Int, updateData: Map[String, Any]): Unit = {
    DBUtils.updateDataInTableByPrimaryKey(id, updateData, usersConstants.tableName)
  }

  def deleteUser(id: Int): Unit = {
    DBUtils.deleteDataFromTable(usersConstants.tableName, id)
  }

  def getUserData(id: Int): Option[Row] = {
    DBUtils.getTableDataByPrimaryKey(usersConstants, id)
  }

  def getAdminId: Int = {
    DBUtils.getTable(Tables.Users, rename = false).filter(s"${usersConstants.role} = ${UserRoles.admin}")
      .select(usersConstants.id).first.getInt(0)
  }

}
