package com.mryqr.core.appmanual.domain;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.common.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppManualFactory {
    public AppManual create(App app, String content, User user) {
        return new AppManual(app, content, user);
    }
}
