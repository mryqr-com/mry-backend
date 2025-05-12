package com.mryqr.core.job;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.common.domain.indexedfield.IndexedField;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.IntegerAttributeValue;
import com.mryqr.core.qr.job.RemoveQrRangedAttributeValuesForAllTenantsJob;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.THIS_WEEK;
import static com.mryqr.core.app.domain.attribute.AttributeType.INSTANCE_SUBMIT_COUNT;
import static com.mryqr.utils.RandomTestFixture.rAttributeName;
import static com.mryqr.utils.RandomTestFixture.rEmail;
import static com.mryqr.utils.RandomTestFixture.rPassword;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class AutoRemoveRangedAttributeValuesJobTest extends BaseApiTest {
    @Autowired
    private RemoveQrRangedAttributeValuesForAllTenantsJob job;

    @Test
    public void should_auto_remove_ranged_attribute_value_for_all_apps() {
        PreparedQrResponse response1 = setupApi.registerWithQr(rEmail(), rPassword());
        String attributeId1 = newAttributeId();
        Attribute attribute1 = Attribute.builder()
                .id(attributeId1)
                .name(rAttributeName())
                .type(INSTANCE_SUBMIT_COUNT)
                .range(THIS_WEEK)
                .build();
        AppApi.updateAppAttributes(response1.getJwt(), response1.getAppId(), attribute1);
        SubmissionApi.newSubmission(response1.getJwt(), response1.getQrId(), response1.getHomePageId());
        App app1 = appRepository.byId(response1.getAppId());
        QR qr1 = qrRepository.byId(response1.getQrId());
        IntegerAttributeValue attributeValue1 = (IntegerAttributeValue) qr1.attributeValueOf(attributeId1);
        assertEquals(1, attributeValue1.getNumber());
        IndexedField indexedField1 = app1.indexedFieldForAttributeOptional(attributeId1).get();
        assertEquals(1, qr1.getIndexedValues().valueOf(indexedField1).getSv());

        PreparedQrResponse response2 = setupApi.registerWithQr(rEmail(), rPassword());
        String attributeId2 = newAttributeId();
        Attribute attribute2 = Attribute.builder()
                .id(attributeId2)
                .name(rAttributeName())
                .type(INSTANCE_SUBMIT_COUNT)
                .range(THIS_WEEK)
                .build();
        AppApi.updateAppAttributes(response2.getJwt(), response2.getAppId(), attribute2);
        SubmissionApi.newSubmission(response2.getJwt(), response2.getQrId(), response2.getHomePageId());
        App app2 = appRepository.byId(response1.getAppId());
        QR qr2 = qrRepository.byId(response2.getQrId());
        IntegerAttributeValue attributeValue2 = (IntegerAttributeValue) qr2.attributeValueOf(attributeId2);
        assertEquals(1, attributeValue2.getNumber());
        IndexedField indexedField2 = app2.indexedFieldForAttributeOptional(attributeId1).get();
        assertEquals(1, qr1.getIndexedValues().valueOf(indexedField2).getSv());

        job.run(THIS_WEEK);

        QR updatedQr1 = qrRepository.byId(response1.getQrId());
        assertNull(updatedQr1.attributeValueOf(attributeId1));
        assertNull(updatedQr1.getIndexedValues().valueOf(indexedField1));

        QR updatedQr2 = qrRepository.byId(response2.getQrId());
        assertNull(updatedQr2.attributeValueOf(attributeId2));
        assertNull(updatedQr2.getIndexedValues().valueOf(indexedField2));
    }
}
