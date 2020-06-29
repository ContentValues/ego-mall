package com.macro.mall.controller;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.dto.UmsMenuNode;
import com.macro.mall.model.UmsMenu;
import com.macro.mall.service.UmsMenuService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-06-22 16:10
 **/
@Controller
@Api(tags = "UmsMenuController", description = "后台菜单管理")
@RequestMapping("/menu")
public class UmsMenuController {

    @Resource
    UmsMenuService umsMenuService;

    @ApiOperation("树形结构返回所有菜单列表")
    @RequestMapping(value = "/treeList", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<UmsMenuNode>> treeList() {
        List<UmsMenuNode> list = umsMenuService.treeList();
        return CommonResult.success(list);
    }

    @RequestMapping(value = "/list/{parentId}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult list(@PathVariable Long parentId,
                             @RequestParam(defaultValue = "5") Integer pageSize,
                             @RequestParam(defaultValue = "1") Integer pageNum) {
        return CommonResult.success(CommonPage.restPage(umsMenuService.list(parentId, pageSize, pageNum)));
    }


    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult create(@RequestBody UmsMenu umsMenu) {
        int index = umsMenuService.create(umsMenu);
        return CommonResult.success("创建菜单成功");
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult delete(@PathVariable Long id) {
        int index = umsMenuService.delete(id);
        return CommonResult.success("删除菜单成功");
    }

    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult update(@PathVariable Long id, @RequestBody UmsMenu umsMenu) {
        int index = umsMenuService.update(id, umsMenu);
        return CommonResult.success("修改菜单成功");
    }

    @ApiOperation("根据ID获取菜单详情")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<UmsMenu> getItem(@PathVariable Long id) {
        UmsMenu umsMenu = umsMenuService.getItem(id);
        return CommonResult.success(umsMenu);
    }

    @ApiOperation("修改菜单显示状态")
    @RequestMapping(value = "/updateHidden/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateHidden(@PathVariable Long id, @RequestParam("hidden") Integer hidden) {
        UmsMenu umsMenu = new UmsMenu();
        umsMenu.setId(id);
        umsMenu.setHidden(hidden);
        int count = umsMenuService.update(id, umsMenu);
        if (count > 0) {
            return CommonResult.success(count);
        } else {
            return CommonResult.failed();
        }
    }

}