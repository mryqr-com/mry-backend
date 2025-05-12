package com.mryqr.core.app.eventhandler;

import com.mryqr.common.event.consume.AbstractDomainEventHandler;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.event.AppPageChangedToSubmitPerMemberEvent;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.qr.domain.task.RemoveAttributeValuesForAllQrsUnderAppTask;
import com.mryqr.core.qr.domain.task.RemoveIndexedValueUnderAllQrsTask;
import com.mryqr.core.submission.domain.task.CountSubmissionForAppTask;
import com.mryqr.core.submission.domain.task.RemoveAllSubmissionsForPageTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class PageChangedToSubmitPerMemberEventHandler extends AbstractDomainEventHandler<AppPageChangedToSubmitPerMemberEvent> {
    private final AppRepository appRepository;
    private final RemoveAllSubmissionsForPageTask removeAllSubmissionsForPageTask;
    private final CountSubmissionForAppTask countSubmissionForAppTask;
    private final RemoveAttributeValuesForAllQrsUnderAppTask removeAttributeValuesForAllQrsUnderAppTask;
    private final RemoveIndexedValueUnderAllQrsTask removeIndexedValueUnderAllQrsTask;

    @Override
    public int priority() {
        return -100;//需要即时处理
    }

    @Override
    protected void doHandle(AppPageChangedToSubmitPerMemberEvent event) {
        if (event.getRaisedAt().isBefore(now().minus(10, MINUTES))) {
            log.warn("Domain event[{}:{}] is more than 10 minutes old, skip.", event.getType(), event.getId());
            return;
        }

        appRepository.byIdOptional(event.getAppId()).ifPresent(app -> event.getPageIds().stream()
                .flatMap(pageId -> app.pageByIdOptional(pageId).stream())
                .filter(Page::isOncePerMemberSubmitType)
                .forEach(page -> {
                    removeAllSubmissionsForPageTask.run(page.getId(), event.getAppId());
                    List<Attribute> tobeValueDeletedAttributes = app.allPageSubmissionAwareAttributes(page.getId());
                    if (isNotEmpty(tobeValueDeletedAttributes)) {
                        Set<String> tobeValueDeletedAttributeIds = tobeValueDeletedAttributes.stream()
                                .map(Attribute::getId)
                                .collect(toImmutableSet());

                        removeAttributeValuesForAllQrsUnderAppTask.run(tobeValueDeletedAttributeIds, app.getId());

                        tobeValueDeletedAttributes.stream()
                                .filter(attribute -> attribute.getValueType().isIndexable())
                                .forEach(attribute -> app.indexedFieldForAttributeOptional(attribute.getId())
                                        .ifPresent(indexedField -> removeIndexedValueUnderAllQrsTask.run(indexedField, app.getId())));
                    }
                }));

        countSubmissionForAppTask.run(event.getAppId(), event.getArTenantId());
    }
}
