package com.mryqr.core.inappnotification;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.utils.PagedList;
import com.mryqr.common.validation.id.inappnotification.InAppNotificationId;
import com.mryqr.core.inappnotification.command.InAppNotificationCommandService;
import com.mryqr.core.inappnotification.domain.QInAppNotification;
import com.mryqr.core.inappnotification.domain.QInAppNotificationCount;
import com.mryqr.core.inappnotification.query.InAppNotificationQueryService;
import com.mryqr.core.inappnotification.query.ListInAppNotificationsQuery;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/in-app-notifications")
public class InAppNotificationController {
    private final InAppNotificationCommandService inAppNotificationCommandService;
    private final InAppNotificationQueryService inAppNotificationQueryService;

    @PostMapping(value = "/{id}/viewed")
    public void viewInAppNotification(@PathVariable("id") @NotBlank @InAppNotificationId String inAppNotificationId,
                                      @AuthenticationPrincipal User user) {
        this.inAppNotificationCommandService.viewInAppNotification(inAppNotificationId, user);
    }

    @PostMapping(value = "/mark-all-as-viewed")
    public void markAllNotificationsAsViewed(@AuthenticationPrincipal User user) {
        this.inAppNotificationCommandService.markAllNotificationsAsViewed(user);
    }

    @PostMapping(value = "/my-notifications")
    public PagedList<QInAppNotification> listMyInAppNotifications(@RequestBody @Valid ListInAppNotificationsQuery queryCommand,
                                                                  @AuthenticationPrincipal User user) {
        return this.inAppNotificationQueryService.listMyInAppNotifications(queryCommand, user);
    }

    @GetMapping(value = "/my-unviewed-count")
    public QInAppNotificationCount myUnViewedInAppNotificationsCount(@AuthenticationPrincipal User user) {
        return QInAppNotificationCount.builder().count(
                        this.inAppNotificationQueryService.countMyUnViewedInAppNotifications(user))
                .build();
    }

}
