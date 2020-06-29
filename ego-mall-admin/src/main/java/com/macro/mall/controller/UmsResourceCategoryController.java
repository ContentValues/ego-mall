package com.macro.mall.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.UmsResourceCategory;
import com.macro.mall.service.UmsResourceCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-06-22 15:12
 **/
@Controller
@Api(tags = "UmsResourceCategoryController", description = "后台资源分类管理")
@RequestMapping("/resourceCategory")
public class UmsResourceCategoryController {
    @Resource
    private UmsResourceCategoryService umsResourceCategoryService;

    @ApiOperation("查询所有后台资源分类")
    @RequestMapping(value = "/listAll", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<UmsResourceCategory>> listAll() {
        List<UmsResourceCategory> resourceList = umsResourceCategoryService.listAll();
        return CommonResult.success(resourceList);
    }


}