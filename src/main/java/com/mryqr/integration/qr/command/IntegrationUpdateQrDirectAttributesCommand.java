package com.mryqr.integration.qr.command;

import com.mryqr.core.common.utils.Command;
import com.mryqr.core.common.validation.id.attribute.AttributeId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

import static com.mryqr.core.common.utils.MryConstants.MAX_DIRECT_ATTRIBUTE_VALUE_LENGTH;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class IntegrationUpdateQrDirectAttributesCommand implements Command {

    @Valid
    @Size(max = 20)
    private final Map<@AttributeId @NotBlank String, @Size(max = MAX_DIRECT_ATTRIBUTE_VALUE_LENGTH) String> directAttributeValues;

}
