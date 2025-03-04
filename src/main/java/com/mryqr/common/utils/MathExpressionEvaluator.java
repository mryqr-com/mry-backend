package com.mryqr.common.utils;

import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.SimpleEvaluationContext;

import java.util.Map;

import static com.mryqr.common.utils.CommonUtils.requireNonBlank;
import static org.apache.commons.collections4.MapUtils.emptyIfNull;

public class MathExpressionEvaluator {
    private static final SpelExpressionParser parser = new SpelExpressionParser();

    public static boolean evalBoolean(String expression, Map<String, Double> variables) {
        requireNonBlank(expression, "Expression must not be blank.");

        SimpleEvaluationContext context = SimpleEvaluationContext.forPropertyAccessors().build();
        emptyIfNull(variables).forEach(context::setVariable);
        return parser.parseExpression(expression).getValue(context, Boolean.class);
    }

    public static Double evalDouble(String expression, Map<String, Double> variables) {
        requireNonBlank(expression, "Expression must not be blank.");


        SimpleEvaluationContext context = SimpleEvaluationContext.forPropertyAccessors().build();
        emptyIfNull(variables).forEach(context::setVariable);
        return parser.parseExpression(expression).getValue(context, Double.class);
    }
}
