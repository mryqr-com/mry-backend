package com.mryqr.core.submission;

import com.mryqr.core.submission.command.ApproveSubmissionCommand;
import com.mryqr.core.submission.command.NewSubmissionCommand;
import com.mryqr.core.submission.command.UpdateSubmissionCommand;
import com.mryqr.core.submission.domain.answer.Answer;

import static com.google.common.collect.Sets.newHashSet;
import static com.mryqr.utils.RandomTestFixture.rSentence;

public class SubmissionUtils {
    public static NewSubmissionCommand newSubmissionCommand(String qrId, String pageId, Answer... answers) {
        return NewSubmissionCommand.builder()
                .qrId(qrId)
                .pageId(pageId)
                .answers(newHashSet(answers))
                .build();
    }

    public static UpdateSubmissionCommand updateSubmissionCommand(Answer... answers) {
        return UpdateSubmissionCommand.builder()
                .answers(newHashSet(answers))
                .build();
    }

    public static ApproveSubmissionCommand approveSubmissionCommand(boolean passed) {
        return ApproveSubmissionCommand.builder()
                .passed(passed)
                .note(rSentence(20))
                .build();
    }
}
