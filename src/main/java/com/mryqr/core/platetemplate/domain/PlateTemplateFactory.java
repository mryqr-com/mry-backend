package com.mryqr.core.platetemplate.domain;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.plate.PlateSetting;
import com.mryqr.core.common.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlateTemplateFactory {

    public PlateTemplate create(App app, PlateSetting plateSetting, User user) {
        plateSetting.correct();
        plateSetting.validate(app.getSetting().context());
        return new PlateTemplate(plateSetting, user);
    }
}
