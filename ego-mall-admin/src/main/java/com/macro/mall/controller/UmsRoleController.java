package com.macro.mall.controller;

import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.model.UmsMenu;
import com.macro.mall.model.UmsResource;
import com.macro.mall.model.UmsRole;
import com.macro.mall.service.UmsRoleService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-06-19 10:43
 **/
@RestController
@RequestMapping("/role")
public class UmsRoleController {

    @Resource
    UmsRoleService umsRoleService;

    @RequestMapping(value = "/listAll", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult listAll() {
        return CommonResult.success(umsRoleService.list());
    }


    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult list(@RequestParam(required = false) String keyword, @RequestParam Integer pageSize, @RequestParam Integer pageNum) {
        return CommonResult.success(CommonPage.restPage(umsRoleService.list(keyword, pageSize, pageNum)));
    }

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public CommonResult delete(@RequestBody UmsRole umsRole) {
        int index = umsRoleService.create(umsRole);
        if (index != 1) {
            return CommonResult.failed("角色已存在");
        }
        return CommonResult.success("新建成功");
    }

    @RequestMapping(value = "update/{id}", method = RequestMethod.POST)
    public CommonResult update(@PathVariable Long id, @RequestBody UmsRole umsRole) {
        umsRoleService.update(id, umsRole);
        return CommonResult.success("更新成功");
    }

    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public CommonResult delete(@RequestParam("ids") List<Long> ids) {
        umsRoleService.delete(ids);
        return CommonResult.success("删除成功");
    }

    @ApiOperation("修改角色状态")
    @RequestMapping(value = "/updateStatus/{id}", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateStatus(@PathVariable Long id, @RequestParam(value = "status") Integer status) {
        UmsRole umsRole = new UmsRole();
        umsRole.setId(id);
        umsRole.setStatus(status);
        umsRoleService.update(id,umsRole);
        return CommonResult.success("更新成功");
    }

    @ApiOperation("获取角色相关菜单")
    @RequestMapping(value = "/listMenu/{roleId}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<UmsMenu>> listMenu(@PathVariable Long roleId) {
        List<UmsMenu> roleList = umsRoleService.listMenu(roleId);
        return CommonResult.success(roleList);
    }

    @ApiOperation("获取角色相关资源")
    @RequestMapping(value = "/listResource/{roleId}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<UmsResource>> listResource(@PathVariable Long roleId) {
        List<UmsResource> roleList = umsRoleService.listResource(roleId);
        return CommonResult.success(roleList);
    }

    @ApiOperation("给角色分配菜单")
    @RequestMapping(value = "/allocMenu", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult allocMenu(@RequestParam Long roleId, @RequestParam List<Long> menuIds) {
        int count = umsRoleService.allocMenu(roleId, menuIds);
        return CommonResult.success(count);
    }

    @ApiOperation("给角色分配资源")
    @RequestMapping(value = "/allocResource", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult allocResource(@RequestParam Long roleId, @RequestParam List<Long> resourceIds) {
        int count = umsRoleService.allocResource(roleId, resourceIds);
        return CommonResult.success(count);
    }
}