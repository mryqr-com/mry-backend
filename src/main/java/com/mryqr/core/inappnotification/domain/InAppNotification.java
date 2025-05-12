package com.mryqr.core.inappnotification.domain;

import com.mryqr.common.domain.AggregateRoot;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import static com.mryqr.common.domain.user.User.NO_USER;
import static com.mryqr.common.utils.MryConstants.IN_APP_NOTIFICATION_COLLECTION;
import static com.mryqr.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static java.util.Objects.requireNonNull;
import static lombok.AccessLevel.PRIVATE;

@Getter
@FieldNameConstants
@Document(IN_APP_NOTIFICATION_COLLECTION)
@TypeAlias(IN_APP_NOTIFICATION_COLLECTION)
@NoArgsConstructor(access = PRIVATE)
public class InAppNotification extends AggregateRoot {
    private String memberId;
    private boolean viewed;
    private String pcUrl;

    private String mobileUrl;
    private String content;

    public InAppNotification(String memberId, String tenantId, String pcUrl, String mobileUrl, String content) {
        super(newInAppNotificationId(), tenantId, NO_USER);
        requireNonNull(memberId, "memberId must not be null");
        requireNonNull(tenantId, "tenantId must not be null");
        requireNonNull(pcUrl, "pcUrl must not be null");
        requireNonNull(mobileUrl, "mobileUrl must not be null");
        requireNonNull(content, "content must not be null");

        this.memberId = memberId;
        this.pcUrl = pcUrl;
        this.mobileUrl = mobileUrl;
        this.content = content;
    }

    public static String newInAppNotificationId() {
        return "IAN" + newSnowflakeId();
    }
}
