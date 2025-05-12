package com.mryqr.core.appmanual.domain;

import com.mryqr.core.app.domain.App;
import com.mryqr.core.common.domain.AggregateRoot;
import com.mryqr.core.common.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.mryqr.core.common.utils.MryConstants.APP_MANUAL_COLLECTION;
import static com.mryqr.core.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static lombok.AccessLevel.PRIVATE;

@Getter
@Document(APP_MANUAL_COLLECTION)
@TypeAlias(APP_MANUAL_COLLECTION)
@NoArgsConstructor(access = PRIVATE)
public class AppManual extends AggregateRoot {
    private String appId;
    private String content;

    public AppManual(App app, String content, User user) {
        super(newAppManualId(), app.getTenantId(), user);
        this.appId = app.getId();
        this.content = content;
        addOpsLog("新建", user);
    }

    public static String newAppManualId() {
        return "MAN" + newSnowflakeId();
    }

    public void updateContent(String content, User user) {
        this.content = content;
        addOpsLog("更新用户手册", user);
    }

}
