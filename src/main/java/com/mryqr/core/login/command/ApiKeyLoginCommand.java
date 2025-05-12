package com.mryqr.core.login.command;

import com.mryqr.common.utils.Command;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ApiKeyLoginCommand implements Command {
    @NotBlank
    @Size(max = 50)
    private final String apiKey;

    @NotBlank
    @Size(max = 50)
    private final String apiSecret;

}
