package com.mryqr.core.app.menu;

import com.mryqr.BaseApiTest;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.AppSetting;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.ui.pagelink.PageLink;
import com.mryqr.utils.PreparedAppResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.mryqr.common.exception.ErrorCode.MENU_LINK_ID_DUPLICATED;
import static com.mryqr.common.exception.ErrorCode.VALIDATION_LINK_PAGE_NOT_EXIST;
import static com.mryqr.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.app.domain.ui.pagelink.PageLinkType.EXTERNAL_URL;
import static com.mryqr.core.app.domain.ui.pagelink.PageLinkType.PAGE;
import static com.mryqr.utils.RandomTestFixture.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MenuApiTest extends BaseApiTest {

    @Test
    public void update_app_setting_should_also_update_menu() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String appId = response.getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();
        Page page = setting.homePage();

        PageLink completeExternalLink = PageLink.builder().id(newShortUuid()).name(rPageLinkName()).type(EXTERNAL_URL).url(rUrl()).build();
        PageLink incompleteExternalLink = PageLink.builder().id(newShortUuid()).name(rPageLinkName()).type(EXTERNAL_URL).build();
        PageLink completePageLink = PageLink.builder().id(newShortUuid()).name(rPageLinkName()).type(PAGE).pageId(page.getId()).build();
        PageLink incompletePageLink = PageLink.builder().id(newShortUuid()).name(rPageLinkName()).type(PAGE).build();
        List<PageLink> links = newArrayList(completeExternalLink, incompleteExternalLink, completePageLink, incompletePageLink);
        setting.getMenu().getLinks().addAll(links);
        AppApi.updateAppSetting(response.getJwt(), appId, setting);

        App updatedApp = appRepository.byId(appId);
        List<PageLink> updatedLinks = updatedApp.getSetting().getMenu().getLinks();
        assertTrue(updatedLinks.get(0).isComplete());
        assertFalse(updatedLinks.get(1).isComplete());
        assertTrue(updatedLinks.get(2).isComplete());
        assertFalse(updatedLinks.get(3).isComplete());
    }

    @Test
    public void should_fail_update_app_setting_if_menu_reference_non_exists_page() {
        PreparedAppResponse response = setupApi.registerWithApp(rMobile(), rPassword());
        String appId = response.getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();

        String nonExistsPageId = Page.newPageId();
        PageLink pageLink = PageLink.builder().id(newShortUuid()).name(rPageLinkName()).type(PAGE).pageId(nonExistsPageId).build();
        setting.getMenu().getLinks().add(pageLink);

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), appId, app.getVersion(), setting), VALIDATION_LINK_PAGE_NOT_EXIST);
    }

    @Test
    public void should_fail_update_app_if_menu_id_duplicated() {
        PreparedAppResponse response = setupApi.registerWithApp();
        String appId = response.getAppId();
        App app = appRepository.byId(appId);
        AppSetting setting = app.getSetting();

        String linkId = newShortUuid();
        PageLink link1 = PageLink.builder().id(linkId).name(rPageLinkName()).type(EXTERNAL_URL).url(rUrl()).build();
        PageLink link2 = PageLink.builder().id(linkId).name(rPageLinkName()).type(EXTERNAL_URL).url(rUrl()).build();
        setting.getMenu().getLinks().addAll(newArrayList(link1, link2));

        assertError(() -> AppApi.updateAppSettingRaw(response.getJwt(), appId, app.getVersion(), setting), MENU_LINK_ID_DUPLICATED);
    }

}
