package com.mryqr.core.printing.query;

import com.mryqr.core.printing.domain.MaterialType;
import com.mryqr.core.printing.domain.PlatePrintingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import static lombok.AccessLevel.PRIVATE;

@Value
@Builder
@AllArgsConstructor(access = PRIVATE)
public class QPlatePrintingType {
    private PlatePrintingType type;
    private MaterialType materialType;
    private String size;
    private double unitPrice;
    private double deliveryFee;
}
