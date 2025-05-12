package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.report.NumberRangeSegment;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.FNumberInputControl;
import com.mryqr.core.app.domain.page.control.FSingleLineTextControl;
import com.mryqr.core.app.domain.page.control.PNumberRangeSegmentControl;
import com.mryqr.core.presentation.PresentationApi;
import com.mryqr.core.presentation.query.numberrangesegment.QNumberRangeSegmentPresentation;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.utils.PreparedAppResponse;
import com.mryqr.utils.PreparedQrResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.common.domain.report.SubmissionSegmentType.CONTROL_VALUE_MAX;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.utils.RandomTestFixture.*;
import static org.junit.jupiter.api.Assertions.*;

public class NumberRangeSegmentControlApiTest extends BaseApiTest {
    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        PNumberRangeSegmentControl control = defaultValueSegmentControlBuilder().numberRangesString("1,2,3").pageId(response.getHomePageId())
                .basedControlId(numberInputControl.getId()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, control);

        App app = appRepository.byId(response.getAppId());
        PNumberRangeSegmentControl updatedControl = (PNumberRangeSegmentControl) app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
        assertTrue(updatedControl.isComplete());
        assertEquals(newArrayList(1d, 2d, 3d), updatedControl.getNumberRanges());
    }

    @Test
    public void should_normalise_segments() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        PNumberRangeSegmentControl control = defaultValueSegmentControlBuilder().numberRangesString("1,2,whatever,3")
                .pageId(response.getHomePageId()).basedControlId(numberInputControl.getId()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, control);

        App app = appRepository.byId(response.getAppId());
        PNumberRangeSegmentControl updatedControl = (PNumberRangeSegmentControl) app.controlByIdOptional(control.getId()).get();
        assertEquals("1,2,3", updatedControl.getNumberRangesString());
    }

    @Test
    public void should_not_complete_with_no_page() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl);
        PNumberRangeSegmentControl control = defaultValueSegmentControlBuilder().numberRangesString("1,2,3")
                .basedControlId(numberInputControl.getId()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isComplete());
    }

    @Test
    public void should_not_complete_with_no_control() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl);
        PNumberRangeSegmentControl control = defaultValueSegmentControlBuilder().numberRangesString("1,2,3").pageId(response.getHomePageId())
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isComplete());
    }

    @Test
    public void should_not_complete_with_no_segments() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl);
        PNumberRangeSegmentControl control = defaultValueSegmentControlBuilder().numberRangesString(null).pageId(response.getHomePageId())
                .basedControlId(numberInputControl.getId()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isComplete());
    }

    @Test
    public void should_not_complete_with_invalid_segments() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl);
        PNumberRangeSegmentControl control = defaultValueSegmentControlBuilder().numberRangesString("1").pageId(response.getHomePageId())
                .basedControlId(numberInputControl.getId()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isComplete());
    }

    @Test
    public void should_fail_create_control_if_referenced_page_not_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl);
        PNumberRangeSegmentControl control = defaultValueSegmentControlBuilder().numberRangesString("1,2,3").pageId(Page.newPageId())
                .basedControlId(numberInputControl.getId()).build();

        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);
        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                VALIDATION_PAGE_NOT_EXIST);
    }

    @Test
    public void should_fail_create_control_if_referenced_control_not_exist() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(3).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl);
        PNumberRangeSegmentControl control = defaultValueSegmentControlBuilder().numberRangesString("1,2,3").pageId(response.getHomePageId())
                .basedControlId(Control.newControlId()).build();

        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);
        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                VALIDATION_CONTROL_NOT_EXIST);
    }

    @Test
    public void should_fail_create_control_if_referenced_control_not_supported() {
        PreparedAppResponse response = setupApi.registerWithApp();

        FSingleLineTextControl lineTextControl = defaultSingleLineTextControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), lineTextControl);
        PNumberRangeSegmentControl control = defaultValueSegmentControlBuilder().numberRangesString("1,2,3").pageId(response.getHomePageId())
                .basedControlId(lineTextControl.getId()).build();

        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);
        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                CONTROL_NOT_SUPPORT_NUMBER_RANGE_SEGMENT);
    }

    @Test
    public void should_fetch_value_segment_for_submit_count() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl numberInputControl = defaultNumberInputControlBuilder().precision(2).build();
        String allScenrioSegments = "17.55, 1.55 ,5.55 ，9.55, 13.55 ,invalid";//无序的，包含空格，中英文逗号均有，非法字符
        PNumberRangeSegmentControl control = defaultValueSegmentControlBuilder().numberRangesString(allScenrioSegments)
                .pageId(response.getHomePageId()).basedControlId(numberInputControl.getId()).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), numberInputControl, control);

        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(1d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(1.55d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(2d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(3d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(4d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(5.45d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(5.55d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(9.55d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(10d).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(numberInputControl).number(20d).build());

        QNumberRangeSegmentPresentation presentation = (QNumberRangeSegmentPresentation) PresentationApi.fetchPresentation(response.getJwt(),
                response.getQrId(), response.getHomePageId(), control.getId());
        assertEquals(newArrayList(1.55, 5.55, 9.55, 13.55, 17.55), presentation.getNumberRanges());

        List<NumberRangeSegment> counts = presentation.getSegments();
        assertEquals(4, counts.size());

        assertEquals(1.55, counts.get(0).getSegment());
        assertEquals(5, counts.get(0).getValue());

        assertEquals(5.55, counts.get(1).getSegment());
        assertEquals(1, counts.get(1).getValue());

        assertEquals(9.55, counts.get(2).getSegment());
        assertEquals(2, counts.get(2).getValue());

        assertEquals(13.55, counts.get(3).getSegment());
        assertEquals(0, counts.get(3).getValue());
    }

    @Test
    public void should_fetch_value_segment_for_numbered_control_aggregation_data() {
        PreparedQrResponse response = setupApi.registerWithQr();

        FNumberInputControl basedControl = defaultNumberInputControlBuilder().precision(2).build();
        FNumberInputControl targetControl = defaultNumberInputControlBuilder().precision(2).build();
        String allScenrioSegments = "10,20,30,40,50";
        PNumberRangeSegmentControl control = defaultValueSegmentControlBuilder()
                .segmentType(CONTROL_VALUE_MAX)
                .numberRangesString(allScenrioSegments)
                .pageId(response.getHomePageId())
                .basedControlId(basedControl.getId())
                .targetControlId(targetControl.getId())
                .build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), basedControl, targetControl, control);

        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(basedControl).number(11D).build(), rAnswerBuilder(targetControl).number(1D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(basedControl).number(12D).build(), rAnswerBuilder(targetControl).number(2D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(basedControl).number(21D).build(), rAnswerBuilder(targetControl).number(3D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(basedControl).number(22D).build(), rAnswerBuilder(targetControl).number(4D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(basedControl).number(31D).build(), rAnswerBuilder(targetControl).number(5D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(basedControl).number(32D).build(), rAnswerBuilder(targetControl).number(6D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(basedControl).number(41D).build(), rAnswerBuilder(targetControl).number(7D).build());
        SubmissionApi.newSubmission(response.getJwt(), response.getQrId(), response.getHomePageId(),
                rAnswerBuilder(basedControl).number(42D).build(), rAnswerBuilder(targetControl).number(8D).build());

        QNumberRangeSegmentPresentation presentation = (QNumberRangeSegmentPresentation) PresentationApi.fetchPresentation(response.getJwt(),
                response.getQrId(), response.getHomePageId(), control.getId());
        assertEquals(newArrayList(10D, 20D, 30D, 40D, 50D), presentation.getNumberRanges());

        List<NumberRangeSegment> counts = presentation.getSegments();
        assertEquals(4, counts.size());

        assertEquals(10, counts.get(0).getSegment());
        assertEquals(2, counts.get(0).getValue());

        assertEquals(20, counts.get(1).getSegment());
        assertEquals(4, counts.get(1).getValue());

        assertEquals(30, counts.get(2).getSegment());
        assertEquals(6, counts.get(2).getValue());

        assertEquals(40, counts.get(3).getSegment());
        assertEquals(8, counts.get(3).getValue());
    }
}
