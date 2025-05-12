package com.mryqr.core.plan;

import com.mryqr.BaseApiTest;
import com.mryqr.core.plan.query.QListPlan;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.utils.LoginResponse;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static com.mryqr.core.plan.domain.Plan.BASIC_PLAN;
import static com.mryqr.core.plan.domain.Plan.FREE_PLAN;
import static com.mryqr.core.plan.domain.PlanType.*;
import static com.mryqr.core.plan.query.QEnabledFeature.GEO_PREVENT_FRAUD;
import static com.mryqr.utils.RandomTestFixture.rMobile;
import static com.mryqr.utils.RandomTestFixture.rPassword;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PlanControllerApiTest extends BaseApiTest {

    @Test
    public void should_fetch_all_plans_info() {
        List<QListPlan> planInfos = PlanApi.listPlans();
        QListPlan freePlan = planInfos.get(0);
        QListPlan basicPlan = planInfos.get(1);
        QListPlan advancedPlan = planInfos.get(2);
        QListPlan professionalPlan = planInfos.get(3);
        QListPlan flagshipPlan = planInfos.get(4);
        assertEquals(FREE, freePlan.getType());
        assertEquals(BASIC, basicPlan.getType());
        assertEquals(ADVANCED, advancedPlan.getType());
        assertEquals(PROFESSIONAL, professionalPlan.getType());
        assertEquals(FLAGSHIP, flagshipPlan.getType());

        assertEquals(1, freePlan.getLevel());
        assertEquals(2, basicPlan.getLevel());
        assertEquals(3, advancedPlan.getLevel());
        assertEquals(4, professionalPlan.getLevel());
        assertEquals(5, flagshipPlan.getLevel());

        assertTrue(flagshipPlan.getAllFeatures().contains(GEO_PREVENT_FRAUD));
    }

    @Test
    public void expired_plan_should_fall_back_to_free_plan() {
        LoginResponse response = setupApi.registerWithLogin(rMobile(), rPassword());
        Tenant theTenant = tenantRepository.byId(response.getTenantId());
        setupApi.updateTenantPackages(theTenant, BASIC, Instant.now().minus(10, DAYS));
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertEquals(FREE_PLAN, tenant.effectivePlan());
        assertEquals(BASIC_PLAN, tenant.currentPlan());
    }
}
