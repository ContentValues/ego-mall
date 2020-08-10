package com.macro.mall.service;

import com.macro.mall.model.SmsFlashPromotion;

import java.util.List;

public interface SmsFlashPromotionService {

    int create(SmsFlashPromotion flashPromotion);

    int delete(Long id);

    SmsFlashPromotion getItem(Long id);

    int update(Long id,SmsFlashPromotion flashPromotion);

    List<SmsFlashPromotion> list(String keyword, Integer pageNum, Integer pageSize);

}
