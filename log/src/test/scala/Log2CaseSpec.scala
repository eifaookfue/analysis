import java.nio.file.Files

import jp.co.nri.nefs.tool.log.analysis.Log2Case
import org.scalatest._
import scala.collection.JavaConverters._

class Log2CaseSpec extends FlatSpec with PrivateMethodTester {

  def fixture =
    new {
      val logs = List(
        "2019-12-16 12:06:38.278 [OMS:INFO ][TradeSheet]Handler start.[main][j.c.n.n.o.t.h.SmartSplitHandler]",
        "2019-12-16 12:06:38.687 [OMS:INFO ][TradeSheet]Handler end.[main][j.c.n.n.o.t.h.SmartSplitHandler]",
        "2019-12-16 12:06:38.708 [OMS:INFO ][TradeSheet][Smart Split    - Parent Order]Dialog opend.[main][j.c.n.n.o.r.p.d.s.s.SmartSplitDialog]",
        "2019-12-16 12:06:46.091 [OMS:INFO ][TradeSheet][Smart Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.s.SmartSplitDialog]",
        "2019-12-16 12:06:46.380 [OMS:INFO ][TradeSheet][Smart Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.s.SmartSplitDialog]",
        "2019-12-16 12:06:46.391 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Dialog opened.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
        "2019-12-16 12:06:46.980 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
        "2019-12-16 12:06:48.038 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
        "2019-12-16 12:06:48.533 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
        "2019-12-16 12:06:48.629 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Dialog closed.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
        "2019-12-16 12:06:48.665 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]"
      )
      val messages = List()
    }

  "Execute method" should "complete" in {
    val f = fixture
    val outPath = Files.createTempDirectory("Log2CaseSpec")
    val path = outPath.resolve("TradeSheet_OMS_TKY_FID2CAD332_356435_20190319151525583.log")
    Files.write(path, f.logs.asJava)
    new Log2Case(outPath).execute(path)
  }




}
