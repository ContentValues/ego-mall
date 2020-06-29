package com.macro.mall.service;

import com.macro.mall.dto.UmsMenuNode;
import com.macro.mall.model.UmsMenu;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-06-22 15:47
 **/
public interface UmsMenuService {

    /**
     * 树形结构返回所有菜单列表
     */
    List<UmsMenuNode> treeList();

    /**
     * 分页查询后台菜单
     */
    List<UmsMenu> list(Long parentId, Integer pageSize, Integer pageNum);

    int create(UmsMenu umsMenu);

    int update(Long id, UmsMenu umsMenu);

    int delete(Long id);

    UmsMenu getItem(Long id);
}