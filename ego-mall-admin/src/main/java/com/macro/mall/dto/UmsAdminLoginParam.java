package com.macro.mall.dto;


import javax.validation.constraints.NotEmpty;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-06-18 11:03
 **/
public class UmsAdminLoginParam {

    @NotEmpty(message = "用户名不能为空")
    private String username;
    @NotEmpty(message = "密码不能为空")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}