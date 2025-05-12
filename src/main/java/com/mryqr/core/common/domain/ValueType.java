package com.mryqr.core.common.domain;

public enum ValueType {
    TEXT_VALUE(false, false, false, true, false, false, false),
    MULTI_LINE_TEXT_VALUE(false, false, false, false, false, false, false),
    RICH_TEXT_VALUE(false, false, false, false, false, false, false),
    TIMESTAMP_VALUE(true, false, false, true, false, false, false),
    LOCAL_DATE_VALUE(true, false, false, true, false, false, false),
    LOCAL_TIME_VALUE(false, false, false, true, false, false, false),
    DOUBLE_VALUE(true, false, false, true, true, true, false),
    INTEGER_VALUE(true, false, false, true, true, true, false),
    POINT_CHECK_VALUE(false, true, false, true, false, false, true),
    MOBILE_VALUE(false, true, true, true, false, false, false),
    EMAIL_VALUE(false, true, true, true, false, false, false),
    IDENTIFIER_VALUE(false, true, true, true, false, false, false),
    ADDRESS_VALUE(false, true, true, true, false, false, true),
    MULTI_LEVEL_SELECTION_VALUE(true, true, false, true, true, false, true),
    GEOLOCATION_VALUE(false, true, true, true, false, false, true),
    GROUP_VALUE(false, true, false, true, false, false, true),
    MEMBER_VALUE(false, true, false, true, false, false, false),
    MEMBER_MOBILE_VALUE(false, true, false, true, false, false, false),
    MEMBER_EMAIL_VALUE(false, true, false, true, false, false, false),
    MEMBERS_VALUE(false, true, false, true, false, false, false),
    MEMBERS_MOBILE_VALUE(false, true, false, true, false, false, false),
    MEMBERS_EMAIL_VALUE(false, true, false, true, false, false, false),
    RADIO_VALUE(false, true, false, true, true, false, true),
    CHECKBOX_VALUE(false, true, false, true, true, false, true),
    DROPDOWN_VALUE(false, true, false, true, true, false, true),
    ITEM_STATUS_VALUE(false, true, false, true, true, false, true),
    ITEM_COUNT_VALUE(false, true, false, true, true, false, false),
    FILES_VALUE(false, false, false, false, false, false, false),
    IMAGES_VALUE(false, false, false, false, false, false, false),
    SIGNATURE_VALUE(false, false, false, false, false, false, false),
    BOOLEAN_VALUE(false, true, false, true, false, false, true),
    CIRCULATION_STATUS_VALUE(false, true, false, true, false, false, true);

    private final boolean sortable;//是否支持排序索引，基于IndexedValue.sv排序
    private final boolean textable;//是否支持字符化索引，基于IndexedValue.tv进行筛选或去重等
    private final boolean searchable;//是否可文本搜索，基于QR和Submission的svs搜索
    private final boolean exportable;//是否可导出为excel
    private final boolean numerical;//是否可以表示为一个数值，无论是直接的还是赋值的，主要用于控件填值的自动计算
    private final boolean numbered;//是否表示一个直接数字，主要用于数字项统计报表和数值属性引用判断
    private final boolean categorized;//是否可分类，用于判断是否可以创建BAR/PIE等报表

    ValueType(boolean sortable,
              boolean textable,
              boolean searchable,
              boolean exportable,
              boolean numerical,
              boolean numbered,
              boolean categorized) {
        this.sortable = sortable;
        this.textable = textable;
        this.searchable = searchable;
        this.exportable = exportable;
        this.numerical = numerical;
        this.numbered = numbered;
        this.categorized = categorized;
    }

    public boolean isSortable() {
        return sortable;
    }

    public boolean isTextable() {
        return textable;
    }

    public boolean isIndexable() {
        return sortable || textable;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public boolean isExportable() {
        return exportable;
    }

    public boolean isNumerical() {
        return numerical;
    }

    public boolean isNumbered() {
        return numbered;
    }

    public boolean isCategorized() {
        return categorized;
    }

}
