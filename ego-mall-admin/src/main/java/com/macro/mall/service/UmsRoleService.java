package com.macro.mall.service;

import com.macro.mall.model.UmsMenu;
import com.macro.mall.model.UmsResource;
import com.macro.mall.model.UmsRole;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-06-18 15:53
 **/
public interface UmsRoleService {

    List<UmsMenu> getMenuListByAdminId(@Param("adminId") Long adminId);

    /**
     * 获取所有角色列表
     */
    List<UmsRole> list();


    /**
     * 分页获取角色列表
     */
    List<UmsRole> list(String keyword, Integer pageSize, Integer pageNum);


    /**
     * 创建角色
     *
     * @param umsRole
     * @return
     */
    int create(UmsRole umsRole);

    /**
     * 编辑角色
     *
     * @param umsRole
     * @return
     */
    int update(Long id,UmsRole umsRole);

    /**
     * 批量删除角色
     * @param ids
     * @return
     */
    int delete(List<Long> ids);

    /**
     * 获取角色相关菜单
     */
    List<UmsMenu> listMenu(Long roleId);

    /**
     * 获取角色相关资源
     */
    List<UmsResource> listResource(Long roleId);

    /**
     * 给角色分配菜单
     */
    @Transactional
    int allocMenu(Long roleId, List<Long> menuIds);

    /**
     * 给角色分配资源
     */
    @Transactional
    int allocResource(Long roleId, List<Long> resourceIds);
}