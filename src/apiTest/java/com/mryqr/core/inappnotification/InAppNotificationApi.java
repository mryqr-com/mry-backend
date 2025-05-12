package com.mryqr.core.inappnotification;

import com.mryqr.BaseApiTest;
import com.mryqr.common.utils.PagedList;
import com.mryqr.core.inappnotification.domain.QInAppNotification;
import com.mryqr.core.inappnotification.domain.QInAppNotificationCount;
import com.mryqr.core.inappnotification.query.ListInAppNotificationsQuery;
import io.restassured.common.mapper.TypeRef;

public class InAppNotificationApi {

    public static void viewInAppNotification(String jwt, String inAppNotificationId) {
        BaseApiTest.given(jwt)
                .when()
                .post("/in-app-notifications/{id}/viewed", inAppNotificationId)
                .then()
                .statusCode(200);
    }

    public static void markAllNotificationsAsViewed(String jwt) {
        BaseApiTest.given(jwt)
                .when()
                .post("/in-app-notifications/mark-all-as-viewed")
                .then()
                .statusCode(200);
    }

    public static PagedList<QInAppNotification> listMyInAppNotifications(String jwt, ListInAppNotificationsQuery query) {
        return BaseApiTest.given(jwt)
                .body(query)
                .when()
                .post("/in-app-notifications/my-notifications")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static QInAppNotificationCount myUnViewedInAppNotificationsCount(String jwt) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/in-app-notifications/my-unviewed-count")
                .then()
                .statusCode(200)
                .extract()
                .as(QInAppNotificationCount.class);
    }
}
