package com.mryqr.core.qr.query.plate;

import com.google.common.collect.ImmutableMap;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppRepository;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.plate.control.PlateControl;
import com.mryqr.core.group.domain.GroupAware;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.member.domain.MemberAware;
import com.mryqr.core.member.domain.MemberReference;
import com.mryqr.core.member.domain.MemberRepository;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrReferenceContext;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.app.domain.attribute.AttributeType.FIXED;
import static java.util.function.Function.identity;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@RequiredArgsConstructor
public class QrPlateAttributeValueQueryService {
    private final QrRepository qrRepository;
    private final AppRepository appRepository;
    private final MemberRepository memberRepository;
    private final GroupRepository groupRepository;
    private final MryRateLimiter mryRateLimiter;

    public Map<String, Map<String, String>> fetchQrPlateAttributeValues(ListPlateAttributeValuesQuery queryCommand, User user) {
        mryRateLimiter.applyFor(user.getTenantId(), "QR:FetchPlateAttributeValues", 20);

        App app = appRepository.cachedByIdAndCheckTenantShip(queryCommand.getAppId(), user);
        List<QR> qrs = qrRepository.byIdsAllAndCheckTenantShip(queryCommand.getQrIds(), user);
        return fetchQrPlateAttributeValues(app, qrs);
    }

    public Map<String, String> fetchQrPlateAttributeValues(App app, QR qr) {
        Map<String, Map<String, String>> attributeValues = fetchQrPlateAttributeValues(app, List.of(qr));
        Map<String, String> qrAttributeValues = attributeValues.get(qr.getId());
        return qrAttributeValues == null ? Map.of() : qrAttributeValues;
    }

    private Map<String, Map<String, String>> fetchQrPlateAttributeValues(App app, List<QR> qrs) {
        List<PlateControl> plateControls = app.plateSetting().getControls();
        Set<String> plateReferencedAttributeIds = plateControls.stream().map(PlateControl::referencedAttributeIds)
                .flatMap(Collection::stream)
                .collect(toImmutableSet());

        if (isEmpty(plateReferencedAttributeIds)) {
            return ImmutableMap.of();
        }

        List<Attribute> plateReferencedAttributes = app.allAttributes().stream()
                .filter(attribute -> plateReferencedAttributeIds.contains(attribute.getId()))
                .collect(toImmutableList());

        QrReferenceContext referenceContext = buildReferenceContext(plateReferencedAttributeIds, qrs, app);
        Map<String, Control> allControls = app.allControls().stream().collect(toImmutableMap(Control::getId, identity()));

        Map<String, Map<String, String>> results = new HashMap<>();
        qrs.forEach(qr -> {
            Map<String, String> qrAttributeMap = new HashMap<>();
            plateReferencedAttributes.forEach(attribute -> {
                qrAttributeMap.put(attribute.getId(), calculatePlateAttributeValue(attribute, qr, allControls, referenceContext));
            });
            results.put(qr.getId(), qrAttributeMap);
        });

        return results;
    }

    private QrReferenceContext buildReferenceContext(Set<String> plateReferencedAttributeIds, List<QR> qrs, App app) {
        Set<String> referencedMemberIds = new HashSet<>();
        Set<String> referencedGroupIds = new HashSet<>();

        qrs.forEach(qr -> {
            List<AttributeValue> referencedAttributeValues = qr.getAttributeValues().values().stream()
                    .filter(value -> plateReferencedAttributeIds.contains(value.getAttributeId()))
                    .collect(toImmutableList());

            referencedMemberIds.addAll(referencedAttributeValues.stream()
                    .filter(value -> value instanceof MemberAware)
                    .map(value -> ((MemberAware) value).awaredMemberIds())
                    .flatMap(Collection::stream)
                    .collect(toImmutableSet()));

            referencedGroupIds.addAll(referencedAttributeValues.stream()
                    .filter(value -> value instanceof GroupAware)
                    .map(value -> ((GroupAware) value).awaredGroupIds())
                    .flatMap(Collection::stream)
                    .collect(toImmutableSet()));
        });

        Map<String, MemberReference> memberReferences = memberRepository.cachedMemberReferences(app.getTenantId(), referencedMemberIds);
        Map<String, String> groupFullNames = groupRepository.cachedGroupFullNamesOf(app.getId(), referencedGroupIds);

        return QrReferenceContext.builder()
                .app(app)
                .memberReferences(memberReferences)
                .groupFullNames(groupFullNames)
                .build();
    }

    private String calculatePlateAttributeValue(Attribute attribute,
                                                QR qr,
                                                Map<String, Control> allControls,
                                                QrReferenceContext context) {
        if (attribute.getType() == FIXED) {
            return attribute.getFixedValue();
        }

        AttributeValue attributeValue = qr.getAttributeValues().get(attribute.getId());
        if (attributeValue != null) {
            Control control = isNotBlank(attribute.getControlId()) ? allControls.get(attribute.getControlId()) : null;
            return attributeValue.toExportValue(attribute, context, control);
        }

        return null;
    }

}
