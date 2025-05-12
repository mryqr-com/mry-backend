package com.mryqr.core.printing.query;

import com.mryqr.core.common.domain.UploadedFile;
import com.mryqr.core.printing.domain.MaterialType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QPrintingProduct {
    private String id;
    private String name;
    private UploadedFile image;
    private String description;
    private String introduction;
    private MaterialType materialType;
    private List<QPlatePrintingType> printingTypes;
}
