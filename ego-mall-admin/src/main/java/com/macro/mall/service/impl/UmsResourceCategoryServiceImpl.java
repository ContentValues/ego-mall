package com.macro.mall.service.impl;

import com.macro.mall.mapper.UmsResourceCategoryMapper;
import com.macro.mall.model.UmsResourceCategory;
import com.macro.mall.model.UmsResourceCategoryExample;
import com.macro.mall.service.UmsResourceCategoryService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-06-22 15:13
 **/
@Service
public class UmsResourceCategoryServiceImpl implements UmsResourceCategoryService {
    @Resource
    UmsResourceCategoryMapper umsResourceCategoryMapper;

    @Override
    public List<UmsResourceCategory> listAll() {
        UmsResourceCategoryExample example = new UmsResourceCategoryExample();
        example.setOrderByClause("sort desc");
        return umsResourceCategoryMapper.selectByExample(example);
    }
}