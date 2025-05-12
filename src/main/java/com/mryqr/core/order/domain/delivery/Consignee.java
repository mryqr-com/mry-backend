package com.mryqr.core.order.domain.delivery;

import com.mryqr.core.common.domain.Address;
import com.mryqr.core.common.utils.Identified;
import com.mryqr.core.common.validation.id.shoruuid.ShortUuid;
import com.mryqr.core.common.validation.mobile.Mobile;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.mryqr.core.common.utils.MryConstants.MAX_GENERIC_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class Consignee implements Identified {
    @NotBlank
    @ShortUuid
    private final String id;

    @NotBlank
    @Size(max = MAX_GENERIC_NAME_LENGTH)
    private final String name;

    @Mobile
    @NotBlank
    private final String mobile;

    @Valid
    @NotNull
    private final Address address;

    @Override
    public String getIdentifier() {
        return id;
    }
}
