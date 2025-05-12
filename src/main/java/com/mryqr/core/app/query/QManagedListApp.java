package com.mryqr.core.app.query;

import com.mryqr.core.common.domain.UploadedFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QManagedListApp {
    private String id;
    private String name;
    private Instant createdAt;
    private String createdBy;
    private String creator;
    private boolean active;
    private boolean locked;
    private UploadedFile icon;
}
