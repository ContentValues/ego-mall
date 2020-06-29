package com.macro.mall.dao;

import com.macro.mall.model.UmsMenu;
import com.macro.mall.model.UmsResource;
import com.macro.mall.model.UmsRole;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 后台用户角色自定义Dao
 */
public interface UmsRoleDao {
    /**
     * 根据后台用户ID获取菜单
     */
    List<UmsMenu> getMenuListByAdminId(@Param("adminId") Long adminId);


    /**
     * 获取用户对于角色
     */
    List<UmsRole> getRoleList(@Param("adminId") Long adminId);

    /**
     * 根据角色ID获取菜单
     */
    List<UmsMenu> getMenuListByRoleId(@Param("roleId") Long roleId);
    /**
     * 根据角色ID获取资源
     */
    List<UmsResource> getResourceListByRoleId(@Param("roleId") Long roleId);

}
