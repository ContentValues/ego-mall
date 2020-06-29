package com.macro.mall.controller;

import com.macro.mall.bo.AdminUserDetails;
import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.api.CommonResult;
import com.macro.mall.dto.UmsAdminLoginParam;
import com.macro.mall.dto.UmsAdminParam;
import com.macro.mall.model.UmsAdmin;
import com.macro.mall.model.UmsRole;
import com.macro.mall.service.UmsAdminService;
import com.macro.mall.service.UmsRoleService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-06-18 10:16
 **/
@RestController
@RequestMapping("/admin")
public class UmsAdminController {
    @Value("${jwt.tokenHead}")
    private String tokenHead;
    @Resource
    UmsAdminService umsAdminService;

    @Resource
    UmsRoleService umsRoleService;

    @Value("${jwt.tokenHeader}")
    private String tokenHeader;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public CommonResult login(@RequestBody @Valid UmsAdminLoginParam umsAdminLoginParam, BindingResult result) {
        String token = umsAdminService.login(umsAdminLoginParam.getUsername(), umsAdminLoginParam.getPassword());
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        tokenMap.put("tokenHead", tokenHead);
        return CommonResult.success(tokenMap);
    }

    @ApiOperation(value = "获取当前登录用户信息")
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    public CommonResult getAdminInfo(Principal principal) {
        if (principal == null) {
            return CommonResult.unauthorized(null);
        }
        UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
        AdminUserDetails adminUserDetails = (AdminUserDetails) token.getPrincipal();
        UmsAdmin umsAdmin = adminUserDetails.umsAdmin;
        Map<String, Object> data = new HashMap<>();
        data.put("username", umsAdmin.getUsername());
        data.put("roles", new String[]{"TEST"});
        data.put("menus", umsRoleService.getMenuListByAdminId(umsAdmin.getId()));
        data.put("icon", umsAdmin.getIcon());
        return CommonResult.success(data);
    }

    @ApiOperation(value = "登出功能")
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public CommonResult logout() {
        return CommonResult.success(null);
    }

    @ApiOperation(value = "刷新token")
    @RequestMapping(value = "/refreshToken", method = RequestMethod.GET)
    public CommonResult refreshToken(HttpServletRequest request) {
        String token = request.getHeader(tokenHeader);
        String refreshToken = umsAdminService.refreshToken(token);
        if (refreshToken == null) {
            return CommonResult.failed("token已经过期！");
        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", refreshToken);
        tokenMap.put("tokenHead", tokenHead);
        return CommonResult.success(tokenMap);
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonResult list(@RequestParam(required = false) String keyword,
                             @RequestParam(defaultValue = "5") Integer pageSize,
                             @RequestParam(defaultValue = "1") Integer pageNum) {
        return CommonResult.success(CommonPage.restPage(umsAdminService.list(keyword, pageSize, pageNum)));
    }

    @ApiOperation(value = "注册")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public CommonResult register(@Validated @RequestBody UmsAdminParam umsAdminParam, BindingResult bindingResult) {
        int index = umsAdminService.register(umsAdminParam);
        if (index != 1) {
            return CommonResult.failed("注册失败用户名重复");
        }
        return CommonResult.success("注册成功");
    }

    @ApiOperation(value = "删除")
    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    public CommonResult delete(@PathVariable Long id) {
        umsAdminService.delete(id);
        return CommonResult.success("删除成功");
    }


    @ApiOperation(value = "修改用户信息")
    @RequestMapping(value = "/update/{id}", method = RequestMethod.POST)
    public CommonResult update(@PathVariable Long id, @RequestBody UmsAdminParam umsAdminParam) {
        umsAdminService.update(id, umsAdminParam);
        return CommonResult.success("修改用户信息成功");
    }


    @ApiOperation("获取指定用户的角色")
    @RequestMapping(value = "/role/{adminId}", method = RequestMethod.GET)
    @ResponseBody
    public CommonResult<List<UmsRole>> getRoleList(@PathVariable Long adminId) {
        return CommonResult.success(umsAdminService.getRoleList(adminId));
    }

    @ApiOperation("给用户分配角色")
    @RequestMapping(value = "/role/update", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult updateRole(@RequestParam("adminId") Long adminId,
                                   @RequestParam("roleIds") List<Long> roleIds) {
        umsAdminService.updateRole(adminId, roleIds);
        return CommonResult.success("给用户分配角色成功");
    }

}