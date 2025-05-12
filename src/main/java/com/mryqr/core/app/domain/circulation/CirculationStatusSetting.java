package com.mryqr.core.app.domain.circulation;

import com.mryqr.common.domain.TextOption;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.validation.collection.NoNullElement;
import com.mryqr.common.validation.id.shoruuid.ShortUuid;
import com.mryqr.core.app.domain.AppSettingContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.Identified.isDuplicated;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class CirculationStatusSetting {

    @Valid
    @NotNull
    @NoNullElement
    @Size(max = 10)
    private List<@Valid TextOption> options;//状态选项

    @ShortUuid
    private String initOptionId;//新建实例的初始状态

    @Valid
    @NotNull
    @NoNullElement
    @Size(max = 10)
    private List<@Valid StatusAfterSubmission> statusAfterSubmissions;//提交表单后的状态

    @Valid
    @NotNull
    @NoNullElement
    @Size(max = 10)
    private List<@Valid StatusPermission> statusPermissions;//所处状态对应可以提交的表单页面

    public static CirculationStatusSetting create() {
        return CirculationStatusSetting.builder()
                .options(List.of())
                .initOptionId(null)
                .statusAfterSubmissions(List.of())
                .statusPermissions(List.of())
                .build();
    }

    public void correct() {
        this.options.forEach(TextOption::correct);

        this.statusAfterSubmissions = this.statusAfterSubmissions.stream()
                .filter(it -> isNotBlank(it.getPageId()) && isNotBlank(it.getOptionId()))//为了前端不填是不报错
                .collect(toImmutableList());

        this.statusPermissions = this.statusPermissions.stream()
                .filter(it -> isNotBlank(it.getOptionId()) && isNotEmpty(it.getNotAllowedPageIds()))//为了前端不填是不报错
                .collect(toImmutableList());
    }

    public void validate(AppSettingContext context) {
        if (isDuplicated(options)) {
            throw new MryException(TEXT_OPTION_ID_DUPLICATED, "流转状态选项ID不能重复。");
        }

        if (isDuplicated(statusAfterSubmissions)) {
            throw new MryException(CIRCULATION_AFTER_SUBMISSION_ID_DUPLICATED, "流转状态提交设置项ID不能重复。");
        }

        if (isDuplicated(statusPermissions)) {
            throw new MryException(CIRCULATION_PERMISSION_ID_DUPLICATED, "流转状态权限设置项ID不能重复。");
        }

        Set<String> allOptionIds = allOptionIds();
        if (hasInitOption() && !allOptionIds.contains(this.initOptionId)) {
            throw new MryException(CIRCULATION_OPTION_NOT_EXISTS, "流转状态的初始状态不存在。");
        }

        this.statusAfterSubmissions.forEach(it -> {
            if (!allOptionIds.contains(it.getOptionId())) {
                throw new MryException(CIRCULATION_OPTION_NOT_EXISTS, "流转状态提交设置引用选项不存在。");
            }

            if (context.pageNotExists(it.getPageId())) {
                throw new MryException(VALIDATION_PAGE_NOT_EXIST, "流转状态提交设置引用页面不存在。");
            }
        });

        this.statusPermissions.forEach(it -> {
            if (!allOptionIds.contains(it.getOptionId())) {
                throw new MryException(CIRCULATION_OPTION_NOT_EXISTS, "流转状态权限设置引用选项不存在。");
            }

            it.getNotAllowedPageIds().forEach(pageId -> {
                if (context.pageNotExists(pageId)) {
                    throw new MryException(VALIDATION_PAGE_NOT_EXIST, "流转状态权限设置引用页面不存在。");
                }
            });
        });
    }

    public Set<String> allOptionIds() {
        return options.stream().map(TextOption::getId).collect(toImmutableSet());
    }

    public boolean hasInitOption() {
        return isNotBlank(this.initOptionId);
    }

    public Optional<String> statusAfterSubmission(String pageId) {
        return this.statusAfterSubmissions.stream()
                .filter(it -> Objects.equals(it.getPageId(), pageId))
                .map(StatusAfterSubmission::getOptionId)
                .findFirst();
    }

    public boolean canSubmit(String circulationStatusOptionId, String pageId) {
        if (isBlank(circulationStatusOptionId)) {
            return true;
        }

        return this.statusPermissions.stream().filter(it -> Objects.equals(it.getOptionId(), circulationStatusOptionId))
                .findFirst()
                .map(it -> !it.getNotAllowedPageIds().contains(pageId))
                .orElse(true);
    }
}
