package com.macro.mall.service;

import com.macro.mall.model.UmsResourceCategory;

import java.util.List;

public interface UmsResourceCategoryService {

    /**
     * 获取所有资源分类
     */
    List<UmsResourceCategory> listAll();
}
