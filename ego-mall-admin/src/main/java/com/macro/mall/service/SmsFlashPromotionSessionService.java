package com.macro.mall.service;

import com.macro.mall.dto.SmsFlashPromotionSessionDetail;
import com.macro.mall.model.SmsFlashPromotionSession;

import java.util.List;

public interface SmsFlashPromotionSessionService {

    int create(SmsFlashPromotionSession flashPromotion);

    int delete(Long id);

    SmsFlashPromotionSession getItem(Long id);

    int update(Long id,SmsFlashPromotionSession flashPromotion);

    List<SmsFlashPromotionSession> list(String keyword, Integer pageNum, Integer pageSize);


    /**
     * 获取全部可选场次及其数量
     */
    List<SmsFlashPromotionSessionDetail> selectList(Long flashPromotionId);

}
