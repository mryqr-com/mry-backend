package com.mryqr.core.app.domain.report.number.attribute;

import com.mryqr.common.domain.report.NumberAggregationType;
import com.mryqr.common.exception.MryException;
import com.mryqr.common.validation.id.attribute.AttributeId;
import com.mryqr.core.app.domain.AppSettingContext;
import com.mryqr.core.app.domain.attribute.Attribute;
import com.mryqr.core.app.domain.attribute.AttributeAware;
import com.mryqr.core.app.domain.report.number.NumberReport;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import java.util.Set;

import static com.mryqr.common.exception.ErrorCode.ATTRIBUTE_NOT_NUMBER_VALUED;
import static com.mryqr.common.exception.ErrorCode.VALIDATION_ATTRIBUTE_NOT_EXIST;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Getter
@SuperBuilder
@TypeAlias("ATTRIBUTE_NUMBER_REPORT")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class AttributeNumberReport extends NumberReport implements AttributeAware {
    @NotNull
    @AttributeId
    private String attributeId;

    @NotNull
    private NumberAggregationType numberAggregationType;

    @Override
    public void validate(AppSettingContext context) {
        Attribute attribute = context.attributeByIdOptional(this.attributeId)
                .orElseThrow(() -> new MryException(VALIDATION_ATTRIBUTE_NOT_EXIST, "所引用属性项不存在。",
                        mapOf("reportId", this.getId(), "attributeId", attributeId)));

        if (!attribute.isValueNumbered()) {
            throw new MryException(ATTRIBUTE_NOT_NUMBER_VALUED, "所引用属性项不支持数字报表。",
                    mapOf("reportId", this.getId(), "attributeId", attributeId));
        }
    }

    @Override
    public Set<String> awaredAttributeIds() {
        return isNotBlank(this.attributeId) ? Set.of(this.attributeId) : Set.of();
    }
}
