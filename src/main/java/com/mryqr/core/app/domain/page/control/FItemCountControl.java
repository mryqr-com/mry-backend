package com.mryqr.core.app.domain.page.control;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.ui.ButtonStyle;
import com.mryqr.core.common.domain.CountedItem;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.domain.answer.itemcount.ItemCountAnswer;
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
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.core.common.exception.ErrorCode.ITEM_ANSWER_OPTION_DUPLICATED;
import static com.mryqr.core.common.exception.ErrorCode.MAX_ITEM_COUNT_REACHED;
import static com.mryqr.core.common.exception.ErrorCode.MAX_ITEM_NUMBER_REACHED;
import static com.mryqr.core.common.exception.ErrorCode.NOT_ALL_ANSWERS_IN_OPTIONS;
import static com.mryqr.core.common.utils.MryConstants.MAX_SHORT_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@SuperBuilder
@TypeAlias("ITEM_COUNT_CONTROL")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class FItemCountControl extends AbstractTextOptionControl {
    public static final int MIN_OPTION_SIZE = 2;
    public static final int MAX_OPTION_SIZE = 100;

    public static final int MIN_MAX_ITEM_SIZE = 1;
    public static final int MAX_MAX_ITEM_SIZE = MAX_OPTION_SIZE;

    public static final int MIN_MAX_PER_ITEM_COUNT = 1;
    public static final int MAX_MAX_PER_ITEM_COUNT = 1000000;

    @Min(MIN_MAX_ITEM_SIZE)
    @Max(MAX_MAX_ITEM_SIZE)
    private int maxItem;//最大可选项目数

    @Min(MIN_MAX_PER_ITEM_COUNT)
    @Max(MAX_MAX_PER_ITEM_COUNT)
    private int maxNumberPerItem;//单项可填写的最大数量

    @Size(max = MAX_SHORT_NAME_LENGTH)
    private String buttonText;//按钮文本

    @Valid
    @NotNull
    private ButtonStyle buttonStyle;//按钮样式

    @Override
    public void doCorrect(AppSettingContext context) {
        if (isBlank(buttonText)) {
            this.buttonText = "新增";
        }

        correctOptions();
    }

    @Override
    protected void doValidate(AppSettingContext context) {
        validateOptions(MIN_OPTION_SIZE, MAX_OPTION_SIZE);
    }

    @Override
    protected Answer doCreateAnswerFrom(String value) {
        return null;
    }

    public ItemCountAnswer check(ItemCountAnswer answer) {
        List<CountedItem> items = answer.getItems();

        if (items.size() > maxItem) {
            failAnswerValidation(MAX_ITEM_NUMBER_REACHED, "所选物品项目超过了最大限制。");
        }

        List<String> answeredOptionIds = items.stream().map(CountedItem::getOptionId).collect(toImmutableList());

        if (Set.copyOf(answeredOptionIds).size() != answeredOptionIds.size()) {
            failAnswerValidation(ITEM_ANSWER_OPTION_DUPLICATED, "所选物品项目重复。");
        }

        if (notContainsAll(answeredOptionIds)) {
            failAnswerValidation(NOT_ALL_ANSWERS_IN_OPTIONS, "答案项不完全在控件列表项中。");
        }

        if (items.stream().anyMatch(item -> item.getNumber() > maxNumberPerItem)) {
            failAnswerValidation(MAX_ITEM_COUNT_REACHED, "答案中有选项数量超过了单个选项数量最大限制。");
        }

        return answer;
    }

}
