package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.Address;
import com.mryqr.common.domain.indexedfield.IndexedField;
import com.mryqr.common.domain.indexedfield.IndexedValue;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FAddressControl;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.AddressAttributeValue;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.command.NewSubmissionCommand;
import com.mryqr.core.submission.domain.Submission;
import com.mryqr.core.submission.domain.answer.address.AddressAnswer;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.mryqr.common.domain.Address.joinAddress;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_FIRST;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.submission.SubmissionUtils.newSubmissionCommand;
import static com.mryqr.utils.RandomTestFixture.*;
import static org.junit.jupiter.api.Assertions.*;

public class AddressControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FAddressControl control = defaultAddressControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
    }

    @Test
    public void should_answer_normally() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FAddressControl control = defaultAddressControlBuilder().precision(4).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        AddressAnswer answer = rAnswer(control);
        Address address = answer.getAddress();
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForControlOptional(response.getHomePageId(), control.getId()).get();
        Submission submission = submissionRepository.byId(submissionId);
        AddressAnswer updatedAnswer = (AddressAnswer) submission.allAnswers().get(control.getId());
        assertEquals(answer, updatedAnswer);
        IndexedValue indexedValue = submission.getIndexedValues().valueOf(indexedField);
        assertEquals(control.getId(), indexedValue.getRid());
        assertTrue(indexedValue.getTv().contains(address.getProvince()));
        assertTrue(indexedValue.getTv().contains(joinAddress(address.getProvince(), address.getCity())));
        assertTrue(indexedValue.getTv().contains(joinAddress(address.getProvince(), address.getCity(), address.getDistrict())));
    }


    @Test
    public void should_answer_with_extra_address_field_for_precision_1() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FAddressControl control = defaultAddressControlBuilder().precision(1).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        AddressAnswer answer = rAnswer(control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        Submission submission = submissionRepository.byId(submissionId);
        AddressAnswer loadedAnswer = (AddressAnswer) submission.allAnswers().get(control.getId());
        assertNotNull(loadedAnswer.getAddress().getProvince());
        assertNull(loadedAnswer.getAddress().getCity());
        assertNull(loadedAnswer.getAddress().getDistrict());
        assertNull(loadedAnswer.getAddress().getAddress());
    }

    @Test
    public void should_answer_with_extra_address_field_for_precision_2() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FAddressControl control = defaultAddressControlBuilder().precision(2).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        AddressAnswer answer = rAnswer(control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        Submission submission = submissionRepository.byId(submissionId);
        AddressAnswer loadedAnswer = (AddressAnswer) submission.allAnswers().get(control.getId());
        assertNotNull(loadedAnswer.getAddress().getProvince());
        assertNotNull(loadedAnswer.getAddress().getCity());
        assertNull(loadedAnswer.getAddress().getDistrict());
        assertNull(loadedAnswer.getAddress().getAddress());
    }

    @Test
    public void should_answer_with_extra_address_field_for_precision_3() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FAddressControl control = defaultAddressControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        AddressAnswer answer = rAnswer(control);
        String submissionId = SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        Submission submission = submissionRepository.byId(submissionId);
        AddressAnswer loadedAnswer = (AddressAnswer) submission.allAnswers().get(control.getId());
        assertNotNull(loadedAnswer.getAddress().getProvince());
        assertNotNull(loadedAnswer.getAddress().getCity());
        assertNotNull(loadedAnswer.getAddress().getDistrict());
        assertNull(loadedAnswer.getAddress().getAddress());
    }

    @Test
    public void should_fail_answer_for_incomplete_province_no_matter_mandatory_or_not() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FAddressControl control = defaultAddressControlBuilder().precision(4).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        AddressAnswer answer = rAnswerBuilder(control).address(Address.builder().city(rAddress().getCity()).build()).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), PROVINCE_NOT_PROVIDED);
    }

    @Test
    public void should_fail_answer_for_incomplete_city_no_matter_mandatory_or_not() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FAddressControl control = defaultAddressControlBuilder().precision(4).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        AddressAnswer answer = rAnswerBuilder(control).address(Address.builder().province(rAddress().getProvince()).build()).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), CITY_NOT_PROVIDED);
    }

    @Test
    public void should_fail_answer_for_incomplete_district_no_matter_mandatory_or_not() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FAddressControl control = defaultAddressControlBuilder().precision(4).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        Address address = rAddress();
        AddressAnswer answer = rAnswerBuilder(control).address(Address.builder()
                .province(address.getProvince())
                .city(address.getCity())
                .build()).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), DISTRICT_NOT_PROVIDED);
    }

    @Test
    public void should_fail_answer_for_incomplete_detailed_address_no_matter_mandatory_or_not() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FAddressControl control = defaultAddressControlBuilder().precision(4).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        Address address = rAddress();
        AddressAnswer answer = rAnswerBuilder(control).address(Address.builder()
                .province(address.getProvince())
                .city(address.getCity())
                .district(address.getDistrict())
                .build()).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), DETAIL_ADDRESS_NOT_PROVIDED);
    }

    @Test
    public void should_fail_answer_if_not_filled_for_mandatory() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FAddressControl control = defaultAddressControlBuilder().fillableSetting(defaultFillableSettingBuilder().mandatory(true).build()).precision(2).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        AddressAnswer answer = rAnswerBuilder(control).address(Address.builder().build()).build();
        NewSubmissionCommand command = newSubmissionCommand(response.getQrId(), response.getHomePageId(), answer);

        assertError(() -> SubmissionApi.newSubmissionRaw(response.getJwt(), command), MANDATORY_ANSWER_REQUIRED);
    }

    @Test
    public void should_calculate_first_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FAddressControl control = defaultAddressControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_FIRST).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        AddressAnswer answer = rAnswer(control);
        Address address = answer.getAddress();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        QR qr = qrRepository.byId(response.getQrId());
        AddressAttributeValue attributeValue = (AddressAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(address, attributeValue.getAddress());
        Set<String> textValues = qr.getIndexedValues().valueOf(indexedField).getTv();
        assertTrue(textValues.contains(address.getProvince()));
        assertTrue(textValues.contains(joinAddress(address.getProvince(), address.getCity())));
        assertTrue(textValues.contains(joinAddress(address.getProvince(), address.getCity(), address.getDistrict())));
    }

    @Test
    public void should_calculate_last_submission_answer_as_attribute_value() {
        PreparedQrResponse response = setupApi.registerWithQr();
        FAddressControl control = defaultAddressControlBuilder().precision(4).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        AddressAnswer answer = rAnswer(control);
        Address address = answer.getAddress();
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), rAnswer(control));
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(), answer);

        App app = appRepository.byId(response.getAppId());
        IndexedField indexedField = app.indexedFieldForAttributeOptional(attribute.getId()).get();
        QR qr = qrRepository.byId(response.getQrId());
        AddressAttributeValue attributeValue = (AddressAttributeValue) qr.getAttributeValues().get(attribute.getId());
        assertEquals(address, attributeValue.getAddress());
        Set<String> textValues = qr.getIndexedValues().valueOf(indexedField).getTv();
        assertTrue(textValues.contains(address.getProvince()));
        assertTrue(textValues.contains(joinAddress(address.getProvince(), address.getCity())));
        assertTrue(textValues.contains(joinAddress(address.getProvince(), address.getCity(), address.getDistrict())));
    }

}
