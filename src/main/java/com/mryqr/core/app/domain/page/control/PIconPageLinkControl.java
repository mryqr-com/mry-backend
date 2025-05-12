package com.mryqr.core.app.domain.page.control;

import com.mryqr.common.exception.MryException;
import com.mryqr.common.validation.collection.NoNullElement;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.AppearanceStyle;
import com.mryqr.core.app.domain.ui.FontStyle;
import com.mryqr.core.app.domain.ui.pagelink.PageLink;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.List;

import static com.mryqr.common.exception.ErrorCode.PAGE_LINK_ID_DUPLICATED;
import static com.mryqr.common.utils.Identified.isDuplicated;
import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@TypeAlias("ICON_PAGE_LINK_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class PIconPageLinkControl extends Control {
    public static final int MAX_LINK_SIZE = 50;

    @Valid
    @NotNull
    @NoNullElement
    @Size(max = MAX_LINK_SIZE)
    private List<PageLink> links;//链接项

    @Min(20)
    @Max(100)
    private int iconWidth;//图标边长

    @Min(3)
    @Max(4)
    private int numberPerRow;//每行图标数

    @Min(0)
    @Max(50)
    private int textIconSpace;//名称和图标之间的间距

    @Min(10)
    @Max(100)
    private int rowGutter;//行间距

    @Valid
    @NotNull
    private FontStyle fontStyle;//字体类型

    private boolean showBasedOnPermission;//是否根据权限显示或隐藏

    @Valid
    @NotNull
    private AppearanceStyle appearanceStyle;//外观样式

    @Override
    protected void doCorrect(AppSettingContext context) {
        links.forEach(PageLink::correct);
        this.complete = !links.isEmpty() && links.stream().allMatch(PageLink::isComplete);
    }

    @Override
    protected void doValidate(AppSettingContext context) {
        if (isDuplicated(links)) {
            throw new MryException(PAGE_LINK_ID_DUPLICATED, "页面链接项ID不能重复。");
        }

        links.forEach(pageLink -> pageLink.validate(context));
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }
}
