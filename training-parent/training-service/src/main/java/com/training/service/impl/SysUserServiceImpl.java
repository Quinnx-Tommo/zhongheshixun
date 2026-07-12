package com.training.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.training.common.dto.UserForm;
import com.training.common.dto.UserPageQuery;
import com.training.common.entity.SysUser;
import com.training.mapper.SysUserMapper;
import com.training.service.SysUserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 用户服务实现
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Resource
    private BCryptPasswordEncoder passwordEncoder;

    @Override
    public SysUser getByUsername(String username) {
        return baseMapper.selectByUsername(username);
    }

    @Override
    public SysUser getUserDetail(Long id) {
        return baseMapper.selectUserById(id);
    }

    @Override
    public boolean checkPassword(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    @Override
    public IPage<SysUser> page(UserPageQuery query) {
        Page<SysUser> page = new Page<>(query.getPageNum(), query.getPageSize());
        return baseMapper.selectUserPage(page, query.getRole(), query.getStatus(), query.getKeyword());
    }

    @Override
    public boolean createUser(UserForm form) {
        // 校验用户名唯一
        SysUser exist = getByUsername(form.getUsername());
        if (exist != null) {
            throw new IllegalArgumentException("用户名已存在");
        }
        SysUser user = new SysUser();
        user.setUsername(form.getUsername());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRealName(form.getRealName());
        user.setPhone(form.getPhone());
        user.setEmail(form.getEmail());
        user.setRole(form.getRole());
        user.setAvatar(form.getAvatar());
        user.setOrgName(form.getOrgName());
        user.setJobType(form.getJobType());
        user.setStatus(form.getStatus() == null ? 1 : form.getStatus());
        return save(user);
    }

    @Override
    public boolean updateUser(UserForm form) {
        if (form.getId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        SysUser exist = getById(form.getId());
        if (exist == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        // 用户名唯一校验（排除自己）
        if (StringUtils.hasText(form.getUsername())) {
            SysUser sameName = getByUsername(form.getUsername());
            if (sameName != null && !sameName.getId().equals(form.getId())) {
                throw new IllegalArgumentException("用户名已存在");
            }
            exist.setUsername(form.getUsername());
        }
        if (StringUtils.hasText(form.getPassword())) {
            exist.setPassword(passwordEncoder.encode(form.getPassword()));
        }
        if (form.getRealName() != null) {
            exist.setRealName(form.getRealName());
        }
        if (form.getPhone() != null) {
            exist.setPhone(form.getPhone());
        }
        if (form.getEmail() != null) {
            exist.setEmail(form.getEmail());
        }
        if (form.getRole() != null) {
            exist.setRole(form.getRole());
        }
        if (form.getAvatar() != null) {
            exist.setAvatar(form.getAvatar());
        }
        if (form.getOrgName() != null) {
            exist.setOrgName(form.getOrgName());
        }
        if (form.getJobType() != null) {
            exist.setJobType(form.getJobType());
        }
        if (form.getStatus() != null) {
            exist.setStatus(form.getStatus());
        }
        return updateById(exist);
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        SysUser user = getById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        user.setStatus(status);
        return updateById(user);
    }
}
