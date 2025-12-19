-- 验证 tokens 表结构
-- 检查是否还有 user_id 的唯一约束
SHOW INDEX FROM tokens WHERE Key_name = 'uk_tokens_user_id';

-- 显示所有索引
SHOW INDEX FROM tokens;

-- 显示表结构
DESCRIBE tokens;