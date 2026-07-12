package com.training.admin.controller;

import com.training.common.dto.LoginDTO;
import com.training.common.entity.SysUser;
import com.training.common.result.Result;
import com.training.common.utils.JwtUtils;
import com.training.common.vo.LoginVO;
import com.training.service.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 后台认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin")
public class AuthController {

    @Resource
    private SysUserService userService;

    @Resource
    private JwtUtils jwtUtils;

    /**
     * 后台登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO dto) {
        // 1. 查询用户
        SysUser user = userService.getByUsername(dto.getUsername());
        if (user == null) {
            return Result.error(1002, "用户名或密码错误");
        }

        // 2. 校验密码
        if (!userService.checkPassword(dto.getPassword(), user.getPassword())) {
            return Result.error(1002, "用户名或密码错误");
        }

        // 3. 校验状态
        if (user.getStatus() == null || user.getStatus() != 1) {
            return Result.error(403, "账号已被禁用");
        }

        // 4. 生成 JWT（含 roleId 向前兼容）
        String token = jwtUtils.generate(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.getRoleId()
        );

        // 5. 组装返回（过滤敏感字段）
        user.setPassword(null);
        LoginVO vo = new LoginVO();
        vo.setToken(token);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("realName", user.getRealName());
        userInfo.put("role", user.getRole());
        userInfo.put("roleCode", user.getRole()); // 前端角色 Badge 显示
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("orgName", user.getOrgName());
        vo.setUserInfo(userInfo);

        log.info("用户 {} 登录成功，角色：{}", user.getUsername(), user.getRole());
        return Result.success(vo);
    }
}
