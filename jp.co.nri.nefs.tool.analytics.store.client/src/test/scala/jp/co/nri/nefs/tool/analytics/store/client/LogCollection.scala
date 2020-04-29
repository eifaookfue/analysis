package jp.co.nri.nefs.tool.analytics.store.client

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

  lazy val selectSymbolFromViewLog = List(
    "2019-12-16 12:08:39.044 [OMS:INFO ][TradeSheet]Start find properties. class jp.co.nri.nefs.oms.rcp.parts.selector.SymbolSelectFindComponentCreator$EDisplayProductType.[main][j.c.n.n.c.r.p.l.AbstractLifeCycleDelegate$EnumDisplayValuePropertiesProviderFinder]",
    "2019-12-16 12:08:39.044 [OMS:INFO ][TradeSheet]End find properties. class jp.co.nri.nefs.oms.rcp.parts.selector.SymbolSelectFindComponentCreator$EDisplayProductType.[main][j.c.n.n.c.r.p.l.AbstractLifeCycleDelegate$EnumDisplayValuePropertiesProviderFinder]",
    "2019-12-16 12:08:39.044 [OMS:INFO ][TradeSheet][Select Symbol Multi]Dialog opened.[main][j.c.n.n.o.r.p.d.SelectMultiDialog]",
    "2019-12-16 12:08:39.044 [OMS:INFO ][TradeSheet][Select Symbol Multi]Button event starts -> (Cancel).[main][j.c.n.n.o.r.p.d.SelectMultiDialog]",
    "2019-12-16 12:08:39.044 [OMS:INFO ][TradeSheet][Select Symbol Multi]Dialog closed.[main][j.c.n.n.o.r.p.d.SelectMultiDialog]",
    "2019-12-16 12:08:39.044 [OMS:INFO ][TradeSheet][Select Symbol Multi]Button event ends -> (Cancel).[main][j.c.n.n.o.r.p.d.SelectMultiDialog]"
  )

  lazy val selectSymbolFromNewOrderSingleLog = List(
    "2019-12-16 12:10:15.667 [OMS:INFO ][TradeSheet]Handler start.[main][j.c.n.n.o.t.h.t.CareBuyHandler]",
    "2019-12-16 12:10:16.530 [OMS:INFO ][TradeSheet]Start LoadState from C:\\APL\\OMS\\DEV-G1\\USER\\356435\\TradeSheet\\20191120_1933306_734\\defaultsetting\\workspace\\AUTO\\NatTableUtil\\nattable_persistence\\DefaultFeedbackComposite.properties.[main][j.c.n.n.c.n.util.NatTableUtil]",
    "2019-12-16 12:10:16.537 [OMS:INFO ][TradeSheet]End LoadState from C:\\APL\\OMS\\DEV-G1\\USER\\356435\\TradeSheet\\20191120_1933306_734\\defaultsetting\\workspace\\AUTO\\NatTableUtil\\nattable_persistence\\DefaultFeedbackComposite.properties.[main][j.c.n.n.c.n.util.NatTableUtil]",
    "2019-12-16 12:10:16.740 [OMS:INFO ][TradeSheet]Handler end.[main][j.c.n.n.o.t.h.t.CareBuyHandler]",
    "2019-12-16 12:10:16.761 [OMS:INFO ][TradeSheet][New Order Single]Dialog opened.[main][j.c.n.n.o.r.p.d.NewOrderSingleDialog$1]",
    "2019-12-16 12:10:18.244 [OMS:INFO ][TradeSheet][Select Symbol]Dialog opened.[main][j.c.n.n.o.r.p.d.SymbolSelectionDialog]",
    "2019-12-16 12:11:00.754 [OMS:INFO ][TradeSheet][Select Symbol]Button event starts -> (Cancel).[main][j.c.n.n.o.r.p.d.SymbolSelectionDialog]",
    "2019-12-16 12:11:00.754 [OMS:INFO ][TradeSheet][Select Symbol]Dialog closed.[main][j.c.n.n.o.r.p.d.SymbolSelectionDialog]",
    "2019-12-16 12:11:00.774 [OMS:INFO ][TradeSheet][Select Symbol]Button event ends -> (Cancel).[main][j.c.n.n.o.r.p.d.SymbolSelectionDialog]",
    "2019-12-16 12:11:01.982 [OMS:INFO ][TradeSheet][Select Symbol]Dialog closed.[main][j.c.n.n.o.r.p.d.NewOrderSingleDialog$1]",
  )

  lazy val newSplitAndNewSplitLog = List(
    "2019-12-16 12:11:03.860 [OMS:INFO ][TradeSheet]Handler start.[main][j.c.n.n.o.t.h.NewSplitHandler]",
    "2019-12-16 12:11:04.967 [OMS:INFO ][TradeSheet]Start LoadState from C:\\APL\\OMS\\DEV-G1\\USER\\356435\\TradeSheet\\20191120_1933306_734\\defaultsetting\\workspace\\AUTO\\NatTableUtil\\nattable_persistence\\NewSplitComponentCreator__first.properties[main][j.c.n.n.c.n.util.NatTableUtil]",
    "2019-12-16 12:11:04.972 [OMS:INFO ][TradeSheet]End LoadState from C:\\APL\\OMS\\DEV-G1\\USER\\356435\\TradeSheet\\20191120_1933306_734\\defaultsetting\\workspace\\AUTO\\NatTableUtil\\nattable_persistence\\NewSplitComponentCreator__first.properties[main][j.c.n.n.c.n.util.NatTableUtil]",
    "2019-12-16 12:11:05.066 [OMS:INFO ][TradeSheet]Handler end.[main][j.c.n.n.o.t.h.NewSplitHandler]",
    "2019-12-16 12:11:05.085 [OMS:INFO ][TradeSheet][New Split  - Parent Order]Dialog Opened.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-12-16 12:11:08.745 [OMS:INFO ][TradeSheet]Handler start.[main][j.c.n.n.o.t.h.NewSplitHandler]",
    "2019-12-16 12:11:08.943 [OMS:INFO ][TradeSheet]Handler end.[main][j.c.n.n.o.t.h.NewSplitHandler]",
    "2019-12-16 12:11:08.972 [OMS:INFO ][TradeSheet][New Split  - Parent Order]Dialog Opened.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-12-16 12:11:10:302 [OMS:INFO ][TradeSheet][New Split  - Parent Order]Button event starts -> (Cancel).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-12-16 12:11:10.310 [OMS:INFO ][TradeSheet][New Split  - Parent Order]Dialog closed.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-12-16 12:11:10:361 [OMS:INFO ][TradeSheet][New Split  - Parent Order]Button event ends -> (Cancel).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-12-16 12:11:10:440 [OMS:INFO ][TradeSheet][New Split  - Parent Order]Button event starts -> (Cancel).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-12-16 12:11:11.449 [OMS:INFO ][TradeSheet][New Split  - Parent Order]Dialog closed.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-12-16 12:11:11:497 [OMS:INFO ][TradeSheet][New Split  - Parent Order]Button event ends -> (Cancel).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
  )

  lazy val destinationTypeNewSplitLog = List(
    "2019-10-10 15:54:12.452 [OMS:INFO ][TradeSheet]Handler start.[main][j.c.n.n.o.t.h.NewSplitHandler]",
    "2019-10-10 15:54:12.521 [OMS:INFO ][TradeSheet]Start find properties. class=[class jp.co.nri.nefs.oms.rcp.parts.entity.property.definition.ESummaryUnit].[main][j.c.n.n.c.r.p.l.AbstractLifeCycleDelegate$EnumDisplayValuePropertiesProviderFinder]",
    "2019-10-10 15:54:12.830 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Dialog opened.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:17.434 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:17.515 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:17.593 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:17.605 [OMS:INFO ][TradeSheet]REQUEST=[ENewSliceProperty], SIZE=[1][main][j.c.n.n.o.r.p.c.CustomOrderServiceProxy]",
    "2019-10-10 15:54:17.605 [OMS:INFO ][TradeSheet]PARAMETER=DefaultEntity:{ORDERID=OO201910100000005298,MARKET=TYO_MAIN,MARKET_SECTION_CODE=SPACE,PRICE_TYPE=LMT,PRICE=6136.0,ORDERQTY=100,ORDER_CONDITION=NONE,SOR_TYPE=NOT_SOR,CROSS_TYPE=null,CROSS_PRICE_TYPE=null,REF_MARKET=null,REF_PRICE=null,CROSS_COUNTERPARTY=null,JNET_COUNTERPARTY=null,CROSS_CAPACITY=null,JNET_PURPOSETYPE=null,AGENCY_CROSS_ID=null,CROSS_NOTE=null,CROSS_DONE_TIME=null,OPERATE_USER_ID=356435,APPROVE_USER_ID=null,CHECK_SKIP_LIST={0}}[main][j.c.n.n.o.r.p.c.CustomOrderServiceProxy]",
    "2019-10-10 15:54:17.670 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Dialog closed.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:17.716 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:17.723 [OMS:INFO ][TradeSheet]Handler end.[main][j.c.n.n.o.t.h.NewSplitHandler]",
    "2019-10-10 15:54:19.076 [OMS:INFO ][TradeSheet]Handler start.[main][j.c.n.n.o.t.h.NewSplitHandler]",
    "2019-10-10 15:54:19.356 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Dialog opened.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:23.900 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:23.959 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:24.369 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:24.378 [OMS:INFO ][TradeSheet]REQUEST=[ENewChildOrderProperty], SIZE=[1][main][j.c.n.n.o.r.p.c.CustomOrderServiceProxy]",
    "2019-10-10 15:54:24.378 [OMS:INFO ][TradeSheet]PARAMETER=DefaultEntity:{ORDERID=OO201910100000005298,MARKET=TYO_MAIN,MARKET_SECTION_CODE=SPACE,PRICE_TYPE=MKT,PRICE=null,ORDERQTY=100,ORDER_CONDITION=NONE,SOR_TYPE=NOT_SOR,CROSS_TYPE=null,CROSS_PRICE_TYPE=null,REF_MARKET=null,REF_PRICE=null,CROSS_COUNTERPARTY=null,JNET_COUNTERPARTY=null,CROSS_CAPACITY=null,JNET_PURPOSETYPE=null,AGENCY_CROSS_ID=null,CROSS_NOTE=null,CROSS_DONE_TIME=null,OPERATE_USER_ID=356435,APPROVE_USER_ID=null,CHECK_SKIP_LIST={0}}[main][j.c.n.n.o.r.p.c.CustomOrderServiceProxy]",
    "2019-10-10 15:54:24.450 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Dialog closed.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:24.494 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:24.500 [OMS:INFO ][TradeSheet]Handler end.[main][j.c.n.n.o.t.h.NewSplitHandler]",
    "2019-10-10 15:54:26.466 [OMS:INFO ][TradeSheet]Handler start.[main][j.c.n.n.o.t.h.NewSplitHandler]",
    "2019-10-10 15:54:26.739 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Dialog opened.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:34.185 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:34.258 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:34.645 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:34.663 [OMS:INFO ][TradeSheet]REQUEST=[ENewChildOrderAndAlgoProperty], SIZE=[1][main][j.c.n.n.o.r.p.c.CustomOrderServiceProxy]",
    "2019-10-10 15:54:34.663 [OMS:INFO ][TradeSheet]PARAMETER=DefaultEntity:{ORDERID=OO201910100000005298,MARKET=TYO_MAIN,MARKET_SECTION_CODE=SPACE,PRICE_TYPE=MKT,PRICE=null,ORDERQTY=100,ORDER_CONDITION=NONE,SOR_TYPE=NOT_SOR,CROSS_TYPE=null,CROSS_PRICE_TYPE=null,REF_MARKET=null,REF_PRICE=null,CROSS_COUNTERPARTY=null,JNET_COUNTERPARTY=null,CROSS_CAPACITY=null,JNET_PURPOSETYPE=null,AGENCY_CROSS_ID=null,CROSS_NOTE=null,CROSS_DONE_TIME=null,OPERATE_USER_ID=356435,APPROVE_USER_ID=null,CHECK_SKIP_LIST={0}}[main][j.c.n.n.o.r.p.c.CustomOrderServiceProxy]",
    "2019-10-10 15:54:34.775 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Dialog closed.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:34.820 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:34.827 [OMS:INFO ][TradeSheet]Handler end.[main][j.c.n.n.o.t.h.NewSplitHandler]",
    "2019-10-10 15:54:36.341 [OMS:INFO ][TradeSheet]Handler start.[main][j.c.n.n.o.t.h.NewSplitHandler]",
    "2019-10-10 15:54:36.613 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Dialog opened.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:42.975 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:43.036 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:43.425 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:43.491 [OMS:INFO ][TradeSheet]REQUEST=[ENewReservedSliceProperty], SIZE=[1][main][j.c.n.n.o.r.p.c.CustomOrderServiceProxy]",
    "2019-10-10 15:54:43.491 [OMS:INFO ][TradeSheet]PARAMETER=DefaultEntity:{ORDERID=OO201910100000005298,MARKET=TYO_TOST,MARKET_SECTION_CODE=ONE,PRICE_TYPE=MKT,PRICE=null,ORDERQTY=100,ORDER_CONDITION=NONE,SOR_TYPE=NOT_SOR,CROSS_TYPE=null,CROSS_PRICE_TYPE=null,REF_MARKET=null,REF_PRICE=null,CROSS_COUNTERPARTY=null,JNET_COUNTERPARTY=null,CROSS_CAPACITY=PRINCIPAL,JNET_PURPOSETYPE=null,AGENCY_CROSS_ID=null,CROSS_NOTE=null,CROSS_DONE_TIME=null,OPERATE_USER_ID=356435,APPROVE_USER_ID=null,CHECK_SKIP_LIST={0}}[main][j.c.n.n.o.r.p.c.CustomOrderServiceProxy]",
    "2019-10-10 15:54:43.545 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Dialog closed.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:43.598 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:43.609 [OMS:INFO ][TradeSheet]Handler end.[main][j.c.n.n.o.t.h.NewSplitHandler]",
    "2019-10-10 15:54:44.841 [OMS:INFO ][TradeSheet]Handler start.[main][j.c.n.n.o.t.h.NewSplitHandler]",
    "2019-10-10 15:54:45.111 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Dialog opened.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:49.281 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:49.285 [OMS:INFO ][TradeSheet][New Split    - Parent Order]KEY=1, NO=1, TYPE=ERROR, MESSAGE=AA Crossのとき、AACrossID は必須項目です。[O7509][main][j.c.n.n.o.r.p.d.v.DefaultValidationDataManager]",
    "2019-10-10 15:54:49.286 [OMS:INFO ][TradeSheet][New Split    - Parent Order]KEY=1, NO=1, TYPE=ERROR, MESSAGE=組み合わせが不正です (PriceType, Price)。[O7673][main][j.c.n.n.o.r.p.d.v.DefaultValidationDataManager]",
    "2019-10-10 15:54:49.351 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:53.721 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:53.723 [OMS:INFO ][TradeSheet][New Split    - Parent Order]KEY=1, NO=1, TYPE=ERROR, MESSAGE=組み合わせが不正です (PriceType, Price)。[O7673][main][j.c.n.n.o.r.p.d.v.DefaultValidationDataManager]",
    "2019-10-10 15:54:53.752 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:54.129 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:58.166 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:58.620 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:58.637 [OMS:INFO ][TradeSheet]REQUEST=[ENewSliceProperty], SIZE=[1][main][j.c.n.n.o.r.p.c.CustomOrderServiceProxy]",
    "2019-10-10 15:54:58.637 [OMS:INFO ][TradeSheet]PARAMETER=DefaultEntity:{ORDERID=OO201910100000005298,MARKET=TYO_TOST,MARKET_SECTION_CODE=ONE,PRICE_TYPE=MKT,PRICE=null,ORDERQTY=100,ORDER_CONDITION=NONE,SOR_TYPE=NOT_SOR,CROSS_TYPE=SINGLE_CLOSS,CROSS_PRICE_TYPE=null,REF_MARKET=null,REF_PRICE=null,CROSS_COUNTERPARTY=null,JNET_COUNTERPARTY=null,CROSS_CAPACITY=AGENCY,JNET_PURPOSETYPE=null,AGENCY_CROSS_ID=11,CROSS_NOTE=null,CROSS_DONE_TIME=null,OPERATE_USER_ID=356435,APPROVE_USER_ID=null,CHECK_SKIP_LIST={0}}[main][j.c.n.n.o.r.p.c.CustomOrderServiceProxy]",
    "2019-10-10 15:54:58.704 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Dialog closed.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:58.751 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:54:58.757 [OMS:INFO ][TradeSheet]Handler end.[main][j.c.n.n.o.t.h.NewSplitHandler]",
    "2019-10-10 15:55:00.143 [OMS:INFO ][TradeSheet]Handler start.[main][j.c.n.n.o.t.h.NewSplitHandler]",
    "2019-10-10 15:55:00.414 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Dialog opened.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:55:07.798 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:55:07.855 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:55:08.336 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event starts -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:55:08.346 [OMS:INFO ][TradeSheet]REQUEST=[ENewSliceProperty], SIZE=[1][main][j.c.n.n.o.r.p.c.CustomOrderServiceProxy]",
    "2019-10-10 15:55:08.346 [OMS:INFO ][TradeSheet]PARAMETER=DefaultEntity:{ORDERID=OO201910100000005298,MARKET=TYO_TOST,MARKET_SECTION_CODE=TWO,PRICE_TYPE=MKT,PRICE=null,ORDERQTY=100,ORDER_CONDITION=NONE,SOR_TYPE=NOT_SOR,CROSS_TYPE=FIXED_PRICE,CROSS_PRICE_TYPE=PRE_CLOSE,REF_MARKET=null,REF_PRICE=null,CROSS_COUNTERPARTY=null,JNET_COUNTERPARTY=null,CROSS_CAPACITY=null,JNET_PURPOSETYPE=null,AGENCY_CROSS_ID=null,CROSS_NOTE=null,CROSS_DONE_TIME=null,OPERATE_USER_ID=356435,APPROVE_USER_ID=null,CHECK_SKIP_LIST={0}}[main][j.c.n.n.o.r.p.c.CustomOrderServiceProxy]",
    "2019-10-10 15:55:08.413 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Dialog closed.[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:55:08.466 [OMS:INFO ][TradeSheet][New Split    - Parent Order]Button event ends -> (OK).[main][j.c.n.n.o.r.p.d.s.n.NewSplitDialog]",
    "2019-10-10 15:55:08.473 [OMS:INFO ][TradeSheet]Handler end.[main][j.c.n.n.o.t.h.NewSplitHandler]"
  )

  lazy val waveLog = List(
    "a"
  )

  lazy val newOrderAndSliceDerivativeLog = List(
    "a"
  )

  lazy val principalBasketCross = List(
    "a"
  )
}