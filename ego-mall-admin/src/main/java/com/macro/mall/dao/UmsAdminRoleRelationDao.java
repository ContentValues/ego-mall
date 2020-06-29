package com.macro.mall.dao;

import com.macro.mall.model.UmsResource;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-06-18 13:29
 *
 * 后台用户与角色管理自定义Dao
 **/
public interface UmsAdminRoleRelationDao {


    /**
     * 获取用户所有可访问资源
     */
    List<UmsResource> getResourceList(@Param("adminId") Long adminId);
}