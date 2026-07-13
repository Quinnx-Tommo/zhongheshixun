package com.training.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.training.common.dto.UserForm;
import com.training.common.dto.UserPageQuery;
import com.training.common.entity.SysUser;

/**
 用户服务接口
 */
public interface SysUserService extends IService<SysUser> {

    /**
     * 根据用户名查询用户（含 role/roleName 关联字段）
     */
    SysUser getByUsername(String username);

    /**
     * 根据主键查询用户详情（含 role/roleName 关联字段）。
     * 与 {@link #getById(Object)} 的区别：本方法走自定义 SQL，规避
     * MyBatis-Plus 自动生成 SQL 引用 {@code role} 列导致的 Unknown column 错误。
     *
     * @param id 用户ID
     * @return 用户实体（含 role/roleName），不存在返回 null
     */
    SysUser getUserDetail(Long id);

    /**
     * 校验密码
     * @param rawPassword 原始密码
     * @param encodedPassword 加密后的密码
     * @return 是否匹配
     */
    boolean checkPassword(String rawPassword, String encodedPassword);

    /**
     * 分页查询用户列表
     */
    IPage<SysUser> page(UserPageQuery query);

    /**
     * 创建用户（含 BCrypt 密码加密）
     */
    boolean createUser(UserForm form);

    /**
     * 编辑用户（密码为空则不修改）
     */
    boolean updateUser(UserForm form);

    /**
     * 启用/禁用用户
     */
    boolean updateStatus(Long id, Integer status);

    /**
     * 修改个人密码（需校验原密码）
     *
     * @param userId      当前登录用户ID
     * @param oldPassword 原密码
     * @param newPassword 新密码（明文，由 service 层 BCrypt 加密入库）
     * @return 是否修改成功
     */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 管理员重置他人密码（无需原密码）
     *
     * @param userId      目标用户ID
     * @param newPassword 新密码（明文，由 service 层 BCrypt 加密入库）
     * @return 是否重置成功
     */
    boolean resetPassword(Long userId, String newPassword);
}
