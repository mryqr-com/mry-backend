package com.mryqr.common.validation;

import com.mryqr.common.utils.Command;
import com.mryqr.common.utils.Query;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ControllerPayloadValidationAspect {

    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controller() {
    }

    @Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
              "@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void methodPointcut() {
    }

    @Before("controller() && methodPointcut() ")
    public void correctCommand(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof Command command) {
                command.correctAndValidate();
            }
            if (arg instanceof Query query) {
                query.correctAndValidate();
            }
        }
    }
}
