-- =============================================
-- 减减 (Diet Butler) 数据库表结构说明
-- =============================================

-- ---------------------------------------------
-- 表1: users - 用户表
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID，自增主键',
    openid              VARCHAR(64) NOT NULL UNIQUE COMMENT '微信OpenID，唯一标识用户',
    nickname            VARCHAR(50) NOT NULL DEFAULT '减减用户' COMMENT '用户昵称',
    avatar              VARCHAR(500) COMMENT '用户头像URL',
    gender              TINYINT COMMENT '性别: 0=未知, 1=男, 2=女',
    age                 INT COMMENT '年龄（岁）',
    height              DECIMAL(5,1) COMMENT '身高（cm）',
    initial_weight      DECIMAL(5,1) COMMENT '初始体重（kg），建档时记录的起始体重',
    target_weight       DECIMAL(5,1) COMMENT '目标体重（kg）',
    weight_loss_period INT COMMENT '减重周期（天），计划多少天完成目标',
    start_weight_date   DATE COMMENT '开始减重日期',
    dietary_taboo       VARCHAR(255) COMMENT '饮食忌口，逗号分隔，如"海鲜,辛辣,酒精"',
    sleep_start         TIME COMMENT '睡眠开始时间，如 22:00',
    sleep_end           TIME COMMENT '起床时间，如 06:00',
    reminder_enabled    BOOLEAN DEFAULT TRUE COMMENT '是否启用体重提醒: true=启用, false=关闭',
    reminder_interval_hours INT DEFAULT 24 COMMENT '提醒间隔（小时），如24=每24小时提醒一次',
    basic_metabolism    DECIMAL(8,2) COMMENT '基础代谢率（kcal/天），根据身高体重年龄计算',
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at          DATETIME COMMENT '最后更新时间',
    INDEX idx_openid (openid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- ---------------------------------------------
-- 表2: weight_records - 体重记录表
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS weight_records (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID，自增主键',
    user_id         BIGINT NOT NULL COMMENT '用户ID，关联users.id',
    weight          DECIMAL(5,1) NOT NULL COMMENT '体重数值（kg）',
    note            VARCHAR(500) COMMENT '备注，记录当日特殊情况',
    record_date     DATE NOT NULL COMMENT '记录日期（自然日期，非创建时间）',
    sleep_start     TIME COMMENT '睡眠开始时间，如22:00，记录体重时可从用户档案默认携带',
    sleep_end       TIME COMMENT '起床时间，如06:00，记录体重时可从用户档案默认携带',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_record_date (record_date),
    UNIQUE KEY uk_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='体重记录表，每日体重和作息数据';

-- ---------------------------------------------
-- 表3: chat_messages - 对话记录表【已废弃，现使用Redis存储】
-- ---------------------------------------------
-- 说明：此表已不再用于存储对话记录，保留用于历史数据兼容
-- 当前对话使用Redis存储，Key格式: chat:session:{userId}:{sessionId}
-- Redis存储内容：消息列表、创建时间、最后活跃时间
-- 有效期：24小时无互动自动过期
CREATE TABLE IF NOT EXISTS chat_messages (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '消息ID，自增主键',
    user_id         BIGINT NOT NULL COMMENT '用户ID，关联users.id',
    role            VARCHAR(20) NOT NULL COMMENT '角色: user=用户, assistant=助手',
    content         VARCHAR(2000) NOT NULL COMMENT '消息内容，最大2000字符',
    intent          VARCHAR(50) COMMENT '意图分类: greeting/profile_query/weight_curve/profile_update/weight_record/chat',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消息创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对话消息记录表【已废弃，新对话使用Redis存储】';

-- ---------------------------------------------
-- 表4: body_measurements - 身体维度记录表
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS body_measurements (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID，自增主键',
    user_id         BIGINT NOT NULL COMMENT '用户ID，关联users.id',
    record_date     DATE NOT NULL COMMENT '记录日期',
    waist           DECIMAL(5,1) COMMENT '腰围（cm）',
    hip             DECIMAL(5,1) COMMENT '臀围（cm）',
    chest           DECIMAL(5,1) COMMENT '胸围（cm）',
    arm             DECIMAL(5,1) COMMENT '上臂围（cm）',
    thigh           DECIMAL(5,1) COMMENT '大腿围（cm）',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='身体维度记录表，记录腰围/臀围/胸围/上臂围/大腿围';

-- ---------------------------------------------
-- 表5: menstrual_records - 经期记录表
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS menstrual_records (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID，自增主键',
    user_id             BIGINT NOT NULL COMMENT '用户ID，关联users.id',
    cycle_start_date   DATE NOT NULL COMMENT '本次经期开始日期',
    cycle_end_date     DATE COMMENT '本次经期结束日期',
    cycle_length        INT COMMENT '本次周期天数',
    flow_level          VARCHAR(20) COMMENT '经量: light=少/medium=中/heavy=多',
    symptoms            TEXT COMMENT '症状，逗号分隔: bloating=腹胀/cramps=痛经/fatigue=疲倦/headache=头痛/mood_swings=情绪波动/acne=长痘',
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_cycle_start_date (cycle_start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='经期记录表，支持历史查询和周期分析';

-- ---------------------------------------------
-- 表6: water_records - 喝水量记录表
-- ---------------------------------------------
CREATE TABLE IF NOT EXISTS water_records (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID，自增主键',
    user_id         BIGINT NOT NULL COMMENT '用户ID，关联users.id',
    amount          INT NOT NULL COMMENT '喝水量（ml）',
    record_date     DATE NOT NULL COMMENT '记录日期',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='喝水量记录表';
