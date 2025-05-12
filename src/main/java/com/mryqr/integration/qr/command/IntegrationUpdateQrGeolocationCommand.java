package com.mryqr.integration.qr.command;

import com.mryqr.common.domain.Geolocation;
import com.mryqr.common.utils.Command;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class IntegrationUpdateQrGeolocationCommand implements Command {
    @Valid
    private final Geolocation geolocation;

}
