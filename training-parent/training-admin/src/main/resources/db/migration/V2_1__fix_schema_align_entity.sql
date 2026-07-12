-- =============================================================
-- V2_1__fix_schema_align_entity.sql
-- 修复实体类与 schema 不匹配（2026-07-12 联调期）
--
-- 修复 3 个问题：
--   1) teacher.user_id     NOT NULL → 允许 NULL
--      （原因：新增教师前端表单不传 user_id,改为可独立维护讲师档案）
--   2) question.question_type NOT NULL → 允许 NULL + DEFAULT 1
--      （原因：增加容错,避免后续若遗漏前端字段导致 500）
--   3) course 表已有,difficulty 字段在 schema 中不存在,无需 DDL
--      （原因：实体类 Course.difficulty 已被删除,与 schema 对齐）
--
-- 执行方式: mysql -uroot -proot training < V2_1__fix_schema_align_entity.sql
-- =============================================================

-- 1) teacher.user_id 允许 NULL
ALTER TABLE teacher MODIFY COLUMN user_id BIGINT COMMENT '关联用户ID(可空,允许讲师独立维护档案)';

-- 2) question.question_type 允许 NULL + 默认 1 (单选)
ALTER TABLE question MODIFY COLUMN question_type TINYINT DEFAULT 1 COMMENT '类型:1 单选 2 多选 3 判断 4 填空 5 问答';
