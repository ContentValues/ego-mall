package com.macro.mall.portal.service.impl;

import com.macro.mall.portal.component.CancelOrderSender;
import com.macro.mall.portal.service.OmsPortalOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-05-12 13:44
 **/
@Service
public class OmsPortalOrderServiceImpl implements OmsPortalOrderService {

    Logger logger = LoggerFactory.getLogger(OmsPortalOrderServiceImpl.class);


    @Autowired
    private CancelOrderSender cancelOrderSender;

    @Override
    public Integer cancelTimeOutOrder() {

        logger.info("cancelTimeOutOrder 取消超时订单");

        return null;
    }

    @Override
    public void cancelOrder(Long orderId) {
        logger.info("cancelTimeOutOrder 手动取消订单" + orderId);
    }

    @Override
    public void sendDelayMessageCancelOrder(Long orderId) {

        //获取订单超时时间
//        OmsOrderSetting orderSetting = orderSettingMapper.selectByPrimaryKey(1L);
//        long delayTimes = orderSetting.getNormalOrderOvertime() * 60 * 1000;
        //发送延迟消息
        cancelOrderSender.sendMessage(orderId, 4 * 1000);
    }
}