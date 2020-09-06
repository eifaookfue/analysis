package jp.co.nri.nefs.tool.entity.producer;

import jp.co.nri.nefs.common.model.entity.DefaultEntity;
import jp.co.nri.nefs.common.model.entity.IEntity;
import jp.co.nri.nefs.oms.order.service.entity.property.ETranferExecutionChildProperty;

public class TestEntity {

    private DefaultEntity<ETranferExecutionChildProperty> entity = DefaultEntity.valueOf(ETranferExecutionChildProperty.class);

    public TestEntity() {
        Integer skipList[] = {1, 2, 3};
        entity.putValue(ETranferExecutionChildProperty.CHECK_SKIP_LIST, skipList);
    }

    public IEntity<ETranferExecutionChildProperty> get() {
        return entity;
    }
}
