package com.mryqr.core.qr.query.plate;

import com.mryqr.common.utils.Query;
import com.mryqr.common.validation.collection.NoBlankString;
import com.mryqr.common.validation.id.app.AppId;
import com.mryqr.common.validation.id.qr.QrId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Set;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class ListPlateAttributeValuesQuery implements Query {
    @AppId
    @NotBlank
    private final String appId;

    @Valid
    @NotNull
    @NoBlankString
    private final Set<@QrId String> qrIds;
}
