package com.mryqr.core.printing.query;

import com.google.common.collect.ImmutableMap;
import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.printing.domain.MaterialType;
import com.mryqr.core.printing.domain.PlatePrintingType;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.DropdownAttributeValue;
import com.mryqr.core.qr.domain.attribute.MultiLineTextAttributeValue;
import com.mryqr.core.qr.domain.attribute.TextAttributeValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.mryqr.management.printingproduct.PrintingProductApp.*;
import static java.util.function.Function.identity;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
@RequiredArgsConstructor
public class PrintingProductQueryService {
    private final MryRateLimiter mryRateLimiter;
    private final MongoTemplate mongoTemplate;

    public List<QPrintingProduct> listPrintingProducts() {
        mryRateLimiter.applyFor("ListPrintingProducts", 50);

        Query query = Query.query(where("appId").is(PP_APP_ID).and("active").is(true)).limit(10).with(by(DESC, "createdAt"));
        List<QR> products = mongoTemplate.find(query, QR.class);

        Map<MaterialType, QrProductInfo> qrProductInfoMap = toProductInfoMap(products);

        return Arrays.stream(MaterialType.values()).map(materialType -> {
                    QrProductInfo qrProductInfo = qrProductInfoMap.get(materialType);
                    if (qrProductInfo == null) {
                        return null;
                    }

                    return QPrintingProduct.builder()
                            .id(qrProductInfo.getId())
                            .name(materialType.getName())
                            .materialType(materialType)
                            .description(qrProductInfo.getDescription())
                            .introduction(qrProductInfo.getIntroduction())
                            .image(qrProductInfo.getImage())
                            .printingTypes(toPrintTypes(materialType))
                            .build();
                }).filter(Objects::nonNull)
                .collect(toImmutableList());
    }

    private ImmutableMap<MaterialType, QrProductInfo> toProductInfoMap(List<QR> products) {
        return products.stream().map(qr -> {
                    String materialOptionId = qr.attributeValueOfOptional(PP_MATERIAL_TYPE_ATTRIBUTE_ID)
                            .map(value -> {
                                List<String> optionIds = ((DropdownAttributeValue) value).getOptionIds();
                                return isNotEmpty(optionIds) ? optionIds.get(0) : null;
                            }).orElse(null);

                    MaterialType materialType = isNotBlank(materialOptionId) ? MATERIAL_TYPE_MAP.get(materialOptionId) : null;

                    String description = qr.attributeValueOfOptional(PP_DESCRIPTION_ATTRIBUTE_ID)
                            .map(value -> ((TextAttributeValue) value).getText()).orElse(null);

                    String introduction = qr.attributeValueOfOptional(PP_INTRODUCTION_ATTRIBUTE_ID)
                            .map(value -> ((MultiLineTextAttributeValue) value).getContent()).orElse(null);

                    if (materialType == null ||
                        isBlank(description) ||
                        isBlank(introduction) ||
                        qr.getHeaderImage() == null) {
                        return null;
                    }

                    return QrProductInfo.builder()
                            .id(qr.getId())
                            .materialType(materialType)
                            .description(description)
                            .introduction(introduction)
                            .image(qr.getHeaderImage())
                            .build();
                }).filter(Objects::nonNull)
                .collect(toImmutableMap(QrProductInfo::getMaterialType, identity(), (v1, v2) -> v1));
    }

    private List<QPlatePrintingType> toPrintTypes(MaterialType materialType) {
        return Arrays.stream(PlatePrintingType.values())
                .filter(type -> type.getMaterialType() == materialType)
                .map(printingType -> QPlatePrintingType.builder()
                        .type(printingType)
                        .materialType(printingType.getMaterialType())
                        .size(printingType.getSize())
                        .unitPrice(printingType.getUnitPrice())
                        .deliveryFee(printingType.getDeliveryFee())
                        .build())
                .collect(toImmutableList());
    }

    @Value
    @Builder
    @AllArgsConstructor(access = PRIVATE)
    private static class QrProductInfo {
        private final String id;
        private final MaterialType materialType;
        private final String description;
        private final String introduction;
        private final UploadedFile image;
    }
}
