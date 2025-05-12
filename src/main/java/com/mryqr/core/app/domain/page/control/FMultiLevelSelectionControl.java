package com.mryqr.core.app.domain.page.control;

import com.google.common.collect.ImmutableList;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.multilevelselection.MultiLevelSelection;
import com.mryqr.core.submission.domain.answer.multilevelselection.MultiLevelSelectionAnswer;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.annotation.TypeAlias;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.core.common.exception.ErrorCode.MULTI_SELECTION_LEVEL1_NOT_PROVIDED;
import static com.mryqr.core.common.exception.ErrorCode.MULTI_SELECTION_LEVEL2_NOT_PROVIDED;
import static com.mryqr.core.common.exception.ErrorCode.MULTI_SELECTION_LEVEL3_NOT_PROVIDED;
import static java.lang.Double.parseDouble;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.substring;
import static org.apache.commons.lang3.StringUtils.trim;
import static org.apache.commons.lang3.tuple.Pair.of;

@Slf4j
@Getter
@SuperBuilder
@TypeAlias("MULTI_LEVEL_SELECTION_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FMultiLevelSelectionControl extends Control {
    public static final int MAX_OPTION_NAME_LENGTH = 10;
    public static final int MAX_TITLE_TEXT_LENGTH = 50;

    public static final int MAX_OPTION_TEXT_LENGTH = 10000;

    public static final String MULTI_LEVEL_SELECTION_SEPARATOR = "/";
    public static final String NUMERICAL_VALUE_SEPARATOR = ":";
    private static final String ROOT = "ROOT";

    @Size(max = MAX_TITLE_TEXT_LENGTH)
    private String titleText;//原始标题文本

    @Size(max = MAX_OPTION_TEXT_LENGTH)
    private String optionText;//原始选项文本

    private boolean filterable;//可搜索

    @EqualsAndHashCode.Exclude
    private List<String> titles;

    @EqualsAndHashCode.Exclude
    private MultiLevelOption option;

    @EqualsAndHashCode.Exclude
    private int totalLevel;

    @Override
    protected void doCorrect(AppSettingContext context) {
        doCorrect();
    }

    public void doCorrect() {
        this.titles = this.buildTitles(titleText);
        Pair<Integer, List<MultiLevelOption>> optionPair = this.buildOptions(optionText);
        this.totalLevel = optionPair.getLeft();
        this.option = removeDuplicate(MultiLevelOption.builder().name(ROOT).options(optionPair.getRight()).build());
    }

    @Override
    protected void doValidate(AppSettingContext context) {
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }

    public Answer check(MultiLevelSelectionAnswer answer) {
        MultiLevelSelection selection = answer.getSelection();

        switch (totalLevel) {
            case 1 -> {
                checkLevel1(selection);
                return MultiLevelSelectionAnswer.builder()
                        .controlId(answer.getControlId())
                        .controlType(answer.getControlType())
                        .selection(MultiLevelSelection.builder()
                                .level1(selection.getLevel1())
                                .build()).build();
            }
            case 2 -> {
                checkLevel1(selection);
                checkLevel2(selection);
                return MultiLevelSelectionAnswer.builder()
                        .controlId(answer.getControlId())
                        .controlType(answer.getControlType())
                        .selection(MultiLevelSelection.builder()
                                .level1(selection.getLevel1())
                                .level2(selection.getLevel2())
                                .build()).build();
            }
            case 3 -> {
                checkLevel1(selection);
                checkLevel2(selection);
                checkLevel3(selection);
                return MultiLevelSelectionAnswer.builder()
                        .controlId(answer.getControlId())
                        .controlType(answer.getControlType())
                        .selection(MultiLevelSelection.builder()
                                .level1(selection.getLevel1())
                                .level2(selection.getLevel2())
                                .level3(selection.getLevel3())
                                .build()).build();
            }
            default -> {
                throw new RuntimeException("多级下拉框验证失败:[" + this.getId() + "]。");
            }
        }
    }

    private void checkLevel1(MultiLevelSelection selection) {
        if (isBlank(selection.getLevel1())) {
            failAnswerValidation(MULTI_SELECTION_LEVEL1_NOT_PROVIDED, "未填写第一级数据。");
        }
    }

    private void checkLevel2(MultiLevelSelection selection) {
        option.childOption(selection.getLevel1()).ifPresent(level1Option -> {
            if (isNotEmpty(level1Option.getOptions()) && isBlank(selection.getLevel2())) {
                failAnswerValidation(MULTI_SELECTION_LEVEL2_NOT_PROVIDED, "未填写第二级数据。");
            }
        });
    }

    private void checkLevel3(MultiLevelSelection selection) {
        option.childOption(selection.getLevel1())
                .flatMap(level1Option -> level1Option.childOption(selection.getLevel2())).ifPresent(level2Option -> {
                    if (isNotEmpty(level2Option.getOptions()) && isBlank(selection.getLevel3())) {
                        failAnswerValidation(MULTI_SELECTION_LEVEL3_NOT_PROVIDED, "未填写第三级数据。");
                    }
                });
    }

    private List<String> buildTitles(String titleText) {
        if (isBlank(titleText)) {
            return List.of();
        }

        return Arrays.stream(split(titleText, MULTI_LEVEL_SELECTION_SEPARATOR))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .limit(3)
                .collect(toImmutableList());
    }

    private Pair<Integer, List<MultiLevelOption>> buildOptions(String input) {
        if (isBlank(input)) {
            return of(0, List.of());
        }

        AtomicInteger totalLevel = new AtomicInteger(0);
        String cleanedInput = input.replace("：", ":").replaceAll("/\\s*:", ":");
        Map<String, ImmutableList<SplitRecord>> firstSplit = Arrays.stream(split(cleanedInput, "\n"))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .map(str -> toSplitRecord(str, 2))
                .filter(Objects::nonNull)
                .filter(record -> isNotBlank(record.getLeft()))
                .collect(groupingBy(SplitRecord::getLeft, LinkedHashMap::new, toImmutableList()));

        List<MultiLevelOption> firstLevelOptions = firstSplit.entrySet().stream()
                .map(firstLevelEntry -> {
                    Map<String, ImmutableList<SplitRecord>> secondSplit = firstLevelEntry.getValue().stream()
                            .map(SplitRecord::getRight)
                            .map(String::trim)
                            .filter(StringUtils::isNotBlank)
                            .distinct()
                            .map(str -> toSplitRecord(str, -1))
                            .filter(Objects::nonNull)
                            .filter(record -> isNotBlank(record.getLeft()))
                            .collect(groupingBy(SplitRecord::getLeft, LinkedHashMap::new, toImmutableList()));

                    List<MultiLevelOption> secondLevelOptions = secondSplit.entrySet().stream()
                            .map(secondLevelEntry -> {
                                List<MultiLevelOption> thirdLevelOptions = secondLevelEntry.getValue().stream()
                                        .map(SplitRecord::getRight)
                                        .map(String::trim)
                                        .filter(StringUtils::isNotBlank)
                                        .distinct()
                                        .map(thirdLevelName -> {
                                            if (totalLevel.get() < 3) {
                                                totalLevel.set(3);
                                            }

                                            //第3层级
                                            Pair<String, Double> nameWithValue = parseNameWithNumericalValue(thirdLevelName);
                                            return MultiLevelOption.builder()
                                                    .name(nameWithValue.getLeft())
                                                    .numericalValue(nameWithValue.getRight())
                                                    .options(List.of()).build();
                                        })
                                        .collect(toImmutableList());

                                if (totalLevel.get() < 2) {
                                    totalLevel.set(2);
                                }

                                //第2层级
                                if (isEmpty(thirdLevelOptions)) {//thirdLevelOptions为空表示自身即可赋值
                                    Pair<String, Double> nameWithValue = parseNameWithNumericalValue(secondLevelEntry.getKey());
                                    return MultiLevelOption.builder()
                                            .name(nameWithValue.getLeft())
                                            .numericalValue(nameWithValue.getRight())
                                            .options(thirdLevelOptions).build();
                                }

                                return MultiLevelOption.builder()
                                        .name(cropOptionName(secondLevelEntry.getKey()))
                                        .options(thirdLevelOptions).build();
                            }).collect(toImmutableList());

                    if (totalLevel.get() < 1) {
                        totalLevel.set(1);
                    }

                    //第1层级
                    if (isEmpty(secondLevelOptions)) {//secondLevelOptions为空表示自身即可赋值
                        Pair<String, Double> nameWithValue = parseNameWithNumericalValue(firstLevelEntry.getKey());
                        return MultiLevelOption.builder()
                                .name(nameWithValue.getLeft())
                                .numericalValue(nameWithValue.getRight())
                                .options(secondLevelOptions).build();
                    }

                    return MultiLevelOption.builder()
                            .name(cropOptionName(firstLevelEntry.getKey()))
                            .options(secondLevelOptions).build();
                }).collect(toImmutableList());

        return of(totalLevel.get(), firstLevelOptions);
    }

    private MultiLevelOption removeDuplicate(MultiLevelOption multiLevelOption) {
        if (isEmpty(multiLevelOption.getOptions())) {
            return multiLevelOption;
        }

        List<MultiLevelOption> deduplicated = new ArrayList<>(multiLevelOption.getOptions().stream()
                .map(this::removeDuplicate)
                .collect(toMap(MultiLevelOption::getName, identity(), (a, b) -> {
                    if (a.getOptions().size() > 0) {//按照当前算法，在name重复的元素中，只存在最多一个具有options元素，有的话即胜出，否则后出现的胜出
                        return a;
                    }
                    return b;
                }, LinkedHashMap::new)).values());

        return MultiLevelOption.builder().name(multiLevelOption.getName())
                .numericalValue(multiLevelOption.getNumericalValue())
                .options(deduplicated)
                .build();
    }

    private SplitRecord toSplitRecord(String str, int max) {
        String[] split = split(str, MULTI_LEVEL_SELECTION_SEPARATOR, max);

        if (split.length >= 2) {
            return new SplitRecord(trim(split[0]), trim(split[1]));
        }

        if (split.length == 0) {
            return null;
        }

        return new SplitRecord(trim(split[0]), "");
    }

    private Pair<String, Double> parseNameWithNumericalValue(String optionName) {
        double numericalValue = 0.0;
        String[] numericalValueSplit = split(optionName, NUMERICAL_VALUE_SEPARATOR);

        if (numericalValueSplit.length >= 2) {
            String numericalValueString = numericalValueSplit[1];
            if (isNotBlank(numericalValueString)) {
                try {
                    numericalValue = parseDouble(numericalValueString);
                } catch (Throwable t) {
                    log.warn("Error while parsing string[{}] into double, default to 0.", numericalValueString);
                }
            }
        }

        return of(cropOptionName(trim(numericalValueSplit[0])), numericalValue);
    }

    private String cropOptionName(String str) {
        return substring(str, 0, 10);
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    private static class SplitRecord {
        private final String left;
        private final String right;
    }

}
