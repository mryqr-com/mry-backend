package com.mryqr.core.qr.domain.attribute.sync.calculator;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.group.domain.GroupRepository;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.AttributeValue;
import com.mryqr.core.qr.domain.attribute.MembersAttributeValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_GROUP_MANAGERS;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Component
@RequiredArgsConstructor
public class InstanceGroupManagersCalculator implements AttributeValueCalculator {
    private final GroupRepository groupRepository;

    @Override
    public boolean canCalculate(Attribute attribute, App app) {
        return attribute.getType() == INSTANCE_GROUP_MANAGERS;
    }

    @Override
    public AttributeValue calculate(Attribute attribute, QR qr, App app) {
        Group group = groupRepository.cachedById(qr.getGroupId());
        List<String> managerIds = group.allManagerIds();
        return isNotEmpty(managerIds) ? new MembersAttributeValue(attribute, managerIds) : null;
    }
}
