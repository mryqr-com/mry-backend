package com.mryqr.common.sms;

public interface MrySmsSender {
    boolean sendVerificationCode(String mobile, String code);
}
