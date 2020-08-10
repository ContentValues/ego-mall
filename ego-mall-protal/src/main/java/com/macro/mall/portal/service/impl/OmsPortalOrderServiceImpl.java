package com.macro.mall.portal.service.impl;

import com.macro.mall.common.exception.ApiException;
import com.macro.mall.mapper.OmsOrderItemMapper;
import com.macro.mall.mapper.OmsOrderMapper;
import com.macro.mall.mapper.PmsSkuStockMapper;
import com.macro.mall.model.*;
import com.macro.mall.portal.domain.CartPromotionItem;
import com.macro.mall.portal.domain.OrderParam;
import com.macro.mall.portal.domain.SmsCouponHistoryDetail;
import com.macro.mall.portal.service.*;
import com.macro.mall.security.service.RedisService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-07-14 10:09
 **/
@Service
public class OmsPortalOrderServiceImpl implements OmsPortalOrderService {
    @Resource
    UmsMemberService umsMemberService;
    @Resource
    OmsCartItemService omsCartItemService;
    @Resource
    UmsMemberCouponServiceImpl umsMemberCouponService;
    @Resource
    PmsSkuStockMapper pmsSkuStockMapper;
    @Resource
    OmsOrderItemMapper omsOrderItemMapper;
    @Resource
    OmsOrderMapper omsOrderMapper;
    @Resource
    UmsMemberReceiveAddressService umsMemberReceiveAddressService;
    @Resource
    SmsCouponHistoryService smsCouponHistoryService;
//    @Resource
//    private RedisService redisService;
//    @Value("${redis.key.orderId}")
//    private String REDIS_KEY_ORDER_ID;
//    @Value("${redis.database}")
//    private String REDIS_DATABASE;

    @Override
    public Map<String, Object> generateOrder(OrderParam orderParam) {

        UmsMember umsMember = umsMemberService.getCurrentMember();
        List<CartPromotionItem> cartPromotionItems = omsCartItemService.listPromotion(umsMember.getId());
        List<OmsOrderItem> orderItems = new ArrayList<>();
        for (CartPromotionItem cartPromotionItem : cartPromotionItems) {
            OmsOrderItem orderItem = new OmsOrderItem();
            orderItem.setProductId(cartPromotionItem.getProductId());
            orderItem.setProductPic(cartPromotionItem.getProductPic());
            orderItem.setProductName(cartPromotionItem.getProductName());
            orderItem.setProductBrand(cartPromotionItem.getProductBrand());
            orderItem.setProductSn(cartPromotionItem.getProductSn());
            orderItem.setProductPrice(cartPromotionItem.getPrice());
            orderItem.setProductQuantity(cartPromotionItem.getQuantity());
            orderItem.setProductSkuId(cartPromotionItem.getProductSkuId());
            orderItem.setProductSkuCode(cartPromotionItem.getProductSkuCode());
            orderItem.setProductCategoryId(cartPromotionItem.getProductCategoryId());
            orderItem.setPromotionName(cartPromotionItem.getPromotionMessage());
            orderItem.setPromotionAmount(cartPromotionItem.getReduceAmount());
            orderItem.setGiftIntegration(cartPromotionItem.getIntegration());
            orderItem.setGiftGrowth(cartPromotionItem.getGrowth());
            orderItem.setProductAttr(cartPromotionItem.getProductAttr());
            orderItems.add(orderItem);
        }

        //判断购物车中商品是否都有库存
        if (!StringUtils.isEmpty(hasStock(cartPromotionItems))) {
            throw new ApiException(hasStock(cartPromotionItems));
        }
        /**
         *  @ApiModelProperty(value = "优惠券优惠分解金额")
         *     private BigDecimal couponAmount;
         *
         *     @ApiModelProperty(value = "积分优惠分解金额")
         *     private BigDecimal integrationAmount;
         *
         *     @ApiModelProperty(value = "该商品经过优惠后的分解金额")
         *     private BigDecimal realAmount;
         */
        //判断使用使用了优惠券
        if (orderParam.getCouponId() == null) {
            for (OmsOrderItem orderItem : orderItems) {
                orderItem.setCouponAmount(BigDecimal.ZERO);
            }
        } else {
            SmsCouponHistoryDetail smsCouponHistoryDetail = getUseCoupon(cartPromotionItems, orderParam.getCouponId());
            if (smsCouponHistoryDetail == null) {
                throw new ApiException("该优惠券不可用");
            }
            handleCouponAmount(orderItems, smsCouponHistoryDetail);
        }

        //判断是否使用积分
        if (orderParam.getUseIntegration() == null) {
            for (OmsOrderItem orderItem : orderItems) {
                orderItem.setIntegrationAmount(BigDecimal.ZERO);
            }
        } else {
            //todo 未完成
        }
        //计算order_item的实付金额
        handleRealAmount(orderItems);
        //进行库存锁定
        lockStock(cartPromotionItems);
        //根据商品合计、运费、活动优惠、优惠券、积分计算应付金额

        OmsOrder order = new OmsOrder();
        order.setMemberId(umsMember.getId());
        order.setCouponId(orderParam.getCouponId());
        Date now = new Date();
        order.setCreateTime(now);
        order.setMemberUsername(umsMember.getUsername());
        order.setTotalAmount(calcTotalAmount(orderItems));

        order.setFreightAmount(BigDecimal.ZERO);
        order.setPromotionAmount(calcOrderPromotionAmount(orderItems));
        order.setIntegrationAmount(calcOrderIntegrationAmount(orderItems));
        order.setCouponAmount(calcOrderCouponAmount(orderItems));
        order.setDiscountAmount(BigDecimal.ZERO);
        //支付方式：0->未支付；1->支付宝；2->微信
        order.setPayType(orderParam.getPayType());
        //订单来源：0->PC订单；1->app订单
        order.setSourceType(1);
        //订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单
        order.setStatus(0);
        //订单类型：0->正常订单；1->秒杀订单
        order.setOrderType(0);
        //物流公司(配送方式)
        order.setDeliveryCompany(null);
        //物流单号
        order.setDeliverySn(null);
        //自动确认时间（天）
        order.setAutoConfirmDay(null);
        //计算赠送积分
        order.setIntegration(calcOrderGifIntegration(orderItems));
        //计算赠送成长值
        order.setGrowth(calcOrderGiftGrowth(orderItems));
        order.setPromotionInfo(getOrderPromotionInfo(orderItems));
        order.setBillType(null);
        order.setBillContent(null);
        order.setBillHeader(null);
        order.setBillReceiverEmail(null);
        order.setBillReceiverPhone(null);
        //收货人信息：姓名、电话、邮编、地址
        UmsMemberReceiveAddress address = umsMemberReceiveAddressService.getItem(orderParam.getMemberReceiveAddressId());
        order.setReceiverName(address.getName());
        order.setReceiverPhone(address.getPhoneNumber());
        order.setReceiverPostCode(address.getPostCode());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverCity(address.getCity());
        order.setReceiverRegion(address.getRegion());
        order.setReceiverDetailAddress(address.getDetailAddress());

        //订单备注
        order.setNote(null);
        //确认收货状态 0->未确认；1->已确认
        order.setConfirmStatus(0);
        //删除状态：0->未删除；1->已删除
        order.setDeleteStatus(0);
        //下单时使用的积分
        order.setUseIntegration(orderParam.getUseIntegration());
        //支付时间
        order.setPaymentTime(null);
        //发货时间
        order.setDeliveryTime(null);
        //确认收货时间
        order.setReceiveTime(null);
        //评价时间
        order.setCommentTime(null);
        //修改时间
        order.setModifyTime(null);
        //生成订单号
        order.setOrderSn(generateOrderSn(order));

        //订单编号
        order.setOrderSn(generateOrderSn(order));//todo
        //应付金额（实际支付金额）
        order.setPayAmount(calcOrderPayAmount(order));

        omsOrderMapper.insert(order);

        for (OmsOrderItem orderItem : orderItems) {
            orderItem.setOrderId(order.getId());
            orderItem.setOrderSn(order.getOrderSn());
            omsOrderItemMapper.insert(orderItem);
        }
        //如使用优惠券更新优惠券使用状态
//        SmsCouponHistoryDetail smsCouponHistoryDetail = getUseCoupon(cartPromotionItems, orderParam.getCouponId());
        //如使用积分需要扣除积分
        if (orderParam.getUseIntegration() != null) {
            umsMemberService.updateIntegration(umsMember.getId(), umsMember.getIntegration() - orderParam.getUseIntegration());
        }
        //删除购物车中的下单商品
        deleteCartItemList(cartPromotionItems, umsMember);
        //发送延迟消息取消订单

        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("orderItemList", order);
        return result;
    }


    /**
     * 删除购物车中的下单商品
     *
     * @param cartPromotionItems
     * @param umsMember
     */
    private void deleteCartItemList(List<CartPromotionItem> cartPromotionItems, UmsMember umsMember) {
        List<Long> list = new ArrayList<>();
        for (CartPromotionItem cartPromotionItem : cartPromotionItems) {
            list.add(cartPromotionItem.getId());
        }
        omsCartItemService.delete(umsMember.getId(), list);
    }


    private BigDecimal calcOrderPayAmount(OmsOrder omsOrder) {
        //总金额+运费-促销优惠-优惠券优惠-积分抵扣-管理员后台调整订单使用的折扣金额
        BigDecimal payAmount = omsOrder.getTotalAmount()
                .add(omsOrder.getFreightAmount())
                .subtract(omsOrder.getPromotionAmount())
                .subtract(omsOrder.getIntegrationAmount())
                .subtract(omsOrder.getCouponAmount())
                .subtract(omsOrder.getDiscountAmount());
        return payAmount;
    }


    /**
     * 生成订单优惠信息描述
     *
     * @param orderItemList
     * @return
     */
    private String getOrderPromotionInfo(List<OmsOrderItem> orderItemList) {
        StringBuilder sb = new StringBuilder();
        for (OmsOrderItem orderItem : orderItemList) {
            sb.append(orderItem.getPromotionName());
            sb.append(",");
        }
        String result = sb.toString();
        if (result.endsWith(",")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }


    /**
     * 生成18位订单编号:8位日期+2位平台号码+2位支付方式+6位以上自增id
     */
    private String generateOrderSn(OmsOrder order) {
//        StringBuilder sb = new StringBuilder();
//        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
//        String key = REDIS_DATABASE + ":" + REDIS_KEY_ORDER_ID + date;
//        Long increment = redisService.incr(key, 1);
//        sb.append(date);
//        sb.append(String.format("%02d", order.getSourceType()));
//        sb.append(String.format("%02d", order.getPayType()));
//        String incrementStr = increment.toString();
//        if (incrementStr.length() <= 6) {
//            sb.append(String.format("%06d", increment));
//        } else {
//            sb.append(incrementStr);
//        }
//        return sb.toString();

        StringBuilder sb = new StringBuilder();
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
//        String key = REDIS_DATABASE + ":" + REDIS_KEY_ORDER_ID + date;
//        Long increment = redisService.incr(key, 1);
        sb.append(date);
        sb.append(String.format("%02d", order.getSourceType()));
        sb.append(String.format("%02d", order.getPayType()));
        sb.append(String.format("%06d", new Random().nextInt(1000)));

//        String incrementStr = increment.toString();
//        if (incrementStr.length() <= 6) {
//            sb.append(String.format("%06d", increment));
//        } else {
//            sb.append(incrementStr);
//        }
        return sb.toString();

    }


    /**
     * 计算该订单赠送的成长值
     */
    private Integer calcOrderGiftGrowth(List<OmsOrderItem> orderItemList) {
        Integer sum = 0;
        for (OmsOrderItem orderItem : orderItemList) {
            sum = sum + orderItem.getGiftGrowth() * orderItem.getProductQuantity();
        }
        return sum;
    }

    /**
     * 计算该订单赠送的积分
     */
    private Integer calcOrderGifIntegration(List<OmsOrderItem> orderItemList) {
        int sum = 0;
        for (OmsOrderItem orderItem : orderItemList) {
            sum += orderItem.getGiftIntegration() * orderItem.getProductQuantity();
        }
        return sum;
    }

    /**
     * 计算订单的优惠券抵扣金额
     *
     * @param orderItems
     * @return
     */
    private BigDecimal calcOrderCouponAmount(List<OmsOrderItem> orderItems) {
        BigDecimal couponAmount = BigDecimal.ZERO;
        for (OmsOrderItem orderItem : orderItems) {
            couponAmount = couponAmount.add(orderItem.getCouponAmount().multiply(new BigDecimal(orderItem.getProductQuantity())));
        }
        return couponAmount;
    }


    /**
     * 计算订单的积分抵扣金额
     *
     * @param orderItems
     * @return
     */
    private BigDecimal calcOrderIntegrationAmount(List<OmsOrderItem> orderItems) {
        BigDecimal integrationAmount = BigDecimal.ZERO;
        for (OmsOrderItem orderItem : orderItems) {
            integrationAmount = integrationAmount.add(orderItem.getIntegrationAmount().multiply(new BigDecimal(orderItem.getProductQuantity())));
        }
        return integrationAmount;
    }


    /**
     * 计算订单的促销价格
     *
     * @param orderItems
     * @return
     */
    private BigDecimal calcOrderPromotionAmount(List<OmsOrderItem> orderItems) {
        BigDecimal promotionAmount = BigDecimal.ZERO;
        for (OmsOrderItem orderItem : orderItems) {
            promotionAmount = promotionAmount.add(orderItem.getPromotionAmount().multiply(new BigDecimal(orderItem.getProductQuantity())));
        }
        return promotionAmount;
    }


    /**
     * 锁定库存
     *
     * @param cartPromotionItemList
     */
    private void lockStock(List<CartPromotionItem> cartPromotionItemList) {
        for (CartPromotionItem cartPromotionItem : cartPromotionItemList) {
            PmsSkuStock skuStock = pmsSkuStockMapper.selectByPrimaryKey(cartPromotionItem.getProductSkuId());
            skuStock.setLockStock(skuStock.getLockStock() + cartPromotionItem.getQuantity());
            pmsSkuStockMapper.updateByPrimaryKeySelective(skuStock);
        }
    }

    /**
     * 计算order_item的实付金额
     *
     * @param orderItems
     */
    private void handleRealAmount(List<OmsOrderItem> orderItems) {
        for (OmsOrderItem orderItem : orderItems) {
            //原价-促销优惠-优惠券抵扣-积分抵扣
            BigDecimal realAmount = orderItem.getProductPrice()
                    .subtract(orderItem.getPromotionAmount())
                    .subtract(orderItem.getCouponAmount())
                    .subtract(orderItem.getIntegrationAmount());
            orderItem.setRealAmount(realAmount);
        }
    }


    /**
     * 对优惠券进行处理
     *
     * @param orderItems
     * @param smsCouponHistoryDetail
     */
    private void handleCouponAmount(List<OmsOrderItem> orderItems, SmsCouponHistoryDetail smsCouponHistoryDetail) {
        // @ApiModelProperty(value = "使用类型：0->全场通用；1->指定分类；2->指定商品")
        int useType = smsCouponHistoryDetail.getCoupon().getUseType();
        SmsCoupon coupon = smsCouponHistoryDetail.getCoupon();
        if (useType == 0) {
            //全场通用
            calcPerCouponAmount(orderItems, coupon);
        } else if (useType == 1) {
            //指定分类
            List<OmsOrderItem> couponOrderItemList = getOmsOrderItemBySmsCategory(orderItems, smsCouponHistoryDetail);
            calcPerCouponAmount(couponOrderItemList, coupon);
        } else if (useType == 2) {
            //指定商品
            List<OmsOrderItem> couponOrderItemList = getOmsOrderItemBySmsProduct(orderItems, smsCouponHistoryDetail);
            calcPerCouponAmount(couponOrderItemList, coupon);

        }
    }

    /**
     * 根据营销分类获取到可以支持的购物车商品
     *
     * @param orderItems
     * @param smsCouponHistoryDetail
     * @return
     */
    private List<OmsOrderItem> getOmsOrderItemBySmsCategory(List<OmsOrderItem> orderItems, SmsCouponHistoryDetail smsCouponHistoryDetail) {
        List<OmsOrderItem> items = new ArrayList<>();
        List<Long> categoryIds = new ArrayList<>();
        for (SmsCouponProductCategoryRelation relation : smsCouponHistoryDetail.getCategoryRelationList()) {
            categoryIds.add(relation.getProductCategoryId());
        }
        for (OmsOrderItem orderItem : orderItems) {
            if (categoryIds.contains(orderItem.getProductCategoryId())) {
                items.add(orderItem);
            } else {
                orderItem.setCouponAmount(BigDecimal.ZERO);
            }
        }
        return items;
    }


    /**
     * 根据营销产品获取到可以支持的购物车商品
     *
     * @param orderItems
     * @param smsCouponHistoryDetail
     * @return
     */
    private List<OmsOrderItem> getOmsOrderItemBySmsProduct(List<OmsOrderItem> orderItems, SmsCouponHistoryDetail smsCouponHistoryDetail) {
        List<OmsOrderItem> items = new ArrayList<>();
        List<Long> productIds = new ArrayList<>();
        for (SmsCouponProductRelation relation : smsCouponHistoryDetail.getProductRelationList()) {
            productIds.add(relation.getProductId());
        }
        for (OmsOrderItem orderItem : orderItems) {
            if (productIds.contains(orderItem.getProductId())) {
                items.add(orderItem);
            } else {
                orderItem.setCouponAmount(BigDecimal.ZERO);
            }
        }
        return items;
    }

    /**
     * 对每个下单商品进行优惠券金额分摊的计算
     *
     * @param orderItemList 可用优惠券的下单商品商品
     */
    private void calcPerCouponAmount(List<OmsOrderItem> orderItemList, SmsCoupon smsCoupon) {
        BigDecimal totalAmount = calcTotalAmount(orderItemList);
        for (OmsOrderItem orderItem : orderItemList) {
            //(商品价格/可用商品总价)*优惠券面额
            BigDecimal couponAmount = orderItem.getProductPrice().divide(totalAmount, 3, RoundingMode.HALF_EVEN).multiply(smsCoupon.getAmount());
            orderItem.setCouponAmount(couponAmount);
        }
    }

    /**
     * 计算总额
     *
     * @param orderItems
     * @return
     */
    private BigDecimal calcTotalAmount(List<OmsOrderItem> orderItems) {
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OmsOrderItem orderItem : orderItems) {
            totalAmount = totalAmount.add(orderItem.getProductPrice().multiply(new BigDecimal(orderItem.getProductQuantity())));
        }
        return totalAmount;
    }


    /**
     * 获取该用户可以使用的优惠券 //todo 一个会员会有很多可用优惠券具体选哪张优惠券由用户自己决定传参过来
     *
     * @param cartPromotionItemList 购物车优惠列表
     * @param couponId              使用优惠券id   用户自己选择的可用优惠券
     */
    private SmsCouponHistoryDetail getUseCoupon(List<CartPromotionItem> cartPromotionItemList, Long couponId) {
        List<SmsCouponHistoryDetail> smsCouponHistoryDetails = umsMemberCouponService.listCart(cartPromotionItemList, 1);
        for (SmsCouponHistoryDetail smsCouponHistoryDetail : smsCouponHistoryDetails) {
            SmsCoupon smsCoupon = smsCouponHistoryDetail.getCoupon();
            if (smsCoupon.getId().equals(couponId)) {//todo 一个会员是不是可能会领取多张相同的优惠券呢？？？
                return smsCouponHistoryDetail;
            }
        }
        return null;
    }


    /**
     * 判断是否有库存
     *
     * @param cartPromotionItems
     * @return
     */
    private String hasStock(List<CartPromotionItem> cartPromotionItems) {
        StringBuilder sb = new StringBuilder();
        for (CartPromotionItem cartPromotionItem : cartPromotionItems) {
            Integer realStock = cartPromotionItem.getRealStock();
            if (realStock == null || realStock <= 0) {
                sb.append("商品[");
                sb.append(cartPromotionItem.getProductName());
                sb.append("]");
                sb.append("   ");
                sb.append("[" + cartPromotionItem.getProductId() + "]");
                sb.append("库存不足了");
                return sb.toString();
            }
        }
        return sb.toString();
    }

}