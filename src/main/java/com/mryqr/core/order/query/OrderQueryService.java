package com.mryqr.core.order.query;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.common.utils.PagedList;
import com.mryqr.common.utils.Pagination;
import com.mryqr.core.order.domain.Order;
import com.mryqr.core.order.domain.OrderPrice;
import com.mryqr.core.order.domain.OrderRepository;
import com.mryqr.core.order.domain.OrderStatus;
import com.mryqr.core.tenant.domain.Tenant;
import com.mryqr.core.tenant.domain.TenantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.mryqr.common.utils.Pagination.pagination;
import static com.mryqr.common.validation.id.order.OrderIdValidator.isOrderId;
import static com.mryqr.core.order.domain.OrderStatus.PAID;
import static com.mryqr.core.order.domain.OrderStatus.REFUNDED;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.springframework.data.domain.Sort.Direction.DESC;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Component
@RequiredArgsConstructor
public class OrderQueryService {
    private static final List<OrderStatus> VIEWABLE_ORDER_STATUSES = List.of(PAID, REFUNDED);
    private final TenantRepository tenantRepository;
    private final MryRateLimiter mryRateLimiter;
    private final OrderRepository orderRepository;
    private final MongoTemplate mongoTemplate;

    public QPriceQuotation quoteOrderPrice(QuotePriceQuery queryCommand, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Order:Quote", 5);

        Tenant tenant = tenantRepository.byId(user.getTenantId());
        OrderPrice price = queryCommand.getDetail().calculatePrice(tenant);

        return QPriceQuotation.builder()
                .price(price)
                .build();
    }

    public OrderStatus fetchOrderStatus(String orderId, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Order:FetchStatus", 5);

        Order order = orderRepository.byIdAndCheckTenantShip(orderId, user);
        return order.getStatus();
    }

    public PagedList<QListOrder> listOrders(ListOrdersQuery queryCommand, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Order:List", 5);

        Pagination pagination = pagination(queryCommand.getPageIndex(), queryCommand.getPageSize());
        Criteria criteria = where("tenantId").is(user.getTenantId()).and("status").in(VIEWABLE_ORDER_STATUSES);

        String search = queryCommand.getSearch();
        if (isNotBlank(search)) {
            if (isOrderId(search)) {
                criteria.and("_id").is(search);
            } else {
                criteria.orOperator(where("wxTxnId").is(search),
                        where("bankTransferCode").is(search),
                        where("bankTransferAccountId").is(search));
            }
        }

        Query query = query(criteria);
        long count = mongoTemplate.count(query, Order.class);
        if (count == 0) {
            return pagedList(pagination, 0, List.of());
        }

        query.skip(pagination.skip()).limit(pagination.getPageSize()).with(Sort.by(DESC, "createdAt"));

        List<Order> orders = mongoTemplate.find(query, Order.class);
        List<QListOrder> listOrders = orders.stream()
                .map(order -> QListOrder.builder()
                        .id(order.getId())
                        .orderDetailTypeEnum(order.getDetail().getType())
                        .orderDetailType(order.getDetail().getType().getName())
                        .status(order.getStatus().getName())
                        .statusEnum(order.getStatus())
                        .description(order.description())
                        .paidPrice(order.getPrice().getDiscountedTotalPrice())
                        .paymentType(order.getPaymentType().getName())
                        .paidAt(order.getPaidAt())
                        .createdAt(order.getCreatedAt())
                        .invoiceRequested(order.isInvoiceRequested())
                        .invoiceIssued(order.isInvoiceIssued())
                        .build())
                .collect(toImmutableList());

        return pagedList(pagination, (int) count, listOrders);
    }

    private PagedList<QListOrder> pagedList(Pagination pagination, int count, List<QListOrder> apps) {
        return PagedList.<QListOrder>builder()
                .totalNumber(count)
                .pageIndex(pagination.getPageIndex())
                .pageSize(pagination.getPageSize())
                .data(apps)
                .build();
    }

    public QDetailedOrder fetchDetailedOrder(String orderId, User user) {
        user.checkIsTenantAdmin();
        mryRateLimiter.applyFor(user.getTenantId(), "Order:FetchDetailedOrder", 5);

        Order order = orderRepository.byIdAndCheckTenantShip(orderId, user);

        return QDetailedOrder.builder()
                .id(order.getId())
                .description(order.description())
                .orderDetailType(order.getDetail().getType().getName())
                .orderDetail(order.getDetail())
                .status(order.getStatus().getName())
                .discountedTotalPrice(order.getPrice().getDiscountedTotalPrice())
                .paymentType(order.getPaymentType().getName())
                .wxTxnId(order.getWxTxnId())
                .bankTransferCode(order.getBankTransferCode())
                .bankName(order.getBankName())
                .bankTransferAccountId(order.getBankTransferAccountId())
                .paidAt(order.getPaidAt())
                .invoiceRequested(order.isInvoiceRequested())
                .invoiceIssued(order.isInvoiceIssued())
                .invoiceTitle(order.isInvoiceRequested() ? order.getInvoice().getTitle().getTitle() : null)
                .invoiceType(order.isInvoiceRequested() ? order.getInvoice().getType().getName() : null)
                .invoiceEmail(order.isInvoiceRequested() ? order.getInvoice().getEmail() : null)
                .carrier(order.getDelivery() != null ? order.getDelivery().getCarrier().getName() : null)
                .deliveryOrderId(order.getDelivery() != null ? order.getDelivery().getDeliveryOrderId() : null)
                .createdAt(order.getCreatedAt())
                .createdBy(order.getCreatedBy())
                .creator(order.getCreator())
                .build();
    }

}
