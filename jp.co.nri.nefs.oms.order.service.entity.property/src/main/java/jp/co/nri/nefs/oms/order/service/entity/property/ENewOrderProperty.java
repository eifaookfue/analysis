package jp.co.nri.nefs.oms.order.service.entity.property;


import jp.co.nri.nefs.common.util.property.Type;
import jp.co.nri.nefs.oms.entity.property.definition.EBSType;
import jp.co.nri.nefs.oms.entity.property.definition.EMarket;

import javax.persistence.Column;
import java.math.BigDecimal;

public enum ENewOrderProperty {
    @Column(nullable = false)
    @Type(BigDecimal.class)
    BASKET_ID,

    @Column(nullable = false)
    @Type(String.class)
    INSTRUMENT_ID,

    @Column(nullable = false)
    @Type(EBSType.class)
    BS_TYPE,

    @Column(nullable = true, scale = 4)
    @Type(BigDecimal.class)
    PRICE,

    @Column(nullable = false)
    @Type(BigDecimal.class)
    ORDERQTY,

    @Column(nullable = false)
    @Type(EMarket.class)
    MARKET

}
