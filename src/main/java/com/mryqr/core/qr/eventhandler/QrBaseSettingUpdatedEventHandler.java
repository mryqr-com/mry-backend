package com.mryqr.core.qr.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.common.utils.MryTaskRunner;
import com.mryqr.core.qr.domain.event.QrBaseSettingUpdatedEvent;
import com.mryqr.core.qr.domain.task.SyncAttributeValuesForQrTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrBaseSettingUpdatedEventHandler extends AbstractDomainEventHandler<QrBaseSettingUpdatedEvent> {
    private final SyncAttributeValuesForQrTask syncAttributeValuesForQrTask;

    @Override
    protected void doHandle(QrBaseSettingUpdatedEvent event) {
        //更新基本设置时，刻意重新计算所有的属性，相当于一个手动更新全部属性的口子
        MryTaskRunner.run(() -> syncAttributeValuesForQrTask.run(event.getQrId()));
    }

    @Override
    public boolean isIdempotent() {
        return true;
    }
}
