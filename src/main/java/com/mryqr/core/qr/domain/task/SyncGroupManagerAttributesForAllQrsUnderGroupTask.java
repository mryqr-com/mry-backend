package com.mryqr.core.qr.domain.task;

import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.common.domain.indexedfield.IndexedValue;
import com.mryqr.core.common.domain.task.RepeatableTask;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.MembersAttributeValue;
import com.mryqr.core.qr.domain.attribute.MembersEmailAttributeValue;
import com.mryqr.core.qr.domain.attribute.MembersMobileAttributeValue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_GROUP_MANAGERS;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_GROUP_MANAGERS_AND_EMAIL;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_GROUP_MANAGERS_AND_MOBILE;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class SyncGroupManagerAttributesForAllQrsUnderGroupTask implements RepeatableTask {
    private final AppRepository appRepository;
    private final GroupRepository groupRepository;
    private final QrRepository qrRepository;

    public void run(String groupId) {
        groupRepository.byIdOptional(groupId).ifPresent(group -> appRepository.cachedByIdOptional(group.getAppId()).ifPresent(app -> {
            List<Attribute> attributes = app.allAttributesOfTypes(
                    INSTANCE_GROUP_MANAGERS,
                    INSTANCE_GROUP_MANAGERS_AND_MOBILE,
                    INSTANCE_GROUP_MANAGERS_AND_EMAIL
            );

            if (isEmpty(attributes)) {
                return;
            }

            attributes.forEach(attribute -> {
                AttributeValue attributeValue;
                switch (attribute.getType()) {
                    case INSTANCE_GROUP_MANAGERS -> {
                        attributeValue = new MembersAttributeValue(attribute, group.getManagers());
                    }
                    case INSTANCE_GROUP_MANAGERS_AND_MOBILE -> {
                        attributeValue = new MembersMobileAttributeValue(attribute, group.getManagers());
                    }
                    case INSTANCE_GROUP_MANAGERS_AND_EMAIL -> {
                        attributeValue = new MembersEmailAttributeValue(attribute, group.getManagers());
                    }
                    default -> {
                        throw new IllegalArgumentException("Cannot handle attribute type: " + attribute.getType());
                    }
                }

                int attributeUpdateCount = qrRepository.updateAttributeValueForAllQrsUnderGroup(groupId, attributeValue);
                log.info("Synced group[{}] managers for attributes[{}] of all {} qrs of app[{}].",
                        groupId, attributes.stream().map(Attribute::getId).collect(toImmutableList()), attributeUpdateCount, app.getId());

                app.indexedFieldForAttributeOptional(attribute.getId()).ifPresent(indexedField -> {
                    IndexedValue indexedValue = IndexedValue.builder()
                            .rid(attribute.getId())
                            .sv(null)
                            .tv(Set.copyOf(group.getManagers()))
                            .build();
                    int indexedUpdateCount = qrRepository.updateIndexValueForAllQrsUnderGroup(groupId, indexedField, indexedValue);
                    log.info("Synced group[{}] managers for attributes[{}] indexed values of all {} qrs of app[{}].",
                            groupId, attributes.stream().map(Attribute::getId).collect(toImmutableList()), indexedUpdateCount, app.getId());
                });
            });
        }));
    }

}
