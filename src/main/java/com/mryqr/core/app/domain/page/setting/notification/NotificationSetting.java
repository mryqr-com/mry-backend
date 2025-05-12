package com.mryqr.core.app.domain.page.setting.notification;

import com.mryqr.common.validation.collection.NoNullElement;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.List;

import static lombok.AccessLevel.PRIVATE;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = PRIVATE)
public class NotificationSetting {
    private boolean notificationEnabled;

    @NotNull
    @NoNullElement
    @Size(max = 5)
    private List<NotificationRole> onCreateNotificationRoles;

    @NotNull
    @NoNullElement
    @Size(max = 5)
    private List<NotificationRole> onUpdateNotificationRoles;

    public void correct() {
        if (!notificationEnabled) {
            this.onCreateNotificationRoles = List.of();
            this.onUpdateNotificationRoles = List.of();
        }
    }

    public void reset() {
        this.notificationEnabled = false;
        this.onCreateNotificationRoles = List.of();
        this.onUpdateNotificationRoles = List.of();
    }

    public boolean shouldNotifyOnCreateSubmission() {
        return notificationEnabled && isNotEmpty(onCreateNotificationRoles);
    }

    public boolean shouldNotifyOnUpdateSubmission() {
        return notificationEnabled && isNotEmpty(onUpdateNotificationRoles);
    }
}
