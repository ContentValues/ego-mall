package com.macro.mall.portal.service;

import com.macro.mall.model.SmsCouponHistory;

public interface SmsCouponHistoryService {

    int update(SmsCouponHistory history);

    SmsCouponHistory query(Long couponId,Long memberId);

}
