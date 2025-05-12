package com.mryqr.common.sms;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.mryqr.common.profile.ProdProfile;
import com.mryqr.common.properties.AliyunProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.aliyuncs.http.MethodType.POST;
import static com.mryqr.common.utils.CommonUtils.maskMobileOrEmail;

@Slf4j
@Component
@ProdProfile
@RequiredArgsConstructor
public class AliyunSmsSender implements MrySmsSender {
    private final AliyunProperties aliyunProperties;
    private IAcsClient acsClient;

    @Override
    public boolean sendVerificationCode(String mobile, String code) {
        SendSmsRequest request = new SendSmsRequest();
        request.setSysMethod(POST);
        request.setPhoneNumbers(mobile);
        request.setSignName(aliyunProperties.getSmsSignName());
        request.setTemplateCode(aliyunProperties.getSmsTemplateCode());
        request.setTemplateParam("{\"code\":\"" + code + "\"}");
        try {
            SendSmsResponse response = getAcsClient().getAcsResponse(request);
            if ("OK".equalsIgnoreCase(response.getCode())) {
                log.info("Sent SMS verification code to [{}].", maskMobileOrEmail(mobile));
                return true;
            } else {
                log.error("Failed to send verification code to mobile[{}]: {}.", maskMobileOrEmail(mobile), response.getMessage());
                return false;
            }
        } catch (Throwable t) {
            log.error("Failed to send verification code to mobile[{}].", maskMobileOrEmail(mobile), t);
            return false;
        }
    }

    private IAcsClient getAcsClient() {
        if (acsClient == null) {
            IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", aliyunProperties.getAk(), aliyunProperties.getAks());
            DefaultProfile.addEndpoint("cn-hangzhou", "Dysmsapi", "dysmsapi.aliyuncs.com");
            acsClient = new DefaultAcsClient(profile);
        }

        return acsClient;
    }
}
