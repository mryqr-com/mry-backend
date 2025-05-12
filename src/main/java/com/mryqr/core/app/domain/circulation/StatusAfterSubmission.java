package com.mryqr.core.app.domain.circulation;

import com.mryqr.core.common.utils.Identified;
import com.mryqr.core.common.validation.id.page.PageId;
import com.mryqr.core.common.validation.id.shoruuid.ShortUuid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class StatusAfterSubmission implements Identified {
    @NotBlank
    @ShortUuid
    private final String id;

    @PageId
    private final String pageId;

    @ShortUuid
    private final String optionId;

    @Override
    public String getIdentifier() {
        return id;
    }
}
