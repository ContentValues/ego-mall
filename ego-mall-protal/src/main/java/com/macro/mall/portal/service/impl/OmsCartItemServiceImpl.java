package com.macro.mall.portal.service.impl;

import com.macro.mall.mapper.OmsCartItemMapper;
import com.macro.mall.model.OmsCartItem;
import com.macro.mall.model.OmsCartItemExample;
import com.macro.mall.model.UmsMember;
import com.macro.mall.portal.domain.CartProduct;
import com.macro.mall.portal.domain.CartPromotionItem;
import com.macro.mall.portal.service.OmsCartItemService;
import com.macro.mall.portal.service.OmsPromotionService;
import com.macro.mall.portal.service.UmsMemberService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-07-13 10:48
 **/
@Service
public class OmsCartItemServiceImpl implements OmsCartItemService {

    @Resource
    OmsCartItemMapper omsCartItemMapper;

    @Resource
    UmsMemberService umsMemberService;

    @Resource
    OmsPromotionService omsPromotionService;

    @Override
    public int add(OmsCartItem cartItem) {
        int index = 0;
        UmsMember umsMember = umsMemberService.getCurrentMember();
        cartItem.setMemberId(umsMember.getId());
        cartItem.setMemberNickname(umsMember.getNickname());

        OmsCartItemExample example = new OmsCartItemExample();
        OmsCartItemExample.Criteria criteria = example.createCriteria()
                .andProductIdEqualTo(cartItem.getProductId())
                .andMemberIdEqualTo(cartItem.getMemberId())
                .andDeleteStatusEqualTo(0);
        if (!StringUtils.isEmpty(cartItem.getProductSkuId())) {
            criteria.andProductSkuIdEqualTo(cartItem.getProductSkuId());
        }
        List<OmsCartItem> omsCartItemList = omsCartItemMapper.selectByExample(example);
        if (!CollectionUtils.isEmpty(omsCartItemList)) {
            OmsCartItem omsCartItemOrigin = omsCartItemList.get(0);
            omsCartItemOrigin.setQuantity(omsCartItemOrigin.getQuantity() + cartItem.getQuantity());
            omsCartItemOrigin.setModifyDate(new Date());
            index = omsCartItemMapper.updateByPrimaryKey(omsCartItemOrigin);
        } else {
            Date date = new Date();
            cartItem.setCreateDate(date);
            cartItem.setModifyDate(date);
            cartItem.setDeleteStatus(0);
            index = omsCartItemMapper.insert(cartItem);
        }
        return index;
    }

    @Override
    public List<OmsCartItem> list(Long memberId) {
        OmsCartItemExample example = new OmsCartItemExample();
        example.createCriteria().andDeleteStatusEqualTo(0)
                .andMemberIdEqualTo(memberId);
        return omsCartItemMapper.selectByExample(example);
    }

    @Override
    public List<CartPromotionItem> listPromotion(Long memberId) {
        List<OmsCartItem> omsCartItems =  list(memberId);
        return omsPromotionService.calcCartPromotion(omsCartItems);
    }

    @Override
    public CartProduct getCartProduct(Long productId) {
        return null;
    }

    @Override
    public int delete(Long memberId, List<Long> ids) {
        OmsCartItem record = new OmsCartItem();
        record.setDeleteStatus(1);
        OmsCartItemExample example = new OmsCartItemExample();
        example.createCriteria().andIdIn(ids).andMemberIdEqualTo(memberId);
        return omsCartItemMapper.updateByExampleSelective(record, example);
    }


}