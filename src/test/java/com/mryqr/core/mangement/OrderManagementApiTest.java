package com.mryqr.core.mangement;

import com.mryqr.BaseApiTest;
import com.mryqr.common.domain.invoice.InvoiceTitle;
import com.mryqr.common.properties.CommonProperties;
import com.mryqr.core.app.AppApi;
import com.mryqr.core.app.command.UpdateAppWebhookSettingCommand;
import com.mryqr.core.app.domain.App;
import com.mryqr.core.app.domain.WebhookSetting;
import com.mryqr.core.app.domain.page.control.*;
import com.mryqr.core.login.LoginApi;
import com.mryqr.core.order.OrderApi;
import com.mryqr.core.order.StubOrderPaidNotifyApi;
import com.mryqr.core.order.command.CreateOrderCommand;
import com.mryqr.core.order.command.CreateOrderResponse;
import com.mryqr.core.order.command.RequestInvoiceCommand;
import com.mryqr.core.order.domain.Order;
import com.mryqr.core.order.domain.delivery.Consignee;
import com.mryqr.core.order.domain.detail.PlanOrderDetail;
import com.mryqr.core.order.domain.detail.PlatePrintingOrderDetail;
import com.mryqr.core.qr.domain.QR;
import com.mryqr.core.qr.domain.attribute.DoubleAttributeValue;
import com.mryqr.core.qr.domain.attribute.DropdownAttributeValue;
import com.mryqr.core.qr.domain.attribute.IdentifierAttributeValue;
import com.mryqr.core.qr.domain.attribute.ItemStatusAttributeValue;
import com.mryqr.core.submission.SubmissionApi;
import com.mryqr.core.submission.domain.answer.date.DateAnswer;
import com.mryqr.core.submission.domain.answer.dropdown.DropdownAnswer;
import com.mryqr.core.submission.domain.answer.fileupload.FileUploadAnswer;
import com.mryqr.core.submission.domain.answer.identifier.IdentifierAnswer;
import com.mryqr.core.submission.domain.answer.singlelinetext.SingleLineTextAnswer;
import com.mryqr.core.submission.domain.answer.time.TimeAnswer;
import com.mryqr.core.tenant.TenantApi;
import com.mryqr.core.tenant.command.UpdateTenantInvoiceTitleCommand;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.utils.LoginResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static com.mryqr.common.utils.UuidGenerator.newShortUuid;
import static com.mryqr.core.order.domain.OrderStatus.REFUNDED;
import static com.mryqr.core.order.domain.PaymentType.*;
import static com.mryqr.core.order.domain.delivery.Carrier.EMS;
import static com.mryqr.core.order.domain.detail.OrderDetailType.PLAN;
import static com.mryqr.core.order.domain.detail.OrderDetailType.PLATE_PRINTING;
import static com.mryqr.core.order.domain.invoice.InvoiceType.VAT_NORMAL;
import static com.mryqr.core.plan.domain.Plan.FREE_PLAN;
import static com.mryqr.core.plan.domain.PlanType.ADVANCED;
import static com.mryqr.core.printing.domain.PlatePrintingType.TRANSPARENT_ACRYLIC_70x50;
import static com.mryqr.management.MryManageTenant.ADMIN_INIT_MOBILE;
import static com.mryqr.management.MryManageTenant.ADMIN_INIT_PASSWORD;
import static com.mryqr.management.order.MryOrderManageApp.*;
import static com.mryqr.utils.RandomTestFixture.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@Execution(SAME_THREAD)
public class OrderManagementApiTest extends BaseApiTest {
    @Autowired
    private CommonProperties commonProperties;

    @Test
    public void create_order_should_sync_to_managed_order_qr() {
        LoginResponse response = setupApi.registerWithLogin();
        Tenant theTenant = tenantRepository.byId(response.getTenantId());
        setupApi.updateTenantPlan(theTenant, FREE_PLAN);
        CreateOrderCommand command = CreateOrderCommand.builder()
                .detail(PlanOrderDetail.builder()
                        .type(PLAN)
                        .planType(ADVANCED)
                        .yearDuration(1)
                        .build())
                .paymentType(WX_NATIVE)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(response.getJwt(), command);
        Order order = orderRepository.byId(orderResponse.getId());
        QR qr = qrRepository.byCustomId(ORDER_APP_ID, order.getId());
        Tenant tenant = tenantRepository.byId(order.getTenantId());

        assertEquals(tenant.getName(), qr.getName());

        IdentifierAttributeValue orderIdAttrValue = (IdentifierAttributeValue) qr.attributeValueOf(ORDER_ID_ATTRIBUTE_ID);
        assertEquals(orderResponse.getId(), orderIdAttrValue.getContent());

        DropdownAttributeValue orderTypeAttrValue = (DropdownAttributeValue) qr.attributeValueOf(ORDER_TYPE_ATTRIBUTE_ID);
        assertEquals(List.of(ORDER_TYPE_PACKAGE_OPTION_ID), orderTypeAttrValue.getOptionIds());

        ItemStatusAttributeValue orderStatusAttrValue = (ItemStatusAttributeValue) qr.attributeValueOf(ORDER_STATUS_ATTRIBUTE_ID);
        assertEquals(ORDER_STATUS_CREATED_OPTION_ID, orderStatusAttrValue.getOptionId());

        DoubleAttributeValue orderPriceAttrValue = (DoubleAttributeValue) qr.attributeValueOf(ORDER_PRICE_ATTRIBUTE_ID);
        assertEquals(Double.valueOf(order.getPrice().getDiscountedTotalPrice()), orderPriceAttrValue.getNumber());

        ItemStatusAttributeValue orderDeliveryStatusAttrValue = (ItemStatusAttributeValue) qr.attributeValueOf(
                ORDER_DELIVERY_STATUS_ATTRIBUTE_ID);
        assertEquals(ORDER_DELIVERY_NONE_OPTION_ID, orderDeliveryStatusAttrValue.getOptionId());

        ItemStatusAttributeValue orderInvoiceStatusAttrValue = (ItemStatusAttributeValue) qr.attributeValueOf(
                ORDER_INVOICE_STATUS_ATTRIBUTE_ID);
        assertEquals(ORDER_INVOICE_STATUS_NONE_OPTION_ID, orderInvoiceStatusAttrValue.getOptionId());

        DropdownAttributeValue orderPayChannelAttrValue = (DropdownAttributeValue) qr.attributeValueOf(ORDER_PAY_CHANNEL_ATTRIBUTE_ID);
        assertEquals(List.of(ORDER_CHANNEL_WX_PAY_OPTION_ID), orderPayChannelAttrValue.getOptionIds());

        IdentifierAttributeValue orderTenantIdAttrValue = (IdentifierAttributeValue) qr.attributeValueOf(ORDER_TENANT_ATTRIBUTE_ID);
        assertEquals(order.getTenantId(), orderTenantIdAttrValue.getContent());
    }

    @Test
    public void should_register_delivery_info_for_order() {
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
        StubOrderPaidNotifyApi.notifyWxPaid(orderResponse.getId(), "fakeWxPayTxnId");

        QR qr = qrRepository.byCustomId(ORDER_APP_ID, orderResponse.getId());
        assertNotNull(qr);

        App orderManageApp = appRepository.byId(ORDER_APP_ID);
        FDropdownControl deliverControl = (FDropdownControl) orderManageApp.controlById(ORDER_REGISTER_DELIVERY_DELIVER_CONTROL_ID);
        DropdownAnswer deliverAnswer = DropdownAnswer.answerBuilder(deliverControl).optionIds(List.of(ORDER_DELIVER_EMS_OPTION_ID)).build();

        FIdentifierControl deliveryIdControl = (FIdentifierControl) orderManageApp.controlById(ORDER_REGISTER_DELIVERY_DELIVERY_ID_CONTROL_ID);
        IdentifierAnswer deliveryIdAnswer = IdentifierAnswer.answerBuilder(deliveryIdControl).content(rDeliveryId()).build();

        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        AppApi.updateWebhookSetting(jwt, ORDER_APP_ID, UpdateAppWebhookSettingCommand.builder()
                .webhookSetting(WebhookSetting.builder()
                        .enabled(true)
                        .url("http://localhost:" + port + "/webhook")
                        .username(commonProperties.getWebhookUserName())
                        .password(commonProperties.getWebhookPassword())
                        .build())
                .build());
        SubmissionApi.newSubmission(jwt, qr.getId(), ORDER_REGISTER_DELIVERY_PAGE_ID, deliverAnswer, deliveryIdAnswer);

        Order order = orderRepository.byId(orderResponse.getId());
        assertEquals(EMS, order.getDelivery().getCarrier());
        assertEquals(deliveryIdAnswer.getContent(), order.getDelivery().getDeliveryOrderId());
    }

    @Test
    public void should_register_wx_transfer_info_for_order() {
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
                .paymentType(WX_TRANSFER)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(loginResponse.getJwt(), command);
        QR qr = qrRepository.byCustomId(ORDER_APP_ID, orderResponse.getId());
        assertNotNull(qr);

        App orderManageApp = appRepository.byId(ORDER_APP_ID);

        FFileUploadControl screenShotsControl = (FFileUploadControl) orderManageApp.controlById(
                ORDER_REGISTER_WX_TRANSFER_SCREENSHOTS_CONTROL_ID);
        FileUploadAnswer screenShotsAnswer = FileUploadAnswer.answerBuilder(screenShotsControl).files(List.of(rImageFile())).build();

        FDateControl dateControl = (FDateControl) orderManageApp.controlById(ORDER_REGISTER_WX_TRANSFER_DATE_CONTROL_ID);
        DateAnswer dateAnswer = DateAnswer.answerBuilder(dateControl).date(LocalDate.now().toString()).build();

        FTimeControl timeControl = (FTimeControl) orderManageApp.controlById(ORDER_REGISTER_WX_TRANSFER_TIME_CONTROL_ID);
        TimeAnswer timeAnswer = TimeAnswer.answerBuilder(timeControl).time("00:00").build();

        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        AppApi.updateWebhookSetting(jwt, ORDER_APP_ID, UpdateAppWebhookSettingCommand.builder()
                .webhookSetting(WebhookSetting.builder()
                        .enabled(true)
                        .url("http://localhost:" + port + "/webhook")
                        .username(commonProperties.getWebhookUserName())
                        .password(commonProperties.getWebhookPassword())
                        .build())
                .build());
        SubmissionApi.newSubmission(jwt, qr.getId(), ORDER_REGISTER_WX_TRANSFER_PAGE_ID, screenShotsAnswer, dateAnswer, timeAnswer);

        Order order = orderRepository.byId(orderResponse.getId());
        assertEquals(screenShotsAnswer.getFiles(), order.getScreenShots());
        assertNotNull(order.getPaidAt());
    }

    @Test
    public void should_register_bank_transfer_info_for_order() {
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
                .paymentType(BANK_TRANSFER)
                .build();

        CreateOrderResponse orderResponse = OrderApi.createOrder(loginResponse.getJwt(), command);
        QR qr = qrRepository.byCustomId(ORDER_APP_ID, orderResponse.getId());
        assertNotNull(qr);

        App orderManageApp = appRepository.byId(ORDER_APP_ID);

        FIdentifierControl bankNameControl = (FIdentifierControl) orderManageApp.controlById(ORDER_REGISTER_BANK_NAME_CONTROL_ID);
        IdentifierAnswer bankNameAnswer = IdentifierAnswer.answerBuilder(bankNameControl).content("工商银行").build();

        FIdentifierControl bankAccountIdControl = (FIdentifierControl) orderManageApp.controlById(ORDER_REGISTER_BANK_ACCOUNT_CONTROL_ID);
        IdentifierAnswer bankAccountIdAnswer = IdentifierAnswer.answerBuilder(bankAccountIdControl).content(rBankAccountId()).build();

        FDateControl dateControl = (FDateControl) orderManageApp.controlById(ORDER_REGISTER_BANK_DATE_CONTROL_ID);
        DateAnswer dateAnswer = DateAnswer.answerBuilder(dateControl).date(LocalDate.now().toString()).build();

        FTimeControl timeControl = (FTimeControl) orderManageApp.controlById(ORDER_REGISTER_BANK_TIME_CONTROL_ID);
        TimeAnswer timeAnswer = TimeAnswer.answerBuilder(timeControl).time("00:00").build();

        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        AppApi.updateWebhookSetting(jwt, ORDER_APP_ID, UpdateAppWebhookSettingCommand.builder()
                .webhookSetting(WebhookSetting.builder()
                        .enabled(true)
                        .url("http://localhost:" + port + "/webhook")
                        .username(commonProperties.getWebhookUserName())
                        .password(commonProperties.getWebhookPassword())
                        .build())
                .build());
        SubmissionApi.newSubmission(jwt, qr.getId(), ORDER_REGISTER_BANK_PAGE_ID, bankNameAnswer, bankAccountIdAnswer, dateAnswer, timeAnswer);

        Order order = orderRepository.byId(orderResponse.getId());
        assertEquals(bankNameAnswer.getContent(), order.getBankName());
        assertEquals(bankAccountIdAnswer.getContent(), order.getBankTransferAccountId());
        assertNotNull(order.getPaidAt());
    }

    @Test
    public void should_register_wx_pay_info_for_order() {
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
        QR qr = qrRepository.byCustomId(ORDER_APP_ID, orderResponse.getId());
        assertNotNull(qr);

        App orderManageApp = appRepository.byId(ORDER_APP_ID);
        FIdentifierControl wxTxnIdControl = (FIdentifierControl) orderManageApp.controlById(ORDER_REGISTER_WX_TXN_CONTROL_ID);
        IdentifierAnswer wxTxnAnswer = IdentifierAnswer.answerBuilder(wxTxnIdControl).content(rWxPayTxnId()).build();

        FDateControl dateControl = (FDateControl) orderManageApp.controlById(ORDER_REGISTER_WX_DATE_CONTROL_ID);
        DateAnswer dateAnswer = DateAnswer.answerBuilder(dateControl).date(LocalDate.now().toString()).build();

        FTimeControl timeControl = (FTimeControl) orderManageApp.controlById(ORDER_REGISTER_WX_TIME_CONTROL_ID);
        TimeAnswer timeAnswer = TimeAnswer.answerBuilder(timeControl).time("00:00").build();

        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        AppApi.updateWebhookSetting(jwt, ORDER_APP_ID, UpdateAppWebhookSettingCommand.builder()
                .webhookSetting(WebhookSetting.builder()
                        .enabled(true)
                        .url("http://localhost:" + port + "/webhook")
                        .username(commonProperties.getWebhookUserName())
                        .password(commonProperties.getWebhookPassword())
                        .build())
                .build());
        SubmissionApi.newSubmission(jwt, qr.getId(), ORDER_REGISTER_WX_PAGE_ID, wxTxnAnswer, dateAnswer, timeAnswer);

        Order order = orderRepository.byId(orderResponse.getId());
        assertEquals(wxTxnAnswer.getContent(), order.getWxTxnId());
        assertNotNull(order.getPaidAt());
    }

    @Test
    public void should_issue_invoice_for_order() {
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
        OrderApi.requestInvoice(response.getJwt(), orderResponse.getId(),
                RequestInvoiceCommand.builder().type(VAT_NORMAL).email(rEmail()).build());

        QR qr = qrRepository.byCustomId(ORDER_APP_ID, orderResponse.getId());
        assertNotNull(qr);

        App orderManageApp = appRepository.byId(ORDER_APP_ID);
        FFileUploadControl invoiceFileControl = (FFileUploadControl) orderManageApp.controlById(ORDER_REGISTER_INVOICE_FILE_CONTROL_ID);
        FileUploadAnswer invoiceFileAnswer = FileUploadAnswer.answerBuilder(invoiceFileControl).files(List.of(rUploadedFile())).build();

        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        AppApi.updateWebhookSetting(jwt, ORDER_APP_ID, UpdateAppWebhookSettingCommand.builder()
                .webhookSetting(WebhookSetting.builder()
                        .enabled(true)
                        .url("http://localhost:" + port + "/webhook")
                        .username(commonProperties.getWebhookUserName())
                        .password(commonProperties.getWebhookPassword())
                        .build())
                .build());
        SubmissionApi.newSubmission(jwt, qr.getId(), ORDER_REGISTER_INVOICE_PAGE_ID, invoiceFileAnswer);

        Order order = orderRepository.byId(orderResponse.getId());
        assertEquals(invoiceFileAnswer.getFiles(), order.getInvoice().getFiles());
        assertNotNull(order.getInvoice().getIssuedAt());
    }

    @Test
    public void should_refund_order() {
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
        QR qr = qrRepository.byCustomId(ORDER_APP_ID, orderResponse.getId());
        assertNotNull(qr);

        App orderManageApp = appRepository.byId(ORDER_APP_ID);
        FIdentifierControl wxTxnIdControl = (FIdentifierControl) orderManageApp.controlById(ORDER_REGISTER_WX_TXN_CONTROL_ID);
        IdentifierAnswer wxTxnAnswer = IdentifierAnswer.answerBuilder(wxTxnIdControl).content(rWxPayTxnId()).build();

        FDateControl dateControl = (FDateControl) orderManageApp.controlById(ORDER_REGISTER_WX_DATE_CONTROL_ID);
        DateAnswer dateAnswer = DateAnswer.answerBuilder(dateControl).date(LocalDate.now().toString()).build();

        FTimeControl timeControl = (FTimeControl) orderManageApp.controlById(ORDER_REGISTER_WX_TIME_CONTROL_ID);
        TimeAnswer timeAnswer = TimeAnswer.answerBuilder(timeControl).time("00:00").build();

        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        AppApi.updateWebhookSetting(jwt, ORDER_APP_ID, UpdateAppWebhookSettingCommand.builder()
                .webhookSetting(WebhookSetting.builder()
                        .enabled(true)
                        .url("http://localhost:" + port + "/webhook")
                        .username(commonProperties.getWebhookUserName())
                        .password(commonProperties.getWebhookPassword())
                        .build())
                .build());
        SubmissionApi.newSubmission(jwt, qr.getId(), ORDER_REGISTER_WX_PAGE_ID, wxTxnAnswer, dateAnswer, timeAnswer);

        FSingleLineTextControl refundReasonControl = (FSingleLineTextControl) orderManageApp.controlById(ORDER_REFUND_REASON_CONTROL_ID);
        SingleLineTextAnswer reasonAnswer = SingleLineTextAnswer.answerBuilder(refundReasonControl).content("不想要了").build();
        SubmissionApi.newSubmission(jwt, qr.getId(), ORDER_REFUND_PAGE_ID, reasonAnswer);

        Order order = orderRepository.byId(orderResponse.getId());
        assertEquals(REFUNDED, order.getStatus());
    }

    @Test
    public void should_delete_order() {
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
                .paymentType(BANK_TRANSFER)
                .build();

        String jwt = LoginApi.loginWithMobileOrEmail(ADMIN_INIT_MOBILE, ADMIN_INIT_PASSWORD);
        CreateOrderResponse orderResponse = OrderApi.createOrder(loginResponse.getJwt(), command);
        Order order = orderRepository.byId(orderResponse.getId());
        QR qr = qrRepository.byCustomId(ORDER_APP_ID, orderResponse.getId());
        App orderManageApp = appRepository.byId(ORDER_APP_ID);

        AppApi.updateWebhookSetting(jwt, ORDER_APP_ID, UpdateAppWebhookSettingCommand.builder()
                .webhookSetting(WebhookSetting.builder()
                        .enabled(true)
                        .url("http://localhost:" + port + "/webhook")
                        .username(commonProperties.getWebhookUserName())
                        .password(commonProperties.getWebhookPassword())
                        .build())
                .build());

        assertTrue(orderRepository.byIdOptional(order.getId()).isPresent());
        assertTrue(qrRepository.byIdOptional(qr.getId()).isPresent());

        FSingleLineTextControl orderDeleteNoteControl = (FSingleLineTextControl) orderManageApp.controlById(ORDER_DELETE_NOTE_CONTROL_ID);
        SingleLineTextAnswer orderDeleteNoteAnswer = SingleLineTextAnswer.answerBuilder(orderDeleteNoteControl).content("test delete").build();
        SubmissionApi.newSubmission(jwt, qr.getId(), ORDER_DELETE_PAGE_ID, orderDeleteNoteAnswer);

        assertFalse(orderRepository.byIdOptional(order.getId()).isPresent());
        assertFalse(qrRepository.byIdOptional(qr.getId()).isPresent());
    }
}
