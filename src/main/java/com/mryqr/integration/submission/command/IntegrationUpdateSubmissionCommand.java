package com.mryqr.integration.submission.command;

import com.mryqr.common.utils.Command;
import com.mryqr.common.validation.collection.NoNullElement;
import com.mryqr.common.validation.id.custom.CustomId;
import com.mryqr.common.validation.id.member.MemberId;
import com.mryqr.core.submission.domain.answer.Answer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

import static com.mryqr.common.utils.MryConstants.MAX_PER_PAGE_CONTROL_SIZE;
import static com.mryqr.core.submission.domain.answer.Answer.checkNoDuplicatedAnswers;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class IntegrationUpdateSubmissionCommand implements Command {
    @Valid
    @NotNull
    @NoNullElement
    @Size(max = MAX_PER_PAGE_CONTROL_SIZE)
    private final Set<Answer> answers;

    @MemberId
    private final String memberId;//更新者ID，优先于memberCustomId起作用

    @CustomId
    private final String memberCustomId;//更新者customId

    @Override
    public void correctAndValidate() {
        checkNoDuplicatedAnswers(answers);
        answers.forEach(Answer::correctAndValidate);
    }

}
