package com.mryqr.core.app.query;

import com.mryqr.core.common.domain.UploadedFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QViewableListApp {
    private String id;
    private String name;
    private UploadedFile icon;
    private boolean locked;
}
