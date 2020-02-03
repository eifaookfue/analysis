import com.google.inject.{Binder, Module}
import javax.inject.Inject

// =======================
// サービスインターフェイス
trait OnOffDevice {
  def on: Unit
  def off: Unit
}
trait SensorDevice {
  def isCoffeePresent: Boolean
}
trait IWarmer {
  def trigger
}
trait Client

// =======================
// サービス実装
class Heater extends OnOffDevice {
  def on = println("heater.on")
  def off = println("heater.off")
}
class PotSensor extends SensorDevice {
  def isCoffeePresent = true
}

class @Inject AAA

class @Inject Warmer(
val potSensor: SensorDevice,
val heater: OnOffDevice)
extends IWarmer {

  def trigger = {
    if (potSensor.isCoffeePresent) heater.on
    else heater.off
  }
}

// =======================
// クライアント
class @Inject Client(val warmer: Warmer) extends Client {
  warmer.trigger
}

// =======================
// インターフェイスに対する実装の設定を定義する
// Guice の構成クラス
class DependencyModule extends Module {
  def configure(binder: Binder) = {
    binder.bind(classOf[OnOffDevice]).to(classOf[Heater])
    binder.bind(classOf[SensorDevice]).to(classOf[PotSensor])
    binder.bind(classOf[IWarmer]).to(classOf[Warmer])
    binder.bind(classOf[Client]).to(classOf[MyClient])
  }
}

// =======================
// 使用例: val bean = new Bean with ServiceInjector
trait ServiceInjector {
  ServiceInjector.inject(this)
}

// ヘルパー・コンパニオン・オブジェクト
object ServiceInjector {
  private val injector = Guice.createInjector(
    Array[Module](new DependencyModule))
  def inject(obj: AnyRef) = injector.injectMembers(obj)
}

// =======================
// インスタンスの生成時に ServiceInjector trait を
// mix in して依存オブジェクトをインジェクトする
val client = new MyClient with ServiceInjector

println(client)