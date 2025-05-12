package com.mryqr.core.app.domain.report.chart.attribute.setting;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.attribute.AttributeAware;
import com.mryqr.core.app.domain.page.control.MultiLevelSelectionPrecisionType;
import com.mryqr.core.common.domain.AddressPrecisionType;
import com.mryqr.core.common.domain.report.QrSegmentType;
import com.mryqr.core.common.domain.report.ReportRange;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.common.validation.collection.NoNullElement;
import com.mryqr.core.common.validation.id.attribute.AttributeId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.core.app.domain.page.control.MultiLevelSelectionPrecisionType.LEVEL2;
import static com.mryqr.core.common.domain.AddressPrecisionType.CITY;
import static com.mryqr.core.common.domain.report.QrSegmentType.QR_COUNT_SUM;
import static com.mryqr.core.common.exception.ErrorCode.ATTRIBUTE_NOT_CATEGORIZED;
import static com.mryqr.core.common.exception.ErrorCode.ATTRIBUTE_NOT_NUMBER_VALUED;
import static com.mryqr.core.common.exception.ErrorCode.REQUEST_VALIDATION_FAILED;
import static com.mryqr.core.common.exception.ErrorCode.VALIDATION_ATTRIBUTE_NOT_EXIST;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class AttributeCategorizedReportSetting implements AttributeAware {
    @NotNull
    private QrSegmentType segmentType;

    @NotNull
    @AttributeId
    private String basedAttributeId;

    @Valid
    @NotNull
    @Size(max = 2)
    @NoNullElement
    private List<@AttributeId String> targetAttributeIds;

    private AddressPrecisionType addressPrecisionType;
    private MultiLevelSelectionPrecisionType multiLevelSelectionPrecisionType;

    @NotNull
    private ReportRange range;

    public void correct() {
        if (segmentType == QR_COUNT_SUM) {
            this.targetAttributeIds = List.of();
        }

        if (addressPrecisionType == null) {
            addressPrecisionType = CITY;
        }

        if (multiLevelSelectionPrecisionType == null) {
            multiLevelSelectionPrecisionType = LEVEL2;
        }
    }

    public void validate(AppSettingContext context) {
        Attribute basedAttribute = context.attributeByIdOptional(this.basedAttributeId)
                .orElseThrow(() -> new MryException(VALIDATION_ATTRIBUTE_NOT_EXIST, "基准属性项不存在。",
                        mapOf("basedAttributeId", this.basedAttributeId)));

        if (!basedAttribute.isValueCategorized()) {
            throw new MryException(ATTRIBUTE_NOT_CATEGORIZED, "基准属性项不支持分类。",
                    mapOf("basedAttributeId", this.basedAttributeId));
        }

        if (segmentType != QR_COUNT_SUM) {
            if (isEmpty(this.targetAttributeIds)) {
                throw new MryException(REQUEST_VALIDATION_FAILED, "未选择目标属性项。");
            }

            this.targetAttributeIds.forEach(targetAttributeId -> {
                Attribute targetAttribute = context.attributeByIdOptional(targetAttributeId)
                        .orElseThrow(() -> new MryException(VALIDATION_ATTRIBUTE_NOT_EXIST, "目标属性项不存在。",
                                mapOf("targetAttributeId", targetAttributeId)));

                if (!targetAttribute.isValueNumbered()) {
                    throw new MryException(ATTRIBUTE_NOT_NUMBER_VALUED, "目标属性项不支持数值。",
                            mapOf("targetAttributeId", targetAttributeId));
                }
            });
        }
    }

    @Override
    public Set<String> awaredAttributeIds() {
        return Stream.concat(Stream.of(this.basedAttributeId), this.targetAttributeIds.stream())
                .filter(StringUtils::isNotBlank)
                .collect(toImmutableSet());
    }
}
