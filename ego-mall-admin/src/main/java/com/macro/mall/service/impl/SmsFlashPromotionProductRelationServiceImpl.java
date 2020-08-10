package com.macro.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.macro.mall.dto.SmsFlashPromotionProduct;
import com.macro.mall.mapper.PmsProductMapper;
import com.macro.mall.mapper.SmsFlashPromotionProductRelationMapper;
import com.macro.mall.model.PmsProduct;
import com.macro.mall.model.SmsFlashPromotionProductRelation;
import com.macro.mall.model.SmsFlashPromotionProductRelationExample;
import com.macro.mall.service.SmsFlashPromotionProductRelationService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-08-07 16:07
 **/
@Service
public class SmsFlashPromotionProductRelationServiceImpl implements SmsFlashPromotionProductRelationService {
    @Resource
    SmsFlashPromotionProductRelationMapper flashPromotionProductRelationMapper;
    @Resource
    PmsProductMapper pmsProductMapper;

    @Override
    public int create(List<SmsFlashPromotionProductRelation> relationList) {
        relationList.forEach(flashPromotionProductRelation -> {
            flashPromotionProductRelationMapper.insert(flashPromotionProductRelation);
        });
        return relationList.size();
    }

    @Override
    public int delete(Long id) {
        return flashPromotionProductRelationMapper.deleteByPrimaryKey(id);
    }

    @Override
    public SmsFlashPromotionProductRelation getItem(Long id) {
        return flashPromotionProductRelationMapper.selectByPrimaryKey(id);
    }

    @Override
    public int update(Long id, SmsFlashPromotionProductRelation flashPromotionProductRelation) {
        flashPromotionProductRelation.setId(id);
        return flashPromotionProductRelationMapper.updateByPrimaryKeySelective(flashPromotionProductRelation);
    }

    @Override
    public List<SmsFlashPromotionProduct> list(Long flashPromotionId, Long flashPromotionSessionId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        SmsFlashPromotionProductRelationExample example = new SmsFlashPromotionProductRelationExample();
        example.createCriteria().andFlashPromotionIdEqualTo(flashPromotionId)
                .andFlashPromotionSessionIdEqualTo(flashPromotionSessionId);
        return flashPromotionProductRelationMapper.selectByExample(example)
                .stream()
                .map(flashPromotionProductRelation -> {
                    PmsProduct pmsProduct = pmsProductMapper.selectByPrimaryKey(flashPromotionProductRelation.getProductId());
                    SmsFlashPromotionProduct smsFlashPromotionProduct = new SmsFlashPromotionProduct();
                    BeanUtils.copyProperties(flashPromotionProductRelation,smsFlashPromotionProduct);
                    smsFlashPromotionProduct.setProduct(pmsProduct);
                    return smsFlashPromotionProduct;
                }).collect(Collectors.toList());
    }
}