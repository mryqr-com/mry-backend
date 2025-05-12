package com.mryqr.core.order.domain.detail;

import com.mryqr.core.order.domain.OrderPrice;
import com.mryqr.core.tenant.domain.Tenant;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;

import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_UP;
import static lombok.AccessLevel.PRIVATE;

@Getter
@SuperBuilder
@TypeAlias("EXTRA_SMS")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class ExtraSmsOrderDetail extends OrderDetail {
    private ExtraSmsAmountType amountType;

    @Override
    public String description() {
        return "增购短信" + amountType.getAmount() + "条";
    }

    @Override
    public void validate(Tenant tenant) {
        validateRequireNonFreePlan(tenant);
    }

    @Override
    public OrderPrice doCalculatePrice(Tenant tenant) {
        String priceString = valueOf(this.amountType.getPrice())
                .setScale(2, HALF_UP)
                .toString();

        return OrderPrice.builder()
                .originalUpgradePrice(null)
                .originalRenewalPrice(null)
                .originalTotalPrice(priceString)
                .discount(null)
                .discountOffsetPrice(null)
                .discountedTotalPrice(priceString)
                .build();
    }

    public int getSmsAmount() {
        return this.amountType.getAmount();
    }
}
