package com.mryqr.core.qr.domain.task;

import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.qr.domain.QrRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveAllQrsUnderGroupTask implements RepeatableTask {
    private final QrRepository qrRepository;

    public void run(String groupId) {
        int count = qrRepository.removeAllQrsUnderGroup(groupId);
        log.info("Removed all {} qrs under group[{}].", count, groupId);
    }

}
