package com.mryqr.core.apptemplate;

import com.mryqr.core.apptemplate.query.AppTemplateQueryService;
import com.mryqr.core.apptemplate.query.ListAppTemplateQuery;
import com.mryqr.core.apptemplate.query.QDetailedAppTemplate;
import com.mryqr.core.apptemplate.query.QListAppTemplate;
import com.mryqr.core.common.utils.PagedList;
import com.mryqr.core.common.validation.id.qr.QrId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
