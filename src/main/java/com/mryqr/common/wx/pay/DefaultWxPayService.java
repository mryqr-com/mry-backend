package com.mryqr.common.wx.pay;

import com.fasterxml.jackson.databind.JsonNode;
import com.mryqr.common.properties.PayProperties;
import com.mryqr.common.properties.PropertyService;
import com.mryqr.common.properties.WxProperties;
import com.mryqr.common.utils.MryObjectMapper;
import com.mryqr.common.wx.pay.notify.WxNotifyResult;
import com.mryqr.common.wx.pay.notify.WxPayNotifyRequest;
import com.mryqr.core.order.domain.Order;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.core.auth.Validator;
import com.wechat.pay.java.core.http.HttpHeaders;
import com.wechat.pay.java.service.payments.nativepay.NativePayService;
import com.wechat.pay.java.service.payments.nativepay.model.Amount;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayRequest;
import com.wechat.pay.java.service.payments.nativepay.model.PrepayResponse;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ResourceLoader;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.util.Base64;

import static com.wechat.pay.java.core.http.Constant.*;
import static java.lang.Float.parseFloat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javax.crypto.Cipher.DECRYPT_MODE;

//@Component
//@ProdProfile
public class DefaultWxPayService implements WxPayService {
    private final NativePayService nativePayService;
    private final Validator validator;
    private final PayProperties payProperties;
    private final PropertyService propertyService;
    private final WxProperties wxProperties;
    private final MryObjectMapper objectMapper;

    public DefaultWxPayService(
            PayProperties payProperties,
            ResourceLoader resourceLoader,
            PropertyService propertyService,
            WxProperties wxProperties,
            MryObjectMapper objectMapper) {
        this.payProperties = payProperties;
        this.propertyService = propertyService;
        this.wxProperties = wxProperties;
        this.objectMapper = objectMapper;
        try {
            RSAAutoCertificateConfig config = new RSAAutoCertificateConfig.Builder()
                    .merchantId(payProperties.getWxMerchantId())
                    .privateKey(IOUtils.toString(resourceLoader.getResource("classpath:wxpay-apiclient-key.pem").getInputStream(), UTF_8))
                    .merchantSerialNumber(payProperties.getWxMerchantSerialNumber())
                    .apiV3Key(payProperties.getWxApiV3Key())
                    .build();
            nativePayService = new NativePayService.Builder().config(config).build();
            validator = config.createValidator();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public String createWxPayOrder(Order order) {
        Amount amount = new Amount();
        amount.setTotal((int) (parseFloat(order.getPrice().getDiscountedTotalPrice()) * 100));
        PrepayRequest request = new PrepayRequest();
        request.setAmount(amount);
        request.setAppid(wxProperties.getMobileAppId());
        request.setMchid(payProperties.getWxMerchantId());
        request.setDescription(order.description());
        request.setNotifyUrl(propertyService.wxPayNotifyUrl());
        request.setOutTradeNo(order.getId());
        PrepayResponse response = nativePayService.prepay(request);
        return response.getCodeUrl();
    }

    @Override
    public boolean validateCallbackSignature(org.springframework.http.HttpHeaders headers, String body) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.addHeader(WECHAT_PAY_SERIAL, headers.getFirst(WECHAT_PAY_SERIAL));
        httpHeaders.addHeader(WECHAT_PAY_SIGNATURE, headers.getFirst(WECHAT_PAY_SIGNATURE));
        httpHeaders.addHeader(WECHAT_PAY_TIMESTAMP, headers.getFirst(WECHAT_PAY_TIMESTAMP));
        httpHeaders.addHeader(WECHAT_PAY_NONCE, headers.getFirst(WECHAT_PAY_NONCE));
        return validator.validate(httpHeaders, body);
    }

    @Override
    public WxNotifyResult extractResultFromCallback(String body) {
        WxPayNotifyRequest request = objectMapper.readValue(body, WxPayNotifyRequest.class);
        WxPayNotifyRequest.Resource resource = request.getResource();
        String ciphertext = resource.getCiphertext();
        String associatedData = resource.getAssociated_data();
        String nonce = resource.getNonce();

        String decrypted = decrypt(ciphertext, associatedData, nonce);
        JsonNode jsonNode = objectMapper.readTree(decrypted);
        String wxTxnId = jsonNode.get("transaction_id").textValue();
        String successTime = jsonNode.get("success_time").textValue();
        String orderId = jsonNode.get("out_trade_no").textValue();

        return WxNotifyResult.builder()
                .orderId(orderId)
                .wxTxnId(wxTxnId)
                .paidAt(Instant.parse(successTime))
                .build();
    }

    private String decrypt(String ciphertext, String associatedData, String nonce) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            SecretKeySpec key = new SecretKeySpec(payProperties.getWxApiV3Key().getBytes(UTF_8), "AES");
            GCMParameterSpec spec = new GCMParameterSpec(128, nonce.getBytes(UTF_8));
            cipher.init(DECRYPT_MODE, key, spec);
            cipher.updateAAD(associatedData.getBytes(UTF_8));
            return new String(cipher.doFinal(Base64.getDecoder().decode(ciphertext)), UTF_8);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
