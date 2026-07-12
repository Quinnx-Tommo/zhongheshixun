/*
 Navicat Premium Dump SQL

 Source Server         : localhost
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : training

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 09/07/2026 16:09:01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for consult_record
-- ----------------------------
DROP TABLE IF EXISTS `consult_record`;
CREATE TABLE `consult_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL COMMENT '瀛﹀憳ID',
  `question` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '闂??',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '鍥炵瓟',
  `is_auto` tinyint NULL DEFAULT 1 COMMENT '绫诲瀷锛?鏅鸿兘鍥炵瓟 2浜哄伐鍥炵瓟',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `reply_time` datetime NULL DEFAULT NULL COMMENT '鍥炲?鏃堕棿锛堢敤浜?SLA 缁熻?锛',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-姝ｅ父锛?-宸插垹',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鍜ㄨ?璁板綍琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of consult_record
-- ----------------------------
INSERT INTO `consult_record` VALUES (1, 16, '考试没通过可以重考吗', '人工回复：请联系管理员', 2, '2026-07-08 10:45:35', '2026-07-08 10:52:17', 0);
INSERT INTO `consult_record` VALUES (2, 16, '考试没通过可以重考吗', '可以，每场考试最多允许重考3次，取最高分作为最终成绩。', 1, '2026-07-08 10:48:55', '2026-07-08 10:48:55', 0);
INSERT INTO `consult_record` VALUES (3, 16, '考试没通过可以重考吗', '这是人工回复：请拨打客服电话 400-xxx-xxxx', 2, '2026-07-08 10:50:04', '2026-07-08 10:50:05', 0);
INSERT INTO `consult_record` VALUES (4, 16, '如何报名课程', '在登录页点击注册按钮，填写用户名、密码、姓名、机构等信息即可注册。', 1, '2026-07-08 10:50:04', '2026-07-08 10:50:04', 0);
INSERT INTO `consult_record` VALUES (5, 16, '今天天气怎么样', NULL, 0, '2026-07-08 10:50:04', NULL, 0);
INSERT INTO `consult_record` VALUES (6, 16, '考试没通过可以重考吗', '可以，每场考试最多允许重考3次，取最高分作为最终成绩。', 1, '2026-07-08 10:52:16', '2026-07-08 10:52:17', 0);
INSERT INTO `consult_record` VALUES (7, 16, '如何报名课程', '在登录页点击注册按钮，填写用户名、密码、姓名、机构等信息即可注册。', 1, '2026-07-08 10:52:16', '2026-07-08 10:52:17', 0);
INSERT INTO `consult_record` VALUES (8, 16, '今天天气怎么样', NULL, 0, '2026-07-08 10:52:16', NULL, 0);
INSERT INTO `consult_record` VALUES (9, 16, 'xyzabc123', NULL, 0, '2026-07-08 10:52:42', NULL, 0);
INSERT INTO `consult_record` VALUES (10, 16, '考试没通过可以重考吗', '可以，每场考试最多允许重考3次，取最高分作为最终成绩。', 1, '2026-07-08 10:53:03', '2026-07-08 10:53:04', 0);
INSERT INTO `consult_record` VALUES (11, 35, 'exam retake policy', NULL, 0, '2026-07-08 10:56:19', NULL, 0);
INSERT INTO `consult_record` VALUES (12, 35, 'how to enroll course', NULL, 0, '2026-07-08 10:56:19', NULL, 0);
INSERT INTO `consult_record` VALUES (13, 35, 'what is the meaning of life', NULL, 0, '2026-07-08 10:56:19', NULL, 0);
INSERT INTO `consult_record` VALUES (14, 36, '考试不及格怎么办', '可以，每场考试最多允许重考3次，取最高分作为最终成绩。', 1, '2026-07-08 10:56:39', '2026-07-08 10:56:40', 0);
INSERT INTO `consult_record` VALUES (15, 36, '如何报名课程', '在登录页点击注册按钮，填写用户名、密码、姓名、机构等信息即可注册。', 1, '2026-07-08 10:56:39', '2026-07-08 10:56:40', 0);
INSERT INTO `consult_record` VALUES (16, 36, '不存在的xxx问题', NULL, 0, '2026-07-08 10:56:39', NULL, 0);
INSERT INTO `consult_record` VALUES (17, 9, 'test——question1', 'test answer', 1, '2026-07-08 11:44:03', '2026-07-08 11:44:04', 0);

-- ----------------------------
-- Table structure for course
-- ----------------------------
DROP TABLE IF EXISTS `course`;
CREATE TABLE `course`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '璇剧▼ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '璇剧▼鍚嶇О',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '璇剧▼鎻忚堪',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '灏侀潰鍥',
  `teacher_id` bigint NULL DEFAULT NULL COMMENT '璁插笀ID',
  `course_type` tinyint NULL DEFAULT 1 COMMENT '绫诲瀷锛?鍏?紑璇?2蹇呬慨璇',
  `total_hours` int NULL DEFAULT 0 COMMENT '鎬诲?鏃',
  `status` tinyint NULL DEFAULT 0 COMMENT '鐘舵?锛?鑽夌? 1宸插彂甯?2宸蹭笅鏋',
  `offline_flag` tinyint NULL DEFAULT 0 COMMENT '鏄?惁鏀?寔绂荤嚎瀛︿範锛?鍚?1鏄',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-姝ｅ父锛?-宸插垹',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_teacher_id`(`teacher_id` ASC) USING BTREE,
  INDEX `idx_course_type`(`course_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '璇剧▼琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course
-- ----------------------------
INSERT INTO `course` VALUES (1, '基层常见病诊疗规范', '针对基层医生的常见病诊疗规范培训', NULL, 1, 2, 40, 1, 0, '2026-07-08 08:29:47', '2026-07-08 08:29:47', 0);
INSERT INTO `course` VALUES (2, '公共卫生服务实务', '基本公共卫生服务实务培训', NULL, 2, 2, 30, 1, 0, '2026-07-08 08:29:47', '2026-07-08 08:29:47', 0);
INSERT INTO `course` VALUES (3, '急救技能培训', '基层急救技能培训', NULL, 1, 1, 20, 1, 0, '2026-07-08 08:29:47', '2026-07-08 08:29:47', 0);
INSERT INTO `course` VALUES (4, '护理基础操作', '护理基础操作规范', NULL, 2, 1, 25, 2, 0, '2026-07-08 08:29:47', '2026-07-08 08:29:47', 0);
INSERT INTO `course` VALUES (5, '测试新课程（已修改）', '修改后的描述', NULL, NULL, 2, 15, 1, 1, '2026-07-08 08:56:30', '2026-07-08 08:56:30', 1);

-- ----------------------------
-- Table structure for course_chapter
-- ----------------------------
DROP TABLE IF EXISTS `course_chapter`;
CREATE TABLE `course_chapter`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '绔犺妭ID',
  `course_id` bigint NOT NULL COMMENT '璇剧▼ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '绔犺妭鏍囬?',
  `sort_order` int NULL DEFAULT 0 COMMENT '鎺掑簭',
  `video_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '瑙嗛?鍦板潃',
  `duration` int NULL DEFAULT 0 COMMENT '鏃堕暱(绉?',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-姝ｅ父锛?-宸插垹',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '璇剧▼绔犺妭琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course_chapter
-- ----------------------------
INSERT INTO `course_chapter` VALUES (1, 1, '第一章 常见病概述', 1, '/video/ch1-1.mp4', 1800, '2026-07-08 08:29:47', 0);
INSERT INTO `course_chapter` VALUES (2, 1, '第二章 诊断要点', 2, '/video/ch1-2.mp4', 2400, '2026-07-08 08:29:47', 0);
INSERT INTO `course_chapter` VALUES (3, 1, '第三章 治疗方案', 3, '/video/ch1-3.mp4', 2000, '2026-07-08 08:29:47', 0);
INSERT INTO `course_chapter` VALUES (4, 2, '第一章 公共卫生服务概述', 1, '/video/ch2-1.mp4', 1500, '2026-07-08 08:29:47', 0);
INSERT INTO `course_chapter` VALUES (5, 2, '第二章 慢病管理', 2, '/video/ch2-2.mp4', 2100, '2026-07-08 08:29:47', 0);

-- ----------------------------
-- Table structure for course_enroll
-- ----------------------------
DROP TABLE IF EXISTS `course_enroll`;
CREATE TABLE `course_enroll`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL COMMENT '瀛﹀憳ID',
  `course_id` bigint NOT NULL COMMENT '璇剧▼ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-姝ｅ父锛?-宸插垹',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_student_course`(`student_id` ASC, `course_id` ASC) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '璇剧▼鎶ュ悕琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of course_enroll
-- ----------------------------
INSERT INTO `course_enroll` VALUES (1, 10, 1, '2026-07-08 09:22:16', 0);
INSERT INTO `course_enroll` VALUES (2, 10, 2, '2026-07-08 09:23:19', 0);
INSERT INTO `course_enroll` VALUES (3, 4, 1, '2026-07-08 15:25:13', 0);
INSERT INTO `course_enroll` VALUES (4, 4, 2, '2026-07-08 15:26:57', 0);
INSERT INTO `course_enroll` VALUES (5, 9, 1, '2026-07-09 10:12:44', 0);
INSERT INTO `course_enroll` VALUES (6, 9, 2, '2026-07-09 10:12:49', 0);
INSERT INTO `course_enroll` VALUES (7, 4, 3, '2026-07-09 11:38:39', 0);

-- ----------------------------
-- Table structure for exam
-- ----------------------------
DROP TABLE IF EXISTS `exam`;
CREATE TABLE `exam`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鑰冭瘯ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鑰冭瘯鍚嶇О',
  `exam_type` tinyint NOT NULL COMMENT '鑰冭瘯绫诲瀷锛?璇剧▼鑰冭瘯 2璁″垝鑰冭瘯 3鍗曠嫭鑰冭瘯',
  `course_id` bigint NULL DEFAULT NULL COMMENT '璇剧▼鑰冭瘯鍏宠仈璇剧▼ID',
  `plan_id` bigint NULL DEFAULT NULL COMMENT '璁″垝鑰冭瘯鍏宠仈璁″垝ID',
  `total_score` int NULL DEFAULT 100 COMMENT '鎬诲垎',
  `pass_score` int NULL DEFAULT 60 COMMENT '鍙婃牸鍒',
  `duration` int NULL DEFAULT 120 COMMENT '鑰冭瘯鏃堕暱(鍒嗛挓)',
  `max_retry` int NULL DEFAULT 1 COMMENT '鏈?ぇ閲嶈?娆℃暟',
  `question_count` int NULL DEFAULT 20 COMMENT '棰樼洰鏁伴噺',
  `status` tinyint NULL DEFAULT 0 COMMENT '鐘舵?锛?鑽夌? 1宸插彂甯',
  `start_time` datetime NULL DEFAULT NULL COMMENT '鑰冭瘯寮??鏃堕棿',
  `end_time` datetime NULL DEFAULT NULL COMMENT '鑰冭瘯缁撴潫鏃堕棿',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-姝ｅ父锛?-宸插垹',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_exam_type`(`exam_type` ASC) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_plan_id`(`plan_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鑰冭瘯琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of exam
-- ----------------------------
INSERT INTO `exam` VALUES (1, '基层常见病诊疗规范期末考试', 1, 1, NULL, 100, 60, 120, 3, 10, 1, '2026-01-01 00:00:00', '2027-12-31 23:59:59', '2026-07-08 08:30:10', 0);
INSERT INTO `exam` VALUES (2, '公共卫生服务实务期末考试', 1, 2, NULL, 100, 60, 120, 3, 8, 1, '2026-01-01 00:00:00', '2027-12-31 23:59:59', '2026-07-08 08:30:10', 0);
INSERT INTO `exam` VALUES (3, '2026年度结业考试', 1, 3, NULL, 100, 60, 150, 1, 10, 1, '2026-01-01 00:00:00', '2027-12-31 23:59:59', '2026-07-08 10:08:40', 0);
INSERT INTO `exam` VALUES (4, '11', 1, 4, NULL, 100, 60, 60, 1, 20, 0, NULL, NULL, '2026-07-09 14:39:18', 0);

-- ----------------------------
-- Table structure for exam_answer
-- ----------------------------
DROP TABLE IF EXISTS `exam_answer`;
CREATE TABLE `exam_answer`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `record_id` bigint NOT NULL COMMENT '鑰冭瘯璁板綍ID',
  `question_id` bigint NOT NULL COMMENT '璇曢?ID',
  `student_answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '瀛﹀憳绛旀?',
  `is_correct` tinyint NULL DEFAULT NULL COMMENT '鏄?惁姝ｇ‘锛?閿?1瀵',
  `score` int NULL DEFAULT 0 COMMENT '寰楀垎',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-姝ｅ父锛?-宸插垹',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_record_id`(`record_id` ASC) USING BTREE,
  INDEX `idx_question_id`(`question_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 111 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '绛旈?璁板綍琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of exam_answer
-- ----------------------------
INSERT INTO `exam_answer` VALUES (1, 9, 18, '6.1-7.0', 1, 3, '2026-07-08 10:31:01', 0);
INSERT INTO `exam_answer` VALUES (2, 9, 16, 'D', 1, 3, '2026-07-08 10:31:01', 0);
INSERT INTO `exam_answer` VALUES (3, 9, 22, '控制血糖,定期检查足部', 1, 3, '2026-07-08 10:31:01', 0);
INSERT INTO `exam_answer` VALUES (4, 9, 10, 'B', 1, 2, '2026-07-08 10:31:01', 0);
INSERT INTO `exam_answer` VALUES (5, 9, 23, 'D', 1, 3, '2026-07-08 10:31:01', 0);
INSERT INTO `exam_answer` VALUES (6, 9, 12, '140/90', 1, 3, '2026-07-08 10:31:01', 0);
INSERT INTO `exam_answer` VALUES (7, 9, 15, 'C', 1, 2, '2026-07-08 10:31:01', 0);
INSERT INTO `exam_answer` VALUES (8, 9, 24, 'B', 1, 2, '2026-07-08 10:31:01', 0);
INSERT INTO `exam_answer` VALUES (9, 9, 11, 'T', 1, 2, '2026-07-08 10:31:01', 0);
INSERT INTO `exam_answer` VALUES (10, 9, 2, 'C', 1, 5, '2026-07-08 10:31:01', 0);
INSERT INTO `exam_answer` VALUES (11, 11, 8, '3', 1, 3, '2026-07-08 10:32:45', 0);
INSERT INTO `exam_answer` VALUES (12, 11, 2, 'C', 1, 5, '2026-07-08 10:32:45', 0);
INSERT INTO `exam_answer` VALUES (13, 11, 17, 'F', 1, 2, '2026-07-08 10:32:45', 0);
INSERT INTO `exam_answer` VALUES (14, 11, 5, 'B', 1, 2, '2026-07-08 10:32:45', 0);
INSERT INTO `exam_answer` VALUES (15, 11, 16, 'D', 1, 3, '2026-07-08 10:32:45', 0);
INSERT INTO `exam_answer` VALUES (16, 11, 10, 'B', 1, 2, '2026-07-08 10:32:45', 0);
INSERT INTO `exam_answer` VALUES (17, 11, 22, '控制血糖', 0, 0, '2026-07-08 10:32:45', 0);
INSERT INTO `exam_answer` VALUES (18, 11, 19, 'D', 1, 3, '2026-07-08 10:32:45', 0);
INSERT INTO `exam_answer` VALUES (19, 11, 20, 'C', 1, 2, '2026-07-08 10:32:45', 0);
INSERT INTO `exam_answer` VALUES (20, 11, 14, 'A', 1, 2, '2026-07-08 10:32:45', 0);
INSERT INTO `exam_answer` VALUES (21, 13, 17, 'F', 1, 2, '2026-07-08 10:33:29', 0);
INSERT INTO `exam_answer` VALUES (22, 13, 16, 'D', 1, 3, '2026-07-08 10:33:29', 0);
INSERT INTO `exam_answer` VALUES (23, 13, 15, 'A', 0, 0, '2026-07-08 10:33:29', 0);
INSERT INTO `exam_answer` VALUES (24, 13, 10, 'B', 1, 2, '2026-07-08 10:33:29', 0);
INSERT INTO `exam_answer` VALUES (25, 13, 7, 'A', 0, 0, '2026-07-08 10:33:29', 0);
INSERT INTO `exam_answer` VALUES (26, 13, 23, 'A', 0, 0, '2026-07-08 10:33:29', 0);
INSERT INTO `exam_answer` VALUES (27, 13, 19, 'D', 1, 3, '2026-07-08 10:33:29', 0);
INSERT INTO `exam_answer` VALUES (28, 13, 22, 'A', 0, 0, '2026-07-08 10:33:29', 0);
INSERT INTO `exam_answer` VALUES (29, 13, 20, 'A', 0, 0, '2026-07-08 10:33:29', 0);
INSERT INTO `exam_answer` VALUES (30, 13, 13, 'A', 0, 0, '2026-07-08 10:33:29', 0);
INSERT INTO `exam_answer` VALUES (31, 14, 8, '3', 1, 3, '2026-07-08 10:34:05', 0);
INSERT INTO `exam_answer` VALUES (32, 14, 2, 'C', 1, 5, '2026-07-08 10:34:05', 0);
INSERT INTO `exam_answer` VALUES (33, 14, 17, 'F', 1, 2, '2026-07-08 10:34:05', 0);
INSERT INTO `exam_answer` VALUES (34, 14, 5, 'B', 1, 2, '2026-07-08 10:34:05', 0);
INSERT INTO `exam_answer` VALUES (35, 14, 16, 'D', 1, 3, '2026-07-08 10:34:05', 0);
INSERT INTO `exam_answer` VALUES (36, 14, 10, 'B', 1, 2, '2026-07-08 10:34:05', 0);
INSERT INTO `exam_answer` VALUES (37, 14, 22, '控制血糖', 1, 3, '2026-07-08 10:34:05', 0);
INSERT INTO `exam_answer` VALUES (38, 14, 19, 'D', 1, 3, '2026-07-08 10:34:05', 0);
INSERT INTO `exam_answer` VALUES (39, 14, 20, 'C', 1, 2, '2026-07-08 10:34:05', 0);
INSERT INTO `exam_answer` VALUES (40, 14, 14, 'A', 1, 2, '2026-07-08 10:34:05', 0);
INSERT INTO `exam_answer` VALUES (41, 15, 9, 'A', 0, 0, '2026-07-08 10:34:05', 0);
INSERT INTO `exam_answer` VALUES (42, 15, 1, 'A', 0, 0, '2026-07-08 10:34:05', 0);
INSERT INTO `exam_answer` VALUES (43, 15, 17, 'A', 0, 0, '2026-07-08 10:34:05', 0);
INSERT INTO `exam_answer` VALUES (44, 15, 16, 'A', 0, 0, '2026-07-08 10:34:05', 0);
INSERT INTO `exam_answer` VALUES (45, 15, 2, 'A', 0, 0, '2026-07-08 10:34:05', 0);
INSERT INTO `exam_answer` VALUES (46, 15, 6, 'A', 0, 0, '2026-07-08 10:34:05', 0);
INSERT INTO `exam_answer` VALUES (47, 15, 5, 'A', 0, 0, '2026-07-08 10:34:05', 0);
INSERT INTO `exam_answer` VALUES (48, 15, 10, 'A', 0, 0, '2026-07-08 10:34:05', 0);
INSERT INTO `exam_answer` VALUES (49, 15, 14, 'A', 1, 2, '2026-07-08 10:34:05', 0);
INSERT INTO `exam_answer` VALUES (50, 15, 19, 'A', 0, 0, '2026-07-08 10:34:05', 0);
INSERT INTO `exam_answer` VALUES (51, 16, 9, NULL, 0, 0, '2026-07-08 10:34:34', 0);
INSERT INTO `exam_answer` VALUES (52, 16, 20, NULL, 0, 0, '2026-07-08 10:34:34', 0);
INSERT INTO `exam_answer` VALUES (53, 16, 16, NULL, 0, 0, '2026-07-08 10:34:34', 0);
INSERT INTO `exam_answer` VALUES (54, 16, 24, NULL, 0, 0, '2026-07-08 10:34:34', 0);
INSERT INTO `exam_answer` VALUES (55, 16, 10, NULL, 0, 0, '2026-07-08 10:34:34', 0);
INSERT INTO `exam_answer` VALUES (56, 16, 14, NULL, 0, 0, '2026-07-08 10:34:34', 0);
INSERT INTO `exam_answer` VALUES (57, 16, 2, 'A', 0, 0, '2026-07-08 10:34:34', 0);
INSERT INTO `exam_answer` VALUES (58, 16, 21, NULL, 0, 0, '2026-07-08 10:34:34', 0);
INSERT INTO `exam_answer` VALUES (59, 16, 18, NULL, 0, 0, '2026-07-08 10:34:34', 0);
INSERT INTO `exam_answer` VALUES (60, 16, 22, NULL, 0, 0, '2026-07-08 10:34:34', 0);
INSERT INTO `exam_answer` VALUES (101, 34, 39, 'B', 1, 2, '2026-07-09 15:17:09', 0);
INSERT INTO `exam_answer` VALUES (102, 34, 15, 'B', 1, 3, '2026-07-09 15:17:09', 0);
INSERT INTO `exam_answer` VALUES (103, 34, 10, 'A', 0, 0, '2026-07-09 15:17:09', 0);
INSERT INTO `exam_answer` VALUES (104, 34, 21, 'C', 1, 2, '2026-07-09 15:17:09', 0);
INSERT INTO `exam_answer` VALUES (105, 34, 30, 'D', 0, 0, '2026-07-09 15:17:09', 0);
INSERT INTO `exam_answer` VALUES (106, 34, 25, 'D', 0, 0, '2026-07-09 15:17:09', 0);
INSERT INTO `exam_answer` VALUES (107, 34, 12, 'D', 0, 0, '2026-07-09 15:17:09', 0);
INSERT INTO `exam_answer` VALUES (108, 34, 2, 'D', 1, 3, '2026-07-09 15:17:09', 0);
INSERT INTO `exam_answer` VALUES (109, 34, 38, 'A', 0, 0, '2026-07-09 15:17:09', 0);
INSERT INTO `exam_answer` VALUES (110, 34, 37, 'B', 1, 3, '2026-07-09 15:17:09', 0);

-- ----------------------------
-- Table structure for exam_paper
-- ----------------------------
DROP TABLE IF EXISTS `exam_paper`;
CREATE TABLE `exam_paper`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '璇曞嵎ID',
  `exam_id` bigint NOT NULL COMMENT '鑰冭瘯ID',
  `student_id` bigint NOT NULL COMMENT '瀛﹀憳ID',
  `questions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '棰樼洰鍒楄〃锛圝SON鏍煎紡锛歲uestion_id鏁扮粍锛',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-姝ｅ父锛?-宸插垹',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_exam_student`(`exam_id` ASC, `student_id` ASC) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '璇曞嵎琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of exam_paper
-- ----------------------------
INSERT INTO `exam_paper` VALUES (1, 1, 16, '[6,12,13,1,8,15,22,5,16,21]', '2026-07-08 10:27:49', 0);
INSERT INTO `exam_paper` VALUES (2, 1, 17, '[14,2,9,23,10,13,19,16,17,5]', '2026-07-08 10:28:01', 0);
INSERT INTO `exam_paper` VALUES (3, 1, 19, '[14,22,7,20,16,23,6,24,12,1]', '2026-07-08 10:28:31', 0);
INSERT INTO `exam_paper` VALUES (4, 1, 20, '[2,6,15,20,23,11,19,9,13,21]', '2026-07-08 10:28:42', 0);
INSERT INTO `exam_paper` VALUES (5, 1, 21, '[14,16,9,22,23,13,17,15,6,10]', '2026-07-08 10:29:09', 0);
INSERT INTO `exam_paper` VALUES (6, 1, 22, '[8,17,24,22,23,15,9,13,1,6]', '2026-07-08 10:29:28', 0);
INSERT INTO `exam_paper` VALUES (7, 1, 23, '[24,23,19,5,16,21,22,8,13,2]', '2026-07-08 10:29:46', 0);
INSERT INTO `exam_paper` VALUES (8, 1, 25, '[24,18,17,23,8,5,12,1,2,9]', '2026-07-08 10:30:21', 0);
INSERT INTO `exam_paper` VALUES (9, 1, 26, '[18,16,22,10,23,12,15,24,11,2]', '2026-07-08 10:31:01', 0);
INSERT INTO `exam_paper` VALUES (10, 1, 27, '[18,10,11,1,17,22,12,2,16,14]', '2026-07-08 10:32:26', 0);
INSERT INTO `exam_paper` VALUES (11, 1, 28, '[8,2,17,5,16,10,22,19,20,14]', '2026-07-08 10:32:45', 0);
INSERT INTO `exam_paper` VALUES (12, 1, 29, '[19,6,11,17,8,10,16,9,14,2]', '2026-07-08 10:32:59', 0);
INSERT INTO `exam_paper` VALUES (13, 1, 31, '[17,16,15,10,7,23,19,22,20,13]', '2026-07-08 10:33:29', 0);
INSERT INTO `exam_paper` VALUES (14, 1, 32, '[9,1,17,16,2,6,5,10,14,19]', '2026-07-08 10:34:05', 0);
INSERT INTO `exam_paper` VALUES (15, 1, 33, '[9,20,16,24,10,14,2,21,18,22]', '2026-07-08 10:34:34', 0);
INSERT INTO `exam_paper` VALUES (17, 4, 0, '[38,75,11,20,83,87,80,71,8,23,6,32,59,51,17,49,66,63,81,21]', '2026-07-09 14:48:42', 0);
INSERT INTO `exam_paper` VALUES (18, 3, 0, '[81,63,88,15,84,10,83,62,68,87]', '2026-07-09 14:51:22', 0);
INSERT INTO `exam_paper` VALUES (24, 1, 4, '[39,15,10,21,30,25,12,2,38,37]', '2026-07-09 15:16:56', 0);

-- ----------------------------
-- Table structure for exam_record
-- ----------------------------
DROP TABLE IF EXISTS `exam_record`;
CREATE TABLE `exam_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL COMMENT '瀛﹀憳ID',
  `exam_id` bigint NOT NULL COMMENT '鑰冭瘯ID',
  `paper_id` bigint NULL DEFAULT NULL COMMENT '璇曞嵎ID',
  `score` int NULL DEFAULT 0 COMMENT '寰楀垎',
  `status` tinyint NULL DEFAULT 0 COMMENT '鐘舵?锛?杩涜?涓?1宸叉彁浜?2宸叉壒闃',
  `start_time` datetime NULL DEFAULT NULL COMMENT '寮??鏃堕棿',
  `submit_time` datetime NULL DEFAULT NULL COMMENT '鎻愪氦鏃堕棿',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-姝ｅ父锛?-宸插垹',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_exam_id`(`exam_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鑰冭瘯璁板綍琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of exam_record
-- ----------------------------
INSERT INTO `exam_record` VALUES (1, 16, 1, 1, 0, 0, '2026-07-08 10:27:49', NULL, '2026-07-08 10:27:49', 0);
INSERT INTO `exam_record` VALUES (2, 17, 1, 2, 0, 0, '2026-07-08 10:28:01', NULL, '2026-07-08 10:28:01', 0);
INSERT INTO `exam_record` VALUES (3, 19, 1, 3, 0, 0, '2026-07-08 10:28:31', NULL, '2026-07-08 10:28:31', 0);
INSERT INTO `exam_record` VALUES (4, 20, 1, 4, 0, 0, '2026-07-08 10:28:43', NULL, '2026-07-08 10:28:42', 0);
INSERT INTO `exam_record` VALUES (5, 21, 1, 5, 0, 0, '2026-07-08 10:29:09', NULL, '2026-07-08 10:29:09', 0);
INSERT INTO `exam_record` VALUES (6, 22, 1, 6, 0, 0, '2026-07-08 10:29:28', NULL, '2026-07-08 10:29:28', 0);
INSERT INTO `exam_record` VALUES (7, 23, 1, 7, 0, 0, '2026-07-08 10:29:47', NULL, '2026-07-08 10:29:46', 0);
INSERT INTO `exam_record` VALUES (8, 25, 1, 8, 0, 0, '2026-07-08 10:30:21', NULL, '2026-07-08 10:30:21', 0);
INSERT INTO `exam_record` VALUES (9, 26, 1, 9, 28, 2, '2026-07-08 10:31:01', '2026-07-08 10:31:01', '2026-07-08 10:31:01', 0);
INSERT INTO `exam_record` VALUES (10, 27, 1, 10, 0, 0, '2026-07-08 10:32:26', NULL, '2026-07-08 10:32:26', 0);
INSERT INTO `exam_record` VALUES (11, 28, 1, 11, 24, 2, '2026-07-08 10:32:45', '2026-07-08 10:32:45', '2026-07-08 10:32:45', 0);
INSERT INTO `exam_record` VALUES (12, 29, 1, 12, 0, 0, '2026-07-08 10:32:59', NULL, '2026-07-08 10:32:59', 0);
INSERT INTO `exam_record` VALUES (13, 31, 1, 13, 10, 2, '2026-07-08 10:33:29', '2026-07-08 10:33:29', '2026-07-08 10:33:29', 0);
INSERT INTO `exam_record` VALUES (14, 28, 1, 11, 27, 2, '2026-07-08 10:34:06', '2026-07-08 10:34:06', '2026-07-08 10:34:05', 0);
INSERT INTO `exam_record` VALUES (15, 32, 1, 14, 2, 2, '2026-07-08 10:34:06', '2026-07-08 10:34:06', '2026-07-08 10:34:05', 0);
INSERT INTO `exam_record` VALUES (16, 33, 1, 15, 0, 2, '2026-07-08 10:34:35', '2026-07-08 10:34:35', '2026-07-08 10:34:34', 0);
INSERT INTO `exam_record` VALUES (34, 4, 1, 24, 13, 2, '2026-07-09 15:16:57', '2026-07-09 15:17:10', '2026-07-09 15:16:56', 0);

-- ----------------------------
-- Table structure for knowledge_base
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_base`;
CREATE TABLE `knowledge_base`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `question` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '闂??',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '绛旀?',
  `keywords` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鍏抽敭璇嶏紙閫楀彿鍒嗛殧锛岀敤浜庡尮閰嶏級',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鍒嗙被',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-姝ｅ父锛?-宸插垹',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐭ヨ瘑搴撹〃' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of knowledge_base
-- ----------------------------
INSERT INTO `knowledge_base` VALUES (1, '如何注册账号？', '在登录页点击注册按钮，填写用户名、密码、姓名、机构等信息即可注册。', '注册,账号,如何注册', NULL, '2026-07-08 08:30:10', '2026-07-08 08:30:10', 0);
INSERT INTO `knowledge_base` VALUES (2, '如何报名课程？', '登录后在课程列表页找到目标课程，点击\"报名\"按钮即可报名。', '报名,课程,如何报名', NULL, '2026-07-08 08:30:10', '2026-07-08 08:30:10', 0);
INSERT INTO `knowledge_base` VALUES (3, '考试可以重考吗？', '可以，每场考试最多允许重考3次，取最高分作为最终成绩。', '考试,重考,次数', NULL, '2026-07-08 08:30:10', '2026-07-08 08:30:10', 0);
INSERT INTO `knowledge_base` VALUES (4, '学习进度如何查看？', '登录后在\'我的学习\'页面可以查看每门课程的学习进度和完成率。', '学习进度,查看,进度', NULL, '2026-07-08 08:30:10', '2026-07-08 08:30:10', 0);
INSERT INTO `knowledge_base` VALUES (5, 'test question', 'test answer', 'test', 'test', '2026-07-08 10:52:16', '2026-07-08 10:52:16', 0);

-- ----------------------------
-- Table structure for knowledge_point
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_point`;
CREATE TABLE `knowledge_point`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NOT NULL COMMENT '璇剧▼ID',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鐭ヨ瘑鐐瑰悕绉',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '鐭ヨ瘑鐐规弿杩',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-姝ｅ父锛?-宸插垹',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鐭ヨ瘑鐐硅〃' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of knowledge_point
-- ----------------------------
INSERT INTO `knowledge_point` VALUES (1, 1, '高血压的诊断标准', '高血压的诊断标准和分级', '2026-07-09 14:13:22', 0);
INSERT INTO `knowledge_point` VALUES (2, 1, '糖尿病的治疗原则', '糖尿病的治疗原则和用药规范', '2026-07-09 14:13:22', 0);
INSERT INTO `knowledge_point` VALUES (3, 2, '健康档案管理', '居民健康档案的建立和管理', '2026-07-09 14:13:22', 0);
INSERT INTO `knowledge_point` VALUES (4, 3, '基层卫生服务规范', '基层医疗卫生机构服务规范与流程', '2026-07-09 14:13:22', 0);
INSERT INTO `knowledge_point` VALUES (5, 3, '急救基本知识', '常见急症的识别与初步处理原则', '2026-07-09 14:13:22', 0);

-- ----------------------------
-- Table structure for plan_course
-- ----------------------------
DROP TABLE IF EXISTS `plan_course`;
CREATE TABLE `plan_course`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `plan_id` bigint NOT NULL COMMENT '璁″垝ID',
  `course_id` bigint NOT NULL COMMENT '璇剧▼ID',
  `sort_order` int NULL DEFAULT 0 COMMENT '瀛︿範椤哄簭',
  `is_required` tinyint NULL DEFAULT 1 COMMENT '鏄?惁蹇呬慨锛?鍚?1鏄',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-姝ｅ父锛?-宸插垹',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_plan_id`(`plan_id` ASC) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '璁″垝鍏宠仈璇剧▼琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of plan_course
-- ----------------------------
INSERT INTO `plan_course` VALUES (1, 1, 1, 1, 1, '2026-07-08 08:30:10', 0);
INSERT INTO `plan_course` VALUES (2, 1, 2, 2, 1, '2026-07-08 08:30:10', 0);

-- ----------------------------
-- Table structure for question
-- ----------------------------
DROP TABLE IF EXISTS `question`;
CREATE TABLE `question`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '璇曢?ID',
  `course_id` bigint NULL DEFAULT NULL COMMENT '鍏宠仈璇剧▼',
  `knowledge_point_id` bigint NULL DEFAULT NULL COMMENT '鍏宠仈鐭ヨ瘑鐐',
  `title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '棰樼洰',
  `question_type` tinyint NOT NULL COMMENT '绫诲瀷锛?鍗曢? 2澶氶? 3鍒ゆ柇 4濉?┖ 5闂?瓟',
  `options` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '閫夐」(JSON)',
  `answer` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姝ｇ‘绛旀?',
  `score` int NULL DEFAULT 1 COMMENT '鍒嗗?',
  `difficulty` tinyint NULL DEFAULT 2 COMMENT '闅惧害锛?绠?崟 2鏅?? 3鍥伴毦',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-姝ｅ父锛?-宸插垹',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE,
  INDEX `idx_knowledge_point_id`(`knowledge_point_id` ASC) USING BTREE,
  INDEX `idx_difficulty`(`difficulty` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 89 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '璇曢?琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of question
-- ----------------------------
INSERT INTO `question` VALUES (1, 1, 1, '高血压的诊断标准是收缩压≥（）mmHg', 1, '[\"A. 130\",\"B. 140\",\"C. 150\",\"D. 160\"]', 'B', 2, 1, '2026-07-09 14:13:22', 0);
INSERT INTO `question` VALUES (2, 1, 1, '以下哪些属于高血压的危险因素', 2, '[\"A. 高盐饮食\",\"B. 肥胖\",\"C. 遗传因素\",\"D. 以上都是\"]', 'D', 3, 2, '2026-07-09 14:13:22', 0);
INSERT INTO `question` VALUES (3, 1, 2, '糖尿病患者应首选饮食治疗', 3, '[\"正确\",\"错误\"]', '正确', 2, 1, '2026-07-09 14:13:22', 0);
INSERT INTO `question` VALUES (4, 2, 3, '健康档案的建立原则是（）', 4, '', '自愿与规范相结合', 3, 2, '2026-07-09 14:13:22', 0);
INSERT INTO `question` VALUES (5, 3, 4, '基層卫生服务机构的首诊负责制是指（）', 1, '[\"A. 谁首诊谁负责到底\",\"B. 仅负责初步诊断\",\"C. 不负责转诊\",\"D. 以上都不对\"]', 'A', 2, 1, '2026-07-09 14:13:22', 0);
INSERT INTO `question` VALUES (6, 3, 4, '居民健康档案的规范服务内容包含哪些', 2, '[\"A. 健康评估\",\"B. 健康教育\",\"C. 慢病管理\",\"D. 以上全部\"]', 'D', 3, 2, '2026-07-09 14:13:22', 0);
INSERT INTO `question` VALUES (7, 3, 5, '心脏骤停的黄金抢救时间是 4 分钟内', 3, '[\"正确\",\"错误\"]', '正确', 2, 1, '2026-07-09 14:13:22', 0);
INSERT INTO `question` VALUES (8, 3, 5, '成人心肺复苏的按压频率是（）次/分', 4, '', '100-120', 3, 2, '2026-07-09 14:13:22', 0);
INSERT INTO `question` VALUES (9, 1, 1, '高血压的诊断标准是收缩压≥（）mmHg', 1, '[\"A.120\",\"B.130\",\"C.140\",\"D.150\"]', 'C', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (10, 1, 1, '高血压的诊断标准是舒张压≥（）mmHg', 1, '[\"A.80\",\"B.90\",\"C.100\",\"D.110\"]', 'B', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (11, 1, 1, '以下哪项是高血压的危險因素', 2, '[\"A.高盐饮食\",\"B.肥胖\",\"C.遗传\",\"D.以上均是\"]', 'D', 3, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (12, 1, 1, '高血压患者应限制每日钠盐摄入量不超过（）g', 1, '[\"A.3\",\"B.5\",\"C.8\",\"D.10\"]', 'B', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (13, 1, 1, '高血压的一级预防主要针对（）', 1, '[\"A.普通人群\",\"B.高危人群\",\"C.患者\",\"D.并发症\"]', 'B', 2, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (14, 1, 1, '测量血压前应至少休息（）分钟', 1, '[\"A.1\",\"B.3\",\"C.5\",\"D.10\"]', 'C', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (15, 1, 1, '高血压患者合并糖尿病时血压控制目标应低于（）mmHg', 1, '[\"A.140/90\",\"B.130/80\",\"C.150/95\",\"D.120/70\"]', 'B', 3, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (16, 1, 1, '老年人高血压的特点是（）', 2, '[\"A.收缩压高\",\"B.脉压大\",\"C.易波动\",\"D.以上均是\"]', 'D', 3, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (17, 1, 1, '高血压患者应避免以下哪种运动', 1, '[\"A.慢跑\",\"B.游泳\",\"C.高强度举重\",\"D.太极拳\"]', 'C', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (18, 1, 1, '以下哪种药物属于钙通道阻滞剂', 1, '[\"A.氨氯地平\",\"B.美托洛尔\",\"C.氢氯噻嗪\",\"D.卡托普利\"]', 'A', 3, 3, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (19, 1, 1, '高血压急症的血压通常超过（）mmHg', 1, '[\"A.180/120\",\"B.160/100\",\"C.200/140\",\"D.140/90\"]', 'A', 3, 3, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (20, 1, 1, '以下哪些属于高血压靶器官损害', 2, '[\"A.左室肥厚\",\"B.微量白蛋白尿\",\"C.颈动脉斑块\",\"D.以上均是\"]', 'D', 3, 3, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (21, 1, 1, '高血压的非药物治疗不包括（）', 1, '[\"A.限盐\",\"B.戒烟\",\"C.高蛋白饮食\",\"D.减重\"]', 'C', 2, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (22, 1, 1, '24小时动态血压监测诊断高血压的标准是', 1, '[\"A.≥140/90\",\"B.≥130/80\",\"C.≥135/85\",\"D.≥120/80\"]', 'C', 3, 3, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (23, 1, 1, '恶性高血压的特征性眼底改变是', 1, '[\"A.动脉变细\",\"B.动静脉交叉\",\"C.视乳头水肿\",\"D.微动脉瘤\"]', 'C', 3, 3, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (24, 1, 1, '难治性高血压的定义是使用几种药物血压仍不达标', 1, '[\"A.2\",\"B.3\",\"C.4\",\"D.5\"]', 'B', 3, 3, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (25, 1, 2, '糖尿病的诊断标准空腹血糖≥（）mmol/L', 1, '[\"A.5.6\",\"B.6.1\",\"C.7.0\",\"D.11.1\"]', 'C', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (26, 1, 2, '糖尿病的诊断标准餐后2小时血糖≥（）mmol/L', 1, '[\"A.7.8\",\"B.11.1\",\"C.13.0\",\"D.6.5\"]', 'B', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (27, 1, 2, '以下哪些是糖尿病的典型症状', 2, '[\"A.多饮\",\"B.多尿\",\"C.体重下降\",\"D.以上均是\"]', 'D', 3, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (28, 1, 2, '2型糖尿病的一线口服药是', 1, '[\"A.二甲双胍\",\"B.格列美脲\",\"C.阿卡波糖\",\"D.胰岛素\"]', 'A', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (29, 1, 2, '糖尿病患者糖化血红蛋白（HbA1c）控制目标应低于（）%', 1, '[\"A.6.0\",\"B.7.0\",\"C.8.0\",\"D.9.0\"]', 'B', 2, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (30, 1, 2, '糖尿病饮食治疗中碳水化合物占总热量的比例应为（）', 1, '[\"A.30-40%\",\"B.40-50%\",\"C.50-60%\",\"D.70-80%\"]', 'C', 2, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (31, 1, 2, '糖尿病肾病的早期标志是', 1, '[\"A.血肌酐升高\",\"B.微量白蛋白尿\",\"C.大量蛋白尿\",\"D.高血压\"]', 'B', 3, 3, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (32, 1, 2, '以下哪种情况应启动胰岛素治疗', 2, '[\"A.新诊断2型糖尿病HbA1c>9%\",\"B.口服药无效\",\"C.急性并发症\",\"D.以上均是\"]', 'D', 3, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (33, 1, 2, '糖尿病患者低血糖诊断标准是血糖≤（）mmol/L', 1, '[\"A.2.8\",\"B.3.9\",\"C.4.4\",\"D.5.6\"]', 'B', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (34, 1, 2, '以下哪项是糖尿病微血管并发症', 2, '[\"A.冠心病\",\"B.糖尿病视网膜病变\",\"C.脑梗死\",\"D.外周动脉疾病\"]', 'B', 3, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (35, 1, 2, '双胍类药物的主要不良反应是', 1, '[\"A.低血糖\",\"B.乳酸酸中毒\",\"C.体重下降\",\"D.胃肠道反应\"]', 'D', 3, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (36, 1, 2, '糖尿病足筛查应至少（）进行一次', 1, '[\"A.每月\",\"B.每季度\",\"C.每年\",\"D.每两年\"]', 'C', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (37, 1, 2, '糖尿病患者血压控制目标应低于（）mmHg', 1, '[\"A.140/90\",\"B.130/80\",\"C.150/95\",\"D.120/70\"]', 'B', 3, 3, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (38, 1, 2, '以下哪项是糖耐量减低（IGT）的诊断标准', 1, '[\"A.空腹≥7.0\",\"B.餐后2小时7.8-11.0\",\"C.餐后2小时>11.1\",\"D.空腹6.1-6.9\"]', 'B', 3, 3, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (39, 1, 2, '1型糖尿病的主要病理机制是', 1, '[\"A.胰岛素抵抗\",\"B.胰岛β细胞破坏\",\"C.肝脏糖异生增加\",\"D.脂肪代谢异常\"]', 'B', 2, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (40, 1, 2, '糖尿病随访中必查的项目不包括', 1, '[\"A.血糖\",\"B.糖化血红蛋白\",\"C.眼底\",\"D.骨密度\"]', 'D', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (41, 2, 3, '居民健康档案的编码位数是', 1, '[\"A.15\",\"B.16\",\"C.17\",\"D.18\"]', 'C', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (42, 2, 3, '健康档案的建立原则是', 1, '[\"A.自愿与规范相结合\",\"B.强制建立\",\"C.仅重点人群\",\"D.仅慢性患者\"]', 'A', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (43, 2, 3, '以下哪些人群应优先建立健康档案', 2, '[\"A.老年人\",\"B.慢性病患者\",\"C.孕产妇\",\"D.以上均是\"]', 'D', 3, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (44, 2, 3, '健康档案的个人基本信息不包括', 1, '[\"A.姓名\",\"B.身份证号\",\"C.血型\",\"D.宗教信仰\"]', 'D', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (45, 2, 3, '高血压患者健康管理每年至少随访（）次', 1, '[\"A.2\",\"B.4\",\"C.6\",\"D.12\"]', 'B', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (46, 2, 3, '糖尿病患者的随访方式包括', 2, '[\"A.门诊\",\"B.电话\",\"C.家庭访视\",\"D.以上均是\"]', 'D', 3, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (47, 2, 3, '健康档案中体格检查项目不包括', 1, '[\"A.体温\",\"B.脉搏\",\"C.血型\",\"D.腰围\"]', 'C', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (48, 2, 3, '居民健康档案的保存期限为', 1, '[\"A.15年\",\"B.30年\",\"C.至居民死亡后5年\",\"D.长期保存\"]', 'D', 2, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (49, 2, 3, '健康档案个人基本信息表中既往史不包括', 1, '[\"A.高血压\",\"B.糖尿病\",\"C.家族肿瘤史\",\"D.外伤手术史\"]', 'C', 3, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (50, 2, 3, '糖尿病患者健康管理中血糖控制满意的空腹血糖值应低于（）mmol/L', 1, '[\"A.5.0\",\"B.6.1\",\"C.7.0\",\"D.11.1\"]', 'C', 3, 3, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (51, 2, 3, '健康档案更新频率要求正确的是', 1, '[\"A.每年至少更新1次\",\"B.仅首次建立\",\"C.无需更新\",\"D.每月更新\"]', 'A', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (52, 2, 3, '重点人群健康管理服务不包括', 1, '[\"A.0-6岁儿童\",\"B.孕产妇\",\"C.企业白领\",\"D.重性精神疾病患者\"]', 'C', 2, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (53, 2, 3, '居民健康档案的内容包括', 2, '[\"A.个人基本信息\",\"B.健康体检\",\"C.重点人群管理记录\",\"D.以上均是\"]', 'D', 3, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (54, 2, 3, '老年人健康管理的服务对象是年龄在（）岁以上的居民', 1, '[\"A.55\",\"B.60\",\"C.65\",\"D.70\"]', 'C', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (55, 2, 3, '健康档案中健康评价不包括', 1, '[\"A.高血压\",\"B.糖尿病\",\"C.收入水平\",\"D.生活方式\"]', 'C', 3, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (56, 2, 3, '以下哪项是健康档案信息安全的基本要求', 1, '[\"A.公开透明\",\"B.保护隐私\",\"C.数据共享\",\"D.强制上报\"]', 'B', 2, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (57, 3, 4, '基层医疗卫生机构的首诊负责制是指', 1, '[\"A.谁首诊谁负责\",\"B.仅初次诊断\",\"C.不负责转诊\",\"D.以上均错\"]', 'A', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (58, 3, 4, '国家基本公共卫生服务项目目前共有（）大类', 1, '[\"A.10\",\"B.12\",\"C.14\",\"D.16\"]', 'B', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (59, 3, 4, '以下哪些属于基层卫生服务基本原则', 2, '[\"A.以健康为中心\",\"B.以人群为对象\",\"C.以需求为导向\",\"D.以上均是\"]', 'D', 3, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (60, 3, 4, '基层卫生服务的首诊机构是', 1, '[\"A.三甲医院\",\"B.县级医院\",\"C.乡镇卫生院/社区卫生服务中心\",\"D.省级医院\"]', 'C', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (61, 3, 4, '双向转诊中\'上转\'的正确流程是', 1, '[\"A.基层→上级\",\"B.上级→基层\",\"C.甲院→乙院\",\"D.急诊→门诊\"]', 'A', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (62, 3, 4, '基层卫生服务的基本特征不包括', 1, '[\"A.综合性\",\"B.连续性\",\"C.高尖技术\",\"D.可及性\"]', 'C', 2, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (63, 3, 4, '家庭医生签约服务的重点人群是', 2, '[\"A.老年人\",\"B.慢性病患者\",\"C.贫困人口\",\"D.以上均是\"]', 'D', 3, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (64, 3, 4, '全科医生的角色不包括', 1, '[\"A.首诊医生\",\"B.健康守门人\",\"C.疑难杂症专家\",\"D.协调者\"]', 'C', 2, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (65, 3, 4, '以下哪项是基层卫生服务的核心内容', 1, '[\"A.开刀手术\",\"B.疑难杂症诊治\",\"C.常见病多发病诊疗\",\"D.基因检测\"]', 'C', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (66, 3, 4, '基本公共卫生服务经费人均补助标准应由（）级财政承担', 1, '[\"A.中央\",\"B.省级\",\"C.市县\",\"D.中央与地方共担\"]', 'D', 3, 3, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (67, 3, 4, '基层卫生服务的运行机制强调', 1, '[\"A.以药养医\",\"B.公益性质\",\"C.市场导向\",\"D.盈利优先\"]', 'B', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (68, 3, 4, '以下哪项不属于中医药健康管理服务内容', 1, '[\"A.中医体质辨识\",\"B.中药汤剂免费发放\",\"C.0-36个月儿童中医调养\",\"D.老年人中医保健\"]', 'B', 3, 3, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (69, 3, 4, '基层卫生人才能力提升的重点方向是', 2, '[\"A.学历提升\",\"B.临床实操\",\"C.全科转岗培训\",\"D.以上均是\"]', 'D', 3, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (70, 3, 4, '乡镇卫生院床位设置标准一般为', 1, '[\"A.5-10张\",\"B.10-50张\",\"C.100-200张\",\"D.500张以上\"]', 'B', 3, 3, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (71, 3, 4, '基层卫生服务\'六位一体\'不包括', 1, '[\"A.预防\",\"B.医疗\",\"C.高精尖科研\",\"D.康复\"]', 'C', 2, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (72, 3, 4, '村卫生室的职责不包括', 1, '[\"A.基本医疗\",\"B.公共卫生\",\"C.开腹手术\",\"D.健康教育\"]', 'C', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (73, 3, 5, '心脏骤停的黄金抢救时间是（）分钟内', 1, '[\"A.2\",\"B.4\",\"C.10\",\"D.30\"]', 'B', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (74, 3, 5, '成人心肺复苏的按压频率是（）次/分', 1, '[\"A.60-80\",\"B.80-100\",\"C.100-120\",\"D.140-160\"]', 'C', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (75, 3, 5, '心肺复苏时按压深度应为（）cm', 1, '[\"A.3-4\",\"B.5-6\",\"C.7-8\",\"D.9-10\"]', 'B', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (76, 3, 5, '海姆立克急救法适用于', 1, '[\"A.心脏骤停\",\"B.气道异物梗阻\",\"C.中风\",\"D.溺水\"]', 'B', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (77, 3, 5, '以下哪种情况应首先进行心肺复苏', 2, '[\"A.无意识\",\"B.无呼吸\",\"C.无脉搏\",\"D.无意识且无正常呼吸\"]', 'D', 3, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (78, 3, 5, '心脏复苏时按压与通气的比例是', 1, '[\"A.15:2\",\"B.30:2\",\"C.20:2\",\"D.10:2\"]', 'B', 3, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (79, 3, 5, '成人气道完全梗阻的表现不包括', 1, '[\"A.不能说话\",\"B.剧烈咳嗽\",\"C.口唇发绀\",\"D.V形手势\"]', 'B', 3, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (80, 3, 5, '中暑患者的错误处理是', 1, '[\"A.转移阴凉处\",\"B.补充水分\",\"C.服用退热药\",\"D.冰敷降温\"]', 'C', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (81, 3, 5, '电击伤急救的第一步是', 1, '[\"A.心肺复苏\",\"B.切断电源\",\"C.包扎伤口\",\"D.呼叫120\"]', 'B', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (82, 3, 5, '以下哪项不是有机磷农药中毒的表现', 1, '[\"A.瞳孔缩小\",\"B.流涎\",\"C.肌颤\",\"D.瞳孔扩大\"]', 'D', 3, 3, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (83, 3, 5, '烧烫伤急救五步法正确顺序是', 1, '[\"A.脱、冲、泡、盖、送\",\"B.冲、脱、泡、盖、送\",\"C.泡、冲、脱、盖、送\",\"D.盖、泡、脱、冲、送\"]', 'B', 3, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (84, 3, 5, '意识丧失但有呼吸的患者应采取的体位是', 1, '[\"A.平卧位\",\"B.半坐位\",\"C.复原卧位\",\"D.俯卧位\"]', 'C', 2, 1, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (85, 3, 5, '以下哪项是过敏性休克的早期表现', 2, '[\"A.皮疹\",\"B.喉头水肿\",\"C.低血压\",\"D.以上均是\"]', 'D', 3, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (86, 3, 5, 'AED（自动体外除颤器）的使用时机是', 1, '[\"A.发现患者倒地立即使用\",\"B.确认无脉搏后\",\"C.按压2分钟后\",\"D.专业人员到场后\"]', 'C', 3, 3, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (87, 3, 5, '中暑中最严重的类型是', 1, '[\"A.热痉挛\",\"B.热衰竭\",\"C.热射病\",\"D.轻微中暑\"]', 'C', 2, 2, '2026-07-09 14:48:25', 0);
INSERT INTO `question` VALUES (88, 3, 5, '对创伤出血患者止血带使用的注意事项不包括', 1, '[\"A.记录使用时间\",\"B.每隔1小时放松\",\"C.直接扎在皮肤上\",\"D.不可覆盖衣物\"]', 'C', 3, 3, '2026-07-09 14:48:25', 0);

-- ----------------------------
-- Table structure for resource_file
-- ----------------------------
DROP TABLE IF EXISTS `resource_file`;
CREATE TABLE `resource_file`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `course_id` bigint NULL DEFAULT NULL COMMENT '璇剧▼ID',
  `file_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鏂囦欢鍚',
  `file_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '鏂囦欢URL',
  `file_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鏂囦欢绫诲瀷锛歷ideo/document/ppt/pdf',
  `file_size` bigint NULL DEFAULT 0 COMMENT '鏂囦欢澶у皬(瀛楄妭)',
  `uploader_id` bigint NULL DEFAULT NULL COMMENT '涓婁紶鑰匢D',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-姝ｅ父锛?-宸插垹',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '璧勬簮鏂囦欢琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of resource_file
-- ----------------------------

-- ----------------------------
-- Table structure for study_record
-- ----------------------------
DROP TABLE IF EXISTS `study_record`;
CREATE TABLE `study_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL COMMENT '瀛﹀憳ID',
  `course_id` bigint NOT NULL COMMENT '璇剧▼ID',
  `chapter_id` bigint NOT NULL COMMENT '绔犺妭ID',
  `progress` int NULL DEFAULT 0 COMMENT '杩涘害鐧惧垎姣',
  `study_duration` int NULL DEFAULT 0 COMMENT '瀛︿範鏃堕暱(绉?',
  `last_position` int NULL DEFAULT 0 COMMENT '涓婃?鎾?斁浣嶇疆(绉?',
  `completed` tinyint NULL DEFAULT 0 COMMENT '是否完成：0否 1是',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-姝ｅ父锛?-宸插垹',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_record`(`student_id` ASC, `course_id` ASC, `chapter_id` ASC) USING BTREE,
  INDEX `idx_student_id`(`student_id` ASC) USING BTREE,
  INDEX `idx_course_id`(`course_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '瀛︿範璁板綍琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of study_record
-- ----------------------------
INSERT INTO `study_record` VALUES (1, 10, 1, 1, 100, 500, 500, 1, '2026-07-08 09:23:06', '2026-07-08 09:23:06', 0);
INSERT INTO `study_record` VALUES (3, 11, 1, 1, 50, 300, 250, 0, '2026-07-08 09:24:32', '2026-07-08 09:24:32', 0);
INSERT INTO `study_record` VALUES (4, 15, 2, 4, 100, 400, 350, 1, '2026-07-08 09:37:43', '2026-07-08 09:37:43', 0);

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `perm_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `perm_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_perm_code`(`perm_code` ASC) USING BTREE,
  INDEX `idx_module`(`module` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission` VALUES (1, 'course:read', '课程查看', '课程列表/详情查看', 'course', '2026-07-08 16:04:30', 0);
INSERT INTO `sys_permission` VALUES (2, 'course:write', '课程编辑', '课程新增/编辑/发布/删除', 'course', '2026-07-08 16:04:30', 0);
INSERT INTO `sys_permission` VALUES (3, 'chapter:read', '章节查看', '章节列表查看', 'chapter', '2026-07-08 16:04:30', 0);
INSERT INTO `sys_permission` VALUES (4, 'chapter:write', '章节编辑', '章节新增/编辑/排序', 'chapter', '2026-07-08 16:04:30', 0);
INSERT INTO `sys_permission` VALUES (5, 'knowledge:read', '知识点查看', '知识点列表查看', 'knowledge', '2026-07-08 16:04:30', 0);
INSERT INTO `sys_permission` VALUES (6, 'knowledge:write', '知识点编辑', '知识点新增/编辑', 'knowledge', '2026-07-08 16:04:30', 0);
INSERT INTO `sys_permission` VALUES (7, 'question:read', '试题查看', '试题列表查看', 'question', '2026-07-08 16:04:30', 0);
INSERT INTO `sys_permission` VALUES (8, 'question:write', '试题编辑', '试题新增/编辑', 'question', '2026-07-08 16:04:30', 0);
INSERT INTO `sys_permission` VALUES (9, 'exam:read', '考试查看', '考试列表查看', 'exam', '2026-07-08 16:04:30', 0);
INSERT INTO `sys_permission` VALUES (10, 'exam:write', '考试编辑', '考试新增/编辑/组卷', 'exam', '2026-07-08 16:04:30', 0);
INSERT INTO `sys_permission` VALUES (11, 'consult:read', '咨询查看', '咨询工单列表查看', 'consult', '2026-07-08 16:04:30', 0);
INSERT INTO `sys_permission` VALUES (12, 'consult:write', '咨询处理', '咨询回复/知识库管理', 'consult', '2026-07-08 16:04:30', 0);
INSERT INTO `sys_permission` VALUES (13, 'stats:read', '统计查看', '统计报表查看', 'stats', '2026-07-08 16:04:30', 0);
INSERT INTO `sys_permission` VALUES (14, 'stats:write', '统计配置', '统计维度配置', 'stats', '2026-07-08 16:04:30', 0);
INSERT INTO `sys_permission` VALUES (15, 'user:read', '用户查看', '用户列表查看', 'user', '2026-07-08 16:04:30', 0);
INSERT INTO `sys_permission` VALUES (16, 'user:write', '用户编辑', '用户新增/编辑/启用/禁用', 'user', '2026-07-08 16:04:30', 0);
INSERT INTO `sys_permission` VALUES (17, 'plan:read', '培训计划查看', '培训计划列表/详情', 'plan', '2026-07-09 08:27:34', 0);
INSERT INTO `sys_permission` VALUES (18, 'plan:write', '知识点查看', '计划增删改+关联课程', 'plan', '2026-07-09 08:27:34', 0);
INSERT INTO `sys_permission` VALUES (19, 'teacher:read', '知识点编辑', '璁插笀鍒楄〃/璇︽儏鍙??', 'teacher', '2026-07-09 10:58:29', 0);
INSERT INTO `sys_permission` VALUES (20, 'teacher:write', '璁插笀缂栬緫', '鏂板?/淇?敼/鍒犻櫎璁插笀', 'teacher', '2026-07-09 10:58:29', 0);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` tinyint NOT NULL DEFAULT 1,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, 'ADMIN', '系统管理员', '拥有系统全部权限', 1, '2026-07-08 16:04:30', 0);
INSERT INTO `sys_role` VALUES (2, 'TEACHER', '讲师', '课程/试题/考试/咨询管理', 1, '2026-07-08 16:04:30', 0);
INSERT INTO `sys_role` VALUES (3, 'STUDENT', '学员', '仅保留扩展，后台不使用', 1, '2026-07-08 16:04:30', 0);

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `role_id` bigint NOT NULL,
  `permission_id` bigint NOT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_perm`(`role_id` ASC, `permission_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_perm_id`(`permission_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 42 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色-权限关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------
INSERT INTO `sys_role_permission` VALUES (1, 1, 1, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (2, 1, 2, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (3, 1, 3, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (4, 1, 4, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (5, 1, 5, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (6, 1, 6, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (7, 1, 7, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (8, 1, 8, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (9, 1, 9, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (10, 1, 10, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (11, 1, 11, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (12, 1, 12, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (13, 1, 13, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (14, 1, 14, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (15, 1, 15, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (16, 1, 16, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (17, 2, 1, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (18, 2, 2, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (19, 2, 5, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (20, 2, 6, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (21, 2, 7, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (22, 2, 8, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (23, 2, 9, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (24, 2, 10, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (25, 2, 11, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (26, 2, 12, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (27, 2, 13, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (28, 3, 1, '2026-07-08 16:04:30');
INSERT INTO `sys_role_permission` VALUES (29, 1, 17, '2026-07-09 08:27:34');
INSERT INTO `sys_role_permission` VALUES (30, 1, 18, '2026-07-09 08:27:34');
INSERT INTO `sys_role_permission` VALUES (32, 1, 19, '2026-07-09 10:58:29');
INSERT INTO `sys_role_permission` VALUES (33, 1, 20, '2026-07-09 10:58:29');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '鐢ㄦ埛ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '鐢ㄦ埛鍚',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '瀵嗙爜',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '濮撳悕',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '鎵嬫満鍙',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '閭??',
  `role_id` bigint NULL DEFAULT NULL COMMENT 'FK -> sys_role.id',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '澶村儚URL',
  `org_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '鎵?睘鏈烘瀯',
  `job_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '宀椾綅绫诲瀷',
  `status` tinyint NULL DEFAULT 1 COMMENT '鐘舵?',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '鐢ㄦ埛琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'admin', '$2a$10$EEeUC1lM2mbe.nOY0CtsDOVYQciytNhzUMLR2rAgI5nfOXzmlGJPK', '系统管理员', '13800000000', NULL, 1, NULL, '四川省卫健委', NULL, 1, '2026-07-08 08:29:24', '2026-07-09 11:07:48', 0);
INSERT INTO `sys_user` VALUES (2, 'teacher01', '$2a$10$EEeUC1lM2mbe.nOY0CtsDOVYQciytNhzUMLR2rAgI5nfOXzmlGJPK', '张教授', '13800000001', NULL, 2, NULL, '四川省人民医院', NULL, 1, '2026-07-08 08:29:24', '2026-07-09 11:07:49', 0);
INSERT INTO `sys_user` VALUES (3, 'teacher02', '$2a$10$EEeUC1lM2mbe.nOY0CtsDOVYQciytNhzUMLR2rAgI5nfOXzmlGJPK', '李主任', '13800000002', NULL, 2, NULL, '成都医学院', NULL, 1, '2026-07-08 08:29:24', '2026-07-09 11:07:49', 0);
INSERT INTO `sys_user` VALUES (4, 'student01', '$2a$10$EEeUC1lM2mbe.nOY0CtsDOVYQciytNhzUMLR2rAgI5nfOXzmlGJPK', '王医生', '13800000003', NULL, 3, NULL, '汶川县人民医院', '临床', 1, '2026-07-08 08:29:24', '2026-07-09 11:07:50', 0);
INSERT INTO `sys_user` VALUES (5, 'student02', '$2a$10$EEeUC1lM2mbe.nOY0CtsDOVYQciytNhzUMLR2rAgI5nfOXzmlGJPK', '赵护士', '13800000004', NULL, 3, NULL, '茂县中医院', '护理', 1, '2026-07-08 08:29:24', '2026-07-09 11:07:55', 0);
INSERT INTO `sys_user` VALUES (8, 'wx_bb5df0cc', 'wx_login_user', 'miniapp_user', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 08:47:40', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (9, 'wx_81f930c9', 'wx_login_user', '微信用户', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 09:02:29', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (10, 'wx_67da9484', 'wx_login_user', 'test_user', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 09:22:03', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (11, 'wx_196552e2', 'wx_login_user', 'test', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 09:24:32', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (12, 'wx_9da1f8e0', 'wx_login_user', 't', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 09:27:37', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (13, 'wx_642713d7', 'wx_login_user', 'u', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 09:29:28', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (14, 'wx_2a1585a8', 'wx_login_user', 'u', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 09:32:15', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (15, 'wx_a3ac1971', 'wx_login_user', 'u', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 09:37:43', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (16, 'wx_5928fca6', 'wx_login_user', '微信用户', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 10:27:48', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (17, 'wx_66afd6e1', 'wx_login_user', '微信用户', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 10:28:01', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (18, 'wx_26ac9e9e', 'wx_login_user', '微信用户', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 10:28:18', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (19, 'wx_99acc1d7', 'wx_login_user', '微信用户', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 10:28:31', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (20, 'wx_1e07d3be', 'wx_login_user', '微信用户', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 10:28:42', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (21, 'wx_f7ad653c', 'wx_login_user', '微信用户', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 10:29:09', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (22, 'wx_95ee2193', 'wx_login_user', '微信用户', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 10:29:28', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (23, 'wx_ea6d4ebc', 'wx_login_user', '微信用户', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 10:29:46', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (24, 'wx_7e49a717', 'wx_login_user', '微信用户', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 10:30:00', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (25, 'wx_e8dc01b0', 'wx_login_user', '微信用户', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 10:30:20', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (26, 'wx_e2797c4e', 'wx_login_user', '微信用户', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 10:31:01', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (27, 'wx_872388ff', 'wx_login_user', 'u', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 10:32:26', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (28, 'wx_7f31ab63', 'wx_login_user', '微信用户', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 10:32:45', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (29, 'wx_39c21ded', 'wx_login_user', '微信用户', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 10:32:59', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (30, 'wx_1c8aeb9c', 'wx_login_user', 'u', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 10:33:23', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (31, 'wx_e9d69ebf', 'wx_login_user', '微信用户', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 10:33:29', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (32, 'wx_0018161c', 'wx_login_user', '微信用户', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 10:34:05', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (33, 'wx_d702fa78', 'wx_login_user', 'u', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 10:34:34', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (34, 'wx_d90f0285', 'wx_login_user', 'u', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 10:55:27', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (35, 'wx_9ab62b5e', 'wx_login_user', 'u', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 10:56:19', '2026-07-08 16:06:59', 0);
INSERT INTO `sys_user` VALUES (36, 'wx_0a3d7213', 'wx_login_user', 'u', NULL, NULL, 3, NULL, NULL, NULL, 1, '2026-07-08 10:56:39', '2026-07-08 16:06:59', 0);

-- ----------------------------
-- Table structure for teacher
-- ----------------------------
DROP TABLE IF EXISTS `teacher`;
CREATE TABLE `teacher`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '鍏宠仈鐢ㄦ埛ID',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '璁插笀濮撳悕',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鑱岀О',
  `education` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '瀛﹀巻',
  `direction` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '鏁欏?鏂瑰悜',
  `intro` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '璁插笀绠?粙',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-姝ｅ父锛?-宸插垹',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鍩硅?璁插笀琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of teacher
-- ----------------------------
INSERT INTO `teacher` VALUES (1, 2, '张教授', '主任医师', '博士', '临床医学', '从事临床医学教学20年，擅长常见病诊疗', '2026-07-08 08:29:47', 0);
INSERT INTO `teacher` VALUES (2, 3, '李主任', '副主任医师', '硕士', '公共卫生', '公共卫生领域专家，主持多项省级课题', '2026-07-08 08:29:47', 0);

-- ----------------------------
-- Table structure for train_plan
-- ----------------------------
DROP TABLE IF EXISTS `train_plan`;
CREATE TABLE `train_plan`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '璁″垝ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '璁″垝鍚嶇О',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '璁″垝鎻忚堪',
  `status` tinyint NULL DEFAULT 0 COMMENT '鐘舵?锛?鑽夌? 1宸插彂甯?2宸茬粨鏉',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '閫昏緫鍒犻櫎锛?-姝ｅ父锛?-宸插垹',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '鍩硅?璁″垝琛' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of train_plan
-- ----------------------------
INSERT INTO `train_plan` VALUES (1, '2024年民族县基层卫生人员培训计划', '针对四川省67个民族县的基层卫生技术人员培训', 1, '2026-07-08 08:30:10', '2026-07-08 08:30:10', 0);
INSERT INTO `train_plan` VALUES (2, '2025', 'test', 0, '2026-07-09 10:50:57', '2026-07-09 10:50:57', 0);

SET FOREIGN_KEY_CHECKS = 1;
