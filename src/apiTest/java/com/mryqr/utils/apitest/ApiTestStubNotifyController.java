package com.mryqr.utils.apitest;

import com.mryqr.common.validation.id.order.OrderId;
import com.mryqr.core.order.command.OrderCommandService;
import com.mryqr.core.order.domain.delivery.Delivery;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

import static com.mryqr.common.domain.user.User.NOUSER;
import static com.mryqr.utils.RandomTestFixture.rImageFile;

@Validated
@RestController
@RequestMapping(value = "/api-testing/orders")
public class ApiTestStubNotifyController {
    private final OrderCommandService orderCommandService;

    public ApiTestStubNotifyController(OrderCommandService orderCommandService) {
        this.orderCommandService = orderCommandService;
    }

    @GetMapping(value = "/{orderId}/wx-pay/{wxPayTxnId}")
    public void notifyWxPay(@PathVariable("orderId") String orderId,
                            @PathVariable("wxPayTxnId") String wxPayTxnId) {
        orderCommandService.wxPay(orderId, wxPayTxnId, Instant.now(), NOUSER);
    }

    @GetMapping(value = "/{orderId}/wx-transfer")
    public void notifyWxTransfer(@PathVariable("orderId") String orderId) {
        orderCommandService.wxTransferPay(orderId, List.of(rImageFile()), Instant.now(), NOUSER);
    }

    @GetMapping(value = "/{orderId}/bank-transfer/{bankTransferAccountId}")
    public void notifyBankTransfer(@PathVariable("orderId") String orderId,
                                   @PathVariable("bankTransferAccountId") String bankTransferAccountId) {
        orderCommandService.bankTransferPay(orderId, bankTransferAccountId, "XX银行", Instant.now(), NOUSER);
    }

    @PutMapping(value = "/{orderId}/delivery")
    public void updateDelivery(@PathVariable("orderId") @NotBlank @OrderId String orderId,
                               @RequestBody @Valid Delivery delivery) {
        orderCommandService.updateDelivery(orderId, delivery, NOUSER);
    }

}
