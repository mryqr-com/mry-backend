package com.mryqr.core.submission;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.utils.EasyExcelResult;
import com.mryqr.core.common.utils.PagedList;
import com.mryqr.core.common.utils.ReturnId;
import com.mryqr.core.common.validation.id.page.PageId;
import com.mryqr.core.common.validation.id.qr.QrId;
import com.mryqr.core.common.validation.id.submission.SubmissionId;
import com.mryqr.core.submission.command.ApproveSubmissionCommand;
import com.mryqr.core.submission.command.NewSubmissionCommand;
import com.mryqr.core.submission.command.SubmissionCommandService;
import com.mryqr.core.submission.command.UpdateSubmissionCommand;
import com.mryqr.core.submission.domain.answer.Answer;
import com.mryqr.core.submission.query.QDetailedSubmission;
import com.mryqr.core.submission.query.SubmissionQueryService;
import com.mryqr.core.submission.query.autocalculate.AutoCalculateQuery;
import com.mryqr.core.submission.query.autocalculate.AutoCalculateQueryService;
import com.mryqr.core.submission.query.autocalculate.ItemStatusAutoCalculateResponse;
import com.mryqr.core.submission.query.autocalculate.NumberInputAutoCalculateResponse;
import com.mryqr.core.submission.query.list.ListSubmissionsQuery;
import com.mryqr.core.submission.query.list.QListSubmission;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Set;

import static com.mryqr.core.common.utils.ReturnId.returnId;
import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.CREATED;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/submissions")
public class SubmissionController {
    private final SubmissionCommandService submissionCommandService;
    private final SubmissionQueryService submissionQueryService;
    private final AutoCalculateQueryService autoCalculateQueryService;

    @PostMapping
    @ResponseStatus(CREATED)
    public ReturnId newSubmission(@RequestBody @Valid NewSubmissionCommand command,
                                  @AuthenticationPrincipal User user) {
        String submissionId = submissionCommandService.newSubmission(command, user);
        return returnId(submissionId);
    }

    @PutMapping(value = "/{submissionId}")
    public ReturnId updateSubmission(@PathVariable("submissionId") @SubmissionId @NotBlank String submissionId,
                                     @RequestBody @Valid UpdateSubmissionCommand command,
                                     @AuthenticationPrincipal User user) {
        submissionCommandService.updateSubmission(submissionId, command, user);
        return returnId(submissionId);
    }

    @DeleteMapping(value = "/{submissionId}")
    public ReturnId deleteSubmission(@PathVariable("submissionId") @SubmissionId @NotBlank String submissionId,
                                     @AuthenticationPrincipal User user) {
        submissionCommandService.deleteSubmission(submissionId, user);
        return returnId(submissionId);
    }

    @ResponseStatus(CREATED)
    @PostMapping(value = "/{submissionId}/approval")
    public ReturnId approveSubmission(@PathVariable("submissionId") @SubmissionId @NotBlank String submissionId,
                                      @RequestBody @Valid ApproveSubmissionCommand command,
                                      @AuthenticationPrincipal User user) {
        submissionCommandService.approveSubmission(submissionId, command, user);
        return returnId(submissionId);
    }

    @PostMapping(value = "/lists")
    public PagedList<QListSubmission> listSubmissions(@RequestBody @Valid ListSubmissionsQuery queryCommand,
                                                      @AuthenticationPrincipal User user) {
        return submissionQueryService.listSubmissions(queryCommand, user);
    }

    @GetMapping(value = "/lists/{submissionId}")
    public QListSubmission fetchListSubmission(@PathVariable("submissionId") @SubmissionId @NotBlank String submissionId,
                                               @AuthenticationPrincipal User user) {
        return submissionQueryService.fetchListSubmission(submissionId, user);
    }

    @GetMapping(value = "/{submissionId}")
    public QDetailedSubmission fetchDetailedSubmission(@PathVariable("submissionId") @SubmissionId @NotBlank String submissionId,
                                                       @AuthenticationPrincipal User user) {
        return submissionQueryService.fetchDetailedSubmission(submissionId, user);
    }

    @GetMapping(value = "/{qrId}/{pageId}/instance-last-submission")
    public QDetailedSubmission tryFetchInstanceLastSubmission(@PathVariable("qrId") @QrId @NotBlank String qrId,
                                                              @PathVariable("pageId") @PageId @NotBlank String pageId,
                                                              @AuthenticationPrincipal User user) {
        return submissionQueryService.tryFetchInstanceLastSubmission(qrId, pageId, user);
    }

    @GetMapping(value = "/{qrId}/{pageId}/my-last-submission")
    public QDetailedSubmission tryFetchMyLastSubmission(@PathVariable("qrId") @QrId @NotBlank String qrId,
                                                        @PathVariable("pageId") @PageId @NotBlank String pageId,
                                                        @AuthenticationPrincipal User user) {
        return submissionQueryService.tryFetchMyLastSubmission(qrId, pageId, user);
    }

    @GetMapping(value = "/{qrId}/{pageId}/auto-fill-answers")
    public Set<Answer> tryFetchSubmissionAnswersForAutoFill(@PathVariable("qrId") @QrId @NotBlank String qrId,
                                                            @PathVariable("pageId") @PageId @NotBlank String pageId,
                                                            @AuthenticationPrincipal User user) {
        return submissionQueryService.tryFetchSubmissionAnswersForAutoFill(qrId, pageId, user);
    }

    @PostMapping(value = "/excel")
    public void exportSubmissionsToExcel(@RequestBody @Valid ListSubmissionsQuery queryCommand,
                                         HttpServletResponse response,
                                         @AuthenticationPrincipal User user) throws IOException {
        EasyExcelResult result = submissionQueryService.exportSubmissionsToExcel(queryCommand, user);
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + encode(result.getFileName(), UTF_8));
        EasyExcel.write(response.getOutputStream())
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .head(result.getHeaders())
                .sheet(1)
                .doWrite(result.getRecords());
    }

    @PostMapping(value = "/auto-calculate/number-input")
    public NumberInputAutoCalculateResponse autoCalculateForNumberInput(@RequestBody @Valid AutoCalculateQuery queryCommand,
                                                                        @AuthenticationPrincipal User user) {
        Double number = autoCalculateQueryService.calculateForNumberInput(queryCommand, user);
        return NumberInputAutoCalculateResponse.builder().number(number).build();
    }

    @PostMapping(value = "/auto-calculate/item-status")
    public ItemStatusAutoCalculateResponse autoCalculateForItemStatus(@RequestBody @Valid AutoCalculateQuery queryCommand,
                                                                      @AuthenticationPrincipal User user) {
        String optionId = autoCalculateQueryService.calculateForItemStatus(queryCommand, user);
        return ItemStatusAutoCalculateResponse.builder().optionId(optionId).build();
    }
}
