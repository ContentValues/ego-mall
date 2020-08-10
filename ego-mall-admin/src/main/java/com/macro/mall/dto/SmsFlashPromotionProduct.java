package com.macro.mall.dto;

import com.macro.mall.model.PmsProduct;
import com.macro.mall.model.SmsFlashPromotionProductRelation;
import lombok.Getter;
import lombok.Setter;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-08-07 16:36
 **/
public class SmsFlashPromotionProduct extends SmsFlashPromotionProductRelation {
    @Setter
    @Getter
    private PmsProduct product;

}