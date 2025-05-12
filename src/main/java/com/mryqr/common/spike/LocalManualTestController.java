package com.mryqr.common.spike;

import com.mryqr.common.webhook.WebhookPayload;
import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.utils.MryObjectMapper;
import com.mryqr.core.order.command.OrderCommandService;
import com.mryqr.core.order.domain.Order;
import com.mryqr.core.plan.domain.Plan;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

import static com.mryqr.core.common.domain.user.User.NOUSER;
import static com.mryqr.core.plan.domain.PlanType.valueOf;
import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.domain.Sort.by;

@Slf4j
@Validated
@RestController
@Profile("local")
@RequestMapping(value = "/local-manual-test")
@RequiredArgsConstructor
public class LocalManualTestController {
    private final OrderCommandService orderCommandService;
    private final MongoTemplate mongoTemplate;
    private final TenantRepository tenantRepository;
    private final MryObjectMapper mapper;

    @GetMapping(value = "/orders/latest/paid")
    public String notifyLatestOrderPaid() {
        Query query = new Query().with(by(DESC, "createdAt"));
        Order order = mongoTemplate.findOne(query, Order.class);
        orderCommandService.wxPay(order.getId(), "FakeWxTxnId", Instant.now(), NOUSER);
        return "Success.";
    }

    @GetMapping(value = "/change-plan-to/{planType}")
    public String changePlan(@PathVariable("planType") String planType, @AuthenticationPrincipal User user) {
        Tenant tenant = tenantRepository.byId(user.getTenantId());
        tenant.updatePlanType(valueOf(planType.toUpperCase()), now().plus(365, DAYS), NOUSER);
        tenantRepository.save(tenant);
        return "Success!";
    }

    @GetMapping(value = "/change-plan-to/expire-plan")
    public String expirePlan(@AuthenticationPrincipal User user) {
        Tenant tenant = tenantRepository.byId(user.getTenantId());
        Plan plan = tenant.currentPlan();
        tenant.updatePlanType(plan.getType(), now().minus(30, DAYS), NOUSER);
        tenantRepository.save(tenant);
        return "Success!";
    }

    @PostMapping(value = "/receive-webhook")
    public void receive(@RequestBody WebhookPayload payload) {
        log.info("Spike webhook received: {}", mapper.writeValueAsString(payload));
    }

}
