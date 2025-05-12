package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.common.domain.TextOption;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.common.validation.collection.NoNullElement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.common.exception.ErrorCode.TEXT_OPTION_ID_DUPLICATED;
import static com.mryqr.core.common.exception.ErrorCode.TEXT_OPTION_NOT_ENOUGH;
import static com.mryqr.core.common.exception.ErrorCode.TEXT_OPTION_OVERFLOW;
import static com.mryqr.core.common.utils.Identified.isDuplicated;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PROTECTED;

@Getter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PROTECTED)
public abstract class AbstractTextOptionControl extends Control {
    private static final String OPTION_NAME_SPLITTER = "[,，]";

    @Valid
    @NotNull
    @NoNullElement
    protected List<@Valid TextOption> options;//选项

    public Set<String> allOptionIds() {
        return options.stream().map(TextOption::getId).collect(toImmutableSet());
    }

    public Set<TextOptionInfo> optionsInfo() {
        return options.stream().map(option
                -> TextOptionInfo.builder()
                .controlId(this.getId())
                .controlType(this.getType())
                .optionId(option.getId())
                .build()).collect(toImmutableSet());
    }

    public String exportedValueFor(List<String> optionIds) {
        Map<String, String> optionNameMap = options.stream().collect(toImmutableMap(TextOption::getId, TextOption::getName));
        return optionIds.stream()
                .map(optionNameMap::get)
                .filter(StringUtils::isNotBlank)
                .collect(joining(", "));
    }

    public String exportedValueFor(String optionId) {
        return options.stream().filter(option -> option.getId().equals(optionId))
                .map(TextOption::getName)
                .findFirst()
                .orElse(null);
    }

    public Double numericalValueFor(List<String> optionIds) {
        Map<String, TextOption> optionMap = options.stream().collect(toImmutableMap(TextOption::getId, identity()));
        return optionIds.stream()
                .map(optionMap::get)
                .filter(Objects::nonNull)
                .distinct()
                .map(TextOption::getNumericalValue)
                .mapToDouble(Double::doubleValue).sum();
    }

    public Double numericalValueFor(String optionId) {
        return options.stream()
                .filter(option -> option.getId().equals(optionId))
                .findFirst()
                .map(TextOption::getNumericalValue)
                .orElse(null);
    }

    protected List<String> optionIdsFromNames(String value) {
        Map<String, String> nameToIdMap = options.stream()
                .collect(toMap(TextOption::getName, TextOption::getId, (oldValue, newValue) -> oldValue));

        String[] optionNames = value.split(OPTION_NAME_SPLITTER);
        return Arrays.stream(optionNames)
                .map(nameToIdMap::get)
                .filter(Objects::nonNull)
                .collect(toImmutableList());
    }

    protected String optionIdFromName(String value) {
        return this.options.stream().filter(option -> Objects.equals(option.getName(), value))
                .map(TextOption::getId)
                .findFirst()
                .orElse(null);
    }

    protected void correctOptions() {
        options.forEach(TextOption::correct);
    }

    protected boolean notContains(String optionId) {
        return !allOptionIds().contains(optionId);
    }

    protected boolean notContainsAll(List<String> optionIds) {
        return !allOptionIds().containsAll(optionIds);
    }

    protected void validateOptions(int min, int max) {
        if (isDuplicated(options)) {
            throw new MryException(TEXT_OPTION_ID_DUPLICATED, "选项ID不能重复。");
        }

        int size = options.size();
        if (size < min) {
            throw new MryException(TEXT_OPTION_NOT_ENOUGH, "选项数量不够。", mapOf("min", min, "actualSize", size));
        }

        if (size > max) {
            throw new MryException(TEXT_OPTION_OVERFLOW, "选项数量超出限制。", mapOf("max", min, "actualSize", size));
        }
    }
}
