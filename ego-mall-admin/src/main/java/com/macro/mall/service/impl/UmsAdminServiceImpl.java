package com.macro.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.macro.mall.bo.AdminUserDetails;
import com.macro.mall.common.exception.ApiException;
import com.macro.mall.dao.UmsAdminRoleRelationDao;
import com.macro.mall.dao.UmsRoleDao;
import com.macro.mall.dto.UmsAdminParam;
import com.macro.mall.mapper.UmsAdminLoginLogMapper;
import com.macro.mall.mapper.UmsAdminMapper;
import com.macro.mall.mapper.UmsAdminRoleRelationMapper;
import com.macro.mall.model.*;
import com.macro.mall.security.util.JwtTokenUtil;
import com.macro.mall.service.UmsAdminService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @program: ego-mall
 * @author: ShyBlue
 * @create: 2020-06-18 10:18
 **/
@Service
public class UmsAdminServiceImpl implements UmsAdminService {

    @Autowired
    UmsAdminMapper umsAdminMapper;

    @Autowired
    UmsAdminLoginLogMapper umsAdminLoginLogMapper;

    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @Autowired
    UmsAdminRoleRelationDao umsAdminRoleRelationDao;

    @Resource
    UmsRoleDao umsRoleDao;

    @Resource
    UmsAdminRoleRelationMapper umsAdminRoleRelationMapper;


    @Override
    public UmsAdmin getAdminByUsername(String username) {
        UmsAdminExample example = new UmsAdminExample();
        example.createCriteria().andUsernameEqualTo(username);
        List<UmsAdmin> adminList = umsAdminMapper.selectByExample(example);
        if (adminList != null && adminList.size() > 0) {
            return adminList.get(0);
        }
        return null;
    }


    @Override
    public String login(String username, String password) {

        /**
         *  1 根据用户名查询用户
         *
         *  2 校验用户
         *
         *  3 生成token
         *
         *  4 更新登录时间
         *
         *  5 插入登录日志
         *
         */
        AdminUserDetails adminUserDetails = loadUserByUsername(username);
        if (!adminUserDetails.getPassword().equals(password)) {
            throw new ApiException("密码不匹配");
        }
        String token = jwtTokenUtil.generateToken(adminUserDetails);
        updateLoginTimeByUsername(username);
        insertLoginLog(username);
        return token;
    }


    /**
     * 添加登录记录
     *
     * @param username 用户名
     */
    private void insertLoginLog(String username) {
        UmsAdmin admin = getAdminByUsername(username);
        if (admin == null) return;
        UmsAdminLoginLog loginLog = new UmsAdminLoginLog();
        loginLog.setAdminId(admin.getId());
        loginLog.setCreateTime(new Date());
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        loginLog.setIp(request.getRemoteAddr());
        umsAdminLoginLogMapper.insert(loginLog);
    }

    /**
     * 根据用户名修改登录时间
     */
    private void updateLoginTimeByUsername(String username) {
        UmsAdmin record = new UmsAdmin();
        record.setLoginTime(new Date());
        UmsAdminExample example = new UmsAdminExample();
        example.createCriteria().andUsernameEqualTo(username);
        umsAdminMapper.updateByExampleSelective(record, example);
    }


    @Override
    public AdminUserDetails loadUserByUsername(String username) {
        UmsAdmin umsAdmin = getAdminByUsername(username);
        if (umsAdmin != null) {
            List<UmsResource> resources = umsAdminRoleRelationDao.getResourceList(umsAdmin.getId());
            return new AdminUserDetails(umsAdmin, resources);
        }
        throw new ApiException("用户名或密码错误");
    }

    @Override
    public String refreshToken(String token) {
        return jwtTokenUtil.refreshHeadToken(token);
    }

    @Override
    public List<UmsAdmin> list(String keyword, Integer pageSize, Integer pageNum) {
        PageHelper.startPage(pageNum, pageSize);
        UmsAdminExample example = new UmsAdminExample();
        if (!StringUtils.isEmpty(keyword)) {
            example.createCriteria().andUsernameLike("%" + keyword + "%");
            example.or(example.createCriteria().andNickNameLike("%" + keyword + "%"));
        }
        return umsAdminMapper.selectByExample(example);
    }

    @Override
    public int delete(Long id) {
        return umsAdminMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int register(UmsAdminParam umsAdminParam) {

        UmsAdminExample example = new UmsAdminExample();
        example.createCriteria().andUsernameEqualTo(umsAdminParam.getUsername());
        if (!umsAdminMapper.selectByExample(example).isEmpty()) {
            return -1;
        }
        UmsAdmin umsAdmin = new UmsAdmin();
        BeanUtils.copyProperties(umsAdminParam, umsAdmin);
        Date date = new Date();
        umsAdmin.setLoginTime(date);
        umsAdmin.setCreateTime(date);
        umsAdmin.setStatus(1);
        return umsAdminMapper.insertSelective(umsAdmin);
    }

    @Override
    public int update(Long id, UmsAdminParam admin) {
        UmsAdmin umsAdmin = new UmsAdmin();
        BeanUtils.copyProperties(admin, umsAdmin);
        umsAdmin.setId(id);
        return umsAdminMapper.updateByPrimaryKeySelective(umsAdmin);
    }

    @Override
    public List<UmsRole> getRoleList(Long adminId) {
        return umsRoleDao.getRoleList(adminId);
    }

    @Override
    public int updateRole(Long adminId, List<Long> roleIds) {
        UmsAdminRoleRelationExample example = new UmsAdminRoleRelationExample();
        example.createCriteria().andAdminIdEqualTo(adminId);
        umsAdminRoleRelationMapper.deleteByExample(example);
        if (!CollectionUtils.isEmpty(roleIds)) {
            roleIds.stream()
                    .map(roleId -> {
                        UmsAdminRoleRelation relation = new UmsAdminRoleRelation();
                        relation.setAdminId(adminId);
                        relation.setRoleId(roleId);
                        return relation;
                    })
                    .forEach(it -> umsAdminRoleRelationMapper.insert(it));
        }
        return 1;
    }
}