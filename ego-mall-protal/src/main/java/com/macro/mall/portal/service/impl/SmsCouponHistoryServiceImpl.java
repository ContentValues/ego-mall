package com.macro.mall.portal.service.impl;

import com.macro.mall.mapper.SmsCouponHistoryMapper;
import com.macro.mall.model.SmsCouponHistory;
import com.macro.mall.portal.service.SmsCouponHistoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-07-14 18:31
 **/
@Service
public class SmsCouponHistoryServiceImpl implements SmsCouponHistoryService {

    @Resource
    SmsCouponHistoryMapper smsCouponHistoryMapper;

    @Override
    public int update(SmsCouponHistory history) {
        SmsCouponHistory smsCouponHistory = new SmsCouponHistory();
        BeanUtils.copyProperties(history,smsCouponHistory);
        return smsCouponHistoryMapper.updateByPrimaryKeySelective(smsCouponHistory);
    }

    @Override
    public SmsCouponHistory query(Long couponId, Long memberId) {
        return null;
    }
}