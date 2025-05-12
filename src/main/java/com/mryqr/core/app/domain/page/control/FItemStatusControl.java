package com.mryqr.core.app.domain.page.control;

import com.mryqr.common.exception.MryException;
import com.mryqr.common.utils.Identified;
import com.mryqr.common.validation.collection.NoNullElement;
import com.mryqr.common.validation.id.shoruuid.ShortUuid;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.itemstatus.ItemStatusAnswer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.TypeAlias;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.Identified.isDuplicated;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.common.utils.MathExpressionEvaluator.evalBoolean;
import static com.mryqr.common.utils.MryConstants.MAX_PLACEHOLDER_LENGTH;
import static java.util.Optional.empty;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.MapUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Getter
@SuperBuilder
@TypeAlias("ITEM_STATUS_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FItemStatusControl extends AbstractTextOptionControl {
    public static final int MIN_OPTION_SIZE = 2;
    public static final int MAX_OPTION_SIZE = 10;

    @Size(max = MAX_PLACEHOLDER_LENGTH)
    private String placeholder;//占位符

    @ShortUuid
    private String initialOptionId;//初始选项

    private boolean autoCalculateEnabled;//是否启用自动计算

    @Valid
    @NotNull
    private AutoCalculateSetting autoCalculateSetting;//自动计算设置

    @EqualsAndHashCode.Exclude
    private boolean shouldAutoCalculate;

    @Override
    public void doCorrect(AppSettingContext context) {
        correctOptions();

        if (autoCalculateEnabled) {
            setAutoFill(false);
            setMandatory(false);
            this.initialOptionId = null;
            this.shouldAutoCalculate = autoCalculateSetting.shouldAutoCalculate();
        } else {
            this.shouldAutoCalculate = false;
            this.autoCalculateSetting = AutoCalculateSetting.builder()
                    .aliasContext(AutoCalculateAliasContext.builder().controlAliases(List.of()).build())
                    .records(List.of())
                    .build();
        }
    }

    @Override
    protected void doValidate(AppSettingContext context) {
        super.validateOptions(MIN_OPTION_SIZE, MAX_OPTION_SIZE);

        Set<String> allOptionIds = allOptionIds();
        if (isNotBlank(initialOptionId) && !allOptionIds.contains(initialOptionId)) {
            throw new MryException(INITIAL_ITEM_STATUS_NOT_VALID, "初始选项不包含在选项中。");
        }

        if (autoCalculateEnabled) {
            autoCalculateSetting.validate(this.getId(), context);

            Set<String> referencedOptionIds = autoCalculateSetting.referencedOptionIds();
            if (isNotEmpty(referencedOptionIds)) {
                referencedOptionIds.forEach(optionId -> {
                    if (!allOptionIds.contains(optionId)) {
                        throw new MryException(VALIDATION_STATUS_OPTION_NOT_EXISTS, "自动计算所引用选项不存在。",
                                mapOf("optionId", optionId));
                    }
                });
            }
        }
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        String optionId = optionIdFromName(value);
        if (isBlank(optionId)) {
            return null;
        }

        return ItemStatusAnswer.answerBuilder(this).optionId(optionId).build();
    }

    public ItemStatusAnswer check(ItemStatusAnswer answer) {
        if (!allOptionIds().contains(answer.getOptionId())) {
            failAnswerValidation(ITEM_STATUS_ANSWER_NOT_IN_CONTROL, "所选状态选项不在状态列表中。");
        }

        return answer;
    }

    public boolean shouldAutoCalculate() {
        return this.shouldAutoCalculate;
    }

    public Optional<String> autoCalculate(Map<String, Answer> answerMap, Map<String, Control> controlMap) {
        if (!shouldAutoCalculate) {
            return empty();
        }

        try {
            Map<String, Double> variables = autoCalculateSetting.buildVariables(answerMap, controlMap);

            if (isEmpty(variables)) {
                return empty();
            }

            return autoCalculateSetting.getRecords().stream()
                    .filter(record -> {
                        try {
                            return evalBoolean(record.getExpression(), variables);
                        } catch (Throwable t) {
                            log.warn("Error while parsing expression[{}]: {}", record.getExpression(), t.getMessage());
                            return false;
                        }
                    })
                    .findFirst().map(AutoCalculateRecord::getOptionId);
        } catch (Throwable t) {
            log.warn("Error while doing auto calculation for control[{}].", this.getId());
            return Optional.empty();
        }
    }

    public Set<String> autoCalculateDependentControlIds() {
        return autoCalculateSetting.getAliasContext().allControlIds();
    }

    @Getter
    @Builder
    @EqualsAndHashCode
    @AllArgsConstructor(access = PRIVATE)
    public static class AutoCalculateSetting {
        @Valid
        @NotNull
        private final AutoCalculateAliasContext aliasContext;

        @Valid
        @NotNull
        @NoNullElement
        @Size(max = 20)
        private final List<AutoCalculateRecord> records;

        public boolean shouldAutoCalculate() {
            return aliasContext.hasAlias() && isNotEmpty(records);
        }

        public Set<String> referencedOptionIds() {
            return records.stream().map(AutoCalculateRecord::getOptionId).collect(toImmutableSet());
        }

        public Map<String, Double> buildVariables(Map<String, Answer> answerMap, Map<String, Control> controlMap) {
            return aliasContext.buildVariables(answerMap, controlMap);
        }

        public void validate(String referencingControlId, AppSettingContext context) {
            this.aliasContext.validate(referencingControlId, context);
            if (isDuplicated(records)) {
                throw new MryException(RECORD_ID_DUPLICATED, "表单式记录ID不能重复。");
            }
        }
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    public static class AutoCalculateRecord implements Identified {
        @NotBlank
        @ShortUuid
        private final String id;

        @NotBlank
        @Size(max = 200)
        private final String expression;

        @NotBlank
        @ShortUuid
        private final String optionId;

        @Override
        public String getIdentifier() {
            return id;
        }
    }

}
