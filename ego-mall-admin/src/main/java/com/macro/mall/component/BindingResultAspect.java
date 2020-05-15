package com.macro.mall.component;

import com.macro.mall.common.api.CommonResult;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * HibernateValidator错误结果处理切面
 * Created by macro on 2018/4/26.
 */
@Aspect
@Component
@Order(2)
public class BindingResultAspect {
    @Pointcut("execution(public * com.macro.mall.controller.*.*(..))")
    public void BindingResult() {
    }

    @Around("BindingResult()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {

        List<FieldError> result = Arrays.stream(joinPoint.getArgs())
                .filter(p -> p instanceof BindingResult)
                .map(o -> (BindingResult) o)
                .filter(p -> p.hasErrors())
                .map(bindingResult -> bindingResult.getFieldError())
                .collect(Collectors.toList());
        if (!result.isEmpty()) {
            return CommonResult.validateFailed(result.get(0).getDefaultMessage());
        }
//        Object[] args = joinPoint.getArgs();
//        for (Object arg : args) {
//            if (arg instanceof BindingResult) {
//                BindingResult result = (BindingResult) arg;
//                if (result.hasErrors()) {
//                    FieldError fieldError = result.getFieldError();
//                    if(fieldError!=null){
//                        return CommonResult.validateFailed(fieldError.getDefaultMessage());
//                    }else{
//                        return CommonResult.validateFailed();
//                    }
//                }
//            }
//        }
        return joinPoint.proceed();
    }
}
