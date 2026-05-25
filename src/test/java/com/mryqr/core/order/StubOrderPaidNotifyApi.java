package com.mryqr.core.order;

import com.mryqr.BaseApiTest;
import com.mryqr.core.order.domain.delivery.Delivery;

public class StubOrderPaidNotifyApi {

    public static void notifyWxPaid(String orderId, String wxPayTxnId) {
        BaseApiTest.given()
                .when()
                .get("/api-testing/orders/{orderId}/wx-pay/{wxPayTxnId}", orderId, wxPayTxnId)
                .then()
                .statusCode(200);
    }

    public static void notifyWxTransferPaid(String orderId) {
        BaseApiTest.given()
                .when()
                .get("/api-testing/orders/{orderId}/wx-transfer", orderId)
                .then()
                .statusCode(200);
    }

    public static void notifyBankTransferPaid(String orderId, String bankTransferAccountId) {
        BaseApiTest.given()
                .when()
                .get("/api-testing/orders/{orderId}/bank-transfer/{bankTransferAccountId}", orderId, bankTransferAccountId)
                .then()
                .statusCode(200);
    }

    public static void updateDelivery(String orderId, Delivery delivery) {
        BaseApiTest.given()
                .body(delivery)
                .when()
                .put("/api-testing/orders/{orderId}/delivery", orderId);
    }

}
