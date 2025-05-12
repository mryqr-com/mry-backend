package com.mryqr.core.app.control;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.control.PButtonPageLinkControl;
import com.mryqr.core.app.domain.ui.pagelink.PageLink;
import com.mryqr.utils.PreparedAppResponse;
import org.junit.jupiter.api.Test;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.common.exception.ErrorCode.PAGE_LINK_ID_DUPLICATED;
import static com.mryqr.common.exception.ErrorCode.VALIDATION_LINK_PAGE_NOT_EXIST;
import static com.mryqr.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.app.domain.ui.pagelink.PageLinkType.EXTERNAL_URL;
import static com.mryqr.core.app.domain.ui.pagelink.PageLinkType.PAGE;
import static com.mryqr.utils.RandomTestFixture.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class ButtonPageLinkControlApiTest extends BaseApiTest {

    @Test
    public void should_create_control_normally() {
        PreparedAppResponse response = setupApi.registerWithApp();

        PButtonPageLinkControl control = defaultButtonPageLinkControl();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);

        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertEquals(control, updatedControl);
    }

    @Test
    public void should_derive_complete() {
        PreparedAppResponse response = setupApi.registerWithApp();

        PageLink pageLink = PageLink.builder().id(newShortUuid()).type(PAGE).build();
        PButtonPageLinkControl control = defaultButtonPageLinkControlBuilder().links(newArrayList(pageLink)).build();
        AppApi.updateAppControls(response.getJwt(), response.getAppId(), control);
        App app = appRepository.byId(response.getAppId());
        Control updatedControl = app.controlByIdOptional(control.getId()).get();
        assertFalse(updatedControl.isComplete());
    }

    @Test
    public void should_fail_create_if_reference_non_exists_page() {
        PreparedAppResponse response = setupApi.registerWithApp();

        PageLink pageLink = PageLink.builder().id(newShortUuid()).type(PAGE).pageId(Page.newPageId()).build();
        PButtonPageLinkControl control = defaultButtonPageLinkControlBuilder().links(newArrayList(pageLink)).build();
        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                VALIDATION_LINK_PAGE_NOT_EXIST);
    }

    @Test
    public void should_fail_update_app_if_page_id_duplicated() {
        PreparedAppResponse response = setupApi.registerWithApp();

        String linkId = newShortUuid();
        PageLink link1 = PageLink.builder().id(linkId).name(rPageLinkName()).type(EXTERNAL_URL).url(rUrl()).build();
        PageLink link2 = PageLink.builder().id(linkId).name(rPageLinkName()).type(EXTERNAL_URL).url(rUrl()).build();
        PButtonPageLinkControl control = defaultButtonPageLinkControlBuilder().links(newArrayList(link1, link2)).build();

        App app = appRepository.byId(response.getAppId());
        AppSetting setting = app.getSetting();
        setting.homePage().getControls().add(control);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), response.getAppId(), app.getVersion(), setting),
                PAGE_LINK_ID_DUPLICATED);
    }
}
