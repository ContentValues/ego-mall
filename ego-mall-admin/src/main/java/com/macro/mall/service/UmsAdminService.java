package com.macro.mall.service;


import com.macro.mall.dto.UmsAdminLoginParam;
import com.macro.mall.dto.UmsAdminParam;
import com.macro.mall.model.UmsAdmin;
import com.macro.mall.model.UmsRole;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-06-18 10:17
 **/
public interface UmsAdminService {

    /**
     * 登录接口
     *
     * @param username
     * @param password
     * @return
     */
    String login(String username, String password);

    /**
     *
     * @param username
     * @return
     */
    UmsAdmin getAdminByUsername(String username);


    /**
     * 获取用户信息
     */
    UserDetails loadUserByUsername(String username);


    /***
     * 刷新token
     * @param token
     * @return
     */
    String refreshToken(String token);


    /**
     * 分页加载用户列表
     * @param keyword
     * @param pageSize
     * @param pageNum
     * @return
     */
    List<UmsAdmin> list(String keyword, Integer pageSize, Integer pageNum);


    /**
     * 删除用户
     * @return
     */
    int delete(Long id);


    /**
     * 用户注册
     * @param umsAdminParam
     * @return
     */
    int register(UmsAdminParam umsAdminParam);

    /**
     * 修改用户信息
     * @param admin
     * @return
     */
    int update(Long id, UmsAdminParam admin);

    /**
     * 获取用户对于角色
     */
    List<UmsRole> getRoleList(Long adminId);


    /**
     * 给用户分配角色
     * @param adminId
     * @param roleIds
     * @return
     */
    int updateRole( Long adminId, List<Long> roleIds);

}