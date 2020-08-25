package jp.co.nri.nefs.oms.entity.property;

import jp.co.nri.nefs.common.util.property.Type;
import jp.co.nri.nefs.oms.entity.property.definition.EBSType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;

//jp.co.nri.nefs.oms.entity.property.definition.EBSType

@Entity
@Table(name = "OMS_ORDER")
public enum EOrderProperty {

    @Id
    @Column(nullable = false)
    @Type(BigDecimal.class)
    ORDERID,

    @Column(nullable = false)
    @Type(BigDecimal.class)
    BASKET_ID,

    @Column(nullable = false)
    @Type(EBSType.class)
    BS_TYPE,

    @Column(nullable = false)
    @Type(BigDecimal.class)
    ORDERQTY,

    @Column(nullable = false, scale = 4)
    @Type(BigDecimal.class)
    PRICE

}
