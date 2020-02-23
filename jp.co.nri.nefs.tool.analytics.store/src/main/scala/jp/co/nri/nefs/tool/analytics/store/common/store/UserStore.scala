package jp.co.nri.nefs.tool.analytics.store.common.store

import java.nio.file.{Files, Paths}
import com.google.inject.{AbstractModule, Guice, ImplementedBy, Injector}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import jp.co.nri.nefs.tool.analytics.store.client.model.UserComponent
import jp.co.nri.nefs.tool.analytics.store.client.store.DefaultDatabaseConfigProvider
import jp.co.nri.nefs.tool.analytics.store.common.model.User
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration.Duration

@ImplementedBy(classOf[DefaultUserStore])
trait UserStore {
  def recreate(): Unit
  def load(userData: Seq[User]): Unit
}

class DefaultUserStore @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends UserStore with UserComponent with HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  val users = TableQuery[Users]

  def recreate(): Unit = {
    val schema = users.schema
    val setup = DBIO.seq(
      schema.dropIfExists,
      schema.createIfNotExists
    )
    val setupFuture = db.run(setup)
    Await.ready(setupFuture, Duration.Inf)
  }

  def load(userData: Seq[User]): Unit = {
    val action = users ++= userData
    val f = db.run(action)
    Await.ready(f, Duration.Inf)
  }

}

object ConfigKey {
  final val STATIC_DATA_DIR = "staticDataDir"
}

object UserStore extends LazyLogging{
  final val USER_DATA = "user.csv"

  val injector: Injector = Guice.createInjector(new AbstractModule {
    override def configure(): Unit = {
      bind(classOf[DatabaseConfigProvider]).to(classOf[DefaultDatabaseConfigProvider])
    }
  })


  def main(args: Array[String]): Unit = {
    val config = ConfigFactory.load()
    val userData = Paths.get(config.getString(ConfigKey.STATIC_DATA_DIR)).resolve(USER_DATA)
    val lines = Files.readAllLines(userData).asScala
    val users = for {
      line <- lines
      (userId, userName) = line.split(",") match {
        case Array(t1, t2) => (t1, t2)
        case _ => throw new RuntimeException("Invalid format.")
      }
      user = User(userId, userName)
    } yield user
    val userStore = injector.getInstance(classOf[UserStore])
    userStore.recreate()
    userStore.load(users)
  }
}