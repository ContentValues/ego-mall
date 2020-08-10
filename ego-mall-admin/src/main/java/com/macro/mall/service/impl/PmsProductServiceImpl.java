package com.macro.mall.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.macro.mall.dao.PmsProductDao;
import com.macro.mall.dto.PmsProductParam;
import com.macro.mall.dto.PmsProductResult;
import com.macro.mall.mapper.*;
import com.macro.mall.model.*;
import com.macro.mall.service.PmsProductService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-06-29 18:33
 **/
@Service
public class PmsProductServiceImpl implements PmsProductService {

    @Resource
    PmsProductMapper productMapper;

    @Resource
    PmsProductLadderMapper ladderMapper;

    @Resource
    PmsProductFullReductionMapper fullReductionMapper;

    @Resource
    PmsMemberPriceMapper memberPriceMapper;

    @Resource
    PmsSkuStockMapper skuStockMapper;

    @Resource
    PmsProductAttributeValueMapper attributeValueMapper;

    @Resource
    private PmsProductDao productDao;


    @Override
    public int create(PmsProductParam productParam) {
        int count;
        //创建商品
        PmsProduct product = productParam;
        productMapper.insert(product);
        //根据促销类型设置价格：会员价格、阶梯价格、满减价格
        Long productId = product.getId();

        //阶梯价格
        for (PmsProductLadder pmsProductLadder : productParam.getProductLadderList()) {
            pmsProductLadder.setProductId(productId);
            ladderMapper.insert(pmsProductLadder);
        }
        //满减价格
        for (PmsProductFullReduction pmsProductFullReduction : productParam.getProductFullReductionList()) {
            pmsProductFullReduction.setProductId(productId);
            fullReductionMapper.insert(pmsProductFullReduction);
        }
        //会员价格
        for (PmsMemberPrice pmsMemberPrice : productParam.getMemberPriceList()) {
            pmsMemberPrice.setProductId(productId);
            memberPriceMapper.insert(pmsMemberPrice);
        }
        //处理sku的编码
        handleSkuStockCode(productParam.getSkuStockList(),productId);
        //添加sku库存信息
        for (PmsSkuStock skuStock : productParam.getSkuStockList()) {
            skuStock.setProductId(productId);
            skuStockMapper.insert(skuStock);
        }
        //添加商品参数,添加自定义商品规格
        for (PmsProductAttributeValue attributeValue : productParam.getProductAttributeValueList()) {
            attributeValue.setProductId(productId);
            attributeValueMapper.insert(attributeValue);
        }
        //关联专题
//        relateAndInsertList(subjectProductRelationDao, productParam.getSubjectProductRelationList(), productId);
        //关联优选
//        relateAndInsertList(prefrenceAreaProductRelationDao, productParam.getPrefrenceAreaProductRelationList(), productId);
        count = 1;
        return count;
    }

    @Override
    public int update(Long id,PmsProductParam productParam) {

        int count;
        //创建商品
        PmsProduct product = productParam;
        product.setId(id);
        //根据促销类型设置价格：会员价格、阶梯价格、满减价格
        Long productId = product.getId();

        //阶梯价格
        PmsProductLadderExample ladderExample = new PmsProductLadderExample();
        ladderExample.createCriteria().andProductIdEqualTo(id);
        ladderMapper.deleteByExample(ladderExample);
        for (PmsProductLadder pmsProductLadder : productParam.getProductLadderList()) {
            pmsProductLadder.setProductId(productId);
            ladderMapper.insert(pmsProductLadder);
        }
        //满减价格
        PmsProductFullReductionExample fullReductionExample = new PmsProductFullReductionExample();
        fullReductionExample.createCriteria().andProductIdEqualTo(id);
        fullReductionMapper.deleteByExample(fullReductionExample);
        for (PmsProductFullReduction pmsProductFullReduction : productParam.getProductFullReductionList()) {
            pmsProductFullReduction.setProductId(productId);
            fullReductionMapper.insert(pmsProductFullReduction);
        }
        //会员价格
        PmsMemberPriceExample pmsMemberPriceExample = new PmsMemberPriceExample();
        pmsMemberPriceExample.createCriteria().andProductIdEqualTo(id);
        memberPriceMapper.deleteByExample(pmsMemberPriceExample);
        for (PmsMemberPrice pmsMemberPrice : productParam.getMemberPriceList()) {
            pmsMemberPrice.setProductId(productId);
            memberPriceMapper.insert(pmsMemberPrice);
        }
        //修改sku库存信息
        handleUpdateSkuStockList(id, productParam);
        //添加sku库存信息
        for (PmsSkuStock skuStock : productParam.getSkuStockList()) {
            skuStock.setProductId(productId);
            skuStockMapper.insert(skuStock);
        }
        //添加商品参数,添加自定义商品规格
        PmsProductAttributeValueExample productAttributeValueExample = new PmsProductAttributeValueExample();
        productAttributeValueExample.createCriteria().andProductIdEqualTo(id);
        attributeValueMapper.deleteByExample(productAttributeValueExample);
        for (PmsProductAttributeValue attributeValue : productParam.getProductAttributeValueList()) {
            attributeValue.setProductId(productId);
            attributeValueMapper.insert(attributeValue);
        }
        //关联专题
//        relateAndInsertList(subjectProductRelationDao, productParam.getSubjectProductRelationList(), productId);
        //关联优选
//        relateAndInsertList(prefrenceAreaProductRelationDao, productParam.getPrefrenceAreaProductRelationList(), productId);
        count = 1;
        return count;
    }

    @Override
    public PmsProductResult getUpdateInfo(Long id) {
        return productDao.getUpdateInfo(id);
    }

    //添加sku库存信息
    private void handleSkuStockCode(List<PmsSkuStock> skuStockList, Long productId) {
        for(int i=0;i<skuStockList.size();i++){
            PmsSkuStock skuStock = skuStockList.get(i);
            if(StringUtils.isEmpty(skuStock.getSkuCode())){
                StringBuilder sb = new StringBuilder();
                //日期
                sb.append(new SimpleDateFormat("yyyyMMdd").format(new Date()));
                //四位商品id
                sb.append(String.format("%04d", productId));
                //三位索引id
                sb.append(String.format("%03d", i+1));
                skuStock.setSkuCode(sb.toString());
            }
        }
    }

    /**
     * 修改sku库存信息
     * 增加和删除可以直接操作
     * 但是修改 不能删除原来的逻辑 只需要修改 不然会对订单有影响
     * @param id
     * @param productParam
     */
    private void handleUpdateSkuStockList(Long id, PmsProductParam productParam) {
        //当前的sku信息
        List<PmsSkuStock> currSkuList = productParam.getSkuStockList();
        //当前没有sku直接删除
        if(CollUtil.isEmpty(currSkuList)){
            PmsSkuStockExample skuStockExample = new PmsSkuStockExample();
            skuStockExample.createCriteria().andProductIdEqualTo(id);
            skuStockMapper.deleteByExample(skuStockExample);
            return;
        }
        //获取初始sku信息
        PmsSkuStockExample skuStockExample = new PmsSkuStockExample();
        skuStockExample.createCriteria().andProductIdEqualTo(id);
        List<PmsSkuStock> oriStuList = skuStockMapper.selectByExample(skuStockExample);
        //获取新增sku信息
        List<PmsSkuStock> insertSkuList = currSkuList.stream().filter(item->item.getId()==null).collect(Collectors.toList());
        //获取需要更新的sku信息
        List<PmsSkuStock> updateSkuList = currSkuList.stream().filter(item->item.getId()!=null).collect(Collectors.toList());
        List<Long> updateSkuIds = updateSkuList.stream().map(PmsSkuStock::getId).collect(Collectors.toList());
        //获取需要删除的sku信息
        List<PmsSkuStock> removeSkuList = oriStuList.stream().filter(item-> !updateSkuIds.contains(item.getId())).collect(Collectors.toList());
        handleSkuStockCode(insertSkuList,id);
        handleSkuStockCode(updateSkuList,id);
        //新增sku
        if(CollUtil.isNotEmpty(insertSkuList)){
            for (PmsSkuStock skuStock : insertSkuList) {
                skuStock.setProductId(id);
                skuStockMapper.insert(skuStock);
            }
        }
        //删除sku
        if(CollUtil.isNotEmpty(removeSkuList)){
            List<Long> removeSkuIds = removeSkuList.stream().map(PmsSkuStock::getId).collect(Collectors.toList());
            PmsSkuStockExample removeExample = new PmsSkuStockExample();
            removeExample.createCriteria().andIdIn(removeSkuIds);
            skuStockMapper.deleteByExample(removeExample);
        }
        //修改sku
        if(CollUtil.isNotEmpty(updateSkuList)){
            for (PmsSkuStock pmsSkuStock : updateSkuList) {
                skuStockMapper.updateByPrimaryKeySelective(pmsSkuStock);
            }
        }

    }

}