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
import com.mryqr.utils.PreparedAppResponse;
import org.junit.jupiter.api.Test;

import static com.mryqr.core.app.domain.attribute.Attribute.newAttributeId;
import static com.mryqr.core.app.domain.attribute.AttributeStatisticRange.NO_LIMIT;
import static com.mryqr.core.app.domain.attribute.AttributeType.CONTROL_LAST;
import static com.mryqr.core.common.exception.ErrorCode.KANBAN_NOT_ALLOWED;
import static com.mryqr.core.plan.domain.PlanType.FLAGSHIP;
import static com.mryqr.utils.RandomTestFixture.defaultCheckboxControl;
import static com.mryqr.utils.RandomTestFixture.defaultItemStatusControl;
import static com.mryqr.utils.RandomTestFixture.rAnswerBuilder;
import static com.mryqr.utils.RandomTestFixture.rAttributeName;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KanbanControllerApiTest extends BaseApiTest {

    @Test
    public void should_fetch_kanban() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        FItemStatusControl control = defaultItemStatusControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        CreateQrResponse qrResponse1 = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        CreateQrResponse qrResponse2 = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        CreateQrResponse qrResponse3 = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());

        ItemStatusAnswer answer1 = rAnswerBuilder(control).optionId(control.getOptions().get(0).getId()).build();
        ItemStatusAnswer answer2 = rAnswerBuilder(control).optionId(control.getOptions().get(1).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), qrResponse1.getQrId(), response.getHomePageId(), answer1);
        SubmissionApi.newSubmission(response.getJwt(), qrResponse2.getQrId(), response.getHomePageId(), answer2);
        SubmissionApi.newSubmission(response.getJwt(), qrResponse3.getQrId(), response.getHomePageId(), answer2);

        QAttributeKanban qAttributeKanban = KanbanApi.fetchKanban(response.getJwt(), FetchKanbanQuery.builder().appId(response.getAppId()).attributeId(attribute.getId()).build());
        QAttributeOptionCount option1Count = qAttributeKanban.getCounts().stream().filter(qAttributeOptionCount -> qAttributeOptionCount.getOptionId().equals(answer1.getOptionId())).findFirst().get();
        assertEquals(1, option1Count.getCount());

        QAttributeOptionCount option2Count = qAttributeKanban.getCounts().stream().filter(qAttributeOptionCount -> qAttributeOptionCount.getOptionId().equals(answer2.getOptionId())).findFirst().get();
        assertEquals(2, option2Count.getCount());
    }

    @Test
    public void should_fetch_kanban_with_sub_groups() {
        PreparedAppResponse response = setupApi.registerWithApp();
        setupApi.updateTenantPackages(response.getTenantId(), FLAGSHIP);
        FItemStatusControl control = defaultItemStatusControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);
        String subGroupId = GroupApi.createGroupWithParent(response.getJwt(), response.getAppId(), response.getDefaultGroupId());

        CreateQrResponse qrResponse1 = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        CreateQrResponse qrResponse2 = QrApi.createQr(response.getJwt(), response.getDefaultGroupId());
        CreateQrResponse qrResponse3 = QrApi.createQr(response.getJwt(), subGroupId);

        ItemStatusAnswer answer1 = rAnswerBuilder(control).optionId(control.getOptions().get(0).getId()).build();
        ItemStatusAnswer answer2 = rAnswerBuilder(control).optionId(control.getOptions().get(1).getId()).build();
        SubmissionApi.newSubmission(response.getJwt(), qrResponse1.getQrId(), response.getHomePageId(), answer1);
        SubmissionApi.newSubmission(response.getJwt(), qrResponse2.getQrId(), response.getHomePageId(), answer2);
        SubmissionApi.newSubmission(response.getJwt(), qrResponse3.getQrId(), response.getHomePageId(), answer2);

        QAttributeKanban qAttributeKanban = KanbanApi.fetchKanban(response.getJwt(), FetchKanbanQuery.builder().appId(response.getAppId()).groupId(response.getDefaultGroupId()).attributeId(attribute.getId()).build());
        QAttributeOptionCount option1Count = qAttributeKanban.getCounts().stream().filter(qAttributeOptionCount -> qAttributeOptionCount.getOptionId().equals(answer1.getOptionId())).findFirst().get();
        assertEquals(1, option1Count.getCount());

        QAttributeOptionCount option2Count = qAttributeKanban.getCounts().stream().filter(qAttributeOptionCount -> qAttributeOptionCount.getOptionId().equals(answer2.getOptionId())).findFirst().get();
        assertEquals(2, option2Count.getCount());
    }

    @Test
    public void should_fail_fetch_kanban_if_plan_not_allowed() {
        PreparedAppResponse response = setupApi.registerWithApp();
        FCheckboxControl control = defaultCheckboxControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        Attribute attribute = Attribute.builder().name(rAttributeName()).id(newAttributeId()).type(CONTROL_LAST).pageId(response.getHomePageId()).controlId(control.getId()).range(NO_LIMIT).build();
        AppApi.updateAppAttributes(response.getJwt(), response.getAppId(), attribute);

        assertError(() -> KanbanApi.fetchKanbanRaw(response.getJwt(), FetchKanbanQuery.builder().appId(response.getAppId()).attributeId(attribute.getId()).build()), KANBAN_NOT_ALLOWED);
    }

}