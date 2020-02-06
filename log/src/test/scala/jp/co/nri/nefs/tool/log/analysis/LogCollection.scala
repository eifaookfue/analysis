package jp.co.nri.nefs.tool.log.analysis

trait LogCollection {

  lazy val newSplitName = "TradeSheet_OMS_TKY_FID2CAD332_356435_20191010155412452.log"
  lazy val newSplitLog = List(
    "2019-10-10 15:54:12.452 [OMS:INFO ][TradeSheet]Handler start.[main][j.c.n.n.o.t.h.NewSplitHandler]",
    "2019-10-10 15:54:12.521 [OMS:INFO ][TradeSheet]Start find properties. class=[class jp.co.nri.nefs.oms.rcp.parts.entity.property.definition.ESummaryUnit].[main][j.c.n.n.c.r.p.l.AbstractLifeCycleDelegate$EnumDisplayValuePropertiesProviderFinder]",
    "2019-10-10 15:54:12.830 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Dialog opened.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:17.434 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:17.515 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:17.593 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:17.670 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Dialog closed.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:17.716 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:17.723 [OMS:INFO ][TradeSheet]Handler end.[main][j.c.n.n.o.t.h.NewSplitHandler]"
  )

  lazy val newSplitFromSmartName = "TradeSheet_OMS_TKY_FID2CAD332_356435_20191216120638278.log"
  lazy val newSplitFromSmartLog = List(
    "2019-12-16 12:06:38.278 [OMS:INFO ][TradeSheet]Handler start.[main][j.c.n.n.o.t.h.SmartSplitHandler]",
    "2019-12-16 12:06:38.687 [OMS:INFO ][TradeSheet]Handler end.[main][j.c.n.n.o.t.h.SmartSplitHandler]",
    "2019-12-16 12:06:38.708 [OMS:INFO ][TradeSheet][Smart Split    - Parent Order]Dialog opened.[main][j.c.n.n.o.r.p.d.s.s.SmartSplitDialog]",
    "2019-12-16 12:06:46.091 [OMS:INFO ][TradeSheet][Smart Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.s.SmartSplitDialog]",
    "2019-12-16 12:06:46.380 [OMS:INFO ][TradeSheet][Smart Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.s.SmartSplitDialog]",
    "2019-12-16 12:06:46.391 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Dialog opened.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-12-16 12:06:46.980 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-12-16 12:06:48.038 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-12-16 12:06:48.533 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-12-16 12:06:48.629 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Dialog closed.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-12-16 12:06:48.665 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]"
  )

  lazy val completeOrderLog = List(
    "2019-03-19 15:16:21.097 [OMS:INFO ][TradeSheet]Handler start.[main][j.c.n.n.o.t.h.CompleteOrderHandler]",
    "2019-03-19 15:16:21.184 [OMS:INFO ][TradeSheet]CompleteOrderAction start.[main][j.c.n.n.o.r.p.a.c.CompleteOrderAction]",
    "2019-03-19 15:16:21.215 [OMS:INFO ][TradeSheet]Opened.[main][j.c.n.n.o.r.p.d.c.QuestionDialog]",
    "2019-03-19 15:16:23.047 [OMS:INFO ][TradeSheet]Button Pressed -> (OK).[main][j.c.n.n.o.r.p.d.c.QuestionDialog]",
    "2019-03-19 15:16:23.047 [OMS:INFO ][TradeSheet]Closed.[main][j.c.n.n.o.r.p.d.c.QuestionDialog]"
  )


}