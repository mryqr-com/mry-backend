package com.mryqr.core.qr;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.utils.EasyExcelResult;
import com.mryqr.core.common.utils.PagedList;
import com.mryqr.core.common.validation.id.group.GroupId;
import com.mryqr.core.common.validation.id.plate.PlateId;
import com.mryqr.core.common.validation.id.qr.QrId;
import com.mryqr.core.qr.command.ChangeQrsGroupCommand;
import com.mryqr.core.qr.command.CreateQrCommand;
import com.mryqr.core.qr.command.CreateQrFromPlateCommand;
import com.mryqr.core.qr.command.CreateQrResponse;
import com.mryqr.core.qr.command.DeleteQrsCommand;
import com.mryqr.core.qr.command.QrCommandService;
import com.mryqr.core.qr.command.RenameQrCommand;
import com.mryqr.core.qr.command.ResetQrCirculationStatusCommand;
import com.mryqr.core.qr.command.ResetQrPlateCommand;
import com.mryqr.core.qr.command.UpdateQrBaseSettingCommand;
import com.mryqr.core.qr.command.importqr.QrImportResponse;
import com.mryqr.core.qr.query.QQrBaseSetting;
import com.mryqr.core.qr.query.QQrSummary;
import com.mryqr.core.qr.query.QrQueryService;
import com.mryqr.core.qr.query.bindplate.BindPlateQueryService;
import com.mryqr.core.qr.query.bindplate.QBindPlateInfo;
import com.mryqr.core.qr.query.list.ListViewableQrsQuery;
import com.mryqr.core.qr.query.list.QViewableListQr;
import com.mryqr.core.qr.query.plate.ListPlateAttributeValuesQuery;
import com.mryqr.core.qr.query.plate.QrPlateAttributeValueQueryService;
import com.mryqr.core.qr.query.submission.QSubmissionQr;
import com.mryqr.core.qr.query.submission.list.ListQrSubmissionsQuery;
import com.mryqr.core.qr.query.submission.list.QrSubmissionQueryService;
import com.mryqr.core.submission.query.list.QListSubmission;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static java.net.URLEncoder.encode;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;


@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/qrs")
public class QrController {
    private final QrQueryService qrQueryService;
    private final QrCommandService qrCommandService;
    private final QrSubmissionQueryService qrSubmissionQueryService;
    private final BindPlateQueryService bindPlateQueryService;
    private final QrPlateAttributeValueQueryService qrPlateAttributeValueQueryService;

    @PostMapping
    @ResponseStatus(CREATED)
    public CreateQrResponse createQr(@RequestBody @Valid CreateQrCommand command,
                                     @AuthenticationPrincipal User user) {
        return qrCommandService.createQr(command, user);
    }

    @ResponseStatus(CREATED)
    @PostMapping(value = "/from-plate")
    public CreateQrResponse createQrFromPlate(@RequestBody @Valid CreateQrFromPlateCommand command,
                                              @AuthenticationPrincipal User user) {
        return qrCommandService.createQrFromPlate(command, user);
    }

    @PostMapping(value = "/import", consumes = MULTIPART_FORM_DATA_VALUE)
    public QrImportResponse importQrs(@RequestParam("file") @NotNull MultipartFile file,
                                      @RequestParam("groupId") @GroupId @NotBlank String groupId,
                                      @AuthenticationPrincipal User user) throws IOException {
        return qrCommandService.importQrs(file.getInputStream(), groupId, user);
    }

    @PutMapping(value = "/{id}/name")
    public void renameQr(@PathVariable("id") @NotBlank @QrId String qrId,
                         @RequestBody @Valid RenameQrCommand command,
                         @AuthenticationPrincipal User user) {
        qrCommandService.renameQr(qrId, command, user);
    }

    @PutMapping(value = "/{id}/plate")
    public void resetQrPlate(@PathVariable("id") @NotBlank @QrId String qrId,
                             @RequestBody @Valid ResetQrPlateCommand command,
                             @AuthenticationPrincipal User user) {
        qrCommandService.resetQrPlate(qrId, command, user);
    }

    @PutMapping(value = "/{id}/circulation-status")
    public void resetCirculationStatus(@PathVariable("id") @NotBlank @QrId String qrId,
                                       @RequestBody @Valid ResetQrCirculationStatusCommand command,
                                       @AuthenticationPrincipal User user) {
        qrCommandService.resetQrCirculationStatus(qrId, command, user);
    }

    @PostMapping(value = "/deletion")
    public void deleteQrs(@RequestBody @Valid DeleteQrsCommand command,
                          @AuthenticationPrincipal User user) {
        qrCommandService.deleteQrs(command, user);
    }

    @DeleteMapping(value = "/{id}")
    public void deleteQr(@PathVariable("id") @NotBlank @QrId String qrId,
                         @AuthenticationPrincipal User user) {
        qrCommandService.deleteQr(qrId, user);
    }

    @PutMapping(value = "/group")
    public void changeQrsGroup(@RequestBody @Valid ChangeQrsGroupCommand command,
                               @AuthenticationPrincipal User user) {
        qrCommandService.changeQrsGroup(command, user);
    }

    @PutMapping(value = "/{id}/template")
    public void markAsTemplate(@PathVariable("id") @NotBlank @QrId String qrId,
                               @AuthenticationPrincipal User user) {
        qrCommandService.markAsTemplate(qrId, user);
    }

    @DeleteMapping(value = "/{id}/template")
    public void unmarkAsTemplate(@PathVariable("id") @NotBlank @QrId String qrId,
                                 @AuthenticationPrincipal User user) {
        qrCommandService.unmarkAsTemplate(qrId, user);
    }

    @PutMapping(value = "/{id}/activation")
    public void activateQr(@PathVariable("id") @NotBlank @QrId String qrId,
                           @AuthenticationPrincipal User user) {
        qrCommandService.activateQr(qrId, user);
    }

    @PutMapping(value = "/{id}/deactivation")
    public void deactivateQr(@PathVariable("id") @NotBlank @QrId String qrId,
                             @AuthenticationPrincipal User user) {
        qrCommandService.deactivateQr(qrId, user);
    }

    @PutMapping(value = "/{id}/base-setting")
    public void updateQrBaseSetting(@PathVariable("id") @NotBlank @QrId String qrId,
                                    @RequestBody @Valid UpdateQrBaseSettingCommand command,
                                    @AuthenticationPrincipal User user) {
        qrCommandService.updateQrBaseSetting(qrId, command, user);
    }

    @PostMapping(value = "/my-viewable-qrs")
    public PagedList<QViewableListQr> listMyViewableQrs(@RequestBody @Valid ListViewableQrsQuery queryCommand,
                                                        @AuthenticationPrincipal User user) {
        return qrQueryService.listMyViewableQrs(queryCommand, user);
    }

    @GetMapping(value = "/my-viewable-qrs/{qrId}")
    public QViewableListQr fetchViewableListQr(@PathVariable("qrId") @NotBlank @QrId String qrId,
                                               @AuthenticationPrincipal User user) {
        return qrQueryService.fetchViewableListQr(qrId, user);
    }

    @PostMapping(value = "/export")
    public void exportQrsToExcel(@RequestBody @Valid ListViewableQrsQuery listViewableQrsQuery,
                                 HttpServletResponse response,
                                 @AuthenticationPrincipal User user) throws IOException {
        EasyExcelResult result = qrQueryService.exportQrsToExcel(listViewableQrsQuery, user);
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-disposition", "attachment;filename=" + encode(result.getFileName(), UTF_8));
        EasyExcel.write(response.getOutputStream())
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .head(result.getHeaders())
                .sheet(1)
                .doWrite(result.getRecords());
    }

    @GetMapping(value = "/submission-qrs/{plateId}")
    public QSubmissionQr fetchSubmissionQr(@PathVariable("plateId") @NotBlank @PlateId String plateId,
                                           @AuthenticationPrincipal User user) {
        return qrQueryService.fetchSubmissionQr(plateId, user);
    }

    @GetMapping(value = "/{id}/base-setting")
    public QQrBaseSetting fetchQrBaseSetting(@PathVariable("id") @NotBlank @QrId String qrId,
                                             @AuthenticationPrincipal User user) {
        return qrQueryService.fetchQrBaseSetting(qrId, user);
    }

    @GetMapping(value = "/{id}/summary")
    public QQrSummary fetchQrSummary(@PathVariable("id") @NotBlank @QrId String qrId,
                                     @AuthenticationPrincipal User user) {
        return qrQueryService.fetchQrSummary(qrId, user);
    }

    @PostMapping(value = "/{id}/submissions")
    public PagedList<QListSubmission> listQrSubmissions(@PathVariable("id") @NotBlank @QrId String qrId,
                                                        @RequestBody @Valid ListQrSubmissionsQuery queryCommand,
                                                        @AuthenticationPrincipal User user) {
        return qrSubmissionQueryService.listQrSubmissions(qrId, queryCommand, user);
    }

    @GetMapping(value = "/bind-plate-infos/{plateId}")
    public QBindPlateInfo fetchBindQrPlateInfo(@PathVariable("plateId") @NotBlank @PlateId String plateId,
                                               @AuthenticationPrincipal User user) {
        return bindPlateQueryService.fetchBindQrPlateInfo(plateId, user);
    }

    @PostMapping(value = "/plate-attribute-values")
    public Map<String, Map<String, String>> fetchQrPlateAttributeValues(@RequestBody @Valid ListPlateAttributeValuesQuery queryCommand,
                                                                        @AuthenticationPrincipal User user) {
        return qrPlateAttributeValueQueryService.fetchQrPlateAttributeValues(queryCommand, user);
    }

}
