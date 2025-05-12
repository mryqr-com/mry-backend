package com.mryqr.core.app.domain.page.control;

import com.mryqr.common.exception.MryException;
import com.mryqr.common.validation.nospace.NoSpace;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.MinMaxSetting;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.numberinput.NumberInputAnswer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.TypeAlias;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.MathExpressionEvaluator.evalDouble;
import static com.mryqr.common.utils.MryConstants.MAX_PLACEHOLDER_LENGTH;
import static java.lang.Double.parseDouble;
import static java.lang.Double.valueOf;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.MapUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Getter
@SuperBuilder
@TypeAlias("NUMBER_INPUT_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FNumberInputControl extends Control {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#");

    static {
        DECIMAL_FORMAT.setMaximumFractionDigits(3);
    }

    public static final double MIN_NUMBER = -1000000000;
    public static final double MAX_NUMBER = 1000000000;

    public static final int MIN_PRECISION = 0;
    public static final int MAX_PRECISION = 3;

    @Size(max = MAX_PLACEHOLDER_LENGTH)
    private String placeholder;//占位符

    @Min(MIN_PRECISION)
    @Max(MAX_PRECISION)
    private int precision;//精度

    @Valid
    @NotNull
    private MinMaxSetting minMaxSetting;//最大值和最小值设置

    @NoSpace
    @Size(max = 10)
    private String suffix;//后缀

    private boolean autoCalculateEnabled;//是否启用自动计算

    @EqualsAndHashCode.Exclude
    private boolean shouldAutoCalculate;

    @Valid
    @NotNull
    private AutoCalculateSetting autoCalculateSetting;//自动计算设置

    @Override
    protected void doCorrect(AppSettingContext context) {
        if (autoCalculateEnabled) {
            setAutoFill(false);
            setMandatory(false);
            this.shouldAutoCalculate = autoCalculateSetting.shouldAutoCalculate();
        } else {
            this.shouldAutoCalculate = false;
            this.autoCalculateSetting = AutoCalculateSetting.builder()
                    .aliasContext(AutoCalculateAliasContext.builder().controlAliases(List.of()).build())
                    .build();
        }
    }

    @Override
    protected void doValidate(AppSettingContext context) {
        minMaxSetting.validate();

        if (minMaxSetting.getMax() > MAX_NUMBER) {
            throw new MryException(MAX_OVERFLOW, "数字超出限制。");
        }

        if (minMaxSetting.getMin() < MIN_NUMBER) {
            throw new MryException(MIN_OVERFLOW, "未达到最小数。");
        }

        if (autoCalculateEnabled) {
            autoCalculateSetting.validate(this.getId(), context);
        }
    }

    public NumberInputAnswer check(NumberInputAnswer answer) {
        if (answer.getNumber() > minMaxSetting.getMax()) {
            failAnswerValidation(MAX_INPUT_NUMBER_REACHED, "数字大于最大限制。");
        }

        if (answer.isFilled() && answer.getNumber() < minMaxSetting.getMin()) {
            failAnswerValidation(MIN_INPUT_NUMBER_REACHED, "数字小于最小限制。");
        }

        String numberString = DECIMAL_FORMAT.format(answer.getNumber());
        int decimalDigits = 0;
        if (numberString.contains(".")) {
            String decimalString = numberString.split("\\.")[1];
            decimalDigits = decimalString.length();
        }

        if (precision == 0 && decimalDigits != 0) {
            failAnswerValidation(INCORRECT_INTEGER_PRECISION, "只允许整数。");
        }

        if (precision > 0 && decimalDigits > precision) {
            failAnswerValidation(INCORRECT_NUMBER_INPUT_PRECISION, "精度不能超过允许值。");
        }

        return answer;
    }

    public boolean shouldAutoCalculate() {
        return this.shouldAutoCalculate;
    }

    public Optional<Double> autoCalculate(Map<String, Answer> answerMap, Map<String, Control> controlMap) {
        if (!shouldAutoCalculate) {
            return Optional.empty();
        }

        try {
            Map<String, Double> variables = autoCalculateSetting.buildVariables(answerMap, controlMap);

            if (isEmpty(variables)) {
                return Optional.empty();
            }

            Double originalValue = evalDouble(autoCalculateSetting.getExpression(), variables);
            return Optional.ofNullable(format(originalValue));
        } catch (Throwable t) {
            log.warn("Error while parsing expression[{}]: {}", autoCalculateSetting.getExpression(), t.getMessage());
            return Optional.empty();
        }
    }

    public Set<String> autoCalculateDependentControlIds() {
        return autoCalculateSetting.getAliasContext().allControlIds();
    }

    public Double format(Double number) {
        return number == null ? null : valueOf(String.format("%." + precision + "f", number));
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        try {
            double theValue = parseDouble(value);
            return NumberInputAnswer.answerBuilder(this).number(format(theValue)).build();
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Getter
    @Builder
    @EqualsAndHashCode
    @AllArgsConstructor(access = PRIVATE)
    public static class AutoCalculateSetting {
        @Valid
        @NotNull
        private final AutoCalculateAliasContext aliasContext;

        @Size(max = 200)
        private final String expression;

        public boolean shouldAutoCalculate() {
            return aliasContext.hasAlias() && isNotBlank(expression);
        }

        public Map<String, Double> buildVariables(Map<String, Answer> answerMap, Map<String, Control> controlMap) {
            return aliasContext.buildVariables(answerMap, controlMap);
        }

        public void validate(String referencingControlId, AppSettingContext context) {
            this.aliasContext.validate(referencingControlId, context);
        }
    }
}
