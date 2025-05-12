package com.mryqr.core.appmanual.domain;

import com.mryqr.common.domain.user.User;
import com.mryqr.core.app.domain.App;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppManualFactory {
    public AppManual create(App app, String content, User user) {
        return new AppManual(app, content, user);
    }
}
