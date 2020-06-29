package com.macro.mall.service;

import com.macro.mall.dto.PmsProductParam;
import org.springframework.transaction.annotation.Transactional;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-06-29 11:26
 **/
public interface PmsProductService {

    @Transactional
    int create(PmsProductParam pmsProductParam);

//    @Transactional
//    int update();
//
//    @Transactional
//    int delete();


}