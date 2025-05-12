package com.mryqr.core.qr.domain;

import com.mryqr.common.domain.Geolocation;
import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.DoubleAttributeValue;
import com.mryqr.core.qr.domain.attribute.TextAttributeValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.mryqr.common.domain.ValueType.DOUBLE_VALUE;
import static com.mryqr.common.domain.ValueType.TEXT_VALUE;
import static com.mryqr.common.exception.ErrorCode.QR_WITH_CUSTOM_ID_ALREADY_EXISTS;
import static com.mryqr.common.exception.ErrorCode.QR_WITH_NAME_ALREADY_EXISTS;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static java.lang.Double.parseDouble;
import static java.util.function.Function.identity;
import static org.apache.commons.collections4.MapUtils.emptyIfNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@RequiredArgsConstructor
public class QrDomainService {
    private final QrRepository qrRepository;

    public void renameQr(QR qr, String name, App app, User user) {
        checkNameDuplication(qr, name, app);
        qr.rename(name, user);
    }

    public void updateQrBaseSetting(QR qr,
                                    App app,
                                    String name,
                                    String description,
                                    UploadedFile headerImage,
                                    Map<String, String> directAttributeValues,
                                    Geolocation geolocation,
                                    String customId,
                                    User user) {
        checkNameDuplication(qr, name, app);
        checkCustomIdDuplication(qr, customId, app);

        Set<String> providedAttributeIds = emptyIfNull(directAttributeValues).keySet();
        Map<String, AttributeValue> resultAttributeValues = app.directAttributes().stream()
                .filter(attribute -> providedAttributeIds.contains(attribute.getId()))
                .map(attribute -> buildDirectAttributeValue(attribute, directAttributeValues.get(attribute.getId())))
                .filter(Objects::nonNull)
                .collect(toImmutableMap(AttributeValue::getAttributeId, identity()));

        qr.updateBaseSetting(name, description, headerImage, resultAttributeValues, geolocation, customId, user);
    }

    public void updateQrCustomId(QR qr, String customId, App app, User user) {
        checkCustomIdDuplication(qr, customId, app);
        qr.updateCustomId(customId, user);
    }

    private void checkNameDuplication(QR qr, String name, App app) {
        if (!Objects.equals(qr.getName(), name)
            && app.notAllowDuplicateInstanceName()
            && qrRepository.existsByName(name, qr.getAppId())) {
            throw new MryException(QR_WITH_NAME_ALREADY_EXISTS,
                    "名称已被占用。",
                    mapOf("qrId", qr.getId(), "name", name));
        }
    }

    private void checkCustomIdDuplication(QR qr, String customId, App app) {
        if (isNotBlank(customId)
            && !Objects.equals(qr.getCustomId(), customId)
            && qrRepository.existsByCustomId(customId, qr.getAppId())) {
            throw new MryException(QR_WITH_CUSTOM_ID_ALREADY_EXISTS,
                    app.customIdDesignation() + "已被占用。", mapOf("qrId", qr.getId(), "customId", customId));
        }
    }

    public void updateQrDirectAttributes(QR qr, App app, Map<String, String> attributeValues, User user) {
        Set<String> providedAttributeIds = emptyIfNull(attributeValues).keySet();
        List<Attribute> directInputAttributes = app.directAttributes();

        Map<String, AttributeValue> directAttributeValues = directInputAttributes.stream()
                .filter(attribute -> providedAttributeIds.contains(attribute.getId()))
                .map(attribute -> buildDirectAttributeValue(attribute, attributeValues.get(attribute.getId())))
                .filter(Objects::nonNull)
                .collect(toImmutableMap(AttributeValue::getAttributeId, identity()));

        qr.putAttributeValues(directAttributeValues, user);
    }

    private AttributeValue buildDirectAttributeValue(Attribute attribute, String value) {
        if (attribute.getValueType() == TEXT_VALUE) {
            return new TextAttributeValue(attribute, value);
        }

        if (attribute.getValueType() == DOUBLE_VALUE) {
            try {
                return new DoubleAttributeValue(attribute, attribute.format(parseDouble(value)));
            } catch (Throwable t) {//parseDouble有可能失败
                return null;
            }
        }
        return null;
    }
}
