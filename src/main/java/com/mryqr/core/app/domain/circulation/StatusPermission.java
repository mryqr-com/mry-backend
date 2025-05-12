package com.mryqr.core.app.domain.circulation;

import com.mryqr.common.utils.Identified;
import com.mryqr.common.validation.collection.NoNullElement;
import com.mryqr.common.validation.id.page.PageId;
import com.mryqr.common.validation.id.shoruuid.ShortUuid;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static com.mryqr.common.utils.MryConstants.MAX_PER_APP_PAGE_SIZE;
import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class StatusPermission implements Identified {

    @NotBlank
    @ShortUuid
    private String id;

    @ShortUuid
    private String optionId;

    @Valid
    @NotNull
    @NoNullElement
    @Size(max = MAX_PER_APP_PAGE_SIZE)
    private List<@PageId String> notAllowedPageIds;

    @Override
    public String getIdentifier() {
        return id;
    }
}
