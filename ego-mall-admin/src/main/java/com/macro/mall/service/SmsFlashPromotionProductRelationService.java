package com.macro.mall.service;


import com.macro.mall.dto.SmsFlashPromotionProduct;
import com.macro.mall.model.SmsFlashPromotionProductRelation;

import java.util.List;

public interface SmsFlashPromotionProductRelationService {

    int create(List<SmsFlashPromotionProductRelation> relationList);

    int delete(Long id);

    SmsFlashPromotionProductRelation getItem(Long id);

    int update(Long id,SmsFlashPromotionProductRelation flashPromotionProductRelation);

    List<SmsFlashPromotionProduct> list(Long flashPromotionId, Long flashPromotionSessionId, Integer pageNum, Integer pageSize);
}
