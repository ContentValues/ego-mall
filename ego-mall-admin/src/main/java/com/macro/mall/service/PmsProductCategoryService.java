package com.macro.mall.service;

import com.macro.mall.dto.PmsProductCategoryParam;
import com.macro.mall.dto.PmsProductCategoryWithChildrenItem;
import com.macro.mall.model.PmsProductCategory;

import java.util.List;

public interface PmsProductCategoryService {

    int create(PmsProductCategoryParam pmsProductCategoryParam);

    int delete(Long id);

    int update(Long id, PmsProductCategoryParam pmsProductCategoryParam);

    PmsProductCategory getItem(Long id);

    /**
     * 分页获取商品分类
     */
    List<PmsProductCategory> getList(Long parentId, Integer pageSize, Integer pageNum);


    /**
     * 以层级形式获取商品分类
     */
    List<PmsProductCategoryWithChildrenItem> listWithChildren();


    int updateNavStatus(Integer navStatus,List<Long> ids);

    int updateShowStatus(Integer showStatus,List<Long> ids);


}
