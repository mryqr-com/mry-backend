package com.mryqr.core.presentation;

import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.validation.id.control.ControlId;
import com.mryqr.core.common.validation.id.page.PageId;
import com.mryqr.core.common.validation.id.qr.QrId;
import com.mryqr.core.presentation.query.PresentationQueryService;
import com.mryqr.core.presentation.query.QControlPresentation;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/presentations/{qrId}/{pageId}/{controlId}")
public class PresentationController {
    private final PresentationQueryService presentationQueryService;

    @GetMapping
    public QControlPresentation fetchPresentation(@PathVariable("qrId") @NotBlank @QrId String qrId,
                                                  @PathVariable("pageId") @NotBlank @PageId String pageId,
                                                  @PathVariable("controlId") @NotBlank @ControlId String controlId,
                                                  @AuthenticationPrincipal User user) {
        return presentationQueryService.fetchPresentation(qrId, pageId, controlId, user);
    }

}
