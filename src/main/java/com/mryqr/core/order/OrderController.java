package com.mryqr.core.order;

import com.mryqr.common.domain.user.User;
import com.mryqr.common.utils.PagedList;
import com.mryqr.common.validation.id.order.OrderId;
import com.mryqr.core.order.command.CreateOrderCommand;
import com.mryqr.core.order.command.CreateOrderResponse;
import com.mryqr.core.order.command.OrderCommandService;
import com.mryqr.core.order.command.RequestInvoiceCommand;
import com.mryqr.core.order.domain.OrderStatus;
import com.mryqr.core.order.query.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/orders")
public class OrderController {
    private final OrderQueryService orderQueryService;
    private final OrderCommandService orderCommandService;

    @PostMapping
    @ResponseStatus(CREATED)
    public CreateOrderResponse createOrder(@RequestBody @Valid CreateOrderCommand command,
                                           @AuthenticationPrincipal User user) {
        return orderCommandService.createOrder(command, user);
    }

    @PostMapping(value = "/{orderId}/invoice-request")
    public void requestInvoice(@PathVariable("orderId") @NotBlank @OrderId String orderId,
                               @RequestBody @Valid RequestInvoiceCommand command,
                               @AuthenticationPrincipal User user) {
        orderCommandService.requestInvoice(orderId, command, user);
    }

    @GetMapping(value = "/{orderId}/status")
    public OrderStatus fetchOrderStatus(@PathVariable("orderId") @NotBlank @OrderId String orderId,
                                        @AuthenticationPrincipal User user) {
        return orderQueryService.fetchOrderStatus(orderId, user);
    }

    @PostMapping(value = "/list")
    public PagedList<QListOrder> listOrders(@RequestBody @Valid ListOrdersQuery queryCommand,
                                            @AuthenticationPrincipal User user) {
        return orderQueryService.listOrders(queryCommand, user);
    }

    @GetMapping(value = "/{orderId}")
    public QDetailedOrder fetchDetailedOrder(@PathVariable("orderId") @NotBlank @OrderId String orderId,
                                             @AuthenticationPrincipal User user) {
        return orderQueryService.fetchDetailedOrder(orderId, user);
    }

    @GetMapping(value = "/{orderId}/shipment")
    public QOrderShipment fetchOrderShipment(@PathVariable("orderId") @NotBlank @OrderId String orderId, @AuthenticationPrincipal User user) {
        return orderQueryService.fetchOrderShipment(orderId, user);
    }

    @PostMapping(value = "/quotations")
    public QPriceQuotation requestQuote(@RequestBody @Valid QuotePriceQuery queryCommand, @AuthenticationPrincipal User user) {
        return orderQueryService.quoteOrderPrice(queryCommand, user);
    }

}
