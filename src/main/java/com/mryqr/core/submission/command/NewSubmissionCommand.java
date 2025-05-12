package com.mryqr.core.submission.command;

import com.mryqr.core.common.utils.Command;
import com.mryqr.core.common.validation.collection.NoNullElement;
import com.mryqr.core.common.validation.id.page.PageId;
import com.mryqr.core.common.validation.id.qr.QrId;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

import static com.mryqr.core.common.utils.MryConstants.MAX_PER_PAGE_CONTROL_SIZE;
import static com.mryqr.core.submission.domain.answer.Answer.checkNoDuplicatedAnswers;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class NewSubmissionCommand implements Command {
    @QrId
    @NotBlank
    private final String qrId;

    @PageId
    @NotBlank
    private final String pageId;

    @Valid
    @NotNull
    @NoNullElement
    @Size(max = MAX_PER_PAGE_CONTROL_SIZE)
    private final Set<Answer> answers;

    @Size(max = 500)
    private final String referenceData;

    @Override
    public void correctAndValidate() {
        checkNoDuplicatedAnswers(answers);
        answers.forEach(Answer::correctAndValidate);
    }
}
