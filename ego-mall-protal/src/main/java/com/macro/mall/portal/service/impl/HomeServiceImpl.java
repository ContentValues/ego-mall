package com.macro.mall.portal.service.impl;

import com.macro.mall.model.CmsSubject;
import com.macro.mall.model.PmsProduct;
import com.macro.mall.model.PmsProductCategory;
import com.macro.mall.portal.domain.HomeContentResult;
import com.macro.mall.portal.service.HomeService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-07-10 16:55
 **/
@Service
public class HomeServiceImpl implements HomeService {

    /**
     * //轮播广告
     *     private List<SmsHomeAdvertise> advertiseList;
     *     //推荐品牌
     *     private List<PmsBrand> brandList;
     *     //当前秒杀场次
     *     private HomeFlashPromotion homeFlashPromotion;
     *     //新品推荐
     *     private List<PmsProduct> newProductList;
     *     //人气推荐
     *     private List<PmsProduct> hotProductList;
     *     //推荐专题
     *     private List<CmsSubject> subjectList;
     * @return
     */

    @Override
    public HomeContentResult content() {




        return null;
    }

    @Override
    public List<PmsProduct> recommendProductList(Integer pageSize, Integer pageNum) {
        return null;
    }

    @Override
    public List<PmsProductCategory> getProductCateList(Long parentId) {
        return null;
    }

    @Override
    public List<CmsSubject> getSubjectList(Long cateId, Integer pageSize, Integer pageNum) {
        return null;
    }
}