package jp.co.nri.tcatool.sba.tca.updater

import com.typesafe.scalalogging.LazyLogging
import javax.inject.Inject
import jp.co.nri.nefs.tool.analytics.store.common.ServiceInjector
import jp.co.nri.tcatool.ref.alpha.marketdata.model.MarketDataPerDayComponent
import jp.co.nri.tcatool.sba.tca.model.OrderComponent
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

class BasePriceUpdater @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
  extends OrderComponent with MarketDataPerDayComponent with HasDatabaseConfigProvider[JdbcProfile] with LazyLogging {

  import profile.api._

  val orders = TableQuery[Orders]
  val marketData = TableQuery[MarketDataPerDays]

  def updateBasePrice(): Unit = {
    val orderQuery = orders.map(o => (o.orderKeyProjection, o.symbol, o.baseDate, o.sliceQty))
    val orderQueryFut = db.run(orderQuery.result)
    Await.ready(orderQueryFut, Duration.Inf)
    val orderList = orderQueryFut.value.get match {
      case Success(value) =>
        logger.info("Select order succeeded.")
        value
      case Failure(e) =>
        logger.error("Select order failed.", e)
        Nil
    }

    val basePriceFuts = for {
      (orderKey, symbol, baseDate, sliceQty) <- orderList
      marketQuery = marketData.filter(m => m.symbol === symbol && m.closePrice.isDefined
        && m.tradeDate <= baseDate.minusDays(1))
        .sortBy(_.tradeDate.desc).map(_.closePrice.get)
      marketQueryFut = db.run(marketQuery.result)
    } yield marketQueryFut.map(seq => (orderKey, sliceQty, seq.headOption))
    val basePriceFutAgg = Future.sequence(basePriceFuts)
    Await.ready(basePriceFutAgg, Duration.Inf)
    val basePrices =  basePriceFutAgg.value.get match {
      case Success(v) =>
        logger.info("Select market data succeeded.")
        v
      case Failure(e) =>
        logger.error("Select market data failed.", e)
        Nil
    }
    val updateFuts = for {
      (k, sliceQty, basePrice) <- basePrices
      if basePrice.nonEmpty
      basePriceQuery = orders.filter(o => o.compId === k.compId && o.orderId === k.orderId).map(o => (o.basePrice, o.orderAmount))
      update = basePriceQuery.update(basePrice, Some(basePrice.get * sliceQty))
      updateFut = db.run(update)
    } yield updateFut
    val updateFutAgg = Future.sequence(updateFuts)
    Await.ready(updateFutAgg, Duration.Inf)
    updateFutAgg.value.get match {
      case Success(_) =>
        logger.info("Update order succeeded.")
      case Failure(e) =>
        logger.error("Update order failed.", e)
    }

  }

}

object BasePriceUpdater {
  def main(args: Array[String]): Unit = {
    ServiceInjector.initialize()
    val basePriceUpdater = ServiceInjector.getComponent(classOf[BasePriceUpdater])
    basePriceUpdater.updateBasePrice()
  }
}
