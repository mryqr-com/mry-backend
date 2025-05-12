package com.mryqr.common.wx.pay.notify;

import com.mryqr.common.ratelimit.MryRateLimiter;
import com.mryqr.common.utils.MryObjectMapper;
import com.mryqr.common.wx.pay.WxPayService;
import com.mryqr.core.order.command.OrderCommandService;
import com.mryqr.core.order.domain.Order;
import com.mryqr.core.order.domain.OrderRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import static com.mryqr.common.domain.user.User.NOUSER;
import static org.apache.commons.codec.CharEncoding.UTF_8;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class WxPayNotifyController {
    private final MryRateLimiter mryRateLimiter;
    private final OrderCommandService orderCommandService;
    private final WxPayService wxPayService;
    private final MryObjectMapper objectMapper;
    private final OrderRepository orderRepository;

    @PostMapping(value = "/preorders/pay-callback/wxpay")
    public void wxPayNotify(HttpEntity<String> httpEntity, HttpServletResponse response) throws IOException {
        try {
            mryRateLimiter.applyFor("WxPayNotify", 100);
            boolean isValid = wxPayService.validateCallbackSignature(httpEntity.getHeaders(), httpEntity.getBody());

            if (!isValid) {
                responseError(response, "签名验证失败");
                return;
            }

            WxNotifyResult wxNotifyResult = wxPayService.extractResultFromCallback(httpEntity.getBody());
            Order order = orderRepository.byId(wxNotifyResult.getOrderId());
            if (!order.atCreated()) {//如果已经支付，则直接返回
                return;
            }

            orderCommandService.wxPay(wxNotifyResult.getOrderId(), wxNotifyResult.getWxTxnId(), wxNotifyResult.getPaidAt(), NOUSER);
        } catch (Throwable t) {
            log.error("WxPay callback failed.", t);
            responseError(response, "支付失败");
        }
    }

    private void responseError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(500);
        response.setContentType(APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(UTF_8);
        PrintWriter writer = response.getWriter();
        writer.print(objectMapper.writeValueAsString(Map.of("code", "FAIL", "message", message)));
        writer.flush();
    }

}
