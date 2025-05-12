package com.mryqr.core.app.domain;

import com.mryqr.core.common.domain.user.User;
import com.mryqr.core.common.exception.MryException;
import com.mryqr.core.member.domain.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.mryqr.core.common.exception.ErrorCode.APP_WITH_NAME_ALREADY_EXISTS;
import static com.mryqr.core.common.exception.ErrorCode.NOT_ALL_MEMBERS_EXIST;
import static com.mryqr.core.common.utils.MapUtils.mapOf;

@Component
@RequiredArgsConstructor
public class AppDomainService {
    private final AppRepository appRepository;
    private final MemberRepository memberRepository;

    public void renameApp(App app, String name, User user) {
        if (!Objects.equals(app.getName(), name) && appRepository.cachedExistsByName(name, app.getTenantId())) {
            throw new MryException(APP_WITH_NAME_ALREADY_EXISTS, "重命名失败，应用名【" + name + "】与已有应用重复。",
                    mapOf("appId", app.getId(), "name", name));
        }

        app.rename(name, user);
    }

    public void setManagers(App app, List<String> managers, User user) {
        if (memberRepository.cachedNotAllMembersExist(managers, app.getTenantId())) {
            throw new MryException(NOT_ALL_MEMBERS_EXIST, "设置应用管理员失败，有成员不存在。", "appId", app.getId(), "managers", managers);
        }

        app.setManagers(managers, user);
    }
}
