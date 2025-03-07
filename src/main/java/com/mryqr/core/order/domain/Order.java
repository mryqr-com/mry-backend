package com.mryqr.core.order.domain;

import com.mryqr.common.domain.AggregateRoot;
import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.domain.invoice.InvoiceTitle;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.exception.MryException;
import com.mryqr.core.order.domain.delivery.Delivery;
import com.mryqr.core.order.domain.detail.OrderDetail;
import com.mryqr.core.order.domain.event.*;
import com.mryqr.core.order.domain.invoice.Invoice;
import com.mryqr.core.order.domain.invoice.InvoiceType;
import com.mryqr.core.tenant.domain.Tenant;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static com.mryqr.common.exception.ErrorCode.*;
import static com.mryqr.common.utils.MapUtils.mapOf;
import static com.mryqr.common.utils.MryConstants.ORDER_COLLECTION;
import static com.mryqr.common.utils.SnowflakeIdGenerator.newSnowflakeId;
import static com.mryqr.core.order.domain.OrderStatus.*;
import static com.mryqr.core.order.domain.PaymentType.*;
import static com.mryqr.core.order.domain.detail.OrderDetailType.PLATE_PRINTING;
import static java.lang.String.valueOf;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Getter
@Document(ORDER_COLLECTION)
@TypeAlias(ORDER_COLLECTION)
@NoArgsConstructor(access = PRIVATE)
public class Order extends AggregateRoot {
    private OrderDetail detail;
    private OrderPrice price;
    private PaymentType paymentType;
    private String planVersion;

    private String wxPayQrUrl;
    private String wxTxnId;

    private List<UploadedFile> screenShots;

    private String bankTransferCode;
    private String bankTransferAccountId;
    private String bankName;

    private Instant paidAt;
    private OrderStatus status;
    private Invoice invoice;
    private Delivery delivery;
    private String refundReason;

    public static String newOrderId() {
        return "ODR" + newSnowflakeId();
    }

    public Order(OrderDetail detail,
                 PaymentType paymentType,
                 Tenant tenant,
                 User user) {
        super(newOrderId(), tenant.getTenantId(), user);
        detail.validate(tenant);
        this.detail = detail;
        this.price = detail.calculatePrice(tenant);
        this.paymentType = paymentType;
        this.planVersion = tenant.planVersion();

        if (paymentType == BANK_TRANSFER) {
            this.bankTransferCode = valueOf(RandomUtils.secure().randomInt(100000, 999999));
        }

        this.screenShots = List.of();
        this.status = CREATED;
        raiseEvent(new OrderCreatedEvent(this.getId(), user));
        addOpsLog("新建", user);
    }

    void setWxPayQrUrl(String url) {
        this.wxPayQrUrl = url;
    }

    public void wxPay(String wxTxnId, Instant paidAt, User user) {
        if (this.paymentType != WX_NATIVE) {
            log.warn("Order[{}] is not created with wx pay, skip.", this.getId());
            return;
        }

        if (atCreated()) {
            this.status = PAID;
        }

        this.wxTxnId = wxTxnId;
        this.paidAt = paidAt;
        raiseEvent(new OrderWxPayUpdatedEvent(this.getId(), user));
        addOpsLog("在线微信支付", user);
    }

    public void wxTransferPay(List<UploadedFile> screenShots, Instant paidAt, User user) {
        if (this.paymentType != WX_TRANSFER) {
            log.warn("Order[{}] is not created with ex transfer pay, skip.", this.getId());
            return;
        }

        if (atCreated()) {
            this.status = PAID;
        }

        this.paidAt = paidAt;
        this.screenShots = screenShots;
        raiseEvent(new OrderWxTransferUpdatedEvent(this.getId(), user));
        addOpsLog("线下微信转账", user);
    }

    public void bankTransferPay(String bankTransferAccountId, String bankName, Instant paidAt, User user) {
        if (this.paymentType != BANK_TRANSFER) {
            log.warn("Order[{}] is not created with bank transfer pay, skip.", this.getId());
            return;
        }

        if (atCreated()) {
            this.status = PAID;
        }

        this.bankTransferAccountId = bankTransferAccountId;
        this.bankName = bankName;
        this.paidAt = paidAt;
        raiseEvent(new OrderBankTransferUpdatedEvent(this.getId(), user));
        addOpsLog("银行对公转账", user);
    }

    public void requestInvoice(InvoiceType type, InvoiceTitle title, String email, User user) {
        if (title == null) {
            throw new MryException(NO_INVOICE_TITLE, "申请失败，尚无发票抬头信息。", mapOf("orderId", this.getId()));
        }

        if (!atPaid()) {
            throw new MryException(ORDER_NOT_PAID, "申请失败，只有处于已支付状态才可申请发票。", mapOf("orderId", this.getId()));
        }

        if (isInvoiceRequested()) {
            throw new MryException(INVOICE_ALREADY_REQUESTED, "申请失败，先前已经申请过发票。", mapOf("orderId", this.getId()));
        }

        this.invoice = new Invoice(title, type, email);
        raiseEvent(new OrderInvoiceRequestedEvent(this.getId(), user));
        addOpsLog("申请发票", user);
    }

    public void updateDelivery(Delivery delivery, User user) {
        if (this.detail.getType() != PLATE_PRINTING) {
            log.warn("Order[{}] is not plate printing type, skip update delivery info.", this.getId());
            return;
        }

        if (atCreated()) {
            log.warn("Order[{}] is not paid, skip update delivery info.", this.getId());
            return;
        }

        if (Objects.equals(delivery, this.delivery)) {
            return;
        }

        this.delivery = delivery;
        raiseEvent(new OrderDeliveryUpdatedEvent(this.getId(), user));
        addOpsLog("添加物流信息", user);
    }

    public void issueInvoice(List<UploadedFile> files, User user) {
        if (!this.isInvoiceRequested()) {
            log.warn("Order[{}] invoice not requested, skip issue invoice.", this.getId());
            return;
        }

        if (Objects.equals(this.invoice.getFiles(), files)) {
            return;
        }

        this.invoice.issue(files);
        raiseEvent(new OrderInvoiceIssuedEvent(this.getId(), user));
        addOpsLog("开具发票", user);
    }

    public void refund(String reason, User user) {
        if (atCreated()) {
            log.warn("Order[{}] is not paid, cannot be refund.", this.getId());
            return;
        }

        if (atPaid()) {
            this.status = REFUNDED;
            addOpsLog("退款", user);
        }
        raiseEvent(new OrderRefundUpdatedEvent(this.getId(), user));
        this.refundReason = reason;
    }

    public String description() {
        return detail.description();
    }

    public boolean atCreated() {
        return this.status == CREATED;
    }

    public boolean atPaid() {
        return this.status == PAID;
    }

    public boolean isInvoiceRequested() {
        return this.invoice != null;
    }

    public boolean isInvoiceIssued() {
        return this.isInvoiceRequested() && this.invoice.isIssued();
    }

}
