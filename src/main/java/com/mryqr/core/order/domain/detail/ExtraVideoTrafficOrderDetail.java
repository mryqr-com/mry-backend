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
@TypeAlias("EXTRA_VIDEO_TRAFFIC")
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = PRIVATE)
public class ExtraVideoTrafficOrderDetail extends OrderDetail {
    private static final double PRICE_PER_G = 0.2;

    @Min(100)
    @Max(10000)
    private int amount;

    @Override
    public String description() {
        return "增购视频流量" + amount + "G";
    }

    @Override
    public void validate(Tenant tenant) {
        validateRequireNonFreePlan(tenant);
        tenant.validateAddExtraVideoTraffic(amount);
    }

    @Override
    public OrderPrice doCalculatePrice(Tenant tenant) {
        String priceString = valueOf(this.amount)
                .multiply(valueOf(PRICE_PER_G))
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
