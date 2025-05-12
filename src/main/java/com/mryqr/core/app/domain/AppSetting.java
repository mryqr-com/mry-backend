package com.mryqr.core.app.domain;

import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.circulation.CirculationStatusSetting;
import com.mryqr.core.app.domain.config.AppConfig;
import com.mryqr.core.app.domain.operationmenu.OperationMenuItem;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.app.domain.page.control.Control;
import com.mryqr.core.app.domain.page.menu.Menu;
import com.mryqr.core.app.domain.plate.PlateSetting;
import com.mryqr.core.common.domain.permission.Permission;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.common.validation.collection.NoNullElement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.Optional;

import static com.mryqr.core.common.domain.permission.Permission.minPermission;
import static com.mryqr.core.common.exception.ErrorCode.CONTROL_NOT_FOUND;
import static com.mryqr.core.common.exception.ErrorCode.PAGE_NOT_FOUND;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static com.mryqr.core.common.utils.MryConstants.MAX_PER_APP_ATTRIBUTE_SIZE;
import static com.mryqr.core.common.utils.MryConstants.MAX_PER_APP_OPERATION_MENU_SIZE;
import static com.mryqr.core.common.utils.MryConstants.MAX_PER_APP_PAGE_SIZE;
import static java.util.Optional.empty;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class AppSetting {

    @Valid
    @NotNull
    private final AppConfig config;//应用整体设置

    @Valid
    @NotNull
    private final AppTopBar appTopBar;//顶部导航栏（大屏设备）

    @Valid
    @NotNull
    @NotEmpty
    @NoNullElement
    @Size(max = MAX_PER_APP_PAGE_SIZE)
    private final List<Page> pages;//页面

    @Valid
    @NotNull
    private final Menu menu;//菜单

    @Valid
    @NotNull
    @NoNullElement
    @Size(max = MAX_PER_APP_ATTRIBUTE_SIZE)
    private final List<Attribute> attributes;//自定义属性

    @Valid
    @NotNull
    @NoNullElement
    @Size(max = MAX_PER_APP_OPERATION_MENU_SIZE)
    private final List<OperationMenuItem> operationMenuItems;//自定义运营菜单

    @Valid
    @NotNull
    private final PlateSetting plateSetting;//码牌设置

    @Valid
    @NotNull
    private final CirculationStatusSetting circulationStatusSetting;//状态流转

    public AppSettingContext context() {
        return new AppSettingContext(this);
    }

    public Permission minPagePermission() {
        Permission[] permissions = pages.stream().map(Page::requiredPermission).toArray(Permission[]::new);
        return minPermission(permissions);
    }

    public Page pageById(String pageId) {
        return pageByIdOptional(pageId)
                .orElseThrow(() -> new MryException(PAGE_NOT_FOUND, "页面未找到。", mapOf("pageId", pageId)));
    }

    public Optional<Page> pageByIdOptional(String pageId) {
        return this.pages
                .stream()
                .filter(page -> page.getId().equals(pageId))
                .findFirst();
    }

    public Control controlById(String controlId) {
        return controlByIdOptional(controlId)
                .orElseThrow(() -> new MryException(CONTROL_NOT_FOUND, "控件未找到。", mapOf("controlId", controlId)));
    }

    public Optional<Control> controlByIdOptional(String controlId) {
        if (isBlank(controlId)) {
            return empty();
        }

        return this.pages.stream()
                .flatMap(page -> page.getControls().stream())
                .filter(control -> control.getId().equals(controlId))
                .findFirst();
    }

    public Page homePage() {
        return pageById(homePageId());
    }

    public String homePageId() {
        return config.getHomePageId();
    }
}
