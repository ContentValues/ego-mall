package com.macro.mall.service;

import com.macro.mall.dto.PmsProductAttributeParam;
import com.macro.mall.model.PmsProductAttribute;

import java.util.List;

/**
 * 商品属性Service
 */
public interface PmsProductAttributeService {

    int create(PmsProductAttributeParam attributeParam);

    int update(Long id,PmsProductAttributeParam attributeParam);

    int delete(List<Long> ids);

    PmsProductAttribute getItem(Long id);

    List<PmsProductAttribute> list(Long cid, int type, Integer pageSize, Integer pageNum);


}
