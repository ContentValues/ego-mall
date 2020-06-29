package com.macro.mall.com.component;

import com.macro.mall.common.api.CommonResult;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-06-19 13:29
 **/
@Aspect
@Component
@Order(2)
public class BindingResultAspect {

    @Pointcut("execution(public * com.macro.mall.controller.*.*(..))")
    public void BindingResult() {
    }

    /**
     * 两种写法而已
     *
     * @param joinPoint
     * @return
     * @throws Throwable
     */
    @Around("BindingResult()")
//    @Around("execution(public * com.macro.mall.controller.*.*(..))")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
//        Object[] args = joinPoint.getArgs();
//        for (Object arg : args) {
//            if (arg instanceof BindingResult) {
//                BindingResult result = (BindingResult) arg;
//                if (result.hasErrors()) {
//                    FieldError fieldError = result.getFieldError();
//                    if (fieldError != null) {
//                        return CommonResult.validateFailed(fieldError.getDefaultMessage());
//                    } else {
//                        return CommonResult.validateFailed();
//                    }
//                }
//            }
//        }

        List<CommonResult> result = Arrays.stream(joinPoint.getArgs())
                .filter(it -> it instanceof BindingResult)
                .map(o -> (BindingResult) o)
                .filter(Errors::hasErrors)
                .map(Errors::getFieldError)
                .filter(Objects::nonNull)
                .map(it -> CommonResult.validateFailed(it.getDefaultMessage()))
                .collect(Collectors.toList());
        if (!result.isEmpty()) {
            return result.get(0);
        }
        return joinPoint.proceed();
    }

}