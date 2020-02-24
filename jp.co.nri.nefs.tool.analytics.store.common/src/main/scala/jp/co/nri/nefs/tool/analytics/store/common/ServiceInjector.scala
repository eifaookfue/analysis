package jp.co.nri.nefs.tool.analytics.store.common

import com.google.inject.{Binder, Guice, Injector, Module}
import play.api.db.slick.DatabaseConfigProvider

import scala.collection.JavaConverters._

class DefaultModule extends Module {
  def configure(binder: Binder): Unit = {
    binder.bind(classOf[DatabaseConfigProvider]).to(classOf[DefaultDatabaseConfigProvider])
  }
}

object ServiceInjector {
  private var injector: Injector = _

  def initialize(module: Module = null): Unit = {
    injector = if (module == null) {
      Guice.createInjector(
        Seq(new DefaultModule).asJava
      )
    } else {
      Guice.createInjector(
        Seq(new DefaultModule, module).asJava
      )
    }
  }

  def getComponent[T](clazz: Class[T]): T = {
    if (injector == null){
      throw new RuntimeException("You must call ServiceInjector#initialize at first.")
    }
    injector.getInstance(clazz)
  }
}

