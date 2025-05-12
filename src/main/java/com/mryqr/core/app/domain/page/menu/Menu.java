package com.mryqr.core.app.domain.page.menu;


import com.mryqr.common.exception.MryException;
import com.mryqr.common.validation.collection.NoNullElement;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.pagelink.PageLink;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

import static com.mryqr.common.exception.ErrorCode.MENU_LINK_ID_DUPLICATED;
import static com.mryqr.common.utils.Identified.isDuplicated;
import static lombok.AccessLevel.PRIVATE;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class Menu {
    @Valid
    @NotNull
    @NoNullElement
    @Size(max = 10)
    private final List<PageLink> links;//菜单项

    private final boolean showBasedOnPermission;//是否根据权限显示或隐藏菜单项

    public void correct() {
        links.forEach(PageLink::correct);
    }

    public void validate(AppSettingContext context) {
        if (isDuplicated(links)) {
            throw new MryException(MENU_LINK_ID_DUPLICATED, "菜单项不能包含重复ID。");
        }

        links.forEach(pageLink -> pageLink.validate(context));
    }
}
