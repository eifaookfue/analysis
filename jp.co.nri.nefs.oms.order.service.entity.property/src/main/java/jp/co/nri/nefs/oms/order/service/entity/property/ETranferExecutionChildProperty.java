package jp.co.nri.nefs.oms.order.service.entity.property;

import jp.co.nri.nefs.common.util.property.Type;

import javax.persistence.Column;
import java.math.BigDecimal;

public enum ETranferExecutionChildProperty {

    @Column(nullable = false)
    @Type(String.class)
    TRANSFER_ORDERID,

    @Column(nullable = false)
    @Type(BigDecimal.class)
    TRANSFER_EXEC_NO,

    @Column(nullable = true)
    @Type(Integer[].class)
    CHECK_SKIP_LIST
}
