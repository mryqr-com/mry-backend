package com.mryqr.core.common.validation.id.order;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class OrderIdValidator implements ConstraintValidator<OrderId, String> {
    private static final Pattern PATTERN = Pattern.compile("^ODR[0-9]{17,19}$");

    @Override
    public void initialize(OrderId constraintAnnotation) {
    }

    @Override
    public boolean isValid(String orderId, ConstraintValidatorContext context) {
        if (isBlank(orderId)) {
            return true;
        }

        return isOrderId(orderId);
    }

    public static boolean isOrderId(String orderId) {
        return PATTERN.matcher(orderId).matches();
    }


}
