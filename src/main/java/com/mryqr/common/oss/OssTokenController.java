package com.mryqr.common.oss;


import com.mryqr.common.domain.user.User;
import com.mryqr.common.oss.command.OssTokenCommandService;
import com.mryqr.common.oss.command.RequestOssTokenCommand;
import com.mryqr.common.oss.domain.QOssToken;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@Slf4j
@Validated
@RestController
@RequestMapping(value = "/aliyun")
@RequiredArgsConstructor
public class OssTokenController {
    private final OssTokenCommandService ossTokenCommandService;

    @ResponseStatus(CREATED)
    @PostMapping(value = "/oss-token-requisitions")
    public QOssToken generateOssToken(@RequestBody @Valid RequestOssTokenCommand command,
                                      @AuthenticationPrincipal User user) {
        return ossTokenCommandService.generateOssToken(command, user);
    }
}
