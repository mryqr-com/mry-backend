package com.mryqr.core.apptemplate;

import com.mryqr.common.utils.PagedList;
import com.mryqr.common.validation.id.qr.QrId;
import com.mryqr.core.apptemplate.query.AppTemplateQueryService;
import com.mryqr.core.apptemplate.query.ListAppTemplateQuery;
import com.mryqr.core.apptemplate.query.QDetailedAppTemplate;
import com.mryqr.core.apptemplate.query.QListAppTemplate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping(value = "/apptemplates")
@RequiredArgsConstructor
public class AppTemplateController {
    private final AppTemplateQueryService appTemplateQueryService;

    @PostMapping("/published-lists")
    public PagedList<QListAppTemplate> listPublishedAppTemplates(@RequestBody @Valid ListAppTemplateQuery queryCommand) {
        return appTemplateQueryService.listPublishedAppTemplates(queryCommand);
    }

    @GetMapping("/{appTemplateId}")
    public QDetailedAppTemplate fetchAppTemplateDetail(@PathVariable("appTemplateId") @NotBlank @QrId String appTemplateId) {
        return appTemplateQueryService.fetchAppTemplateDetail(appTemplateId);
    }
}
