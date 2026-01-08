

CREATE TABLE ai_chat_conversation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Conversation ID',
    user_id BIGINT NOT NULL COMMENT 'User ID',
    model_id BIGINT NOT NULL COMMENT 'Model ID',
    role_id BIGINT COMMENT 'Role ID',
    title VARCHAR(255) COMMENT 'Conversation title',
    pinned CHAR(1) DEFAULT '0' COMMENT 'Pinned status [0: No, 1: Yes]',
    pinned_time DATETIME COMMENT 'Pinned time',
    system_message TEXT COMMENT 'Role setting/system message',
    temperature DECIMAL(3,2) COMMENT 'Temperature parameter',
    max_tokens INT COMMENT 'Maximum tokens per response',
    max_contexts INT COMMENT 'Maximum context messages',
    last_message TEXT COMMENT 'Last response content',
    
    -- BaseDO fields
    create_by VARCHAR(255) COMMENT 'Created by',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
    update_by VARCHAR(255) COMMENT 'Updated by',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    del_flag CHAR(1) DEFAULT '0' COMMENT 'Delete flag [0: Normal, 1: Deleted]',
    
    INDEX idx_user_id (user_id),
    INDEX idx_model_id (model_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI Chat Conversation Table';