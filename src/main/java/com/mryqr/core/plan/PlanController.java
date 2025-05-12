package com.mryqr.core.plan;

import com.mryqr.core.plan.query.PlanQueryService;
import com.mryqr.core.plan.query.QListPlan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping(value = "/plans")
@RequiredArgsConstructor
public class PlanController {

    @GetMapping
    public List<QListPlan> listPlans() {
        return PlanQueryService.listPlans();
    }
}
