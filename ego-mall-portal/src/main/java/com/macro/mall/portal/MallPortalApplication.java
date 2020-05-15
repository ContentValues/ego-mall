package com.macro.mall.portal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-05-12 10:47
 **/
@SpringBootApplication(scanBasePackages = "com.macro.mall")
public class MallPortalApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallPortalApplication.class, args);
    }
}