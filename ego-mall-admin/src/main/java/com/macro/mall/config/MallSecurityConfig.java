package com.macro.mall.config;

import com.macro.mall.security.component.DynamicSecurityService;
import com.macro.mall.service.UmsAdminService;
import com.macro.mall.service.UmsResourceService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.ConfigAttribute;
import com.macro.mall.security.config.SecurityConfig;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-06-18 14:39
 **/
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MallSecurityConfig extends SecurityConfig {

    @Resource
    UmsAdminService umsAdminService;

    @Resource
    UmsResourceService umsResourceService;

    @Bean
    public UserDetailsService userDetailsService() {
        //获取登录用户信息
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                return umsAdminService.loadUserByUsername(username);
            }
        };
    }


    @Bean
    public DynamicSecurityService dynamicSecurityService() {
        return new DynamicSecurityService() {
            @Override
            public Map<String, ConfigAttribute> loadDataSource() {
                Map<String, ConfigAttribute> map = new ConcurrentHashMap<>();
                umsResourceService.listAll().forEach(umsResource -> {
                    map.put(umsResource.getUrl(), new org.springframework.security.access.SecurityConfig(umsResource.getId() + ":" + umsResource.getName()));
                });
                return map;
            }
        };
    }
}