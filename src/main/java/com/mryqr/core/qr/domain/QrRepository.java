package com.mryqr.core.qr.domain;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.common.domain.indexedfield.IndexedField;
import com.mryqr.core.common.domain.indexedfield.IndexedValue;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.group.domain.Group;
import com.mryqr.core.qr.domain.attribute.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface QrRepository {

    void houseKeepSave(QR qr, App app, User user);

    AppedQr appedQrById(String qrId);

    AppedQr appedQrByIdAndCheckTenantShip(String qrId, User user);

    AppedQr appedQrByCustomIdAndCheckTenantShip(String appId, String customId, User user);

    boolean existsByName(String name, String appId);

    Map<String, String> qrNamesOf(Set<String> qrIds);

    String qrNameOf(String qrId);

    boolean existsByCustomId(String customId, String appId);

    QR byCustomId(String appId, String customId);

    Optional<QR> byCustomIdOptional(String appId, String customId);

    QR byCustomIdAndCheckTenantShip(String appId, String customId, User user);

    Optional<QR> byPlateIdOptional(String plateId);

    void save(QR it);

    void save(List<QR> qrs);

    void insert(List<QR> qrs);

    void delete(QR it);

    void delete(List<QR> qrs);

    QR byId(String id);

    Optional<QR> byIdOptional(String id);

    QR byIdAndCheckTenantShip(String id, User user);

    List<QR> byIdsAllAndCheckTenantShip(Set<String> ids, User user);

    List<QR> find(String appId, String startId, int size);

    int count(String tenantId);

    Set<String> assignmentQrIdsOf(String groupId);

    void increaseAccessCount(QR qr);

    int countQrUnderApp(String appId);

    int removeAllQrsUnderApp(String appId);

    int removeAllQrsUnderGroup(String groupId);

    int removeAttributeValuesUnderAllQrs(Set<String> attributeIds, String appId);

    int removeIndexedOptionUnderAllQrs(String optionId, IndexedField field, String appId);

    int removeIndexedValueUnderAllQrs(Set<IndexedField> fields, String appId);

    int removeIndexedValueUnderAllQrs(IndexedField field, String attributeId, String appId);

    int syncGroupActiveStatusToQrs(Group group);

    int updateAttributeValueForAllQrsUnderGroup(String groupId, AttributeValue attributeValue);

    int updateIndexValueForAllQrsUnderGroup(String groupId, IndexedField indexedField, IndexedValue indexedValue);

    Optional<QR> latestQrForTenant(String tenantId);

}
