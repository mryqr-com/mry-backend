package com.mryqr.core.order.domain.detail;

import com.mryqr.core.order.domain.OrderPrice;
import com.mryqr.core.tenant.domain.Tenant;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@TypeAlias("EXTRA_MEMBER")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class ExtraMemberOrderDetail extends OrderDetail {
    private static final int PRICE_PER_MEMBER = 200;

    @Min(1)
    @Max(5000)
    private int amount;

    @Override
    public String description() {
        return "增购成员" + amount + "名";
    }

    @Override
    public void validate(Tenant tenant) {
        validateRequireNonFreePlan(tenant);
        tenant.validateAddExtraMembers(amount);
    }

    @Override
    public OrderPrice doCalculatePrice(Tenant tenant) {
        String priceString = valueOf(this.amount)
                .multiply(valueOf(PRICE_PER_MEMBER))
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
}
