package com.mryqr.core.presentation.query;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.common.domain.display.DisplayValue;
import com.mryqr.core.group.domain.GroupAware;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.member.domain.MemberAware;
import com.mryqr.core.member.domain.MemberReference;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrReferenceContext;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.function.Function.identity;
import static org.apache.commons.collections4.MapUtils.emptyIfNull;

@Component
@RequiredArgsConstructor
public class AttributePresentationFetcher {
    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;

    public Map<String, DisplayValue> fetchAttributePresentations(QR qr,
                                                                 List<String> attributeIds,
                                                                 App app) {
        Map<String, AttributeValue> fixedAttributeValues = app.allFixedAttributeValues();
        Map<String, AttributeValue> qrAttributeValues = emptyIfNull(qr.getAttributeValues());

        List<AttributeValue> finalAttributeValues = Stream.concat(fixedAttributeValues.values().stream(),
                        qrAttributeValues.values().stream())
                .filter(value -> attributeIds.contains(value.getAttributeId()))
                .collect(toImmutableList());

        Set<String> groupIds = finalAttributeValues.stream()
                .filter(attributeValue -> attributeValue instanceof GroupAware)
                .map(value -> ((GroupAware) value).awaredGroupIds())
                .flatMap(Collection::stream)
                .collect(toImmutableSet());

        Set<String> memberIds = finalAttributeValues.stream()
                .filter(attributeValue -> attributeValue instanceof MemberAware)
                .map(value -> ((MemberAware) value).awaredMemberIds())
                .flatMap(Collection::stream)
                .collect(toImmutableSet());

        Map<String, String> groupFullNames = groupRepository.cachedGroupFullNamesOf(app.getId(), groupIds);
        Map<String, MemberReference> memberDetailedReferences = memberRepository.cachedMemberReferences(app.getTenantId(), memberIds);

        QrReferenceContext referenceContext = QrReferenceContext.builder()
                .app(app)
                .memberReferences(memberDetailedReferences)
                .groupFullNames(groupFullNames)
                .build();

        return finalAttributeValues.stream()
                .map(attributeValue -> attributeValue.toDisplayValue(referenceContext))
                .filter(Objects::nonNull)
                .collect(toImmutableMap(DisplayValue::getKey, identity()));

    }
}
