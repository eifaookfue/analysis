package jp.co.nri.nefs.tool.analytics.store.common

import java.nio.file.{Files, Path, Paths}

import com.google.inject.ImplementedBy
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import jp.co.nri.nefs.tool.analytics.model.common.{User, UserComponent}
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

@ImplementedBy(classOf[DefaultUserStore])
trait UserStore {
  def recreateTable(): Unit
  def loadFile(file: Path): Seq[User]
  def upload(userData: Seq[User]): Unit
}

class DefaultUserStore @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends UserStore with UserComponent with HasDatabaseConfigProvider[JdbcProfile] with LazyLogging {

  import profile.api._

  val users = TableQuery[Users]

  def recreateTable(): Unit = {
    val schema = users.schema
    val drop = schema.dropIfExists
    val f1 = db.run(drop)
    Await.ready(f1, Duration.Inf)
    f1.value.get match {
      case Success(_) => logger.info("drop succeeded.")
      case Failure(e) => logger.error("drop failed.", e)
    }
    val create = schema.createIfNotExists
    val f2 = db.run(create)
    Await.ready(f2, Duration.Inf)
  }

  def loadFile(file: Path): Seq[User] = {
    val lines = Files.readAllLines(file).asScala
    for {
      line <- lines
      (userId, userName) = line.split(",") match {
        case Array(t1, t2) => (t1, t2)
        case _ => throw new RuntimeException("Invalid format.")
      }
      user = User(userId, userName)
    } yield user
  }

  def upload(userData: Seq[User]): Unit = {
    val action = users.map(u => (u.userId, u.userName)) ++= userData.map(u => (u.userId, u.userName))
    val f = db.run(action)
    Await.ready(f, Duration.Inf)
  }

}

object ConfigKey {
  final val STATIC_DATA_DIR = "staticDataDir"
}

object UserStoreExecutor {
  final val USER_DATA = "user.csv"

  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val file = Paths.get(config.getString(ConfigKey.STATIC_DATA_DIR)).resolve(USER_DATA)
    ServiceInjector.initialize()
    val userStore = ServiceInjector.getComponent(classOf[UserStore])
    userStore.recreateTable()
    val users = userStore.loadFile(file)
    userStore.upload(users)
  }
}