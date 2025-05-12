package com.mryqr.core.order;

import com.mryqr.BaseApiTest;
import com.mryqr.core.common.utils.PagedList;
import com.mryqr.core.order.command.CreateOrderCommand;
import com.mryqr.core.order.command.CreateOrderResponse;
import com.mryqr.core.order.command.RequestInvoiceCommand;
import com.mryqr.core.order.domain.OrderStatus;
import com.mryqr.core.order.query.ListOrdersQuery;
import com.mryqr.core.order.query.QDetailedOrder;
import com.mryqr.core.order.query.QListOrder;
import com.mryqr.core.order.query.QPriceQuotation;
import com.mryqr.core.order.query.QuotePriceQuery;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;

public class OrderApi {

    public static Response requestQuoteRaw(String jwt, QuotePriceQuery query) {
        return BaseApiTest.given(jwt)
                .body(query)
                .when()
                .post("/orders/quotations");
    }

    public static QPriceQuotation requestQuote(String jwt, QuotePriceQuery query) {
        return requestQuoteRaw(jwt, query)
                .then()
                .statusCode(200)
                .extract()
                .as(QPriceQuotation.class);
    }

    public static Response createOrderRaw(String jwt, CreateOrderCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/orders");
    }

    public static CreateOrderResponse createOrder(String jwt, CreateOrderCommand command) {
        return createOrderRaw(jwt, command)
                .then()
                .statusCode(201)
                .extract()
                .as(CreateOrderResponse.class);
    }

    public static OrderStatus fetchOrderStatus(String jwt, String orderId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/orders/{orderId}/status", orderId)
                .then()
                .statusCode(200)
                .extract()
                .as(OrderStatus.class);
    }

    public static PagedList<QListOrder> listOrders(String jwt, ListOrdersQuery query) {
        return BaseApiTest.given(jwt)
                .body(query)
                .when()
                .post("/orders/list")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });
    }

    public static QDetailedOrder fetchDetailedOrder(String jwt, String orderId) {
        return BaseApiTest.given(jwt)
                .when()
                .get("/orders/{orderId}", orderId)
                .then()
                .statusCode(200)
                .extract()
                .as(QDetailedOrder.class);
    }

    public static Response requestInvoiceRaw(String jwt, String orderId, RequestInvoiceCommand command) {
        return BaseApiTest.given(jwt)
                .body(command)
                .when()
                .post("/orders/{orderId}/invoice-request", orderId);
    }

    public static void requestInvoice(String jwt, String orderId, RequestInvoiceCommand command) {
        requestInvoiceRaw(jwt, orderId, command)
                .then()
                .statusCode(200);
    }

}
