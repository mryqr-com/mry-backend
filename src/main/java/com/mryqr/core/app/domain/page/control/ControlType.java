package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.common.domain.ValueType;

import static com.mryqr.core.common.domain.ValueType.ADDRESS_VALUE;
import static com.mryqr.core.common.domain.ValueType.CHECKBOX_VALUE;
import static com.mryqr.core.common.domain.ValueType.DOUBLE_VALUE;
import static com.mryqr.core.common.domain.ValueType.DROPDOWN_VALUE;
import static com.mryqr.core.common.domain.ValueType.EMAIL_VALUE;
import static com.mryqr.core.common.domain.ValueType.FILES_VALUE;
import static com.mryqr.core.common.domain.ValueType.GEOLOCATION_VALUE;
import static com.mryqr.core.common.domain.ValueType.IDENTIFIER_VALUE;
import static com.mryqr.core.common.domain.ValueType.IMAGES_VALUE;
import static com.mryqr.core.common.domain.ValueType.INTEGER_VALUE;
import static com.mryqr.core.common.domain.ValueType.ITEM_COUNT_VALUE;
import static com.mryqr.core.common.domain.ValueType.ITEM_STATUS_VALUE;
import static com.mryqr.core.common.domain.ValueType.LOCAL_DATE_VALUE;
import static com.mryqr.core.common.domain.ValueType.LOCAL_TIME_VALUE;
import static com.mryqr.core.common.domain.ValueType.MEMBERS_VALUE;
import static com.mryqr.core.common.domain.ValueType.MOBILE_VALUE;
import static com.mryqr.core.common.domain.ValueType.MULTI_LEVEL_SELECTION_VALUE;
import static com.mryqr.core.common.domain.ValueType.MULTI_LINE_TEXT_VALUE;
import static com.mryqr.core.common.domain.ValueType.POINT_CHECK_VALUE;
import static com.mryqr.core.common.domain.ValueType.RADIO_VALUE;
import static com.mryqr.core.common.domain.ValueType.RICH_TEXT_VALUE;
import static com.mryqr.core.common.domain.ValueType.SIGNATURE_VALUE;
import static com.mryqr.core.common.domain.ValueType.TEXT_VALUE;

public enum ControlType {
    SECTION_TITLE(false, false, null, false),
    SEPARATOR(false, false, null, false),
    PARAGRAPH(false, false, null, false),
    RICH_TEXT(false, false, null, false),
    IMAGE_CARD_LINK(false, false, null, false),
    BUTTON_PAGE_LINK(false, false, null, false),
    ICON_PAGE_LINK(false, false, null, false),
    SUBMIT_HISTORY(false, false, null, false),
    INSTANCE_LIST(false, false, null, false),
    ANSWER_REFERENCE(false, false, null, false),
    SUBMISSION_REFERENCE(false, false, null, false),
    IMAGE_VIEW(false, false, null, false),
    VIDEO_VIEW(false, false, null, false),
    ATTACHMENT_VIEW(false, false, null, false),
    ATTRIBUTE_TABLE(false, false, null, false),
    ATTRIBUTE_DASHBOARD(false, false, null, false),
    TREND(false, false, null, false),
    BAR(false, false, null, false),
    PIE(false, false, null, false),
    DOUGHNUT(false, false, null, false),
    TIME_SEGMENT(false, false, null, false),
    NUMBER_RANGE_SEGMENT(false, false, null, false),

    RADIO(true, true, RADIO_VALUE, true),
    CHECKBOX(true, true, CHECKBOX_VALUE, true),
    SINGLE_LINE_TEXT(true, true, TEXT_VALUE, true),
    MULTI_LINE_TEXT(true, true, MULTI_LINE_TEXT_VALUE, false),
    RICH_TEXT_INPUT(true, true, RICH_TEXT_VALUE, false),
    DROPDOWN(true, true, DROPDOWN_VALUE, true),
    MEMBER_SELECT(true, true, MEMBERS_VALUE, false),
    FILE_UPLOAD(true, false, FILES_VALUE, false),
    IMAGE_UPLOAD(true, false, IMAGES_VALUE, false),
    ADDRESS(true, true, ADDRESS_VALUE, false),
    GEOLOCATION(true, true, GEOLOCATION_VALUE, false),
    NUMBER_INPUT(true, true, DOUBLE_VALUE, true),
    NUMBER_RANKING(true, true, INTEGER_VALUE, true),
    MOBILE(true, true, MOBILE_VALUE, true),
    IDENTIFIER(true, true, IDENTIFIER_VALUE, true),
    PERSON_NAME(true, true, IDENTIFIER_VALUE, true),
    EMAIL(true, true, EMAIL_VALUE, true),
    DATE(true, true, LOCAL_DATE_VALUE, true),
    TIME(true, true, LOCAL_TIME_VALUE, true),
    ITEM_COUNT(true, true, ITEM_COUNT_VALUE, false),
    ITEM_STATUS(true, true, ITEM_STATUS_VALUE, true),
    POINT_CHECK(true, true, POINT_CHECK_VALUE, false),
    SIGNATURE(true, false, SIGNATURE_VALUE, false),
    MULTI_LEVEL_SELECTION(true, true, MULTI_LEVEL_SELECTION_VALUE, false);

    private final boolean fillable;//是否为填值控件
    private final boolean autoFillEligible;//answer是否可以自动填充上次提交值
    private final ValueType answerValueType;//控件对应answer的值类型
    private final boolean qrImportable;//是否可包含在实例上传excel中

    ControlType(boolean fillable,
                boolean autoFillEligible,
                ValueType answerValueType,
                boolean qrImportable) {
        this.fillable = fillable;
        this.autoFillEligible = autoFillEligible;
        this.answerValueType = answerValueType;
        this.qrImportable = qrImportable;
    }

    public boolean isFillable() {
        return fillable;
    }

    public boolean isAutoFillEligible() {
        return autoFillEligible;
    }

    public ValueType getAnswerValueType() {
        return answerValueType;
    }

    public boolean isAnswerSortable() {
        return answerValueType != null && answerValueType.isSortable();
    }

    public boolean isAnswerTextable() {
        return answerValueType != null && answerValueType.isTextable();
    }

    public boolean isAnswerIndexable() {
        return answerValueType != null && answerValueType.isIndexable();
    }

    public boolean isAnswerSearchable() {
        return answerValueType != null && answerValueType.isSearchable();
    }

    public boolean isAnswerExportable() {
        return answerValueType != null && answerValueType.isExportable();
    }

    public boolean isAnswerNumerical() {
        return answerValueType != null && answerValueType.isNumerical();
    }

    public boolean isAnswerNumbered() {
        return answerValueType != null && answerValueType.isNumbered();
    }

    public boolean isAnswerCategorized() {
        return answerValueType != null && answerValueType.isCategorized();
    }

    public boolean isQrImportable() {
        return qrImportable;
    }
}
