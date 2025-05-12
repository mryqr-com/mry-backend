package com.mryqr.core.app.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.event.AppAttributesDeletedEvent;
import com.mryqr.core.app.domain.event.DeletedAttributeInfo;
import com.mryqr.core.qr.domain.task.RemoveAttributeValuesForAllQrsUnderAppTask;
import com.mryqr.core.qr.domain.task.RemoveIndexedValueUnderAllQrsTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppAttributesDeletedEventHandler extends AbstractDomainEventHandler<AppAttributesDeletedEvent> {
    private final RemoveAttributeValuesForAllQrsUnderAppTask removeAttributeValuesForAllQrsUnderAppTask;
    private final RemoveIndexedValueUnderAllQrsTask removeIndexedValueUnderAllQrsTask;
    private final AppRepository appRepository;

    @Override
    protected void doHandle(AppAttributesDeletedEvent event) {
        String appId = event.getAppId();
        Set<DeletedAttributeInfo> deletedAttributeInfos = event.getAttributes();

        //删除所有qr中对应的attributeValue
        Set<String> deletedAttributeIds = deletedAttributeInfos.stream()
                .map(DeletedAttributeInfo::getAttributeId)
                .collect(toImmutableSet());
        MryTaskRunner.run(() -> removeAttributeValuesForAllQrsUnderAppTask.run(deletedAttributeIds, appId));

        Set<DeletedAttributeInfo> valueIndexableAttributes = deletedAttributeInfos.stream()
                .filter(info -> info.getValueType().isIndexable())
                .collect(toImmutableSet());

        if (isNotEmpty(valueIndexableAttributes)) {
            //删除对应的indexedValue
            appRepository.byIdOptional(appId).ifPresent(app -> valueIndexableAttributes.forEach(info -> {
                if (!app.hasAttributeIndexedFiled(info.getIndexedField())) {//如果字段尚未被别的属性占用，则直接删除
                    MryTaskRunner.run(() -> removeIndexedValueUnderAllQrsTask.run(info.getIndexedField(), appId));
                } else {//否则需要加上attributeId作为筛选条件
                    MryTaskRunner.run(() -> removeIndexedValueUnderAllQrsTask.run(info.getIndexedField(), info.getAttributeId(), appId));
                }
            }));
        }
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
