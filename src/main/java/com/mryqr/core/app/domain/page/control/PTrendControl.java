package com.mryqr.core.app.domain.page.control;


import com.mryqr.common.domain.report.ReportRange;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.validation.collection.NoNullElement;
import com.mryqr.common.validation.color.Color;
import com.mryqr.core.app.domain.AppSettingContext;
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

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.common.exception.ErrorCode.TREND_ITEM_ID_DUPLICATED;
import static com.mryqr.common.utils.Identified.isDuplicated;
import static com.mryqr.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@SuperBuilder
@TypeAlias("TREND_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class PTrendControl extends Control {
    public static final int MAX_TREND_SIZE = 2;

    public static final int MIN_POINTS = 5;
    public static final int MAX_POINTS = 500;

    @Valid
    @NotNull
    @NoNullElement
    @Size(max = MAX_TREND_SIZE)
    private List<TrendItem> trendItems;//趋势项

    @NotNull
    private ReportRange range;//统计时间范围

    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private String xTitle;

    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private String yTitle;

    private boolean bezier;//是否显示为Bezier曲线

    private boolean hideGrid;//隐藏网格线

    @Min(60)
    @Max(100)
    private int sizeRatio;//缩放比例

    private boolean hideControlIfNoData;//无数据时隐藏整个控件

    private boolean showNumber;//显示数字

    @Min(MIN_POINTS)
    @Max(MAX_POINTS)
    private int maxPoints;//最多显示的数值点数量

    @Valid
    @NotNull
    @Size(max = 2)
    private List<@Color String> colors;//线条颜色

    @Override
    public void doCorrect(AppSettingContext context) {
        trendItems.forEach(TrendItem::correct);
        this.colors = this.colors.stream().map(color -> isNotBlank(color) ? color : "#FF8C00").collect(toImmutableList());
        this.complete = trendItems.stream().anyMatch(TrendItem::isComplete);
    }

    @Override
    protected void doValidate(AppSettingContext context) {
        if (isDuplicated(trendItems)) {
            throw new MryException(TREND_ITEM_ID_DUPLICATED, "趋势图项ID不能重复。");
        }

        trendItems.forEach(item -> item.validate(context));
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }

}
