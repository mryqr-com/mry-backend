package com.mryqr.core.register;

import com.mryqr.core.register.command.RegisterCommand;
import com.mryqr.core.register.command.RegisterCommandService;
import com.mryqr.core.register.command.RegisterResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/registration")
public class RegisterController {
    private final RegisterCommandService registerCommandService;

    @PostMapping
    @ResponseStatus(CREATED)
    public RegisterResponse register(@RequestBody @Valid RegisterCommand command) {
        return registerCommandService.register(command);
    }
}
