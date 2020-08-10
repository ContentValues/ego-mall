package com.macro.mall.service.impl;
import com.github.pagehelper.PageHelper;
import com.macro.mall.dto.SmsFlashPromotionSessionDetail;
import com.macro.mall.mapper.SmsFlashPromotionSessionMapper;
import com.macro.mall.model.SmsFlashPromotionSession;
import com.macro.mall.model.SmsFlashPromotionSessionExample;
import com.macro.mall.service.SmsFlashPromotionSessionService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-08-07 15:03
 **/
@Service
public class SmsFlashPromotionSessionServiceImpl implements SmsFlashPromotionSessionService {
    @Resource
    SmsFlashPromotionSessionMapper flashPromotionSessionMapper;



    @Override
    public int create(SmsFlashPromotionSession flashPromotionSession) {
        flashPromotionSession.setCreateTime(new Date());
        return flashPromotionSessionMapper.insert(flashPromotionSession);
    }

    @Override
    public int delete(Long id) {
        return flashPromotionSessionMapper.deleteByPrimaryKey(id);
    }

    @Override
    public SmsFlashPromotionSession getItem(Long id) {
        return flashPromotionSessionMapper.selectByPrimaryKey(id);
    }

    @Override
    public int update(Long id, SmsFlashPromotionSession flashPromotionSession) {
        flashPromotionSession.setId(id);
        return flashPromotionSessionMapper.updateByPrimaryKeySelective(flashPromotionSession);
    }

    @Override
    public List<SmsFlashPromotionSession> list(String keyword, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        SmsFlashPromotionSessionExample example = new SmsFlashPromotionSessionExample();
        if(!StringUtils.isEmpty(keyword)){
            example.createCriteria().andNameLike("%"+keyword+"%");
        }
        return flashPromotionSessionMapper.selectByExample(example);
    }

    @Override
    public List<SmsFlashPromotionSessionDetail> selectList(Long flashPromotionId) {
        List<SmsFlashPromotionSessionDetail> result = new ArrayList<>();
        SmsFlashPromotionSessionExample example = new SmsFlashPromotionSessionExample();
        example.createCriteria().andStatusEqualTo(1);
        List<SmsFlashPromotionSession> list = flashPromotionSessionMapper.selectByExample(example);
        for (SmsFlashPromotionSession promotionSession : list) {
            SmsFlashPromotionSessionDetail detail = new SmsFlashPromotionSessionDetail();
            BeanUtils.copyProperties(promotionSession, detail);
//            long count = relationService.getCount(flashPromotionId, promotionSession.getId());
//            detail.setProductCount(count);
            detail.setProductCount(0L);
            result.add(detail);
        }
        return result;
    }
}