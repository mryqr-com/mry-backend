package com.mryqr.core.app.domain.report.chart.attribute.setting;

import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.attribute.AttributeAware;
import com.mryqr.core.common.domain.ValueType;
import com.mryqr.core.common.domain.report.QrReportTimeBasedType;
import com.mryqr.core.common.domain.report.QrSegmentType;
import com.mryqr.core.common.domain.report.TimeSegmentInterval;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.common.utils.Identified;
import com.mryqr.core.common.validation.collection.NoNullElement;
import com.mryqr.core.common.validation.id.attribute.AttributeId;
import com.mryqr.core.common.validation.id.shoruuid.ShortUuid;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
import static com.mryqr.core.common.domain.ValueType.LOCAL_DATE_VALUE;
import static com.mryqr.core.common.domain.ValueType.TIMESTAMP_VALUE;
import static com.mryqr.core.common.domain.report.QrReportTimeBasedType.CREATED_AT;
import static com.mryqr.core.common.domain.report.QrSegmentType.QR_COUNT_SUM;
import static com.mryqr.core.common.exception.ErrorCode.ATTRIBUTE_NOT_DATE_OR_TIMESTAMP;
import static com.mryqr.core.common.exception.ErrorCode.ATTRIBUTE_NOT_NUMBER_VALUED;
import static com.mryqr.core.common.exception.ErrorCode.TIME_SEGMENT_ID_DUPLICATED;
import static com.mryqr.core.common.exception.ErrorCode.VALIDATION_ATTRIBUTE_NOT_EXIST;
import static com.mryqr.core.common.utils.Identified.isDuplicated;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static com.mryqr.core.common.utils.MryConstants.MAX_SHORT_NAME_LENGTH;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class AttributeTimeSegmentReportSetting implements AttributeAware {

    @Valid
    @NotNull
    @NotEmpty
    @NoNullElement
    @Size(max = 2)
    private List<TimeSegmentSetting> segmentSettings;

    @NotNull
    private TimeSegmentInterval interval;

    public void correct() {
        this.segmentSettings.forEach(TimeSegmentSetting::correct);
    }

    public void validate(AppSettingContext context) {
        if (isDuplicated(segmentSettings)) {
            throw new MryException(TIME_SEGMENT_ID_DUPLICATED, "统计项ID不能重复。");
        }

        this.segmentSettings.forEach(it -> it.validate(context));
    }

    @Override
    public Set<String> awaredAttributeIds() {
        return this.segmentSettings.stream().flatMap(setting -> Stream.of(setting.getBasedAttributeId(), setting.getTargetAttributeId()))
                .filter(StringUtils::isNotBlank)
                .collect(toImmutableSet());
    }

    @Getter
    @Builder
    @EqualsAndHashCode
    @AllArgsConstructor(access = PRIVATE)
    public static class TimeSegmentSetting implements Identified {
        private static final Set<ValueType> ALLOWED_BASED_TYPES = Set.of(LOCAL_DATE_VALUE, TIMESTAMP_VALUE);

        @NotNull
        @ShortUuid
        private final String id;

        @Size(max = MAX_SHORT_NAME_LENGTH)
        private String name;

        @NotNull
        private QrSegmentType segmentType;

        @NotNull
        private QrReportTimeBasedType basedType;

        @AttributeId
        private String basedAttributeId;

        @AttributeId
        private String targetAttributeId;

        public void correct() {
            if (isBlank(name)) {
                this.name = "未命名";
            }

            if (segmentType == QR_COUNT_SUM) {
                this.targetAttributeId = null;
            }

            if (basedType == CREATED_AT) {
                this.basedAttributeId = null;
            }
        }

        public void validate(AppSettingContext context) {
            if (basedType != CREATED_AT) {
                if (isBlank(this.basedAttributeId)) {
                    throw new MryException(VALIDATION_ATTRIBUTE_NOT_EXIST, "基准属性项不存在。",
                            mapOf("basedAttributeId", this.basedAttributeId));
                }

                Attribute basedAttribute = context.attributeByIdOptional(this.basedAttributeId)
                        .orElseThrow(() -> new MryException(VALIDATION_ATTRIBUTE_NOT_EXIST, "基准属性项不存在。",
                                mapOf("basedAttributeId", this.basedAttributeId)));

                if (!ALLOWED_BASED_TYPES.contains(basedAttribute.getValueType())) {
                    throw new MryException(ATTRIBUTE_NOT_DATE_OR_TIMESTAMP, "基准属性项必须为日期或时间戳类型。",
                            mapOf("basedAttributeId", this.basedAttributeId));
                }
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
        public String getIdentifier() {
            return id;
        }
    }
}
