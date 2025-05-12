package com.mryqr.core.qr.domain.task;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.qr.domain.attribute.sync.QrAttributeValueSyncer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

import static com.mryqr.core.common.domain.user.User.NOUSER;
import static com.mryqr.core.qr.domain.QR.newQrId;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncAttributeValuesForAllQrsUnderAppTask implements RepeatableTask {
    private static final int BATCH_SIZE = 100;
    private final AppRepository appRepository;
    private final QrAttributeValueSyncer qrAttributeValueSyncer;
    private final QrRepository qrRepository;

    public void run(String appId, Set<String> attributeIds) {
        if (isEmpty(attributeIds)) {
            return;
        }

        ForkJoinPool forkJoinPool = new ForkJoinPool(20);

        int count = 0;
        String startId = newQrId();

        try {
            while (true) {
                List<QR> qrs = qrRepository.find(appId, startId, BATCH_SIZE);

                if (isEmpty(qrs)) {
                    break;
                }

                count = count + qrs.size();

                Optional<App> appOptional = appRepository.byIdOptional(appId);//每个批次重新加载一个app以保证app是最新的
                if (appOptional.isEmpty()) {
                    log.warn("App[{}] does not exist, exit task.", appId);
                    break;
                }

                App app = appOptional.get();
                List<Attribute> attributes = app.allAttributesOfIds(attributeIds);
                if (isEmpty(attributes)) {
                    log.warn("No attributes exist by {} of app[{}], exit task.", attributeIds, appId);
                    break;
                }

                forkJoinPool.submit(() -> qrs.parallelStream()
                                .forEach(qr -> {
                                    try {
                                        qrAttributeValueSyncer.sync(qr, app, attributes, NOUSER);
                                    } catch (Throwable t) {
                                        log.error("Error while sync attributes{} for qr[{}] of app[{}].",
                                                attributeIds, qr.getId(), app.getId(), t);
                                    }
                                }))
                        .join();

                startId = qrs.get(qrs.size() - 1).getId();//下一次直接从最后一条开始查询
            }
        } finally {
            forkJoinPool.shutdown();
        }

        log.info("Synced attributes{} values for all {} qrs of app[{}].", attributeIds, count, appId);
    }

}
