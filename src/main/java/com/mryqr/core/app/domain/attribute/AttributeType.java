package com.mryqr.core.app.domain.attribute;

import com.mryqr.common.domain.ValueType;

import static com.mryqr.common.domain.ValueType.*;

public enum AttributeType {
    FIXED(TEXT_VALUE, false, false, false, false),
    DIRECT_INPUT(null, false, false, false, false),

    //instance related
    INSTANCE_NAME(TEXT_VALUE, false, false, false, false),
    INSTANCE_ACTIVE_STATUS(BOOLEAN_VALUE, false, false, false, false),
    INSTANCE_CIRCULATION_STATUS(CIRCULATION_STATUS_VALUE, true, false, false, false),
    INSTANCE_TEMPLATE_STATUS(BOOLEAN_VALUE, false, false, false, false),
    INSTANCE_PLATE_ID(IDENTIFIER_VALUE, false, false, false, false),
    INSTANCE_CUSTOM_ID(IDENTIFIER_VALUE, false, false, false, false),
    INSTANCE_GEOLOCATION(GEOLOCATION_VALUE, false, false, false, false),
    INSTANCE_CREATE_TIME(TIMESTAMP_VALUE, false, false, false, false),
    INSTANCE_CREATE_DATE(LOCAL_DATE_VALUE, false, false, false, false),
    INSTANCE_CREATOR(MEMBER_VALUE, false, false, false, false),
    INSTANCE_CREATOR_AND_MOBILE(MEMBER_MOBILE_VALUE, false, false, false, false),
    INSTANCE_CREATOR_AND_EMAIL(MEMBER_EMAIL_VALUE, false, false, false, false),
    INSTANCE_SUBMIT_COUNT(INTEGER_VALUE, true, true, false, false),
    INSTANCE_ACCESS_COUNT(INTEGER_VALUE, false, false, false, false),
    INSTANCE_GROUP(GROUP_VALUE, false, false, false, false),
    INSTANCE_GROUP_MANAGERS(MEMBERS_VALUE, false, false, false, false),
    INSTANCE_GROUP_MANAGERS_AND_MOBILE(MEMBERS_MOBILE_VALUE, false, false, false, false),
    INSTANCE_GROUP_MANAGERS_AND_EMAIL(MEMBERS_EMAIL_VALUE, false, false, false, false),

    //page related
    PAGE_SUBMIT_COUNT(INTEGER_VALUE, true, true, true, false),
    PAGE_SUBMISSION_EXISTS(BOOLEAN_VALUE, true, false, true, false),
    PAGE_FIRST_SUBMITTED_TIME(TIMESTAMP_VALUE, true, false, true, false),
    PAGE_FIRST_SUBMITTED_DATE(LOCAL_DATE_VALUE, true, false, true, false),
    PAGE_FIRST_SUBMITTER(MEMBER_VALUE, true, false, true, false),
    PAGE_FIRST_SUBMITTER_AND_MOBILE(MEMBER_MOBILE_VALUE, true, false, true, false),
    PAGE_FIRST_SUBMITTER_AND_EMAIL(MEMBER_EMAIL_VALUE, true, false, true, false),
    PAGE_LAST_SUBMITTED_TIME(TIMESTAMP_VALUE, true, false, true, false),
    PAGE_LAST_SUBMITTED_DATE(LOCAL_DATE_VALUE, true, false, true, false),
    PAGE_LAST_SUBMISSION_UPDATED_TIME(TIMESTAMP_VALUE, true, false, true, false),
    PAGE_LAST_SUBMISSION_UPDATE_DATE(LOCAL_DATE_VALUE, true, false, true, false),
    PAGE_LAST_SUBMITTER(MEMBER_VALUE, true, false, true, false),
    PAGE_LAST_SUBMISSION_UPDATER(MEMBER_VALUE, true, false, true, false),
    PAGE_LAST_SUBMITTER_AND_MOBILE(MEMBER_MOBILE_VALUE, true, false, true, false),
    PAGE_LAST_SUBMITTER_AND_EMAIL(MEMBER_EMAIL_VALUE, true, false, true, false),

    //control related
    CONTROL_SUM(null, true, true, true, true),
    CONTROL_AVERAGE(null, true, true, true, true),
    CONTROL_MAX(null, true, true, true, true),
    CONTROL_MIN(null, true, true, true, true),
    CONTROL_FIRST(null, true, false, true, true),
    CONTROL_LAST(null, true, false, true, true);

    private final ValueType valueType;//属性对应的值类型
    private final boolean submissionAware;//提交后是否需要重新计算
    private final boolean rangeAware;//每晚的批处理时是否需要重新计算
    private final boolean pageAware;//是否需要配置页面
    private final boolean controlAware;//是否需要配置控件

    AttributeType(ValueType valueType,
                  boolean submissionAware,
                  boolean rangeAware,
                  boolean pageAware,
                  boolean controlAware) {
        this.valueType = valueType;
        this.submissionAware = submissionAware;
        this.rangeAware = rangeAware;
        this.pageAware = pageAware;
        this.controlAware = controlAware;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public boolean isRangeAware() {
        return rangeAware;
    }

    public boolean isSubmissionAware() {
        return submissionAware;
    }

    public boolean isPageAware() {
        return pageAware;
    }

    public boolean isControlAware() {
        return controlAware;
    }

    public final boolean isValueCalculated() {
        return this != FIXED && this != DIRECT_INPUT;
    }
}
