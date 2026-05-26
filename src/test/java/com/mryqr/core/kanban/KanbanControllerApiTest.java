package com.mryqr.core.kanban;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.page.control.FCheckboxControl;
import com.mryqr.core.app.domain.page.control.FItemStatusControl;
import com.mryqr.core.group.GroupApi;
import com.mryqr.core.kanban.query.FetchKanbanQuery;
import com.mryqr.core.kanban.query.QAttributeKanban;
import com.mryqr.core.kanban.query.QAttributeOptionCount;
import com.mryqr.core.qr.QrApi;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.answer.itemstatus.ItemStatusAnswer;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.utils.PreparedAppResponse;
import org.junit.jupiter.api.Test;

import static com.mryqr.common.exception.ErrorCode.KANBAN_NOT_ALLOWED;
import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.utils.RandomTestFixture.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KanbanControllerApiTest extends BaseApiTest {

    @Test
    public void should_fetch_kanban() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FItemStatusControl control = defaultItemStatusControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);

        CreateQrResponse qrResponse1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qrResponse2 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qrResponse3 = QrApi.createQr(response.jwt(), response.defaultGroupId());

        ItemStatusAnswer answer1 = rAnswerBuilder(control).optionId(control.getOptions().get(0).getId()).build();
        ItemStatusAnswer answer2 = rAnswerBuilder(control).optionId(control.getOptions().get(1).getId()).build();
        SubmissionApi.newSubmission(response.jwt(), qrResponse1.getQrId(), response.homePageId(), answer1);
        SubmissionApi.newSubmission(response.jwt(), qrResponse2.getQrId(), response.homePageId(), answer2);
        SubmissionApi.newSubmission(response.jwt(), qrResponse3.getQrId(), response.homePageId(), answer2);

        QAttributeKanban qAttributeKanban = KanbanApi.fetchKanban(response.jwt(),
                FetchKanbanQuery.builder().appId(response.appId()).attributeId(attribute.getId()).build());
        QAttributeOptionCount option1Count = qAttributeKanban.getCounts().stream()
                .filter(qAttributeOptionCount -> qAttributeOptionCount.getOptionId().equals(answer1.getOptionId())).findFirst().get();
        assertEquals(1, option1Count.getCount());

        QAttributeOptionCount option2Count = qAttributeKanban.getCounts().stream()
                .filter(qAttributeOptionCount -> qAttributeOptionCount.getOptionId().equals(answer2.getOptionId())).findFirst().get();
        assertEquals(2, option2Count.getCount());
    }

    @Test
    public void should_fetch_kanban_with_sub_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FItemStatusControl control = defaultItemStatusControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);
        String subGroupId = GroupApi.createGroupWithParent(response.jwt(), response.appId(), response.defaultGroupId());

        CreateQrResponse qrResponse1 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qrResponse2 = QrApi.createQr(response.jwt(), response.defaultGroupId());
        CreateQrResponse qrResponse3 = QrApi.createQr(response.jwt(), subGroupId);

        ItemStatusAnswer answer1 = rAnswerBuilder(control).optionId(control.getOptions().get(0).getId()).build();
        ItemStatusAnswer answer2 = rAnswerBuilder(control).optionId(control.getOptions().get(1).getId()).build();
        SubmissionApi.newSubmission(response.jwt(), qrResponse1.getQrId(), response.homePageId(), answer1);
        SubmissionApi.newSubmission(response.jwt(), qrResponse2.getQrId(), response.homePageId(), answer2);
        SubmissionApi.newSubmission(response.jwt(), qrResponse3.getQrId(), response.homePageId(), answer2);

        QAttributeKanban qAttributeKanban = KanbanApi.fetchKanban(response.jwt(),
                FetchKanbanQuery.builder().appId(response.appId()).groupId(response.defaultGroupId()).attributeId(attribute.getId()).build());
        QAttributeOptionCount option1Count = qAttributeKanban.getCounts().stream()
                .filter(qAttributeOptionCount -> qAttributeOptionCount.getOptionId().equals(answer1.getOptionId())).findFirst().get();
        assertEquals(1, option1Count.getCount());

        QAttributeOptionCount option2Count = qAttributeKanban.getCounts().stream()
                .filter(qAttributeOptionCount -> qAttributeOptionCount.getOptionId().equals(answer2.getOptionId())).findFirst().get();
        assertEquals(2, option2Count.getCount());
    }

    @Test
    public void should_fail_fetch_kanban_if_plan_not_allowed() {
        PreparedAppResponse response = setupApi.registerWithApp();
        FCheckboxControl control = defaultCheckboxControl();
        AppApi.updateAppControls(response.jwt(), response.appId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST)
                .pageId(response.homePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.jwt(), response.appId(), attribute);

        Tenant theTenant = tenantRepository.byId(response.tenantId());
        setupApi.updateTenantPlan(theTenant, theTenant.currentPlan().withKanbanAllowed(false));

        assertError(() -> KanbanApi.fetchKanbanRaw(response.jwt(),
                FetchKanbanQuery.builder().appId(response.appId()).attributeId(attribute.getId()).build()), KANBAN_NOT_ALLOWED);
    }
}