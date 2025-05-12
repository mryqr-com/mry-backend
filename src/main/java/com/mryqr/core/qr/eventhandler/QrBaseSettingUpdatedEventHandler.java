package com.mryqr.core.qr.eventhandler;

import com.mryqr.core.common.domain.event.DomainEvent;
import com.mryqr.core.common.domain.event.DomainEventHandler;
import com.mryqr.core.common.utils.MryTaskRunner;
import com.mryqr.core.qr.domain.event.QrBaseSettingUpdatedEvent;
import com.mryqr.core.qr.domain.task.SyncAttributeValuesForQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.mryqr.core.common.domain.event.DomainEventType.QR_BASE_SETTING_UPDATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrBaseSettingUpdatedEventHandler implements DomainEventHandler {
    private final SyncAttributeValuesForQrTask syncAttributeValuesForQrTask;

    @Override
    public boolean canHandle(DomainEvent domainEvent) {
        return domainEvent.getType() == QR_BASE_SETTING_UPDATED;
    }

    @Override
    public void handle(DomainEvent domainEvent, MryTaskRunner taskRunner) {
        QrBaseSettingUpdatedEvent event = (QrBaseSettingUpdatedEvent) domainEvent;

        //更新基本设置时，刻意重新计算所有的属性，相当于一个手动更新全部属性的口子
        taskRunner.run(() -> syncAttributeValuesForQrTask.run(event.getQrId()));
    }

}
