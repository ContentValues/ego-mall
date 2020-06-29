package com.macro.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.macro.mall.mapper.UmsResourceMapper;
import com.macro.mall.model.UmsResource;
import com.macro.mall.model.UmsResourceExample;
import com.macro.mall.service.UmsResourceService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-06-22 15:20
 **/
@Service
public class UmsResourceServiceImpl implements UmsResourceService {
    @Resource
    UmsResourceMapper umsResourceMapper;

    @Override
    public List<UmsResource> listAll() {
        return umsResourceMapper.selectByExample(new UmsResourceExample());
    }

    @Override
    public int create(UmsResource umsResource) {
        umsResource.setCreateTime(new Date());
        return umsResourceMapper.insert(umsResource);
    }

    @Override
    public int update(Long id, UmsResource umsResource) {
        umsResource.setId(id);
        return umsResourceMapper.updateByPrimaryKeySelective(umsResource);
    }

    @Override
    public UmsResource getItem(Long id) {
        return umsResourceMapper.selectByPrimaryKey(id);
    }

    @Override
    public int delete(Long id) {
        return umsResourceMapper.deleteByPrimaryKey(id);
    }

    @Override
    public List<UmsResource> list(Long categoryId, String nameKeyword, String urlKeyword, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        UmsResourceExample example = new UmsResourceExample();
        UmsResourceExample.Criteria criteria = example.createCriteria();
        if (categoryId != null) {
            criteria.andCategoryIdEqualTo(categoryId);
        }
        if (!StringUtils.isEmpty(nameKeyword)) {
            criteria.andNameLike("%" + nameKeyword + "%");
        }
        if (!StringUtils.isEmpty(urlKeyword)) {
            criteria.andNameLike("%" + urlKeyword + "%");
        }
        return umsResourceMapper.selectByExample(example);
    }
}