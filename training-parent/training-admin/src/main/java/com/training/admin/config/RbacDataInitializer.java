package com.training.admin.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.training.common.entity.SysPermission;
import com.training.common.entity.SysRole;
import com.training.common.entity.SysRolePermission;
import com.training.mapper.SysPermissionMapper;
import com.training.mapper.SysRoleMapper;
import com.training.mapper.SysRolePermissionMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * RBAC 数据初始化器
 * <p>应用启动时检查并初始化 3 个角色 + 16 个权限 + 角色-权限关联。
 * 通过 training.rbac.init 开关控制（默认开启）。</p>
 */
@Configuration
@ConditionalOnProperty(name = "training.rbac.init", havingValue = "true", matchIfMissing = true)
public class RbacDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(RbacDataInitializer.class);

    /**
     * 16 个预设权限字典
     * 顺序与 spec § 2.3 表格一致，id 1..16
     */
    private static final List<String[]> PERMISSION_SEED = Arrays.asList(
            new String[]{"course:read",      "课程查看",   "课程列表/详情查看",        "course"},
            new String[]{"course:write",     "课程编辑",   "课程新增/编辑/发布/删除", "course"},
            new String[]{"chapter:read",     "章节查看",   "章节列表查看",            "chapter"},
            new String[]{"chapter:write",    "章节编辑",   "章节新增/编辑/排序",      "chapter"},
            new String[]{"knowledge:read",   "知识点查看", "知识点列表查看",          "knowledge"},
            new String[]{"knowledge:write",  "知识点编辑", "知识点新增/编辑",         "knowledge"},
            new String[]{"question:read",    "试题查看",   "试题列表查看",            "question"},
            new String[]{"question:write",   "试题编辑",   "试题新增/编辑",           "question"},
            new String[]{"exam:read",        "考试查看",   "考试列表查看",            "exam"},
            new String[]{"exam:write",       "考试编辑",   "考试新增/编辑/组卷",      "exam"},
            new String[]{"consult:read",     "咨询查看",   "咨询工单列表查看",        "consult"},
            new String[]{"consult:write",    "咨询处理",   "咨询回复/知识库管理",     "consult"},
            new String[]{"stats:read",       "统计查看",   "统计报表查看",            "stats"},
            new String[]{"stats:write",      "统计配置",   "统计维度配置",            "stats"},
            new String[]{"user:read",        "用户查看",   "用户列表查看",            "user"},
            new String[]{"user:write",       "用户编辑",   "用户新增/编辑/启用/禁用", "user"}
    );

    /**
     * TEACHER 角色拥有的权限编码（共 13 个，见 spec § 2.4）
     */
    private static final List<String> TEACHER_PERM_CODES = Arrays.asList(
            "course:read", "course:write",
            "knowledge:read", "knowledge:write",
            "question:read", "question:write",
            "exam:read", "exam:write",
            "consult:read", "consult:write",
            "stats:read"
    );

    @Resource
    private SysRoleMapper sysRoleMapper;

    @Resource
    private SysPermissionMapper sysPermissionMapper;

    @Resource
    private SysRolePermissionMapper sysRolePermissionMapper;

    @PostConstruct
    @Transactional(rollbackFor = Exception.class)
    public void init() {
        // 1. 初始化角色字典（ADMIN=1, TEACHER=2, STUDENT=3）
        int roleCount = Math.toIntExact(sysRoleMapper.selectCount(null));
        if (roleCount == 0) {
            insertRole(1L, "ADMIN",   "系统管理员", "拥有系统全部权限");
            insertRole(2L, "TEACHER", "讲师",      "课程/试题/考试/咨询管理");
            insertRole(3L, "STUDENT", "学员",      "仅保留扩展，后台不使用");
            roleCount = 3;
            log.info("[RBAC] 初始化角色字典完成，共 {} 个", roleCount);
        } else {
            log.info("[RBAC] 角色字典已存在 {} 个，跳过初始化", roleCount);
        }

        // 2. 初始化权限字典（16 条）
        int permCount = Math.toIntExact(sysPermissionMapper.selectCount(null));
        if (permCount == 0) {
            for (int i = 0; i < PERMISSION_SEED.size(); i++) {
                String[] seed = PERMISSION_SEED.get(i);
                SysPermission p = new SysPermission();
                p.setId((long) (i + 1));
                p.setPermCode(seed[0]);
                p.setPermName(seed[1]);
                p.setDescription(seed[2]);
                p.setModule(seed[3]);
                p.setCreateTime(LocalDateTime.now());
                p.setDeleted(0);
                sysPermissionMapper.insert(p);
            }
            permCount = PERMISSION_SEED.size();
            log.info("[RBAC] 初始化权限字典完成，共 {} 条", permCount);
        } else {
            log.info("[RBAC] 权限字典已存在 {} 条，跳过初始化", permCount);
        }

        // 3. 初始化角色-权限关联
        int rpCount = Math.toIntExact(sysRolePermissionMapper.selectCount(null));
        if (rpCount == 0) {
            // ADMIN → 全部 16 个权限（id 1..16）
            SysRole adminRole = sysRoleMapper.selectById(1L);
            if (adminRole != null) {
                for (long pid = 1; pid <= PERMISSION_SEED.size(); pid++) {
                    insertRolePermission(adminRole.getId(), pid);
                }
            }

            // TEACHER → 选定的 10 个权限（按 permCode 反查 id）
            SysRole teacherRole = sysRoleMapper.selectById(2L);
            if (teacherRole != null) {
                for (String code : TEACHER_PERM_CODES) {
                    Long permId = resolvePermIdByCode(code);
                    if (permId != null) {
                        insertRolePermission(teacherRole.getId(), permId);
                    }
                }
            }

            // STUDENT → 仅 course:read（id=1）
            SysRole studentRole = sysRoleMapper.selectById(3L);
            if (studentRole != null) {
                insertRolePermission(studentRole.getId(), 1L);
            }

            rpCount = Math.toIntExact(sysRolePermissionMapper.selectCount(null));
            log.info("[RBAC] 初始化角色-权限关联完成，共 {} 条", rpCount);
        } else {
            log.info("[RBAC] 角色-权限关联已存在 {} 条，跳过初始化", rpCount);
        }

        log.info("RBAC 初始化完成：角色{}个，权限{}条，关联{}条", roleCount, permCount, rpCount);
    }

    private void insertRole(long id, String code, String name, String desc) {
        SysRole role = new SysRole();
        role.setId(id);
        role.setRoleCode(code);
        role.setRoleName(name);
        role.setDescription(desc);
        role.setStatus(1);
        role.setCreateTime(LocalDateTime.now());
        role.setDeleted(0);
        sysRoleMapper.insert(role);
    }

    private void insertRolePermission(Long roleId, Long permId) {
        SysRolePermission rp = new SysRolePermission();
        rp.setRoleId(roleId);
        rp.setPermissionId(permId);
        rp.setCreateTime(LocalDateTime.now());
        sysRolePermissionMapper.insert(rp);
    }

    /**
     * 按 permCode 反查权限 ID（通过遍历字典即可，因为 id 与 PERMISSION_SEED 下标一一对应）
     */
    private Long resolvePermIdByCode(String code) {
        for (int i = 0; i < PERMISSION_SEED.size(); i++) {
            if (PERMISSION_SEED.get(i)[0].equals(code)) {
                return (long) (i + 1);
            }
        }
        return null;
    }
}
