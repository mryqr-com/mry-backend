package com.mryqr.core.app.domain.report.chart.attribute.setting;

import com.mryqr.common.domain.stat.QrSegmentType;
import com.mryqr.common.domain.stat.StatRange;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.validation.id.attribute.AttributeId;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.attribute.AttributeAware;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.mryqr.common.domain.stat.QrSegmentType.QR_COUNT_SUM;
import static com.mryqr.common.exception.ErrorCode.ATTRIBUTE_NOT_NUMBER_VALUED;
import static com.mryqr.common.exception.ErrorCode.VALIDATION_ATTRIBUTE_NOT_EXIST;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class AttributeNumberRangeSegmentReportSetting implements AttributeAware {
    @NotNull
    private QrSegmentType segmentType;

    @NotBlank
    @AttributeId
    private String basedAttributeId;

    @NotBlank
    @Size(max = 100)
    private String numberRangesString;

    @AttributeId
    private String targetAttributeId;

    @NotNull
    private StatRange range;

    public void correct() {
        if (segmentType == QR_COUNT_SUM) {
            this.targetAttributeId = null;
        }
    }

    public void validate(AppSettingContext context) {
        Attribute basedAttribute = context.attributeByIdOptional(this.basedAttributeId)
                .orElseThrow(() -> new MryException(VALIDATION_ATTRIBUTE_NOT_EXIST, "基准属性项不存在。",
                        mapOf("basedAttributeId", this.basedAttributeId)));

        if (!basedAttribute.isValueNumbered()) {
            throw new MryException(ATTRIBUTE_NOT_NUMBER_VALUED, "基准属性项不支持数值。",
                    mapOf("basedAttributeId", this.basedAttributeId));
        }

        if (segmentType != QR_COUNT_SUM) {
            if (isBlank(this.targetAttributeId)) {
                throw new MryException(VALIDATION_ATTRIBUTE_NOT_EXIST, "目标属性项不存在。",
                        mapOf("targetAttributeId", this.targetAttributeId));
            }

            Attribute targetAttribute = context.attributeByIdOptional(this.targetAttributeId)
                    .orElseThrow(() -> new MryException(VALIDATION_ATTRIBUTE_NOT_EXIST, "目标属性项不存在。",
                            mapOf("targetAttributeId", this.targetAttributeId)));

            if (!targetAttribute.isValueNumbered()) {
                throw new MryException(ATTRIBUTE_NOT_NUMBER_VALUED, "目标属性项不支持数值。",
                        mapOf("targetAttributeId", this.targetAttributeId));
            }
        }
    }

    @Override
    public Set<String> awaredAttributeIds() {
        return Stream.of(this.basedAttributeId, this.targetAttributeId)
                .filter(StringUtils::isNotBlank)
                .collect(toImmutableSet());
    }
}
