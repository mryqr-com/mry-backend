package com.mryqr.common.wx.jssdk;

import com.mryqr.core.common.utils.Query;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static com.mryqr.core.common.utils.MryConstants.MAX_URL_LENGTH;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class FetchWxJsSdkConfigQuery implements Query {
    @NotBlank
    @Size(max = MAX_URL_LENGTH)
    private final String url;
}
