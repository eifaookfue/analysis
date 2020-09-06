package jp.co.nri.nefs.oms.order.service.entity.property;

import jp.co.nri.nefs.common.util.property.Repeating;
import jp.co.nri.nefs.common.util.property.Type;

import javax.persistence.Column;
import java.util.List;

public enum ETransferExecutionProperty {

    @Column(nullable = false)
    @Type(String.class)
    TARGET_ORDERID,

    @Column(nullable = false)
    @Type(String.class)
    TARGET_LEGAL_TICKET_NOTE,

    @Column(nullable = false)
    @Type(List.class)
    @Repeating(ETranferExecutionChildProperty.class)
    TRANSFER_EXECUTION_LIST,

    @Column(nullable = false)
    @Type(String.class)
    OPERATE_USER_ID,

    @Column(nullable = true)
    @Type(String.class)
    APPROVE_USER_ID
}
