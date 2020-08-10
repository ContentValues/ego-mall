package com.macro.mall.service;

import com.macro.mall.dto.PmsProductParam;
import com.macro.mall.dto.PmsProductResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-06-29 11:26
 **/
public interface PmsProductService {

    @Transactional
    int create(PmsProductParam pmsProductParam);

    @Transactional
    int update(Long id,PmsProductParam pmsProductParam);

    /**
     * 根据商品编号获取更新信息
     */
    PmsProductResult getUpdateInfo(Long id);
//
//    @Transactional
//    int delete();


}