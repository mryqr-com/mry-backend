package com.mryqr.core.verification;

import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.utils.ReturnId;
import com.mryqr.core.verification.command.CreateChangeMobileVerificationCodeCommand;
import com.mryqr.core.verification.command.CreateFindbackPasswordVerificationCodeCommand;
import com.mryqr.core.verification.command.CreateLoginVerificationCodeCommand;
import com.mryqr.core.verification.command.CreateRegisterVerificationCodeCommand;
import com.mryqr.core.verification.command.IdentifyMobileVerificationCodeCommand;
import com.mryqr.core.verification.command.VerificationCodeCommandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import static com.mryqr.core.common.utils.ReturnId.returnId;
import static org.springframework.http.HttpStatus.CREATED;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/verification-codes")
public class VerificationController {
    private final VerificationCodeCommandService verificationCodeCommandService;

    @ResponseStatus(CREATED)
    @PostMapping(value = "/for-register")
    public ReturnId createVerificationCodeForRegister(@RequestBody @Valid CreateRegisterVerificationCodeCommand command) {
        String verificationCodeId = verificationCodeCommandService.createVerificationCodeForRegister(command);
        return returnId(verificationCodeId);
    }

    @ResponseStatus(CREATED)
    @PostMapping(value = "/for-login")
    public ReturnId createVerificationCodeForLogin(@RequestBody @Valid CreateLoginVerificationCodeCommand command) {
        String verificationCodeId = verificationCodeCommandService.createVerificationCodeForLogin(command);
        return returnId(verificationCodeId);
    }

    @ResponseStatus(CREATED)
    @PostMapping(value = "/for-findback-password")
    public ReturnId createVerificationCodeForFindbackPassword(@RequestBody @Valid CreateFindbackPasswordVerificationCodeCommand command) {
        String verificationCodeId = verificationCodeCommandService.createVerificationCodeForFindbackPassword(command);
        return returnId(verificationCodeId);
    }

    @ResponseStatus(CREATED)
    @PostMapping(value = "/for-change-mobile")
    public ReturnId createVerificationCodeForChangeMobile(@RequestBody @Valid CreateChangeMobileVerificationCodeCommand command,
                                                          @AuthenticationPrincipal User user) {
        String verificationCodeId = verificationCodeCommandService.createVerificationCodeForChangeMobile(command, user);
        return returnId(verificationCodeId);
    }

    @ResponseStatus(CREATED)
    @PostMapping(value = "/for-identify-mobile")
    public ReturnId createVerificationCodeForIdentifyMobile(@RequestBody @Valid IdentifyMobileVerificationCodeCommand command,
                                                            @AuthenticationPrincipal User user) {
        String verificationCodeId = verificationCodeCommandService.createVerificationCodeForIdentifyMobile(command, user);
        return returnId(verificationCodeId);
    }
}
