package com.mryqr.core.order;

import com.mryqr.BaseApiTest;
import com.mryqr.core.common.domain.UploadedFile;
import com.mryqr.core.common.domain.invoice.InvoiceTitle;
import com.mryqr.core.common.utils.PagedList;
import com.mryqr.core.login.LoginApi;
import com.mryqr.core.order.command.CreateOrderCommand;
import com.mryqr.core.order.command.CreateOrderResponse;
import com.mryqr.core.order.command.OrderCommandService;
import com.mryqr.core.order.command.RequestInvoiceCommand;
import com.mryqr.core.order.domain.Order;
import com.mryqr.core.order.domain.OrderPrice;
import com.mryqr.core.order.domain.OrderStatus;
import com.mryqr.core.order.domain.delivery.Carrier;
import com.mryqr.core.order.domain.delivery.Consignee;
import com.mryqr.core.order.domain.delivery.Delivery;
import com.mryqr.core.order.domain.detail.ExtraMemberOrderDetail;
import com.mryqr.core.order.domain.detail.ExtraSmsOrderDetail;
import com.mryqr.core.order.domain.detail.ExtraStorageOrderDetail;
import com.mryqr.core.order.domain.detail.ExtraVideoTrafficOrderDetail;
import com.mryqr.core.order.domain.detail.PlanOrderDetail;
import com.mryqr.core.order.domain.detail.PlatePrintingOrderDetail;
import com.mryqr.core.order.query.ListOrdersQuery;
import com.mryqr.core.order.query.QDetailedOrder;
import com.mryqr.core.order.query.QListOrder;
import com.mryqr.core.order.query.QPriceQuotation;
import com.mryqr.core.order.query.QuotePriceQuery;
import com.mryqr.core.tenant.TenantApi;
import com.mryqr.core.tenant.command.UpdateTenantInvoiceTitleCommand;
import com.mryqr.core.tenant.domain.Packages;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.utils.LoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static com.mryqr.core.common.domain.user.User.NOUSER;
import static com.mryqr.core.common.exception.ErrorCode.DOWNGRADE_PLAN_NOT_ALLOWED;
import static com.mryqr.core.common.exception.ErrorCode.INVOICE_ALREADY_REQUESTED;
import static com.mryqr.core.common.exception.ErrorCode.MAX_EXTRA_STORAGE_REACHED;
import static com.mryqr.core.common.exception.ErrorCode.MAX_TENANT_MEMBER_SIZE_REACHED;
import static com.mryqr.core.common.exception.ErrorCode.MAX_VIDEO_TRAFFIC_REACHED;
import static com.mryqr.core.common.exception.ErrorCode.NO_INVOICE_TITLE;
import static com.mryqr.core.common.exception.ErrorCode.ORDER_NOT_PAID;
import static com.mryqr.core.common.exception.ErrorCode.ORDER_REQUIRE_NON_FREE_PLAN;
import static com.mryqr.core.common.exception.ErrorCode.PACKAGE_DURATION_TOO_LONG;
import static com.mryqr.core.common.exception.ErrorCode.PURCHASE_FREE_PLAN_NOT_ALLOWED;
import static com.mryqr.core.common.exception.ErrorCode.UPGRADE_FREE_PLAN_NOT_ALLOWED;
import static com.mryqr.core.common.exception.ErrorCode.UPGRADE_TO_SAME_PLAN_NOT_ALLOWED;
import static com.mryqr.core.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.order.domain.OrderStatus.CREATED;
import static com.mryqr.core.order.domain.OrderStatus.PAID;
import static com.mryqr.core.order.domain.OrderStatus.REFUNDED;
import static com.mryqr.core.order.domain.PaymentType.BANK_TRANSFER;
import static com.mryqr.core.order.domain.PaymentType.WX_NATIVE;
import static com.mryqr.core.order.domain.PaymentType.WX_TRANSFER;
import static com.mryqr.core.order.domain.detail.ExtraSmsAmountType.TWO_K;
import static com.mryqr.core.order.domain.detail.OrderDetailType.EXTRA_MEMBER;
import static com.mryqr.core.order.domain.detail.OrderDetailType.EXTRA_SMS;
import static com.mryqr.core.order.domain.detail.OrderDetailType.EXTRA_STORAGE;
import static com.mryqr.core.order.domain.detail.OrderDetailType.EXTRA_VIDEO_TRAFFIC;
import static com.mryqr.core.order.domain.detail.OrderDetailType.PLAN;
import static com.mryqr.core.order.domain.detail.OrderDetailType.PLATE_PRINTING;
import static com.mryqr.core.order.domain.invoice.InvoiceType.VAT_NORMAL;
import static com.mryqr.core.plan.domain.PlanType.ADVANCED;
import static com.mryqr.core.plan.domain.PlanType.BASIC;
import static com.mryqr.core.plan.domain.PlanType.FLAGSHIP;
import static com.mryqr.core.plan.domain.PlanType.FREE;
import static com.mryqr.core.plan.domain.PlanType.PROFESSIONAL;
import static com.mryqr.core.printing.domain.PlatePrintingType.TRANSPARENT_ACRYLIC_70x50;
import static com.mryqr.management.MryManageTenant.ADMIN_INIT_MOBILE;
import static com.mryqr.management.MryManageTenant.ADMIN_INIT_PASSWORD;
import static com.mryqr.utils.RandomTestFixture.rAddress;
import static com.mryqr.utils.RandomTestFixture.rDeliveryId;
import static com.mryqr.utils.RandomTestFixture.rEmail;
import static com.mryqr.utils.RandomTestFixture.rMemberName;
import static com.mryqr.utils.RandomTestFixture.rMobile;
import static com.mryqr.utils.RandomTestFixture.rUploadedFile;
import static com.mryqr.utils.RandomTestFixture.rWxPayTxnId;
import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.DAYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OrderControllerApiTest extends BaseApiTest {

    @Autowired
    private OrderCommandService orderCommandService;

    @Test
    public void should_create_online_plan_order_with_wx_native_payment_type() {
        LoginResponse response = setupApi.registerWithLogin();

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(ADVANCED)
                        .yearDuration(2)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);

        Order order = orderRepository.byId(orderResponse.getId());
        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertNotNull(orderResponse.getWxPayQrUrl());
        assertEquals(order.getWxPayQrUrl(), orderResponse.getWxPayQrUrl());
        assertEquals(order.getPrice(), orderResponse.getPrice());
        assertNotNull(orderResponse.getPayDescription());
        assertEquals(WX_NATIVE, orderResponse.getPaymentType());
        assertEquals(order.getCreatedAt(), orderResponse.getCreatedAt());
        assertEquals(tenant.getPackages().planVersion(), order.getPlanVersion());
        assertNull(orderResponse.getBankTransferCode());
        assertTrue(order.getDetail() instanceof PlanOrderDetail);
    }

    @Test
    public void should_create_online_plan_order_with_bank_transfer_payment_type() {
        LoginResponse response = setupApi.registerWithLogin();

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(ADVANCED)
                        .yearDuration(2)
                        .build())
                .paymentType(BANK_TRANSFER)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);

        assertNull(orderResponse.getWxPayQrUrl());
        assertEquals(BANK_TRANSFER, orderResponse.getPaymentType());
        assertNotNull(orderResponse.getBankTransferCode());
    }

    @Test
    public void should_create_extra_member_order() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        setupApi.updateTenantPackages(loginResponse.getTenantId(), ADVANCED);

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(ExtraMemberOrderDetail.builder()
                        .type(EXTRA_MEMBER)
                        .amount(10)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(loginResponse.getJwt(), command);
        Order order = orderRepository.byId(orderResponse.getId());
        assertEquals("2000.00", orderResponse.getPrice().getDiscountedTotalPrice());
        assertTrue(order.getDetail() instanceof ExtraMemberOrderDetail);
    }

    @Test
    public void should_create_extra_sms_order() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        setupApi.updateTenantPackages(loginResponse.getTenantId(), ADVANCED);

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(ExtraSmsOrderDetail.builder()
                        .type(EXTRA_SMS)
                        .amountType(TWO_K)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(loginResponse.getJwt(), command);
        Order order = orderRepository.byId(orderResponse.getId());
        assertEquals("160.00", orderResponse.getPrice().getDiscountedTotalPrice());
        assertTrue(order.getDetail() instanceof ExtraSmsOrderDetail);
    }

    @Test
    public void should_create_extra_storage_order() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        setupApi.updateTenantPackages(loginResponse.getTenantId(), ADVANCED);

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(ExtraStorageOrderDetail.builder()
                        .type(EXTRA_STORAGE)
                        .amount(10)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(loginResponse.getJwt(), command);
        Order order = orderRepository.byId(orderResponse.getId());
        assertEquals("300.00", orderResponse.getPrice().getDiscountedTotalPrice());
        assertTrue(order.getDetail() instanceof ExtraStorageOrderDetail);
    }

    @Test
    public void should_create_extra_video_traffic_order() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        setupApi.updateTenantPackages(loginResponse.getTenantId(), ADVANCED);

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(ExtraVideoTrafficOrderDetail.builder()
                        .type(EXTRA_VIDEO_TRAFFIC)
                        .amount(100)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(loginResponse.getJwt(), command);
        Order order = orderRepository.byId(orderResponse.getId());

        assertEquals("20.00", orderResponse.getPrice().getDiscountedTotalPrice());
        assertTrue(order.getDetail() instanceof ExtraVideoTrafficOrderDetail);
    }

    @Test
    public void should_create_plate_printing_order() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(PlatePrintingOrderDetail.builder()
                        .type(PLATE_PRINTING)
                        .plateType(TRANSPARENT_ACRYLIC_70x50)
                        .files(List.of(rUploadedFile()))
                        .amount(100)
                        .consignee(Consignee.builder()
                                .id(newShortUuid())
                                .name(rMemberName())
                                .mobile(rMobile())
                                .address(rAddress())
                                .build())
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(loginResponse.getJwt(), command);
        Order order = orderRepository.byId(orderResponse.getId());

        assertEquals("400.00", orderResponse.getPrice().getDiscountedTotalPrice());
        assertEquals("0.00", orderResponse.getPrice().getDeliveryFee());
        assertTrue(order.getDetail() instanceof PlatePrintingOrderDetail);
    }

    @Test
    public void should_fail_create_plan_order_if_result_in_more_than_2_years_expiry() {
        LoginResponse response = setupApi.registerWithLogin();

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(ADVANCED)
                        .yearDuration(3)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        assertError(() -> OrderApi.createOrderRaw(response.getJwt(), command), PACKAGE_DURATION_TOO_LONG);
    }

    @Test
    public void should_fail_create_extra_member_order_if_reached_max_member_size() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        setupApi.updateTenantPackages(loginResponse.getTenantId(), ADVANCED);

        Tenant tenant = tenantRepository.byId(loginResponse.getTenantId());
        tenant.getPackages().increaseExtraMemberCount(8000);
        tenantRepository.save(tenant);

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(ExtraMemberOrderDetail.builder()
                        .type(EXTRA_MEMBER)
                        .amount(3000)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        assertError(() -> OrderApi.createOrderRaw(loginResponse.getJwt(), command), MAX_TENANT_MEMBER_SIZE_REACHED);
    }

    @Test
    public void should_fail_create_extra_storage_order_if_reached_max_storage_size() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        setupApi.updateTenantPackages(loginResponse.getTenantId(), ADVANCED);

        Tenant tenant = tenantRepository.byId(loginResponse.getTenantId());
        tenant.getPackages().increaseExtraStorage(8000);
        tenantRepository.save(tenant);

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(ExtraStorageOrderDetail.builder()
                        .type(EXTRA_STORAGE)
                        .amount(3000)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        assertError(() -> OrderApi.createOrderRaw(loginResponse.getJwt(), command), MAX_EXTRA_STORAGE_REACHED);
    }

    @Test
    public void should_fail_create_extra_video_traffic_order_if_reached_max_size() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        setupApi.updateTenantPackages(loginResponse.getTenantId(), ADVANCED);

        Tenant tenant = tenantRepository.byId(loginResponse.getTenantId());
        tenant.getPackages().increaseExtraVideoTraffic(8000);
        tenantRepository.save(tenant);

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(ExtraVideoTrafficOrderDetail.builder()
                        .type(EXTRA_VIDEO_TRAFFIC)
                        .amount(3000)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        assertError(() -> OrderApi.createOrderRaw(loginResponse.getJwt(), command), MAX_VIDEO_TRAFFIC_REACHED);
    }

    @Test
    public void should_fail_create_order_if_non_free_plan_needed() {
        LoginResponse response = setupApi.registerWithLogin();

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(ExtraStorageOrderDetail.builder()
                        .type(EXTRA_STORAGE)
                        .amount(3000)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        assertError(() -> OrderApi.createOrderRaw(response.getJwt(), command), ORDER_REQUIRE_NON_FREE_PLAN);
    }

    @Test
    public void should_request_quote_for_free_plan() {
        LoginResponse loginResponse = setupApi.registerWithLogin();

        QuotePriceQuery query = QuotePriceQuery.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(ADVANCED)
                        .yearDuration(2)
                        .build())
                .build();

        QPriceQuotation quotation = OrderApi.requestQuote(loginResponse.getJwt(), query);
        OrderPrice price = quotation.getPrice();
        assertNull(price.getOriginalUpgradePrice());
        assertEquals("2760.00", price.getOriginalRenewalPrice());
        assertEquals("2760.00", price.getOriginalTotalPrice());
        assertEquals("9.0", price.getDiscount());
        assertEquals("276.00", price.getDiscountOffsetPrice());
        assertEquals("2484.00", price.getDiscountedTotalPrice());
    }

    @Test
    public void should_request_quote_for_expired_plan() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(loginResponse.getTenantId());
        setupApi.updateTenantPackages(tenant, BASIC, Instant.now().minus(10, DAYS));

        QuotePriceQuery query = QuotePriceQuery.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(ADVANCED)
                        .yearDuration(2)
                        .build())
                .build();

        QPriceQuotation quotation = OrderApi.requestQuote(loginResponse.getJwt(), query);
        OrderPrice price = quotation.getPrice();
        assertNull(price.getOriginalUpgradePrice());
        assertEquals("2760.00", price.getOriginalRenewalPrice());
        assertEquals("2760.00", price.getOriginalTotalPrice());
        assertEquals("9.0", price.getDiscount());
        assertEquals("276.00", price.getDiscountOffsetPrice());
        assertEquals("2484.00", price.getDiscountedTotalPrice());
    }

    @Test
    public void should_request_quote_for_non_free_plan_for_upgrade_only() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(loginResponse.getTenantId());
        setupApi.updateTenantPackages(tenant, BASIC, Instant.now().plus(181, DAYS));

        QuotePriceQuery query = QuotePriceQuery.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(ADVANCED)
                        .yearDuration(0)
                        .build())
                .build();

        QPriceQuotation quotation = OrderApi.requestQuote(loginResponse.getJwt(), query);
        OrderPrice price = quotation.getPrice();
        assertNull(price.getOriginalRenewalPrice());


        assertEquals("345.21", price.getOriginalUpgradePrice());
        assertEquals("345.21", price.getOriginalTotalPrice());
        assertNull(price.getDiscount());
        assertNull(price.getDiscountOffsetPrice());
        assertEquals("345.21", price.getDiscountedTotalPrice());
    }

    @Test
    public void should_request_quote_for_non_free_plan_for_renewal_only() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(loginResponse.getTenantId());
        setupApi.updateTenantPackages(tenant, BASIC, Instant.now().plus(181, DAYS));

        QuotePriceQuery query = QuotePriceQuery.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(BASIC)
                        .yearDuration(1)
                        .build())
                .build();

        QPriceQuotation quotation = OrderApi.requestQuote(loginResponse.getJwt(), query);
        OrderPrice price = quotation.getPrice();
        assertNull(price.getOriginalUpgradePrice());

        assertEquals("680.00", price.getOriginalRenewalPrice());
        assertEquals("680.00", price.getOriginalTotalPrice());
        assertEquals("9.5", price.getDiscount());
        assertEquals("34.00", price.getDiscountOffsetPrice());
        assertEquals("646.00", price.getDiscountedTotalPrice());
    }

    @Test
    public void should_request_quote_for_non_free_plan_for_renewal_and_upgrade() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        Tenant tenant = tenantRepository.byId(loginResponse.getTenantId());
        setupApi.updateTenantPackages(tenant, BASIC, Instant.now().plus(181, DAYS));

        QuotePriceQuery query = QuotePriceQuery.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(PROFESSIONAL)
                        .yearDuration(1)
                        .build())
                .build();

        QPriceQuotation quotation = OrderApi.requestQuote(loginResponse.getJwt(), query);
        OrderPrice price = quotation.getPrice();

        assertEquals("3106.85", price.getOriginalUpgradePrice());
        assertEquals("6980.00", price.getOriginalRenewalPrice());
        assertEquals("10086.85", price.getOriginalTotalPrice());
        assertEquals("9.5", price.getDiscount());
        assertEquals("504.34", price.getDiscountOffsetPrice());
        assertEquals("9582.51", price.getDiscountedTotalPrice());
    }

    @Test
    public void should_request_quote_special_price_for_mry_management_account() {
        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);

        QuotePriceQuery query = QuotePriceQuery.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(FLAGSHIP)
                        .yearDuration(1)
                        .build())
                .build();

        QPriceQuotation quotation = OrderApi.requestQuote(jwt, query);
        assertEquals("0.01", quotation.getPrice().getDiscountedTotalPrice());
    }

    @Test
    public void should_fail_request_free_plan_quote() {
        LoginResponse loginResponse = setupApi.registerWithLogin();

        QuotePriceQuery query = QuotePriceQuery.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(FREE)
                        .yearDuration(2)
                        .build())
                .build();

        assertError(() -> OrderApi.requestQuoteRaw(loginResponse.getJwt(), query), PURCHASE_FREE_PLAN_NOT_ALLOWED);
    }

    @Test
    public void should_fail_request_quote_if_upgrade_only_for_free_plan() {
        LoginResponse loginResponse = setupApi.registerWithLogin();

        QuotePriceQuery query = QuotePriceQuery.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(BASIC)
                        .yearDuration(0)
                        .build())
                .build();

        assertError(() -> OrderApi.requestQuoteRaw(loginResponse.getJwt(), query), UPGRADE_FREE_PLAN_NOT_ALLOWED);
    }

    @Test
    public void should_fail_request_quote_if_renewal_only_for_same_plan() {
        LoginResponse loginResponse = setupApi.registerWithLogin();

        Tenant tenant = tenantRepository.byId(loginResponse.getTenantId());
        setupApi.updateTenantPackages(tenant, BASIC, Instant.now().plus(181, DAYS));

        QuotePriceQuery query = QuotePriceQuery.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(BASIC)
                        .yearDuration(0)
                        .build())
                .build();

        assertError(() -> OrderApi.requestQuoteRaw(loginResponse.getJwt(), query), UPGRADE_TO_SAME_PLAN_NOT_ALLOWED);
    }

    @Test
    public void should_fail_request_quote_if_downgrade() {
        LoginResponse loginResponse = setupApi.registerWithLogin();

        Tenant tenant = tenantRepository.byId(loginResponse.getTenantId());
        setupApi.updateTenantPackages(tenant, ADVANCED, Instant.now().plus(181, DAYS));

        QuotePriceQuery query = QuotePriceQuery.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(BASIC)
                        .yearDuration(1)
                        .build())
                .build();

        assertError(() -> OrderApi.requestQuoteRaw(loginResponse.getJwt(), query), DOWNGRADE_PLAN_NOT_ALLOWED);
    }

    @Test
    public void should_request_quote_for_extra_member() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        setupApi.updateTenantPackages(loginResponse.getTenantId(), ADVANCED);

        QuotePriceQuery query = QuotePriceQuery.builder()
                .detail(ExtraMemberOrderDetail.builder()
                        .type(EXTRA_MEMBER)
                        .amount(10)
                        .build())
                .build();

        QPriceQuotation quotation = OrderApi.requestQuote(loginResponse.getJwt(), query);
        OrderPrice price = quotation.getPrice();

        assertNull(price.getOriginalUpgradePrice());
        assertNull(price.getOriginalRenewalPrice());
        assertEquals("2000.00", price.getOriginalTotalPrice());
        assertNull(price.getDiscount());
        assertNull(price.getDiscountOffsetPrice());
        assertEquals("2000.00", price.getDiscountedTotalPrice());
    }

    @Test
    public void should_request_quote_for_extra_sms() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        setupApi.updateTenantPackages(loginResponse.getTenantId(), ADVANCED);

        QuotePriceQuery query = QuotePriceQuery.builder()
                .detail(ExtraSmsOrderDetail.builder()
                        .type(EXTRA_SMS)
                        .amountType(TWO_K)
                        .build())
                .build();

        QPriceQuotation quotation = OrderApi.requestQuote(loginResponse.getJwt(), query);
        OrderPrice price = quotation.getPrice();

        assertNull(price.getOriginalUpgradePrice());
        assertNull(price.getOriginalRenewalPrice());
        assertEquals("160.00", price.getOriginalTotalPrice());
        assertNull(price.getDiscount());
        assertNull(price.getDiscountOffsetPrice());
        assertEquals("160.00", price.getDiscountedTotalPrice());
    }

    @Test
    public void should_request_quote_for_extra_storage() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        setupApi.updateTenantPackages(loginResponse.getTenantId(), ADVANCED);

        QuotePriceQuery query = QuotePriceQuery.builder()
                .detail(ExtraStorageOrderDetail.builder()
                        .type(EXTRA_STORAGE)
                        .amount(10)
                        .build())
                .build();

        QPriceQuotation quotation = OrderApi.requestQuote(loginResponse.getJwt(), query);
        OrderPrice price = quotation.getPrice();

        assertNull(price.getOriginalUpgradePrice());
        assertNull(price.getOriginalRenewalPrice());
        assertEquals("300.00", price.getOriginalTotalPrice());
        assertNull(price.getDiscount());
        assertNull(price.getDiscountOffsetPrice());
        assertEquals("300.00", price.getDiscountedTotalPrice());
    }

    @Test
    public void should_request_quote_for_extra_video_traffic() {
        LoginResponse loginResponse = setupApi.registerWithLogin();
        setupApi.updateTenantPackages(loginResponse.getTenantId(), ADVANCED);

        QuotePriceQuery query = QuotePriceQuery.builder()
                .detail(ExtraVideoTrafficOrderDetail.builder()
                        .type(EXTRA_VIDEO_TRAFFIC)
                        .amount(1000)
                        .build())
                .build();

        QPriceQuotation quotation = OrderApi.requestQuote(loginResponse.getJwt(), query);
        OrderPrice price = quotation.getPrice();

        assertNull(price.getOriginalUpgradePrice());
        assertNull(price.getOriginalRenewalPrice());
        assertEquals("200.00", price.getOriginalTotalPrice());
        assertNull(price.getDiscount());
        assertNull(price.getDiscountOffsetPrice());
        assertEquals("200.00", price.getDiscountedTotalPrice());
    }


    @Test
    public void should_request_quote_for_PLATE_PRINTING_order() {
        LoginResponse loginResponse = setupApi.registerWithLogin();

        QuotePriceQuery query = QuotePriceQuery.builder()
                .detail(PlatePrintingOrderDetail.builder()
                        .type(PLATE_PRINTING)
                        .plateType(TRANSPARENT_ACRYLIC_70x50)
                        .files(List.of(rUploadedFile()))
                        .amount(100)
                        .consignee(Consignee.builder()
                                .id(newShortUuid())
                                .name(rMemberName())
                                .mobile(rMobile())
                                .address(rAddress())
                                .build())
                        .build())
                .build();

        QPriceQuotation quotation = OrderApi.requestQuote(loginResponse.getJwt(), query);
        OrderPrice price = quotation.getPrice();

        assertNull(price.getOriginalUpgradePrice());
        assertNull(price.getOriginalRenewalPrice());
        assertEquals("0.00", price.getDeliveryFee());
        assertEquals("400.00", price.getOriginalTotalPrice());
        assertNull(price.getDiscount());
        assertNull(price.getDiscountOffsetPrice());
        assertEquals("400.00", price.getDiscountedTotalPrice());
    }

    @Test
    public void should_fetch_order_status() {
        LoginResponse response = setupApi.registerWithLogin();

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(ADVANCED)
                        .yearDuration(2)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);
        OrderStatus orderStatus = OrderApi.fetchOrderStatus(response.getJwt(), orderResponse.getId());
        assertEquals(CREATED, orderStatus);
    }

    @Test
    public void should_stub_notify_plan_order_paid() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant initialTenant = tenantRepository.byId(response.getTenantId());

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(ADVANCED)
                        .yearDuration(2)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);

        StubOrderPaidNotifyApi.notifyWxPaid(orderResponse.getId(), "fakeWxPayTxnId");

        Order order = orderRepository.byId(orderResponse.getId());
        assertTrue(order.atPaid());
        assertEquals(order.getStatus(), PAID);
        assertNotNull(order.getPaidAt());
        assertEquals("fakeWxPayTxnId", order.getWxTxnId());

        Tenant tenant = tenantRepository.byId(response.getTenantId());
        Packages packages = tenant.getPackages();
        assertEquals(ADVANCED, packages.currentPlanType());
        assertEquals(LocalDate.now().plusYears(2).toString(), LocalDate.ofInstant(packages.expireAt(), systemDefault()).toString());
        assertNotEquals(initialTenant.getPackages().planVersion(), packages.planVersion());
    }

    @Test
    public void should_stub_notify_extra_member_order_paid() {
        LoginResponse response = setupApi.registerWithLogin();
        setupApi.updateTenantPackages(response.getTenantId(), ADVANCED);

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(ExtraMemberOrderDetail.builder()
                        .type(EXTRA_MEMBER)
                        .amount(10)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);
        StubOrderPaidNotifyApi.notifyWxPaid(orderResponse.getId(), "fakeWxPayTxnId");

        Order order = orderRepository.byId(orderResponse.getId());
        assertTrue(order.atPaid());
        assertEquals(order.getStatus(), PAID);
        assertNotNull(order.getPaidAt());
        assertEquals("fakeWxPayTxnId", order.getWxTxnId());

        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertEquals(10, tenant.getPackages().getExtraMemberCount());
    }

    @Test
    public void should_stub_notify_extra_sms_order_paid() {
        LoginResponse response = setupApi.registerWithLogin();
        setupApi.updateTenantPackages(response.getTenantId(), ADVANCED);

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(ExtraSmsOrderDetail.builder()
                        .type(EXTRA_SMS)
                        .amountType(TWO_K)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);
        StubOrderPaidNotifyApi.notifyWxPaid(orderResponse.getId(), "fakeWxPayTxnId");

        Order order = orderRepository.byId(orderResponse.getId());
        assertTrue(order.atPaid());
        assertEquals(order.getStatus(), PAID);
        assertNotNull(order.getPaidAt());
        assertEquals("fakeWxPayTxnId", order.getWxTxnId());

        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertEquals(2000, tenant.getPackages().getExtraRemainSmsCount());
    }

    @Test
    public void should_stub_notify_extra_storage_order_paid() {
        LoginResponse response = setupApi.registerWithLogin();
        setupApi.updateTenantPackages(response.getTenantId(), ADVANCED);

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(ExtraStorageOrderDetail.builder()
                        .type(EXTRA_STORAGE)
                        .amount(10)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);
        StubOrderPaidNotifyApi.notifyWxPaid(orderResponse.getId(), "fakeWxPayTxnId");

        Order order = orderRepository.byId(orderResponse.getId());
        assertTrue(order.atPaid());
        assertEquals(order.getStatus(), PAID);
        assertNotNull(order.getPaidAt());
        assertEquals("fakeWxPayTxnId", order.getWxTxnId());

        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertEquals(10, tenant.getPackages().getExtraStorage());
    }

    @Test
    public void should_stub_notify_extra_video_traffic_order_paid() {
        LoginResponse response = setupApi.registerWithLogin();
        setupApi.updateTenantPackages(response.getTenantId(), ADVANCED);

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(ExtraVideoTrafficOrderDetail.builder()
                        .type(EXTRA_VIDEO_TRAFFIC)
                        .amount(100)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);
        StubOrderPaidNotifyApi.notifyWxPaid(orderResponse.getId(), "fakeWxPayTxnId");

        Order order = orderRepository.byId(orderResponse.getId());
        assertTrue(order.atPaid());
        assertEquals(order.getStatus(), PAID);
        assertNotNull(order.getPaidAt());
        assertEquals("fakeWxPayTxnId", order.getWxTxnId());

        Tenant tenant = tenantRepository.byId(response.getTenantId());
        assertEquals(100, tenant.getPackages().getExtraRemainVideoTraffic());
    }

    @Test
    public void should_stub_notify_PLATE_PRINTING_order_paid() {
        LoginResponse response = setupApi.registerWithLogin();

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(PlatePrintingOrderDetail.builder()
                        .type(PLATE_PRINTING)
                        .plateType(TRANSPARENT_ACRYLIC_70x50)
                        .files(List.of(rUploadedFile()))
                        .amount(100)
                        .consignee(Consignee.builder()
                                .id(newShortUuid())
                                .name(rMemberName())
                                .mobile(rMobile())
                                .address(rAddress())
                                .build())
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);
        StubOrderPaidNotifyApi.notifyWxPaid(orderResponse.getId(), "fakeWxPayTxnId");

        Order order = orderRepository.byId(orderResponse.getId());
        assertTrue(order.atPaid());
        assertEquals(order.getStatus(), PAID);
        assertNotNull(order.getPaidAt());
        assertEquals("fakeWxPayTxnId", order.getWxTxnId());
    }

    @Test
    public void should_only_update_wx_info_if_called_repeatedly() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant initialTenant = tenantRepository.byId(response.getTenantId());

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(ADVANCED)
                        .yearDuration(2)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);

        StubOrderPaidNotifyApi.notifyWxPaid(orderResponse.getId(), "fakeWxPayTxnId");
        StubOrderPaidNotifyApi.notifyWxPaid(orderResponse.getId(), "fakeWxPayTxnId1");

        Order order = orderRepository.byId(orderResponse.getId());
        assertEquals(order.getStatus(), PAID);
        assertEquals("fakeWxPayTxnId1", order.getWxTxnId());
    }

    @Test
    public void should_stub_notify_order_paid_after_bank_transfer() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant initialTenant = tenantRepository.byId(response.getTenantId());

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(ADVANCED)
                        .yearDuration(2)
                        .build())
                .paymentType(BANK_TRANSFER)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);

        StubOrderPaidNotifyApi.notifyBankTransferPaid(orderResponse.getId(), "fakeBankTransferAccountId");

        Order order = orderRepository.byId(orderResponse.getId());
        assertTrue(order.atPaid());
        assertEquals(order.getStatus(), PAID);
        assertNotNull(order.getPaidAt());
        assertEquals("fakeBankTransferAccountId", order.getBankTransferAccountId());

        Tenant tenant = tenantRepository.byId(response.getTenantId());
        Packages packages = tenant.getPackages();
        assertEquals(ADVANCED, packages.currentPlanType());
        assertEquals(LocalDate.now().plusYears(2).toString(), LocalDate.ofInstant(packages.expireAt(), systemDefault()).toString());
        assertNotEquals(initialTenant.getPackages().planVersion(), packages.planVersion());
    }

    @Test
    public void should_only_update_bank_transfer_pay_info_if_called_repeatedly() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant initialTenant = tenantRepository.byId(response.getTenantId());

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(ADVANCED)
                        .yearDuration(2)
                        .build())
                .paymentType(BANK_TRANSFER)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);

        StubOrderPaidNotifyApi.notifyBankTransferPaid(orderResponse.getId(), "fakeBankTransferAccountId");
        StubOrderPaidNotifyApi.notifyBankTransferPaid(orderResponse.getId(), "fakeBankTransferAccountId1");

        Order order = orderRepository.byId(orderResponse.getId());
        assertEquals(order.getStatus(), PAID);
        assertEquals("fakeBankTransferAccountId1", order.getBankTransferAccountId());
    }

    @Test
    public void should_stub_notify_order_paid_after_wx_transfer() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant initialTenant = tenantRepository.byId(response.getTenantId());

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(ADVANCED)
                        .yearDuration(2)
                        .build())
                .paymentType(WX_TRANSFER)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);
        StubOrderPaidNotifyApi.notifyWxTransferPaid(orderResponse.getId());

        Order order = orderRepository.byId(orderResponse.getId());
        assertTrue(order.atPaid());
        assertEquals(order.getStatus(), PAID);
        assertNotNull(order.getPaidAt());
        assertFalse(order.getScreenShots().isEmpty());

        Tenant tenant = tenantRepository.byId(response.getTenantId());
        Packages packages = tenant.getPackages();
        assertEquals(ADVANCED, packages.currentPlanType());
        assertEquals(LocalDate.now().plusYears(2).toString(), LocalDate.ofInstant(packages.expireAt(), systemDefault()).toString());
        assertNotEquals(initialTenant.getPackages().planVersion(), packages.planVersion());
    }

    @Test
    public void should_not_apply_plan_order_if_plan_version_not_match() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant initialTenant = tenantRepository.byId(response.getTenantId());

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), CreateOrderCommand.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(BASIC)
                        .yearDuration(2)
                        .build())
                .paymentType(WX_NATIVE)
                .build());

        CreateOrderResponse anotherOrderResponse = OrderApi.createOrder(response.getJwt(), CreateOrderCommand.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(ADVANCED)
                        .yearDuration(1)
                        .build())
                .paymentType(WX_NATIVE)
                .build());

        StubOrderPaidNotifyApi.notifyWxPaid(orderResponse.getId(), "fakeWxPayTxnId");
        StubOrderPaidNotifyApi.notifyWxPaid(anotherOrderResponse.getId(), "fakeWxPayTxnId2");

        Order order = orderRepository.byId(orderResponse.getId());
        assertTrue(order.atPaid());
        assertEquals(order.getStatus(), PAID);
        assertNotNull(order.getPaidAt());
        assertEquals("fakeWxPayTxnId", order.getWxTxnId());

        Tenant tenant = tenantRepository.byId(response.getTenantId());
        Packages packages = tenant.getPackages();
        assertEquals(BASIC, packages.currentPlanType());
        assertEquals(LocalDate.now().plusYears(2).toString(), LocalDate.ofInstant(packages.expireAt(), systemDefault()).toString());
        assertNotEquals(initialTenant.getPackages().planVersion(), packages.planVersion());
    }

    @Test
    public void should_update_order_delivery() {
        LoginResponse response = setupApi.registerWithLogin();

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(PlatePrintingOrderDetail.builder()
                        .type(PLATE_PRINTING)
                        .plateType(TRANSPARENT_ACRYLIC_70x50)
                        .files(List.of(rUploadedFile()))
                        .amount(100)
                        .consignee(Consignee.builder()
                                .id(newShortUuid())
                                .name(rMemberName())
                                .mobile(rMobile())
                                .address(rAddress())
                                .build())
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);
        StubOrderPaidNotifyApi.notifyWxPaid(orderResponse.getId(), rWxPayTxnId());

        Delivery delivery = Delivery.builder()
                .carrier(Carrier.EMS)
                .deliveryOrderId(rDeliveryId())
                .build();

        StubOrderPaidNotifyApi.updateDelivery(orderResponse.getId(), delivery);

        Order order = orderRepository.byId(orderResponse.getId());
        assertEquals(delivery, order.getDelivery());
    }

    @Test
    public void should_issue_invoice() {
        LoginResponse response = setupApi.registerWithLogin();

        TenantApi.updateInvoiceTitle(response.getJwt(), UpdateTenantInvoiceTitleCommand.builder()
                .title(InvoiceTitle.builder()
                        .title("成都码如云信息技术有限公司")
                        .unifiedCode("124403987955856482")
                        .bankName("成都天府新区招商银行")
                        .bankAccount("1234567890")
                        .address("成都市高新区天府软件园")
                        .phone("028-12342345")
                        .build())
                .build());

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(PlatePrintingOrderDetail.builder()
                        .type(PLATE_PRINTING)
                        .plateType(TRANSPARENT_ACRYLIC_70x50)
                        .files(List.of(rUploadedFile()))
                        .amount(100)
                        .consignee(Consignee.builder()
                                .id(newShortUuid())
                                .name(rMemberName())
                                .mobile(rMobile())
                                .address(rAddress())
                                .build())
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);
        StubOrderPaidNotifyApi.notifyWxPaid(orderResponse.getId(), "fakeWxPayTxnId");
        OrderApi.requestInvoice(response.getJwt(), orderResponse.getId(), RequestInvoiceCommand.builder().type(VAT_NORMAL).email(rEmail()).build());

        List<UploadedFile> invoices = List.of(rUploadedFile());
        orderCommandService.issueInvoice(orderResponse.getId(), invoices, NOUSER);

        Order order = orderRepository.byId(orderResponse.getId());
        assertEquals(invoices, order.getInvoice().getFiles());
    }

    @Test
    public void should_refund_order() {
        LoginResponse response = setupApi.registerWithLogin();

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(PlatePrintingOrderDetail.builder()
                        .type(PLATE_PRINTING)
                        .plateType(TRANSPARENT_ACRYLIC_70x50)
                        .files(List.of(rUploadedFile()))
                        .amount(100)
                        .consignee(Consignee.builder()
                                .id(newShortUuid())
                                .name(rMemberName())
                                .mobile(rMobile())
                                .address(rAddress())
                                .build())
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);
        StubOrderPaidNotifyApi.notifyWxPaid(orderResponse.getId(), "fakeWxPayTxnId");

        orderCommandService.refund(orderResponse.getId(), "正常退款", NOUSER);
        Order order = orderRepository.byId(orderResponse.getId());
        assertEquals(REFUNDED, order.getStatus());
    }

    @Test
    public void should_list_orders() {
        LoginResponse response = setupApi.registerWithLogin();
        setupApi.updateTenantPackages(response.getTenantId(), ADVANCED);

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(ExtraMemberOrderDetail.builder()
                        .type(EXTRA_MEMBER)
                        .amount(10)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);
        StubOrderPaidNotifyApi.notifyWxPaid(orderResponse.getId(), "fakeWxPayTxnId");

        CreateOrderResponse secondOrderResponse = OrderApi.createOrder(response.getJwt(), command);

        ListOrdersQuery ordersQuery = ListOrdersQuery.builder()
                .pageIndex(1)
                .pageSize(20)
                .build();

        Order order = orderRepository.byId(orderResponse.getId());
        PagedList<QListOrder> orders = OrderApi.listOrders(response.getJwt(), ordersQuery);
        assertEquals(1, orders.getData().size());
        QListOrder listOrder = orders.getData().get(0);
        assertEquals(order.getId(), listOrder.getId());
        assertEquals(order.getDetail().getType().getName(), listOrder.getOrderDetailType());
        assertEquals(order.getStatus().getName(), listOrder.getStatus());
        assertEquals(order.getStatus(), listOrder.getStatusEnum());
        assertEquals(order.getPrice().getDiscountedTotalPrice(), listOrder.getPaidPrice());
        assertEquals(order.getPaymentType().getName(), listOrder.getPaymentType());
        assertEquals(order.getPaidAt(), listOrder.getPaidAt());
        assertEquals(order.description(), listOrder.getDescription());
        assertEquals(order.getCreatedAt(), listOrder.getCreatedAt());
    }

    @Test
    public void should_search_listed_orders() {
        LoginResponse response = setupApi.registerWithLogin();
        setupApi.updateTenantPackages(response.getTenantId(), ADVANCED);

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(ExtraMemberOrderDetail.builder()
                        .type(EXTRA_MEMBER)
                        .amount(10)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);
        StubOrderPaidNotifyApi.notifyWxPaid(orderResponse.getId(), "fakeWxPayTxnId");

        CreateOrderResponse secondOrderResponse = OrderApi.createOrder(response.getJwt(), command);
        StubOrderPaidNotifyApi.notifyWxPaid(secondOrderResponse.getId(), "fakeWxPayTxnId");

        PagedList<QListOrder> withoutSearchOrders = OrderApi.listOrders(response.getJwt(), ListOrdersQuery.builder()
                .pageIndex(1)
                .pageSize(20)
                .build());
        assertEquals(2, withoutSearchOrders.getData().size());

        PagedList<QListOrder> withSearchOrders = OrderApi.listOrders(response.getJwt(), ListOrdersQuery.builder()
                .pageIndex(1)
                .pageSize(20)
                .search(orderResponse.getId())
                .build());
        assertEquals(1, withSearchOrders.getData().size());
        QListOrder listOrder = withSearchOrders.getData().get(0);
        assertEquals(orderResponse.getId(), listOrder.getId());

        PagedList<QListOrder> withWxTxnIdSearchOrders = OrderApi.listOrders(response.getJwt(), ListOrdersQuery.builder()
                .pageIndex(1)
                .pageSize(20)
                .search("fakeWxPayTxnId")
                .build());
        assertEquals(2, withWxTxnIdSearchOrders.getData().size());
    }

    @Test
    public void should_fetch_detailed_order() {
        LoginResponse response = setupApi.registerWithLogin();

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(PlatePrintingOrderDetail.builder()
                        .type(PLATE_PRINTING)
                        .plateType(TRANSPARENT_ACRYLIC_70x50)
                        .files(List.of(rUploadedFile()))
                        .amount(100)
                        .consignee(Consignee.builder()
                                .id(newShortUuid())
                                .name(rMemberName())
                                .mobile(rMobile())
                                .address(rAddress())
                                .build())
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);
        StubOrderPaidNotifyApi.notifyWxPaid(orderResponse.getId(), "fakeWxPayTxnId");

        QDetailedOrder detailedOrder = OrderApi.fetchDetailedOrder(response.getJwt(), orderResponse.getId());
        Order order = orderRepository.byId(orderResponse.getId());
        assertEquals(order.getPrice().getDiscountedTotalPrice(), detailedOrder.getDiscountedTotalPrice());
        assertEquals(order.getWxTxnId(), detailedOrder.getWxTxnId());
        assertEquals(order.getPaymentType().getName(), detailedOrder.getPaymentType());
        assertEquals(order.getStatus().getName(), detailedOrder.getStatus());
        assertEquals(order.getDetail(), detailedOrder.getOrderDetail());
    }

    @Test
    public void should_request_invoice() {
        LoginResponse response = setupApi.registerWithLogin();

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(ADVANCED)
                        .yearDuration(2)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);
        StubOrderPaidNotifyApi.notifyWxPaid(orderResponse.getId(), "fakeWxPayTxnId");

        TenantApi.updateInvoiceTitle(response.getJwt(), UpdateTenantInvoiceTitleCommand.builder()
                .title(InvoiceTitle.builder()
                        .title("成都码如云信息技术有限公司")
                        .unifiedCode("124403987955856482")
                        .bankName("成都天府新区招商银行")
                        .bankAccount("1234567890")
                        .address("成都市高新区天府软件园")
                        .phone("028-12342345")
                        .build())
                .build());

        RequestInvoiceCommand requestInvoiceCommand = RequestInvoiceCommand.builder()
                .type(VAT_NORMAL)
                .email(rEmail())
                .build();
        OrderApi.requestInvoice(response.getJwt(), orderResponse.getId(), requestInvoiceCommand);

        Order order = orderRepository.byId(orderResponse.getId());
        assertEquals(requestInvoiceCommand.getEmail(), order.getInvoice().getEmail());
        assertEquals(requestInvoiceCommand.getType(), order.getInvoice().getType());
        assertNotNull(order.getInvoice().getRequestedAt());
    }

    @Test
    public void should_fail_request_invoice_if_no_invoice_title() {
        LoginResponse response = setupApi.registerWithLogin();

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(ADVANCED)
                        .yearDuration(2)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);
        StubOrderPaidNotifyApi.notifyWxPaid(orderResponse.getId(), "fakeWxPayTxnId");

        RequestInvoiceCommand requestInvoiceCommand = RequestInvoiceCommand.builder()
                .type(VAT_NORMAL)
                .email(rEmail())
                .build();

        assertError(() -> OrderApi.requestInvoiceRaw(response.getJwt(), orderResponse.getId(), requestInvoiceCommand), NO_INVOICE_TITLE);
    }

    @Test
    public void should_fail_request_invoice_if_already_requested() {
        LoginResponse response = setupApi.registerWithLogin();

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(ADVANCED)
                        .yearDuration(2)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);
        StubOrderPaidNotifyApi.notifyWxPaid(orderResponse.getId(), "fakeWxPayTxnId");

        TenantApi.updateInvoiceTitle(response.getJwt(), UpdateTenantInvoiceTitleCommand.builder()
                .title(InvoiceTitle.builder()
                        .title("成都码如云信息技术有限公司")
                        .unifiedCode("124403987955856482")
                        .bankName("成都天府新区招商银行")
                        .bankAccount("1234567890")
                        .address("成都市高新区天府软件园")
                        .phone("028-12342345")
                        .build())
                .build());

        RequestInvoiceCommand requestInvoiceCommand = RequestInvoiceCommand.builder()
                .type(VAT_NORMAL)
                .email(rEmail())
                .build();
        OrderApi.requestInvoice(response.getJwt(), orderResponse.getId(), requestInvoiceCommand);
        assertError(() -> OrderApi.requestInvoiceRaw(response.getJwt(), orderResponse.getId(), requestInvoiceCommand), INVOICE_ALREADY_REQUESTED);
    }

    @Test
    public void should_fail_request_invoice_if_order_is_not_paid() {
        LoginResponse response = setupApi.registerWithLogin();

        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(ADVANCED)
                        .yearDuration(2)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);

        TenantApi.updateInvoiceTitle(response.getJwt(), UpdateTenantInvoiceTitleCommand.builder()
                .title(InvoiceTitle.builder()
                        .title("成都码如云信息技术有限公司")
                        .unifiedCode("124403987955856482")
                        .bankName("成都天府新区招商银行")
                        .bankAccount("1234567890")
                        .address("成都市高新区天府软件园")
                        .phone("028-12342345")
                        .build())
                .build());

        RequestInvoiceCommand requestInvoiceCommand = RequestInvoiceCommand.builder()
                .type(VAT_NORMAL)
                .email(rEmail())
                .build();
        assertError(() -> OrderApi.requestInvoiceRaw(response.getJwt(), orderResponse.getId(), requestInvoiceCommand), ORDER_NOT_PAID);
    }

}
