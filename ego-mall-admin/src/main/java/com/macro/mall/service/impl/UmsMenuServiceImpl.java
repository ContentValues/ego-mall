package com.macro.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.macro.mall.dto.UmsMenuNode;
import com.macro.mall.mapper.UmsMenuMapper;
import com.macro.mall.model.UmsMenu;
import com.macro.mall.model.UmsMenuExample;
import com.macro.mall.service.UmsMenuService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-06-22 15:49
 **/
@Service
public class UmsMenuServiceImpl implements UmsMenuService {
    @Resource
    UmsMenuMapper umsMenuMapper;


    @Override
    public List<UmsMenuNode> treeList() {
        List<UmsMenu> menuList = umsMenuMapper.selectByExample(new UmsMenuExample());
        List<UmsMenuNode> result = menuList.stream()
                .filter(menu -> menu.getParentId().equals(0L))
                .map(menu -> covertMenuNode(menu, menuList)).collect(Collectors.toList());
        return result;
    }

    @Override
    public List<UmsMenu> list(Long parentId, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        UmsMenuExample example = new UmsMenuExample();
        example.createCriteria().andParentIdEqualTo(parentId);
        example.setOrderByClause("sort desc");
        return umsMenuMapper.selectByExample(example);
    }


    /**
     * 将UmsMenu转化为UmsMenuNode并设置children属性
     */
    private UmsMenuNode covertMenuNode(UmsMenu menu, List<UmsMenu> menuList) {
        UmsMenuNode node = new UmsMenuNode();
        BeanUtils.copyProperties(menu, node);
        List<UmsMenuNode> children = menuList.stream()
                .filter(subMenu -> subMenu.getParentId().equals(menu.getId()))
                .map(subMenu -> covertMenuNode(subMenu, menuList)).collect(Collectors.toList());
        node.setChildren(children);
        return node;
    }

    @Override
    public int create(UmsMenu umsMenu) {
        umsMenu.setCreateTime(new Date());
        if (umsMenu.getParentId().equals(0L)) {
            umsMenu.setLevel(0);
        } else {
            //有父菜单时选择根据父菜单level设置
            UmsMenu parentMenu = umsMenuMapper.selectByPrimaryKey(umsMenu.getParentId());
            if (parentMenu != null) {
                umsMenu.setLevel(parentMenu.getLevel() + 1);
            } else {
                umsMenu.setLevel(0);
            }
        }
        return umsMenuMapper.insert(umsMenu);
    }

    @Override
    public int update(Long id, UmsMenu umsMenu) {
        umsMenu.setId(id);
        UmsMenu parentMenu = umsMenuMapper.selectByPrimaryKey(umsMenu.getParentId());
        if (parentMenu != null) {
            umsMenu.setLevel(parentMenu.getLevel() + 1);
        } else {
            umsMenu.setLevel(0);
        }
        return umsMenuMapper.updateByPrimaryKeySelective(umsMenu);
    }

    @Override
    public int delete(Long id) {
        return umsMenuMapper.deleteByPrimaryKey(id);
    }

    @Override
    public UmsMenu getItem(Long id) {
        return umsMenuMapper.selectByPrimaryKey(id);
    }


}