package com.mryqr.core.order.domain.detail;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.order.domain.OrderPrice;
import com.mryqr.core.tenant.domain.Tenant;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.mryqr.core.common.exception.ErrorCode.ORDER_REQUIRE_NON_FREE_PLAN;
import static com.mryqr.core.common.utils.MapUtils.mapOf;
import static lombok.AccessLevel.PROTECTED;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes(value = {
        @JsonSubTypes.Type(value = PlanOrderDetail.class, name = "PLAN"),
        @JsonSubTypes.Type(value = ExtraMemberOrderDetail.class, name = "EXTRA_MEMBER"),
        @JsonSubTypes.Type(value = ExtraSmsOrderDetail.class, name = "EXTRA_SMS"),
        @JsonSubTypes.Type(value = ExtraStorageOrderDetail.class, name = "EXTRA_STORAGE"),
        @JsonSubTypes.Type(value = ExtraVideoTrafficOrderDetail.class, name = "EXTRA_VIDEO_TRAFFIC"),
        @JsonSubTypes.Type(value = PlatePrintingOrderDetail.class, name = "PLATE_PRINTING"),
})

@Getter
@Document
@SuperBuilder
@EqualsAndHashCode
@NoArgsConstructor(access = PROTECTED)
public abstract class OrderDetail {
    @NotNull
    private OrderDetailType type;

    public abstract String description();

    public abstract void validate(Tenant tenant);

    protected void validateRequireNonFreePlan(Tenant tenant) {
        if (tenant.isEffectiveFreePlan()) {
            throw new MryException(ORDER_REQUIRE_NON_FREE_PLAN,
                    "您当前有效套餐为免费版，无法购买，请升级到付费版套餐后再进行购买。",
                    mapOf("orderDetailType", getType()));
        }
    }

    protected abstract OrderPrice doCalculatePrice(Tenant tenant);

    public OrderPrice calculatePrice(Tenant tenant) {
        OrderPrice price = doCalculatePrice(tenant);
        if (tenant.isMryManageTenant() || tenant.isMryTestingTenant()) {
            return OrderPrice.builder()
                    .originalUpgradePrice(price.getOriginalUpgradePrice())
                    .originalRenewalPrice(price.getOriginalRenewalPrice())
                    .originalTotalPrice(price.getOriginalTotalPrice())
                    .deliveryFee(price.getDeliveryFee())
                    .discount(price.getDiscount())
                    .discountOffsetPrice(price.getDiscountOffsetPrice())
                    .discountedTotalPrice("0.01")
                    .build();
        }
        return price;
    }

}
