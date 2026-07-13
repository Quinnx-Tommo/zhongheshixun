package com.training.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.training.common.dto.ChangePasswordDTO;
import com.training.common.dto.ResetPasswordDTO;
import com.training.common.dto.UserForm;
import com.training.common.dto.UserPageQuery;
import com.training.common.entity.SysUser;
import com.training.common.result.Result;
import com.training.admin.security.LoginUser;
import com.training.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

/**
 * 用户管理控制器（后台管理端）
 * 注意：与 AuthController 分离，专注于用户 CRUD
 */
@Slf4j
@RestController
@RequestMapping("/admin/user")
public class UserManagementController {

    @Resource
    private SysUserService userService;

    /**
     * 分页列表（支持 role/keyword/status 筛选）
     */
    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/page")
    public Result<IPage<SysUser>> page(UserPageQuery query) {
        IPage<SysUser> result = userService.page(query);
        // 过滤敏感字段
        result.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(result);
    }

    /**
     * 创建用户（含 BCrypt 密码加密）
     */
    @PreAuthorize("hasAuthority('user:write')")
    @PostMapping
    public Result<Boolean> create(@RequestBody @Valid UserForm form) {
        boolean ok = userService.createUser(form);
        return Result.success(ok);
    }

    /**
     * 编辑用户
     */
    @PreAuthorize("hasAuthority('user:write')")
    @PutMapping
    public Result<Boolean> update(@RequestBody @Valid UserForm form) {
        boolean ok = userService.updateUser(form);
        return Result.success(ok);
    }

    /**
     * 启用/禁用用户
     */
    @PreAuthorize("hasAuthority('user:write')")
    @PutMapping("/{id}/status")
    public Result<Boolean> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null) {
            return Result.error(400, "status 不能为空");
        }
        boolean ok = userService.updateStatus(id, status);
        return Result.success(ok);
    }

    /**
     * 删除用户（逻辑删除）
     */
    @PreAuthorize("hasAuthority('user:write')")
    @DeleteMapping("/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        boolean ok = userService.removeById(id);
        return Result.success(ok);
    }

    /**
     * 用户详情
     */
    @PreAuthorize("hasAuthority('user:read')")
    @GetMapping("/{id}")
    public Result<SysUser> detail(@PathVariable Long id) {
        SysUser user = userService.getUserDetail(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        // 过滤敏感字段
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 修改个人密码（需校验原密码）
     * <p>登录用户自助修改密码，无需管理员权限；通过 token 解析当前用户ID。</p>
     */
    @PreAuthorize("hasAuthority('user:read')")
    @PutMapping("/profile/password")
    public Result<Boolean> changePassword(@AuthenticationPrincipal LoginUser loginUser,
                                          @RequestBody @Valid ChangePasswordDTO dto) {
        if (loginUser == null || loginUser.getId() == null) {
            return Result.error(401, "未登录或登录已失效");
        }
        boolean ok = userService.changePassword(loginUser.getId(), dto.getOldPassword(), dto.getNewPassword());
        return Result.success(ok);
    }

    /**
     * 管理员重置他人密码（无需原密码）
     * <p>管理员忘记用户密码时的兜底接口，需 user:write 权限。</p>
     */
    @PreAuthorize("hasAuthority('user:write')")
    @PutMapping("/{id}/reset-password")
    public Result<Boolean> resetPassword(@PathVariable Long id,
                                         @RequestBody @Valid ResetPasswordDTO dto) {
        boolean ok = userService.resetPassword(id, dto.getNewPassword());
        return Result.success(ok);
    }
}
