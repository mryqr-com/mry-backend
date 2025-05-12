package com.mryqr.core.qr.command;

import com.mryqr.common.domain.Geolocation;
import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.utils.Command;
import com.mryqr.common.validation.id.attribute.AttributeId;
import com.mryqr.common.validation.id.custom.CustomId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

import static com.mryqr.common.utils.MryConstants.*;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class UpdateQrBaseSettingCommand implements Command {
    @NotBlank
    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private final String name;

    @Size(max = MAX_PARAGRAPH_LENGTH)
    private final String description;

    @Valid
    private final UploadedFile headerImage;

    @Valid
    @Size(max = 20)
    private final Map<@AttributeId @NotBlank String, @Size(max = MAX_DIRECT_ATTRIBUTE_VALUE_LENGTH) String> manualAttributeValues;

    @Valid
    private final Geolocation geolocation;

    @CustomId
    private final String customId;
}
