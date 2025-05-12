package com.mryqr.core.qr.job;


import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.attribute.AttributeStatisticRange;
import com.mryqr.core.common.domain.indexedfield.IndexedField;
import com.mryqr.core.qr.domain.task.RemoveAttributeValuesForAllQrsUnderAppTask;
import com.mryqr.core.qr.domain.task.RemoveIndexedValueUnderAllQrsTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.qr.domain.QR.newQrId;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveQrRangedAttributeValuesForAllTenantsJob {
    private static final int BATCH_SIZE = 50;
    private final RemoveAttributeValuesForAllQrsUnderAppTask removeAttributeValuesForAllQrsUnderAppTask;
    private final RemoveIndexedValueUnderAllQrsTask removeIndexedValueUnderAllQrsTask;
    private final AppRepository appRepository;

    public void run(AttributeStatisticRange range) {
        log.info("Start reset all ranged[{}] attributes.", range);

        requireNonNull(range, "Range must not be null.");
        if (range == NO_LIMIT) {
            return;
        }

        ForkJoinPool forkJoinPool = new ForkJoinPool(20);

        int count = 0;
        String startId = newQrId();

        try {
            while (true) {
                List<App> apps = appRepository.appsOfRange(range, startId, BATCH_SIZE);
                if (isEmpty(apps)) {
                    break;
                }

                count = count + apps.size();

                forkJoinPool.submit(() -> apps.parallelStream().forEach(app -> {
                    List<Attribute> attributes = app.allAttributesOfRange(range);
                    if (isEmpty(attributes)) {
                        return;
                    }

                    Set<String> attributeIds = attributes.stream().map(Attribute::getId).collect(toImmutableSet());
                    removeAttributeValuesForAllQrsUnderAppTask.run(attributeIds, app.getId());

                    Set<IndexedField> indexedFields = attributes.stream()
                            .flatMap(attribute -> app.indexedFieldForAttributeOptional(attribute.getId()).stream())
                            .filter(Objects::nonNull)
                            .collect(toImmutableSet());
                    removeIndexedValueUnderAllQrsTask.run(indexedFields, app.getId());
                })).join();

                startId = apps.get(apps.size() - 1).getId();//下一次直接从最后一条开始查询
            }
        } finally {
            forkJoinPool.shutdown();
        }

        log.info("Reset ranged[{}] attribute values for all {} apps of all tenants.", range, count);
    }

}
