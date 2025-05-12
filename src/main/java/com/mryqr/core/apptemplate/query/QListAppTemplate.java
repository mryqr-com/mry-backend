package com.mryqr.core.apptemplate.query;

import com.mryqr.core.common.domain.UploadedFile;
import com.mryqr.core.plan.domain.PlanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QListAppTemplate {
    private String id;
    private String name;
    private UploadedFile poster;
    private PlanType planType;
    private List<String> features;
    private String cardDescription;
    private int appliedCount;
}
