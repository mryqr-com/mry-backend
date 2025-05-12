package com.mryqr.core.qr.domain.attribute.sync;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.QrRepository;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.sync.calculator.AttributeValueCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor
public class QrAttributeValueSyncer {
    private final List<AttributeValueCalculator> calculators;
    private final QrRepository qrRepository;

    @Transactional
    public void sync(QR qr, App app, List<Attribute> attributes, User user) {
        if (isEmpty(attributes)) {
            return;
        }

        //没有必要用parallelStream，因为attributes的数量是很少的，最多才20个
        //需要保证map中包含了null属性值，因为即便是null值，也应该覆盖原有值的属性值
        Map<String, AttributeValue> attributeValues = new HashMap<>();
        attributes.forEach(attribute -> attributeValues.put(attribute.getId(), calculateAttributeValue(qr, attribute, app)));

        qr.putAttributeValues(attributeValues, user);
        qrRepository.houseKeepSave(qr, app, user);
    }

    private AttributeValue calculateAttributeValue(QR qr, Attribute attribute, App app) {
        if (attribute == null) {
            return null;
        }

        if (!app.hasAttribute(attribute.getId())) {
            log.warn("Attribute[{}] not exist in App[{}].", attribute.getId(), app.getId());
            return null;
        }

        return calculators.stream()
                .filter(calculator -> calculator.canCalculate(attribute, app)).findFirst()
                .map(calculator -> calculator.calculate(attribute, qr, app))
                .orElse(null);
    }

}
