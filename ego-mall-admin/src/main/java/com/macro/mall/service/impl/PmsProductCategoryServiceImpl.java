package com.macro.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.macro.mall.dto.PmsProductCategoryParam;
import com.macro.mall.dto.PmsProductCategoryWithChildrenItem;
import com.macro.mall.dto.UmsMenuNode;
import com.macro.mall.mapper.PmsProductCategoryMapper;
import com.macro.mall.model.PmsProductCategory;
import com.macro.mall.model.PmsProductCategoryExample;
import com.macro.mall.model.UmsMenu;
import com.macro.mall.model.UmsMenuExample;
import com.macro.mall.service.PmsProductCategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-06-24 11:49
 **/
@Service
public class PmsProductCategoryServiceImpl implements PmsProductCategoryService {

    @Resource
    PmsProductCategoryMapper pmsProductCategoryMapper;

    @Override
    public int create(PmsProductCategoryParam pmsProductCategoryParam) {
        PmsProductCategory pmsProductCategory = new PmsProductCategory();
        BeanUtils.copyProperties(pmsProductCategoryParam, pmsProductCategory);
        pmsProductCategory.setProductCount(0);
        //没有父分类时为一级分类
        if (pmsProductCategory.getParentId() == 0) {
            pmsProductCategory.setLevel(0);
        } else {
            //有父分类时选择根据父分类level设置
            PmsProductCategory parentCategory = pmsProductCategoryMapper.selectByPrimaryKey(pmsProductCategory.getParentId());
            if (parentCategory != null) {
                pmsProductCategory.setLevel(parentCategory.getLevel() + 1);
            } else {
                pmsProductCategory.setLevel(0);
            }
        }
        return pmsProductCategoryMapper.insert(pmsProductCategory);
    }

    @Override
    public int delete(Long id) {
        return pmsProductCategoryMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int update(Long id, PmsProductCategoryParam pmsProductCategoryParam) {
        PmsProductCategory category = new PmsProductCategory();
        category.setId(id);

        //没有父分类时为一级分类
        if (category.getParentId() == 0) {
            category.setLevel(0);
        } else {
            //有父分类时选择根据父分类level设置
            PmsProductCategory parentCategory = pmsProductCategoryMapper.selectByPrimaryKey(category.getParentId());
            if (parentCategory != null) {
                category.setLevel(parentCategory.getLevel() + 1);
            } else {
                category.setLevel(0);
            }
        }

        return pmsProductCategoryMapper.updateByPrimaryKeySelective(category);
    }

    @Override
    public PmsProductCategory getItem(Long id) {
        return pmsProductCategoryMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<PmsProductCategory> getList(Long parentId, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        PmsProductCategoryExample example = new PmsProductCategoryExample();
        if (parentId != null) {
            example.createCriteria().andParentIdEqualTo(parentId);
        }
        return pmsProductCategoryMapper.selectByExample(example);
    }

    @Override
    public List<PmsProductCategoryWithChildrenItem> listWithChildren() {
        List<PmsProductCategory> productCategoryList = pmsProductCategoryMapper.selectByExample(new PmsProductCategoryExample());
        List<PmsProductCategoryWithChildrenItem> result = productCategoryList.stream()
                .filter(category -> category.getParentId().equals(0L))
                .map(category -> covertNode(category, productCategoryList)).collect(Collectors.toList());
        return result;
    }

    /**
     * 将UmsMenu转化为UmsMenuNode并设置children属性
     */
    private PmsProductCategoryWithChildrenItem covertNode(PmsProductCategory category, List<PmsProductCategory> productCategoryList) {
        PmsProductCategoryWithChildrenItem node = new PmsProductCategoryWithChildrenItem();
        BeanUtils.copyProperties(category, node);
        List<PmsProductCategory> children = productCategoryList.stream()
                .filter(subMenu -> subMenu.getParentId().equals(category.getId()))
                .map(subMenu -> covertNode(subMenu, productCategoryList)).collect(Collectors.toList());
        node.setChildren(children);
        return node;
    }


    @Override
    public int updateNavStatus(Integer navStatus, List<Long> ids) {
        for (Long id : ids) {
            PmsProductCategory category = new PmsProductCategory();
            category.setId(id);
            category.setNavStatus(navStatus);
            pmsProductCategoryMapper.updateByPrimaryKeySelective(category);
        }
        return ids.size();
    }

    @Override
    public int updateShowStatus(Integer showStatus, List<Long> ids) {
        for (Long id : ids) {
            PmsProductCategory category = new PmsProductCategory();
            category.setId(id);
            category.setShowStatus(showStatus);
            pmsProductCategoryMapper.updateByPrimaryKeySelective(category);
        }
        return ids.size();
    }
}