package com.macro.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.macro.mall.dao.UmsRoleDao;
import com.macro.mall.mapper.UmsRoleMapper;
import com.macro.mall.mapper.UmsRoleMenuRelationMapper;
import com.macro.mall.mapper.UmsRoleResourceRelationMapper;
import com.macro.mall.model.*;
import com.macro.mall.service.UmsRoleService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;


/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-06-18 15:53
 **/
@Service
public class UmsRoleServiceImpl implements UmsRoleService {

    @Resource
    UmsRoleDao umsRoleDao;

    @Resource
    UmsRoleMapper umsRoleMapper;

    @Resource
    UmsRoleResourceRelationMapper umsRoleResourceRelationMapper;

    @Resource
    UmsRoleMenuRelationMapper umsRoleMenuRelationMapper;


    @Override
    public List<UmsMenu> getMenuListByAdminId(Long adminId) {
        return umsRoleDao.getMenuListByAdminId(adminId);
    }

    @Override
    public List<UmsRole> list() {
        return umsRoleMapper.selectByExample(new UmsRoleExample());
    }

    @Override
    public List<UmsRole> list(String keyword, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        UmsRoleExample example = new UmsRoleExample();
        if (!StringUtils.isEmpty(keyword)) {
            example.createCriteria().andNameLike("%" + keyword + "%");
        }
        return umsRoleMapper.selectByExample(example);
    }

    @Override
    public int create(UmsRole umsRole) {
        UmsRoleExample example = new UmsRoleExample();
        example.createCriteria().andNameEqualTo(umsRole.getName());
        if (!CollectionUtils.isEmpty(umsRoleMapper.selectByExample(example))) {
            return 0;
        }
        umsRole.setSort(0);
        umsRole.setStatus(1);
        umsRole.setAdminCount(0);
        umsRole.setCreateTime(new Date());
        return umsRoleMapper.insert(umsRole);
    }

    @Override
    public int update(Long id, UmsRole umsRole) {
        umsRole.setId(id);
        return umsRoleMapper.updateByPrimaryKeySelective(umsRole);
    }

    @Override
    public int delete(List<Long> ids) {
        ids.stream().forEach(it -> {
            UmsRoleExample example = new UmsRoleExample();
            example.createCriteria().andStatusEqualTo(0);
        });
        return 1;
    }

    @Override
    public List<UmsMenu> listMenu(Long roleId) {
        return umsRoleDao.getMenuListByRoleId(roleId);
    }

    @Override
    public List<UmsResource> listResource(Long roleId) {
        return umsRoleDao.getResourceListByRoleId(roleId);
    }

    @Override
    public int allocMenu(Long roleId, List<Long> menuIds) {

        UmsRoleMenuRelationExample example = new UmsRoleMenuRelationExample();
        example.createCriteria().andRoleIdEqualTo(roleId);
        umsRoleMenuRelationMapper.deleteByExample(example);
        menuIds.stream().map(menuId -> {
            UmsRoleMenuRelation relation = new UmsRoleMenuRelation();
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            return relation;
        }).forEach(umsRoleMenuRelation -> {
            umsRoleMenuRelationMapper.insert(umsRoleMenuRelation);
        });
        return menuIds.size();
    }

    @Override
    public int allocResource(Long roleId, List<Long> resourceIds) {
        UmsRoleResourceRelationExample example = new UmsRoleResourceRelationExample();
        example.createCriteria().andRoleIdEqualTo(roleId);
        umsRoleResourceRelationMapper.deleteByExample(example);
        resourceIds.stream().map(resourceId -> {
            UmsRoleResourceRelation relation = new UmsRoleResourceRelation();
            relation.setRoleId(roleId);
            relation.setResourceId(resourceId);
            return relation;
        }).forEach(umsRoleResourceRelation -> umsRoleResourceRelationMapper.insert(umsRoleResourceRelation));
        return resourceIds.size();
    }
}