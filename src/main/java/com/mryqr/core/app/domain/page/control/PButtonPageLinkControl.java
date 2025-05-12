package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.AppearanceStyle;
import com.mryqr.core.app.domain.ui.BoxedTextStyle;
import com.mryqr.core.app.domain.ui.ButtonStyle;
import com.mryqr.core.app.domain.ui.pagelink.PageLink;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.common.validation.collection.NoNullElement;
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

import static com.mryqr.core.common.exception.ErrorCode.PAGE_LINK_ID_DUPLICATED;
import static com.mryqr.core.common.utils.Identified.isDuplicated;
import static lombok.AccessLevel.PRIVATE;


@Getter
@SuperBuilder
@TypeAlias("BUTTON_PAGE_LINK_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class PButtonPageLinkControl extends Control {
    public static final int MAX_LINK_SIZE = 20;

    @NotNull
    private StyleType styleType;//显示样式类型

    @Valid
    @NotNull
    @NoNullElement
    @Size(max = MAX_LINK_SIZE)
    private List<PageLink> links;//链接项

    @Valid
    @NotNull
    private ButtonStyle buttonTextStyle;//纯按钮时的样式

    @Valid
    @NotNull
    private BoxedTextStyle nameTextStyle;//卡片时名称样式

    @Valid
    @NotNull
    private BoxedTextStyle descriptionTextStyle;//卡片时的简介样式

    @Valid
    @NotNull
    private ButtonStyle cardButtonTextStyle;//卡片时的按钮样式

    @Min(1)
    @Max(4)
    private int linkPerLine;//纯按钮时的每行按钮数

    @Min(0)
    @Max(100)
    private int gutter;//纯按钮时的按钮间距

    @Min(20)
    @Max(100)
    private int linkImageSize;//卡片时的图标尺寸

    private boolean showBasedOnPermission;//是否根据权限显示或隐藏

    @Valid
    @NotNull
    private AppearanceStyle appearanceStyle;//外观样式

    @Override
    public void doCorrect(AppSettingContext context) {
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

    public enum StyleType {
        PURE_BUTTON,
        CARD_BUTTON
    }
}
