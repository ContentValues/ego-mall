package com.macro.mall.controller;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.UmsResource;
import com.macro.mall.service.UmsResourceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-06-22 15:18
 **/
@Controller
@Api(tags = "UmsResourceController", description = "后台资源管理")
@RequestMapping("/resource")
public class UmsResourceController {

    @Autowired
    private UmsResourceService umsResourceService;

    @ApiOperation("查询所有后台资源")
    @RequestMapping(value = "/listAll", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<UmsResource>> listAll() {
        List<UmsResource> resourceList = umsResourceService.listAll();
        return CommonResult.success(resourceList);
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult create(@RequestBody UmsResource umsResource) {
        int index = umsResourceService.create(umsResource);
        return CommonResult.success("添加资源成功");
    }

    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult update(@PathVariable("id") Long id, @RequestBody UmsResource umsResource) {
        int index = umsResourceService.update(id, umsResource);
        return CommonResult.success("修改资源成功");
    }


    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult getItem(@PathVariable("id") Long id) {
        UmsResource umsResource = umsResourceService.getItem(id);
        return CommonResult.success(umsResource);
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult delete(@PathVariable("id") Long id) {
        int index = umsResourceService.delete(id);
        return CommonResult.success("删除资源成功");
    }

    @ApiOperation("分页模糊查询后台资源")
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<CommonPage<UmsResource>> list(@RequestParam(required = false) Long categoryId,
                                                      @RequestParam(required = false) String nameKeyword,
                                                      @RequestParam(required = false) String urlKeyword,
                                                      @RequestParam(value = "pageSize", defaultValue = "5") Integer pageSize,
                                                      @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum) {
        List<UmsResource> resourceList = umsResourceService.list(categoryId, nameKeyword, urlKeyword, pageSize, pageNum);
        return CommonResult.success(CommonPage.restPage(resourceList));
    }

}