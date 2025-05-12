package com.mryqr.core.submission.query.autocalculate;

import com.mryqr.core.common.utils.Query;
import com.mryqr.core.common.validation.collection.NoNullElement;
import com.mryqr.core.common.validation.id.app.AppId;
import com.mryqr.core.common.validation.id.control.ControlId;
import com.mryqr.core.common.validation.id.page.PageId;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static com.mryqr.core.common.utils.MryConstants.MAX_PER_PAGE_CONTROL_SIZE;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class AutoCalculateQuery implements Query {
    @AppId
    @NotBlank
    private final String appId;

    @PageId
    @NotBlank
    private final String pageId;

    @NotBlank
    @ControlId
    private final String controlId;

    @Valid
    @NotNull
    @NoNullElement
    @Size(max = MAX_PER_PAGE_CONTROL_SIZE)
    private final List<Answer> answers;
}
