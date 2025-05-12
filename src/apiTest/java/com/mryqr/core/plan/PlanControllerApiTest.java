package com.mryqr.core.plan;

import com.mryqr.BaseApiTest;
import com.mryqr.core.plan.query.QListPlan;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.mryqr.core.plan.domain.PlanType.ADVANCED;
import static com.mryqr.core.plan.domain.PlanType.BASIC;
import static com.mryqr.core.plan.domain.PlanType.FLAGSHIP;
import static com.mryqr.core.plan.domain.PlanType.FREE;
import static com.mryqr.core.plan.domain.PlanType.PROFESSIONAL;
import static com.mryqr.core.plan.query.QEnabledFeature.API_ENABLED;
import static com.mryqr.core.plan.query.QEnabledFeature.APPROVAL_ENABLED;
import static com.mryqr.core.plan.query.QEnabledFeature.APP_REPORTING;
import static com.mryqr.core.plan.query.QEnabledFeature.CUSTOM_ATTRIBUTE;
import static com.mryqr.core.plan.query.QEnabledFeature.CUSTOM_LOGO;
import static com.mryqr.core.plan.query.QEnabledFeature.CUSTOM_SUBDOMAIN;
import static com.mryqr.core.plan.query.QEnabledFeature.GEO_PREVENT_FRAUD;
import static com.mryqr.core.plan.query.QEnabledFeature.PLATE_IMAGE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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


        assertTrue(freePlan.getAllFeatures().contains(CUSTOM_ATTRIBUTE));
        assertFalse(freePlan.getAllFeatures().contains(APP_REPORTING));

        assertTrue(basicPlan.getAllFeatures().contains(PLATE_IMAGE));
        assertFalse(basicPlan.getAllFeatures().contains(CUSTOM_SUBDOMAIN));

        assertTrue(advancedPlan.getAllFeatures().contains(CUSTOM_ATTRIBUTE));
        assertTrue(advancedPlan.getAllFeatures().contains(APP_REPORTING));

        assertTrue(professionalPlan.getAllFeatures().contains(APPROVAL_ENABLED));
        assertTrue(professionalPlan.getAllFeatures().contains(CUSTOM_LOGO));

        assertTrue(flagshipPlan.getAllFeatures().contains(GEO_PREVENT_FRAUD));
        assertTrue(flagshipPlan.getAllFeatures().contains(API_ENABLED));
    }
}
