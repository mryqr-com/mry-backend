package com.mryqr.core.order.command;

import com.mryqr.common.domain.UploadedFile;
import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.core.order.domain.Order;
import com.mryqr.core.order.domain.OrderFactory;
import com.mryqr.core.order.domain.OrderRepository;
import com.mryqr.core.order.domain.delivery.Delivery;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCommandService {
    private final OrderFactory orderFactory;
    private final OrderRepository orderRepository;
    private final MryRateLimiter mryRateLimiter;
    private final TenantRepository tenantRepository;

    @Transactional
    public CreateOrderResponse createOrder(CreateOrderCommand command, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Order:Create", 5);

        Tenant tenant = tenantRepository.byId(user.getTenantId());
        Order order = orderFactory.createOrder(command.getDetail(), command.getPaymentType(), tenant, user);
        orderRepository.save(order);
        log.info("Created online order[{}] of type[{}].", order.getId(), order.getPaymentType());

        return CreateOrderResponse.builder()
                .id(order.getId())
                .paymentType(order.getPaymentType())
                .wxPayQrUrl(order.getWxPayQrUrl())
                .bankTransferCode(order.getBankTransferCode())
                .price(order.getPrice())
                .payDescription(order.description())
                .createdAt(order.getCreatedAt())
                .build();
    }

    @Transactional
    public void requestInvoice(String orderId, RequestInvoiceCommand command, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Order:RequestInvoice", 5);

        Order order = orderRepository.byIdAndCheckTenantShip(orderId, user);
        Tenant tenant = tenantRepository.cachedById(user.getTenantId());
        order.requestInvoice(command.getType(), tenant.getInvoiceTitle(), command.getEmail(), user);
        orderRepository.save(order);
        log.info("Requested invoice for order[{}].", orderId);
    }

    @Transactional
    public void wxPay(String orderId, String wxTxnId, Instant paidAt, User user) {
        mryRateLimiter.applyFor("Order:UpdateWxPay", 20);

        orderRepository.byIdOptional(orderId).ifPresent(order -> {
            if (order.atCreated()) {
                order.wxPay(wxTxnId, paidAt, user);
                Tenant tenant = tenantRepository.byId(order.getTenantId());
                tenant.applyOrder(order, user);
                orderRepository.save(order);
                tenantRepository.save(tenant);
                log.info("Order[{}] paid by WxPay with txn[{}].", orderId, wxTxnId);
            } else {
                order.wxPay(wxTxnId, paidAt, user);
                orderRepository.save(order);
                log.info("Order[{}] WxPay info updated with txn[{}].", orderId, wxTxnId);
            }
        });
    }


    @Transactional
    public void wxTransferPay(String orderId, List<UploadedFile> screenShots, Instant paidAt, User user) {
        mryRateLimiter.applyFor("Order:UpdateWxTransfer", 5);

        Order order = orderRepository.byId(orderId);

        if (order.atCreated()) {
            order.wxTransferPay(screenShots, paidAt, user);
            Tenant tenant = tenantRepository.byId(order.getTenantId());
            tenant.applyOrder(order, user);
            orderRepository.save(order);
            tenantRepository.save(tenant);
            log.info("Order[{}] paid by WxTransfer.", orderId);
        } else {
            order.wxTransferPay(screenShots, paidAt, user);
            orderRepository.save(order);
        }
        log.info("Order[{}] WxTransfer info updated.", orderId);
    }

    @Transactional
    public void bankTransferPay(String orderId, String accountId, String bankName, Instant paidAt, User user) {
        mryRateLimiter.applyFor("Order:UpdateBankTransfer", 5);

        Order order = orderRepository.byId(orderId);

        if (order.atCreated()) {
            order.bankTransferPay(accountId, bankName, paidAt, user);
            Tenant tenant = tenantRepository.byId(order.getTenantId());
            tenant.applyOrder(order, user);
            orderRepository.save(order);
            tenantRepository.save(tenant);
            log.info("Order[{}] paid by bank transfer with account[{}].", orderId, accountId);
        } else {
            order.bankTransferPay(accountId, bankName, paidAt, user);
            orderRepository.save(order);
        }
        log.info("Order[{}] Bank transfer updated with account[{}].", orderId, accountId);
    }

    @Transactional
    public void updateDelivery(String orderId, Delivery delivery, User user) {
        mryRateLimiter.applyFor("Order:UpdateDelivery", 5);

        Order order = orderRepository.byId(orderId);
        order.updateDelivery(delivery, user);
        orderRepository.save(order);
        log.info("Order[{}] delivery info updated.", orderId);
    }

    @Transactional
    public void issueInvoice(String orderId, List<UploadedFile> files, User user) {
        mryRateLimiter.applyFor("Order:IssueInvoice", 5);

        Order order = orderRepository.byId(orderId);
        order.issueInvoice(files, user);
        orderRepository.save(order);
        log.info("Order[{}] invoice issued.", orderId);
    }

    @Transactional
    public void refund(String orderId, String reason, User user) {
        mryRateLimiter.applyFor("Order:Refund", 5);

        Order order = orderRepository.byId(orderId);
        order.refund(reason, user);
        orderRepository.save(order);
        log.info("Order[{}] refunded.", orderId);
    }

    @Transactional
    public void delete(String orderId) {
        mryRateLimiter.applyFor("Order:Delete", 5);

        Order order = orderRepository.byId(orderId);
        orderRepository.delete(order);
        log.info("Order[{}] deleted.", orderId);
    }
}
