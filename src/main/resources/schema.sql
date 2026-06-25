-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS question_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE question_db;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码（加密后）',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    last_login_time DATETIME COMMENT '最后登录时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建题目类型字典表
CREATE TABLE IF NOT EXISTS question_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '类型标识(single/multiple/judge/fill/essay)',
    display_name VARCHAR(50) NOT NULL COMMENT '显示名称(单选题/多选题/判断题/填空题/问答题)'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目类型字典表';

-- 插入题目类型数据
INSERT INTO question_types (name, display_name) VALUES
('single', '单选题'),
('multiple', '多选题'),
('judge', '判断题'),
('fill', '填空题'),
('essay', '问答题')
ON DUPLICATE KEY UPDATE display_name=VALUES(display_name);

-- 创建题目表
CREATE TABLE IF NOT EXISTS questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL COMMENT '题目标题',
    content TEXT NOT NULL COMMENT '题目内容',
    type_id BIGINT NOT NULL COMMENT '题目类型ID，外键关联question_types表',
    difficulty ENUM('easy', 'medium', 'hard') NOT NULL COMMENT '难度等级：easy-简单，medium-中等，hard-困难',
    options JSON COMMENT '选项（JSON数组，适用于选择题和判断题）',
    correct_answers JSON COMMENT '正确答案索引（JSON数组，适用于选择题）',
    correct_answer TEXT COMMENT '正确答案（适用于填空题和问答题）',
    tags JSON COMMENT '题目标签（JSON数组）',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    created_by BIGINT COMMENT '创建者ID',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_type_id (type_id),
    INDEX idx_difficulty (difficulty),
    INDEX idx_status (status),
    INDEX idx_created_time (created_time),
    FOREIGN KEY (type_id) REFERENCES question_types(id) ON DELETE RESTRICT ON UPDATE RESTRICT,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='题目表';

-- 插入测试数据（使用明文密码）
INSERT INTO users (username, password, email, nickname, status) VALUES 
('admin', '123456', 'admin@example.com', '管理员', 1),
('user1', '123456', 'user1@example.com', '用户1', 1),
('test', '123456', 'test@example.com', '测试用户', 1)
ON DUPLICATE KEY UPDATE password=VALUES(password);

-- type_id 映射: 1=single, 2=multiple, 3=judge, 4=fill, 5=essay

-- 插入题目测试数据
INSERT INTO questions (title, content, type_id, difficulty, options, correct_answers, tags, created_by) VALUES
('JavaScript基础语法', '以下哪个是JavaScript中声明变量的正确方式？', 1, 'easy',
 '["var x = 10;", "variable x = 10;", "v x = 10;", "declare x = 10;"]',
 '[0]',
 '["JavaScript", "基础", "语法"]', 1),

('Vue.js组件通信', 'Vue.js中父组件向子组件传递数据使用什么？', 1, 'medium',
 '["props", "emit", "slot", "ref"]',
 '[0]',
 '["Vue.js", "组件", "通信"]', 1),

('HTTP状态码', '以下哪些是客户端错误的HTTP状态码？', 2, 'medium',
 '["400", "401", "404", "500"]',
 '[0, 1, 2]',
 '["HTTP", "状态码", "网络"]', 1),

('CSS布局判断', 'CSS中display:flex属性用于创建弹性布局。', 3, 'easy',
 '["正确", "错误"]',
 '[0]',
 '["CSS", "布局", "Flexbox"]', 1)
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- 插入问答题数据（使用correct_answer字段）
INSERT INTO questions (title, content, type_id, difficulty, correct_answer, tags, created_by) VALUES
('算法复杂度', '请简述快速排序算法的时间复杂度。', 5, 'hard',
 '快速排序的平均时间复杂度为O(n log n)，最坏情况下为O(n²)，最好情况下为O(n log n)。空间复杂度为O(log n)。',
 '["算法", "排序", "复杂度"]', 1)
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- 创建学生表
CREATE TABLE IF NOT EXISTS students (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '学生ID',
    student_number VARCHAR(20) NOT NULL UNIQUE COMMENT '学号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    password VARCHAR(255) NOT NULL COMMENT '密码(加密后)',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    class_name VARCHAR(50) COMMENT '班级',
    major VARCHAR(100) COMMENT '专业',
    grade VARCHAR(10) COMMENT '年级',
    status TINYINT DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_student_number (student_number),
    INDEX idx_name (name),
    INDEX idx_class_name (class_name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生用户表';

-- 插入测试学生
INSERT INTO students (student_number, name, password, email, class_name, major, grade) VALUES
('2021001', '张三', '123456', 'zhangsan@example.com', '计算机1班', '计算机科学与技术', '2021'),
('2021002', '李四', '123456', 'lisi@example.com', '计算机1班', '计算机科学与技术', '2021')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 创建考试表
CREATE TABLE IF NOT EXISTS exams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '考试ID',
    title VARCHAR(200) NOT NULL COMMENT '考试标题',
    description TEXT COMMENT '考试描述',
    start_time DATETIME NOT NULL COMMENT '考试开始时间',
    end_time DATETIME NOT NULL COMMENT '考试结束时间',
    duration INT NOT NULL COMMENT '考试时长(分钟)',
    total_score DECIMAL(5,2) DEFAULT 100.00 COMMENT '总分',
    pass_score DECIMAL(5,2) DEFAULT 60.00 COMMENT '及格分数',
    question_count INT DEFAULT 0 COMMENT '题目数量',
    status INT DEFAULT 1,
    created_by BIGINT COMMENT '创建者ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (status),
    INDEX idx_start_time (start_time),
    INDEX idx_end_time (end_time),
    INDEX idx_created_by (created_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试表';

-- 创建考试题目关联表
CREATE TABLE IF NOT EXISTS exam_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '关联ID',
    exam_id BIGINT NOT NULL COMMENT '考试ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    question_order INT DEFAULT 1 COMMENT '题目顺序',
    question_score DECIMAL(5,2) DEFAULT 5.00 COMMENT '题目分值',
    score DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_exam_question (exam_id, question_id),
    INDEX idx_exam_id (exam_id),
    INDEX idx_question_id (question_id),
    INDEX idx_question_order (question_order),
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试题目关联表';

-- 创建考试记录表
CREATE TABLE IF NOT EXISTS exam_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '记录ID',
    exam_id BIGINT NOT NULL COMMENT '考试ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    student_number VARCHAR(20) COMMENT '学号',
    student_name VARCHAR(50) COMMENT '姓名',
    start_time DATETIME COMMENT '开始答题时间',
    end_time DATETIME(6) COMMENT '结束时间',
    submit_time DATETIME COMMENT '提交时间',
    total_score DECIMAL(5,2) DEFAULT 0.00 COMMENT '总得分',
    score DECIMAL(5,2) DEFAULT 0.00 COMMENT '得分',
    correct_count INT DEFAULT 0 COMMENT '正确题数',
    wrong_count INT DEFAULT 0 COMMENT '错误题数',
    unanswered_count INT DEFAULT 0 COMMENT '未答题数',
    status INT DEFAULT 0 COMMENT '0-未开始 1-进行中 2-已提交 3-超时',
    duration_minutes INT DEFAULT 0 COMMENT '实际用时(分钟)',
    ip_address VARCHAR(45) COMMENT '答题IP地址',
    user_agent VARCHAR(500) COMMENT '浏览器UA',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_exam_student (exam_id, student_id),
    INDEX idx_exam_id (exam_id),
    INDEX idx_student_id (student_id),
    INDEX idx_student_number (student_number),
    INDEX idx_status (status),
    INDEX idx_submit_time (submit_time),
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生考试记录表';

-- 创建学生答题记录表
CREATE TABLE IF NOT EXISTS student_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '答题记录ID',
    exam_record_id BIGINT NOT NULL COMMENT '考试记录ID',
    exam_id BIGINT NOT NULL COMMENT '考试ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    student_answer TEXT COMMENT '学生答案内容',
    correct_answer TEXT COMMENT '正确答案',
    is_correct BIT(1) COMMENT '是否正确',
    score DECIMAL(5,2) DEFAULT 0.00 COMMENT '得分',
    answer_time DATETIME COMMENT '答题时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE INDEX uk_record_question (exam_record_id, question_id),
    INDEX idx_exam_record_id (exam_record_id),
    INDEX idx_exam_id (exam_id),
    INDEX idx_question_id (question_id),
    INDEX idx_student_id (student_id),
    INDEX idx_is_correct (is_correct),
    FOREIGN KEY (exam_record_id) REFERENCES exam_records(id) ON DELETE CASCADE,
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生答题记录表';

-- 创建错题本表
CREATE TABLE IF NOT EXISTS wrong_questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL COMMENT '学生ID',
    question_id BIGINT NOT NULL COMMENT '题目ID',
    exam_id BIGINT COMMENT '来源考试ID(最近一次)',
    wrong_count INT DEFAULT 1 COMMENT '累计错误次数',
    mastered TINYINT DEFAULT 0 COMMENT '是否已掌握 0-未掌握 1-已掌握',
    last_wrong_answer TEXT COMMENT '最近一次的错误答案',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_question (student_id, question_id),
    INDEX idx_student_mastered (student_id, mastered),
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE,
    FOREIGN KEY (exam_id) REFERENCES exams(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='错题本表';