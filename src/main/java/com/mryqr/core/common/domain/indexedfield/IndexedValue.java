package com.mryqr.core.common.domain.indexedfield;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class IndexedValue {
    private final String rid;//referenceId，引用ID，不能索引，因为MongoDB最大只支持64个索引
    private final Double sv;//sortableValue，可排序字段，需要索引
    private final Set<String> tv;//textValues，可搜索字段，用于按控件统计或重复数据检查，对于有option的控件为optionIds，对于邮件控件为邮箱地址，需要索引
}
