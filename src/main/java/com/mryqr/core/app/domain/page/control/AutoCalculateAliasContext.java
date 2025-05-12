package com.mryqr.core.app.domain.page.control;

import com.google.common.collect.ImmutableMap;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.utils.Identified;
import com.mryqr.common.validation.collection.NoNullElement;
import com.mryqr.common.validation.id.control.ControlId;
import com.mryqr.common.validation.id.shoruuid.ShortUuid;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.page.Page;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.Identified.isDuplicated;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.common.utils.MryRegexConstants.CONTROL_ALIAS_PATTERN;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class AutoCalculateAliasContext {
    @Valid
    @NotNull
    @NoNullElement
    @Size(max = 20)
    private final List<ControlAlias> controlAliases;

    public boolean hasAlias() {
        return isNotEmpty(controlAliases);
    }

    public void validate(String referencingControlId, AppSettingContext context) {
        if (isEmpty(controlAliases)) {
            return;
        }

        if (isDuplicated(controlAliases)) {
            throw new MryException(ALIAS_ID_DUPLICATED, "变量别名ID不能重复。");
        }

        Page page = context.pageForControl(referencingControlId);
        Set<String> allSamePagedControlIds = page.getControls().stream().map(Control::getId).collect(toImmutableSet());

        controlAliases.forEach(alias -> {
            if (!allSamePagedControlIds.contains(alias.getControlId())) {
                throw new MryException(VALIDATION_CONTROL_NOT_EXIST, "自动计算所引用控件不存在。",
                        mapOf("pageId", page.getId(), "controlId", alias.getControlId()));
            }

            if (!context.controlById(alias.getControlId()).isAnswerNumerical()) {
                throw new MryException(CONTROL_NOT_NUMERICAL_VALUED, "自动计算所引用控件无法提供有效数值。",
                        mapOf("controlId", alias.getControlId()));
            }

            if (referencingControlId.equals(alias.getControlId())) {
                throw new MryException(CONTROL_SHOULD_NOT_SELF, "控件不能引用自身。",
                        mapOf("controlId", alias.getControlId()));
            }
        });
    }

    public Map<String, Double> buildVariables(Map<String, Answer> answerMap, Map<String, Control> controlMap) {
        return controlAliases.stream().map((Function<ControlAlias, Map<String, Double>>) controlAlias -> {
                    String controlId = controlAlias.getControlId();
                    Answer aliasAnswer = answerMap.get(controlId);
                    Control aliasControl = controlMap.get(controlId);
                    Double value = aliasAnswer.calculateNumericalValue(aliasControl);
                    return value != null ? Map.of(controlAlias.getAlias(), value) : ImmutableMap.of();
                }).flatMap(map -> map.entrySet().stream())
                .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Set<String> allControlIds() {
        return controlAliases.stream().map(ControlAlias::getControlId).collect(toImmutableSet());
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    public static class ControlAlias implements Identified {
        @NotBlank
        @ShortUuid
        private final String id;

        @NotBlank
        @ControlId
        private final String controlId;

        @NotBlank
        @Pattern(regexp = CONTROL_ALIAS_PATTERN, message = "变量名格式错误")
        private final String alias;

        @Override
        public String getIdentifier() {
            return id;
        }
    }

}
