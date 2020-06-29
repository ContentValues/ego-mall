package com.macro.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.macro.mall.dto.PmsProductAttributeParam;
import com.macro.mall.mapper.PmsProductAttributeMapper;
import com.macro.mall.model.PmsProductAttribute;
import com.macro.mall.model.PmsProductAttributeExample;
import com.macro.mall.service.PmsProductAttributeService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-06-24 15:35
 **/
@Service
public class PmsProductAttributeServiceImpl implements PmsProductAttributeService {
    @Resource
    PmsProductAttributeMapper pmsProductAttributeMapper;

    @Override
    public int create(PmsProductAttributeParam attributeParam) {
        PmsProductAttribute attribute = new PmsProductAttribute();
        BeanUtils.copyProperties(attributeParam,attribute);
        return pmsProductAttributeMapper.insert(attribute);
    }

    @Override
    public int update(Long id,PmsProductAttributeParam attributeParam) {
        PmsProductAttribute attribute = new PmsProductAttribute();
        BeanUtils.copyProperties(attributeParam,attribute);
        attribute.setId(id);
        return pmsProductAttributeMapper.updateByPrimaryKeySelective(attribute);
    }

    @Override
    public int delete(List<Long> ids) {
        for (Long id : ids) {
            pmsProductAttributeMapper.deleteByPrimaryKey(id);
        }
        return ids.size();
    }

    @Override
    public PmsProductAttribute getItem(Long id) {
        return pmsProductAttributeMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<PmsProductAttribute> list(Long cid, int type, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum,pageSize);
        PmsProductAttributeExample example = new PmsProductAttributeExample();
        example.createCriteria().andProductAttributeCategoryIdEqualTo(cid)
                .andTypeEqualTo(type);
        return pmsProductAttributeMapper.selectByExample(example);
    }
}