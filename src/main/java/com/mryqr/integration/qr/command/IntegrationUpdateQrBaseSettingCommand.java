package com.mryqr.integration.qr.command;

import com.mryqr.core.common.domain.Geolocation;
import com.mryqr.core.common.utils.Command;
import com.mryqr.core.common.validation.id.attribute.AttributeId;
import com.mryqr.core.common.validation.id.custom.CustomId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

import static com.mryqr.core.common.utils.MryConstants.MAX_DIRECT_ATTRIBUTE_VALUE_LENGTH;
import static com.mryqr.core.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static com.mryqr.core.common.utils.MryConstants.MAX_PARAGRAPH_LENGTH;
import static com.mryqr.core.common.utils.MryConstants.MAX_URL_LENGTH;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class IntegrationUpdateQrBaseSettingCommand implements Command {
    @NotBlank
    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private final String name;

    @Size(max = MAX_PARAGRAPH_LENGTH)
    private final String description;

    @Size(max = MAX_URL_LENGTH)
    private final String headerImageUrl;

    @Valid
    @Size(max = 20)
    private final Map<@AttributeId @NotBlank String, @Size(max = MAX_DIRECT_ATTRIBUTE_VALUE_LENGTH) String> directAttributeValues;

    @Valid
    private final Geolocation geolocation;

    @CustomId
    private final String customId;
}
