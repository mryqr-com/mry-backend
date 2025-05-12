package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.AppearanceStyle;
import com.mryqr.core.app.domain.ui.BoxedTextStyle;
import com.mryqr.core.app.domain.ui.FontStyle;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.common.validation.collection.NoBlankString;
import com.mryqr.core.common.validation.collection.NoDuplicatedString;
import com.mryqr.core.common.validation.id.attribute.AttributeId;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.Valid;
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
import static com.mryqr.core.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@SuperBuilder
@TypeAlias("ATTRIBUTE_TABLE_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class PAttributeTableControl extends Control {
    public static final int MAX_ATTRIBUTE_SIZE = 20;

    @Valid
    @NotNull
    @NoBlankString
    @NoDuplicatedString
    @Size(max = MAX_ATTRIBUTE_SIZE)
    private List<@AttributeId String> attributeIds;//所引用的自定义属性项ID

    @NotNull
    private StyleType styleType;//显示样式

    @Valid
    @NotNull
    private FontStyle keyFontStyle;//横向时属性名称样式

    @Valid
    @NotNull
    private FontStyle valueFontStyle;//横向时属性值样式

    private boolean showHeader;//是否显示表头

    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private String headerText;//表头文本

    @Valid
    @NotNull
    private FontStyle headerFontStyle;//横向时表头样式

    private boolean stripped;//横向时是否显示表格条纹

    @Valid
    @NotNull
    private BoxedTextStyle verticalKeyStyle;//竖向时字段名称样式

    @Valid
    @NotNull
    private BoxedTextStyle verticalValueStyle;//竖向时字段值样式

    @Valid
    @NotNull
    private AppearanceStyle appearanceStyle;//竖向时的外观样式

    private boolean hideControlIfNoData;//无数据时隐藏整个控件（包括标题和描述）

    @Override
    public void doCorrect(AppSettingContext context) {
        if (!this.showHeader || isBlank(this.headerText)) {
            this.headerText = "未命名表头";
        }

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

    public enum StyleType {
        HORIZONTAL_TABLE,
        VERTICAL_TABLE,
    }
}
