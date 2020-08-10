package com.macro.mall.portal.service.impl;

import com.macro.mall.common.exception.Asserts;
import com.macro.mall.mapper.SmsCouponHistoryMapper;
import com.macro.mall.mapper.SmsCouponMapper;
import com.macro.mall.model.*;
import com.macro.mall.portal.dao.SmsCouponHistoryDao;
import com.macro.mall.portal.domain.CartPromotionItem;
import com.macro.mall.portal.domain.SmsCouponHistoryDetail;
import com.macro.mall.portal.service.UmsMemberCouponService;
import com.macro.mall.portal.service.UmsMemberService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-07-10 16:21
 **/
@Service
public class UmsMemberCouponServiceImpl implements UmsMemberCouponService {

    @Resource
    SmsCouponMapper smsCouponMapper;

    @Resource
    SmsCouponHistoryMapper smsCouponHistoryMapper;

    @Resource
    UmsMemberService umsMemberService;

    @Resource
    SmsCouponHistoryDao smsCouponHistoryDao;

    @Override
    public void add(Long couponId) {

        /**
         *  领取优惠券规则
         *    1 校验优惠券是否能领取
         *    2 插入一条领取记录
         *    3 修改优惠券的领取数量以及总量
         */


        UmsMember umsMember = umsMemberService.getCurrentMember();
        SmsCoupon smsCoupon = smsCouponMapper.selectByPrimaryKey(couponId);
        if (smsCoupon == null) {
            Asserts.fail("优惠券不存在");
        }
        if (smsCoupon.getCount() <= 0) {
            Asserts.fail("优惠券已经领完了");
        }
        Date now = new Date();
        if (now.before(smsCoupon.getEnableTime())) {
            Asserts.fail("优惠券还没到领取时间");
        }
        //判断用户领取的优惠券数量是否超过限制
        SmsCouponHistoryExample couponHistoryExample = new SmsCouponHistoryExample();
        couponHistoryExample.createCriteria().andCouponIdEqualTo(couponId).andMemberIdEqualTo(umsMember.getId());
        long count = smsCouponHistoryMapper.countByExample(couponHistoryExample);
        if (count >= smsCoupon.getPerLimit()) {
            Asserts.fail("您已经领取过该优惠券");
        }

        //生成领取优惠券历史
        SmsCouponHistory couponHistory = new SmsCouponHistory();
        couponHistory.setCouponId(couponId);
        couponHistory.setCouponCode(generateCouponCode(umsMember.getId()));
        couponHistory.setCreateTime(now);
        couponHistory.setMemberId(umsMember.getId());
        couponHistory.setMemberNickname(umsMember.getNickname());
        //主动领取
        couponHistory.setGetType(1);
        //未使用
        couponHistory.setUseStatus(0);
        smsCouponHistoryMapper.insert(couponHistory);


        //修改优惠券表的数量、领取数量
        smsCoupon.setCount(smsCoupon.getCount() - 1);
        smsCoupon.setReceiveCount(smsCoupon.getReceiveCount() == null ? 1 : smsCoupon.getReceiveCount() + 1);
        smsCouponMapper.updateByPrimaryKey(smsCoupon);
    }

    @Override
    public List<SmsCouponHistory> list(Integer useStatus) {
        UmsMember currentMember = umsMemberService.getCurrentMember();
        SmsCouponHistoryExample couponHistoryExample = new SmsCouponHistoryExample();
        SmsCouponHistoryExample.Criteria criteria = couponHistoryExample.createCriteria();
        criteria.andMemberIdEqualTo(currentMember.getId());
        if (useStatus != null) {
            criteria.andUseStatusEqualTo(useStatus);
        }
        return smsCouponHistoryMapper.selectByExample(couponHistoryExample);
    }


    /**
     * 16位优惠码生成：时间戳后8位+4位随机数+用户id后4位
     */
    private String generateCouponCode(Long memberId) {
        StringBuilder sb = new StringBuilder();
        Long currentTimeMillis = System.currentTimeMillis();
        String timeMillisStr = currentTimeMillis.toString();
        sb.append(timeMillisStr.substring(timeMillisStr.length() - 8));
        for (int i = 0; i < 4; i++) {
            sb.append(new Random().nextInt(10));
        }
        String memberIdStr = memberId.toString();
        if (memberIdStr.length() <= 4) {
            sb.append(String.format("%04d", memberId));
        } else {
            sb.append(memberIdStr.substring(memberIdStr.length() - 4));
        }
        return sb.toString();
    }

    @Override
    public List<SmsCouponHistoryDetail> listCart(List<CartPromotionItem> cartItemList, Integer type) {

        List<SmsCouponHistoryDetail> smsCouponHistoryDetails = smsCouponHistoryDao.getDetailList(umsMemberService.getCurrentMember().getId());

        List<SmsCouponHistoryDetail> enableCouponList = new ArrayList<>();
        List<SmsCouponHistoryDetail> disEnableCouponList = new ArrayList<>();

        Date now = new Date();
        for (SmsCouponHistoryDetail smsCouponHistoryDetail : smsCouponHistoryDetails) {
            SmsCoupon coupon = smsCouponHistoryDetail.getCoupon();
            int useType = coupon.getUseType();//使用类型：0->全场通用；1->指定分类；2->指定商品
            BigDecimal minPoint = coupon.getMinPoint();
            // 当前时间小于优惠券结束时间
            if (!now.before(coupon.getEndTime())) {
                disEnableCouponList.add(smsCouponHistoryDetail);
                break;
            }
            if (useType == 0) {
                //全场通用
                //判断是否满足优惠起点
                BigDecimal totalAmount = calcTotalAmount(cartItemList);
                if(totalAmount.subtract(minPoint).intValue()>=0){
                   enableCouponList.add(smsCouponHistoryDetail);
                }else {
                    disEnableCouponList.add(smsCouponHistoryDetail);
                }
            }
            if (useType == 1) {
                //1->指定分类
                //计算指定分类商品的总价
                List<Long> categoryIds = new ArrayList<>();
                for (SmsCouponProductCategoryRelation smsCouponProductCategoryRelation : smsCouponHistoryDetail.getCategoryRelationList()) {
                    categoryIds.add(smsCouponProductCategoryRelation.getProductCategoryId());
                }
                BigDecimal totalAmount = calcTotalAmountByProductCategoryId(cartItemList,categoryIds);
                if(totalAmount.subtract(minPoint).intValue()>=0){
                    enableCouponList.add(smsCouponHistoryDetail);
                }else {
                    disEnableCouponList.add(smsCouponHistoryDetail);
                }
            }
            if (useType == 2) {
                //2->指定商品
                //计算指定商品的总价
                List<Long> productIds = new ArrayList<>();
                for (SmsCouponProductRelation smsCouponProductRelation : smsCouponHistoryDetail.getProductRelationList()) {
                    productIds.add(smsCouponProductRelation.getProductId());
                }
                BigDecimal totalAmount = calcTotalAmountByProductId(cartItemList,productIds);
                if(totalAmount.subtract(minPoint).intValue()>=0){
                    enableCouponList.add(smsCouponHistoryDetail);
                }else {
                    disEnableCouponList.add(smsCouponHistoryDetail);
                }
            }
        }
        if(type.equals(1)){
            return enableCouponList;
        }else{
            return disEnableCouponList;
        }
    }

    /**
     * 计算总额 = (单价-促销优惠)*数量
     *
     * @param cartItemList
     * @return
     */
    private BigDecimal calcTotalAmount(List<CartPromotionItem> cartItemList) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartPromotionItem item : cartItemList) {
            //todo 这里面为啥不用库存的价格去作为单价
            BigDecimal realPrice = item.getPrice().subtract(item.getReduceAmount());
            totalAmount = totalAmount.add(realPrice.multiply(new BigDecimal(item.getQuantity())));
        }
        return totalAmount;
    }

    private BigDecimal calcTotalAmountByProductCategoryId(List<CartPromotionItem> cartItemList, List<Long> productCategoryIds) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartPromotionItem item : cartItemList) {
            if (productCategoryIds.contains(item.getProductCategoryId())) {
                //todo 这里面为啥不用库存的价格去作为单价
                BigDecimal realPrice = item.getPrice().subtract(item.getReduceAmount());
                totalAmount = totalAmount.add(realPrice.multiply(new BigDecimal(item.getQuantity())));
            }
        }
        return totalAmount;
    }

    /**
     * 需要移除不满足优惠券优惠的产品
     * 计算总额 = (单价-促销优惠)*数量
     *
     * @param cartItemList
     * @return
     */
    private BigDecimal calcTotalAmountByProductId(List<CartPromotionItem> cartItemList, List<Long> productIds) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartPromotionItem item : cartItemList) {
            if (productIds.contains(item.getProductId())) {
                //todo 这里面为啥不用库存的价格去作为单价
                BigDecimal realPrice = item.getPrice().subtract(item.getReduceAmount());
                totalAmount = totalAmount.add(realPrice.multiply(new BigDecimal(item.getQuantity())));
            }
        }
        return totalAmount;
    }

}