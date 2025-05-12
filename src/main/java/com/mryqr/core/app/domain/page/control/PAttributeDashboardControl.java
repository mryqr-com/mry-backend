package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.AppearanceStyle;
import com.mryqr.core.app.domain.ui.BoxedTextStyle;
import com.mryqr.core.app.domain.ui.VerticalPosition;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.common.validation.collection.NoBlankString;
import com.mryqr.core.common.validation.collection.NoDuplicatedString;
import com.mryqr.core.common.validation.id.attribute.AttributeId;
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

import static com.mryqr.core.common.exception.ErrorCode.VALIDATION_ATTRIBUTE_NOT_EXIST;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Getter
@SuperBuilder
@TypeAlias("ATTRIBUTE_DASHBOARD_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class PAttributeDashboardControl extends Control {
    public static final int MAX_DASHBOARD_SIZE = 20;

    @Valid
    @NotNull
    @NoBlankString
    @NoDuplicatedString
    @Size(max = MAX_DASHBOARD_SIZE)
    private List<@AttributeId String> attributeIds;//所引用的自定义属性列表

    @Min(1)
    @Max(4)
    private int itemsPerLine;//每行显示项目数

    @Min(0)
    @Max(50)
    private int gutter;//间距

    @NotNull
    private VerticalPosition itemTitlePosition;//单项抬头位置

    @Valid
    @NotNull
    private AppearanceStyle appearanceStyle;//外观样式

    @Valid
    @NotNull
    private BoxedTextStyle titleStyle;//抬头样式

    @Valid
    @NotNull
    private BoxedTextStyle contentStyle;//内容样式

    private boolean hideControlIfNoData;

    @Override
    public void doCorrect(AppSettingContext context) {
        this.complete = isNotEmpty(attributeIds);
    }

    @Override
    protected void doValidate(AppSettingContext context) {
        this.attributeIds.forEach(attributeId -> {
            if (context.attributeNotExists(attributeId)) {
                throw new MryException(VALIDATION_ATTRIBUTE_NOT_EXIST, "控件引用了不存在的属性项。",
                        mapOf("controlId", this.getId(), "attributeId", attributeId));
            }
        });
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }

}
