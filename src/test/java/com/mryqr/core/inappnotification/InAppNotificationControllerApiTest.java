package com.mryqr.core.inappnotification;

import com.mryqr.BaseApiTest;
import com.mryqr.common.utils.PagedList;
import com.mryqr.core.inappnotification.domain.InAppNotification;
import com.mryqr.core.inappnotification.domain.QInAppNotification;
import com.mryqr.core.inappnotification.query.ListInAppNotificationsQuery;
import com.mryqr.utils.LoginResponse;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class InAppNotificationControllerApiTest extends BaseApiTest {

    @Test
    public void should_list_my_in_app_notifications() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        String url = "https://some.notification.com";
        String content = "You've got a notification";
        List<InAppNotification> notifications = IntStream.range(0, 30)
                .mapToObj(it -> inAppNotificationFactory.createInAppNotification(loginResponse.getMemberId(),
                        loginResponse.getTenantId(),
                        url,
                        url,
                        content)).toList();

        inAppNotificationRepository.insert(notifications);

        PagedList<QInAppNotification> myInAppNotifications = InAppNotificationApi.listMyInAppNotifications(loginResponse.getJwt(),
                ListInAppNotificationsQuery.builder()
                        .pageIndex(1)
                        .pageSize(25)
                        .build());

        assertEquals(25, myInAppNotifications.getPageSize());
        assertEquals(25, myInAppNotifications.getData().size());
        assertEquals(1, myInAppNotifications.getPageIndex());
        QInAppNotification aNotification = myInAppNotifications.getData().get(0);
        assertEquals(content, aNotification.getContent());
        assertEquals(url, aNotification.getPcUrl());
        assertEquals(loginResponse.getMemberId(), aNotification.getMemberId());
        assertFalse(aNotification.isViewed());

        notifications.forEach(inAppNotification -> InAppNotificationApi.viewInAppNotification(loginResponse.getJwt(), inAppNotification.getId()));

        PagedList<QInAppNotification> myUnViewedInAppNotifications = InAppNotificationApi.listMyInAppNotifications(loginResponse.getJwt(),
                ListInAppNotificationsQuery.builder()
                        .pageIndex(1)
                        .pageSize(25)
                        .unViewedOnly(true)
                        .build());

        assertEquals(0, myUnViewedInAppNotifications.getData().size());
    }

    @Test
    public void should_mark_notification_as_viewed() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        InAppNotification notification = inAppNotificationFactory.createInAppNotification(loginResponse.getMemberId(),
                loginResponse.getTenantId(),
                "https://some.notification.com",
                "https://some.notification.com",
                "You've got a notification");
        inAppNotificationRepository.insert(notification);

        InAppNotificationApi.viewInAppNotification(loginResponse.getJwt(), notification.getId());
        InAppNotification viewedNotification = inAppNotificationRepository.byId(notification.getId());
        assertTrue(viewedNotification.isViewed());
    }

    @Test
    public void should_mark_all_notifications_as_viewed() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        InAppNotification notification1 = inAppNotificationFactory.createInAppNotification(loginResponse.getMemberId(),
                loginResponse.getTenantId(),
                "https://some.notification.com",
                "https://some.notification.com",
                "You've got a notification1");
        inAppNotificationRepository.insert(notification1);

        InAppNotification notification2 = inAppNotificationFactory.createInAppNotification(loginResponse.getMemberId(),
                loginResponse.getTenantId(),
                "https://some.notification.com",
                "https://some.notification.com",
                "You've got a notification1");
        inAppNotificationRepository.insert(notification2);

        InAppNotificationApi.markAllNotificationsAsViewed(loginResponse.getJwt());
        assertTrue(inAppNotificationRepository.byId(notification1.getId()).isViewed());
        assertTrue(inAppNotificationRepository.byId(notification2.getId()).isViewed());
    }

    @Test
    public void should_count_my_unviewed_notifications() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        assertEquals(0, InAppNotificationApi.myUnViewedInAppNotificationsCount(loginResponse.getJwt()).getCount());

        InAppNotification notification = inAppNotificationFactory.createInAppNotification(loginResponse.getMemberId(),
                loginResponse.getTenantId(),
                "https://some.notification.com",
                "https://some.notification.com",
                "You've got a notification");
        inAppNotificationRepository.insert(notification);

        assertEquals(1, InAppNotificationApi.myUnViewedInAppNotificationsCount(loginResponse.getJwt()).getCount());
    }

}
