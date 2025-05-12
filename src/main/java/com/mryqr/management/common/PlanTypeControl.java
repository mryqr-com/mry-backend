package com.mryqr.management.common;

import com.google.common.collect.ImmutableMap;
import com.mryqr.common.domain.TextOption;
import com.mryqr.core.app.domain.page.control.FDropdownControl;
import com.mryqr.core.plan.domain.PlanType;

import java.util.List;
import java.util.Map;

import static com.mryqr.common.domain.permission.Permission.CAN_MANAGE_GROUP;
import static com.mryqr.core.app.domain.page.control.ControlFillableSetting.defaultControlFillableSettingBuilder;
import static com.mryqr.core.app.domain.page.control.ControlNameSetting.defaultControlNameSetting;
import static com.mryqr.core.app.domain.page.control.ControlStyleSetting.defaultControlStyleSetting;
import static com.mryqr.core.app.domain.page.control.ControlType.DROPDOWN;
import static com.mryqr.core.app.domain.ui.BoxedTextStyle.defaultControlDescriptionStyle;
import static com.mryqr.core.plan.domain.PlanType.*;

public class PlanTypeControl {
    public static final String FREE_PLAN_OPTION_ID = "zl5YZv3lR6q1F0s86qqgNg";
    public static final String BASIC_PLAN_OPTION_ID = "zl6Zjou1QpCRolrQzsHI6w";
    public static final String ADVANCED_PLAN_OPTION_ID = "UfmCKfp8RDOniOs9yaepTg";
    public static final String PROFESSIONAL_PLAN_OPTION_ID = "i63dQLeqRt-uVBewbCcfPA";
    public static final String FLAGSHIP_PLAN_OPTION_ID = "y-U4La7dQ1qxfNpXHzZ5Sg";
    public static final Map<String, PlanType> OPTION_TO_PLAN_MAP = ImmutableMap.of(
            FREE_PLAN_OPTION_ID, FREE,
            BASIC_PLAN_OPTION_ID, BASIC,
            ADVANCED_PLAN_OPTION_ID, ADVANCED,
            PROFESSIONAL_PLAN_OPTION_ID, PROFESSIONAL,
            FLAGSHIP_PLAN_OPTION_ID, FLAGSHIP
    );

    public static final Map<PlanType, String> PLAN_TO_OPTION_MAP = ImmutableMap.of(
            FREE, FREE_PLAN_OPTION_ID,
            BASIC, BASIC_PLAN_OPTION_ID,
            ADVANCED, ADVANCED_PLAN_OPTION_ID,
            PROFESSIONAL, PROFESSIONAL_PLAN_OPTION_ID,
            FLAGSHIP, FLAGSHIP_PLAN_OPTION_ID
    );

    public static FDropdownControl createPlanTypeControl(String name, String controlId, boolean mandatory) {
        return FDropdownControl.builder()
                .type(DROPDOWN)
                .id(controlId)
                .name(name)
                .nameSetting(defaultControlNameSetting())
                .descriptionStyle(defaultControlDescriptionStyle())
                .styleSetting(defaultControlStyleSetting())
                .fillableSetting(defaultControlFillableSettingBuilder().mandatory(mandatory).build())
                .permission(CAN_MANAGE_GROUP)
                .options(List.of(
                        TextOption.builder().id(FREE_PLAN_OPTION_ID).name("免费版").build(),
                        TextOption.builder().id(BASIC_PLAN_OPTION_ID).name("基础版").build(),
                        TextOption.builder().id(ADVANCED_PLAN_OPTION_ID).name("高级版").build(),
                        TextOption.builder().id(PROFESSIONAL_PLAN_OPTION_ID).name("专业版").build(),
                        TextOption.builder().id(FLAGSHIP_PLAN_OPTION_ID).name("旗舰版").build()
                ))
                .multiple(false)
                .build();
    }
}
