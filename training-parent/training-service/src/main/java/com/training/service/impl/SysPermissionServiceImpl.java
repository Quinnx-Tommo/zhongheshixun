package com.training.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.training.common.dto.PermissionForm;
import com.training.common.dto.PermissionQuery;
import com.training.common.entity.SysPermission;
import com.training.mapper.SysPermissionMapper;
import com.training.service.SysPermissionService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 权限管理服务实现
 */
@Service
public class SysPermissionServiceImpl extends ServiceImpl<SysPermissionMapper, SysPermission> implements SysPermissionService {

    @Resource
    private SysPermissionMapper sysPermissionMapper;

    @Override
    public IPage<SysPermission> page(PermissionQuery query) {
        Page<SysPermission> page = new Page<>(query.getPageNum(), query.getPageSize());
        return sysPermissionMapper.selectPermissionPage(page, query.getModule(), query.getPermCode());
    }

    @Override
    public SysPermission getPermissionDetail(Long id) {
        return sysPermissionMapper.selectById(id);
    }

    @Override
    public java.util.List<SysPermission> listAll() {
        return sysPermissionMapper.selectAll();
    }

    @Override
    public boolean createPermission(PermissionForm form) {
        // 校验权限编码唯一
        SysPermission exist = sysPermissionMapper.selectByCode(form.getPermCode());
        if (exist != null) {
            throw new IllegalArgumentException("权限编码已存在：" + form.getPermCode());
        }
        SysPermission perm = new SysPermission();
        perm.setPermCode(form.getPermCode());
        perm.setPermName(form.getPermName());
        perm.setDescription(form.getDescription());
        perm.setModule(form.getModule());
        return save(perm);
    }

    @Override
    public boolean updatePermission(PermissionForm form) {
        if (form.getId() == null) {
            throw new IllegalArgumentException("权限ID不能为空");
        }
        SysPermission exist = getById(form.getId());
        if (exist == null) {
            throw new IllegalArgumentException("权限不存在");
        }
        // 权限编码唯一校验（排除自己）
        if (StringUtils.hasText(form.getPermCode()) && !form.getPermCode().equals(exist.getPermCode())) {
            SysPermission sameCode = sysPermissionMapper.selectByCode(form.getPermCode());
            if (sameCode != null && !sameCode.getId().equals(form.getId())) {
                throw new IllegalArgumentException("权限编码已存在：" + form.getPermCode());
            }
            exist.setPermCode(form.getPermCode());
        }
        if (form.getPermName() != null) {
            exist.setPermName(form.getPermName());
        }
        if (form.getDescription() != null) {
            exist.setDescription(form.getDescription());
        }
        if (form.getModule() != null) {
            exist.setModule(form.getModule());
        }
        return updateById(exist);
    }

    @Override
    public boolean deletePermission(Long id) {
        // 简化策略：仅校验存在性，逻辑删除由 @TableLogic 自动处理
        SysPermission perm = getById(id);
        if (perm == null) {
            throw new IllegalArgumentException("权限不存在");
        }
        return removeById(id);
    }
}
