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
                                      update_time DATETIME DEFAULT NOT NULL CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
                                      del_flag CHAR(1) DEFAULT '0' COMMENT 'Delete flag [0: Normal, 1: Deleted]',

                                      INDEX idx_user_id (user_id),
                                      INDEX idx_model_id (model_id),
                                      INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI Chat Conversation Table';

CREATE TABLE ai_chat_message (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Message ID',
                                 conversation_id BIGINT NOT NULL COMMENT 'Conversation ID',
                                 reply_id BIGINT COMMENT 'Reply message ID',
                                 user_id BIGINT NOT NULL COMMENT 'User ID',
                                 message_type VARCHAR(50) NOT NULL COMMENT 'Message type',
                                 content TEXT NOT NULL COMMENT 'Message content',
                                 use_context CHAR(1) DEFAULT '0' COMMENT 'Use context flag [0: No, 1: Yes]',
                                 role_id BIGINT COMMENT 'Role ID (references ModelAgentDO.id)',
                                 model VARCHAR(100) COMMENT 'Model identifier (redundant from ChatModelDO.model)',
                                 model_id BIGINT COMMENT 'Model ID (references ChatModelDO.id)',
                                 segment_ids JSON COMMENT 'Knowledge segment IDs array (references KnowledgeSegmentDO.id)',

    -- BaseDO fields
                                 create_by VARCHAR(255) COMMENT 'Created by',
                                 create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
                                 update_by VARCHAR(255) COMMENT 'Updated by',
                                 update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
                                 del_flag CHAR(1) DEFAULT '0' COMMENT 'Delete flag [0: Normal, 1: Deleted]',

                                 INDEX idx_conversation_id (conversation_id),
                                 INDEX idx_user_id (user_id),
                                 INDEX idx_reply_id (reply_id),
                                 INDEX idx_model_id (model_id),
                                 FOREIGN KEY (conversation_id) REFERENCES ai_chat_conversation(id),
                                 FOREIGN KEY (model_id) REFERENCES ai_chat_model(id),
                                 FOREIGN KEY (`reply_id`) REFERENCES `ai_chat_message` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
                                 FOREIGN KEY (`role_id`) REFERENCES `model_agent` (`id`) ON DELETE SET NULL ON UPDATE CASCADE

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI Chat Message Table';


CREATE TABLE ai_knowledge (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Knowledge base ID',
                              user_id BIGINT NOT NULL COMMENT 'User ID',
                              name VARCHAR(255) NOT NULL COMMENT 'Knowledge base name',
                              cover_img VARCHAR(500) COMMENT 'Cover image URL',
                              description TEXT COMMENT 'Knowledge base description',
                              embedding_model_id BIGINT COMMENT 'Embedding model ID',
                              embedding_model VARCHAR(100) COMMENT 'Embedding model identifier',
                              top_k INT COMMENT 'Top K value for retrieval',
                              similarity_threshold DECIMAL(5,4) COMMENT 'Similarity threshold',
                              state_flag CHAR(1) DEFAULT '1' COMMENT 'State flag [0: Disabled, 1: Enabled]',

    -- BaseDO fields
                              create_by VARCHAR(255) COMMENT 'Created by',
                              create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
                              update_by VARCHAR(255) COMMENT 'Updated by',
                              update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
                              del_flag CHAR(1) DEFAULT '0' COMMENT 'Delete flag [0: Normal, 1: Deleted]',

                              INDEX idx_user_id (user_id),
                              INDEX idx_state_flag (state_flag),
                              INDEX idx_embedding_model_id (embedding_model_id),
                              INDEX idx_create_time (create_time),
                              UNIQUE KEY uk_user_name (user_id, name) COMMENT 'Unique knowledge base name per user'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI Knowledge Base Table';


CREATE TABLE ai_knowledge_document (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Document ID',
                                       knowledge_id BIGINT NOT NULL COMMENT 'Knowledge base ID',
                                       name VARCHAR(255) NOT NULL COMMENT 'Document name',
                                       url VARCHAR(500) COMMENT 'File URL',
                                       content LONGTEXT COMMENT 'Document content',
                                       content_length INT COMMENT 'Character count',
                                       tokens INT COMMENT 'Token count',
                                       segment_max_tokens INT COMMENT 'Maximum tokens per segment',
                                       retrieval_count INT DEFAULT 0 COMMENT 'Retrieval count',
                                       state_flag CHAR(1) DEFAULT '1' COMMENT 'State flag [0: Disabled, 1: Enabled]',

    -- BaseDO fields
                                       create_by VARCHAR(255) COMMENT 'Created by',
                                       create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
                                       update_by VARCHAR(255) COMMENT 'Updated by',
                                       update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
                                       del_flag CHAR(1) DEFAULT '0' COMMENT 'Delete flag [0: Normal, 1: Deleted]',

                                       INDEX idx_knowledge_id (knowledge_id),
                                       INDEX idx_state_flag (state_flag),
                                       INDEX idx_retrieval_count (retrieval_count),
                                       INDEX idx_create_time (create_time),
                                       FOREIGN KEY (knowledge_id) REFERENCES ai_knowledge(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI Knowledge Document Table';

CREATE TABLE ai_knowledge_segment (
                                      id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Segment ID',
                                      knowledge_id BIGINT NOT NULL COMMENT 'Knowledge base ID',
                                      document_id BIGINT NOT NULL COMMENT 'Document ID',
                                      content TEXT NOT NULL COMMENT 'Segment content',
                                      content_length INT COMMENT 'Character count',
                                      vector_id VARCHAR(255) DEFAULT '' COMMENT 'Vector database ID',
                                      tokens INT COMMENT 'Token count',
                                      retrieval_count INT DEFAULT 0 COMMENT 'Retrieval count',
                                      state_flag CHAR(1) DEFAULT '1' COMMENT 'State flag [0: Disabled, 1: Enabled]',

    -- BaseDO fields
                                      create_by VARCHAR(255) COMMENT 'Created by',
                                      create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
                                      update_by VARCHAR(255) COMMENT 'Updated by',
                                      update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
                                      del_flag CHAR(1) DEFAULT '0' COMMENT 'Delete flag [0: Normal, 1: Deleted]',

                                      INDEX idx_knowledge_id (knowledge_id),
                                      INDEX idx_document_id (document_id),
                                      INDEX idx_vector_id (vector_id),
                                      INDEX idx_state_flag (state_flag),
                                      INDEX idx_retrieval_count (retrieval_count),
                                      INDEX idx_create_time (create_time),
                                      message_id BIGINT,
                                      CONSTRAINT fk_message FOREIGN KEY (message_id) REFERENCES ai_chat_message(id),
                                      FOREIGN KEY (knowledge_id) REFERENCES ai_knowledge(id) ON DELETE CASCADE,
                                      FOREIGN KEY (document_id) REFERENCES ai_knowledge_document(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI Knowledge Segment Table';

CREATE TABLE ai_api_key (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'API Key ID',
                            name VARCHAR(255) NOT NULL COMMENT 'API Key name',
                            api_key VARCHAR(500) NOT NULL COMMENT 'API Key value',
                            platform VARCHAR(100) NOT NULL COMMENT 'Platform (e.g., OpenAI, Azure, etc.)',
                            url VARCHAR(500) COMMENT 'Custom API URL',
                            state_flag CHAR(1) DEFAULT '0' COMMENT 'State flag [0: Normal, 1: Disabled]',

    -- BaseDO fields
                            create_by VARCHAR(255) COMMENT 'Created by',
                            create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
                            update_by VARCHAR(255) COMMENT 'Updated by',
                            update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
                            del_flag CHAR(1) DEFAULT '0' COMMENT 'Delete flag [0: Normal, 1: Deleted]',

                            INDEX idx_platform (platform),
                            INDEX idx_state_flag (state_flag),
                            INDEX idx_create_time (create_time),
                            UNIQUE KEY uk_api_key (api_key) COMMENT 'Unique API key constraint'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI API Key Table';

CREATE TABLE ai_chat_model (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Model ID',
                               key_id BIGINT NOT NULL COMMENT 'API Key ID',
                               name VARCHAR(255) NOT NULL COMMENT 'Model display name',
                               model VARCHAR(100) NOT NULL COMMENT 'Model identifier',
                               platform VARCHAR(50) NOT NULL COMMENT 'Platform (enum: AiPlatformEnum)',
                               type VARCHAR(50) NOT NULL COMMENT 'Model type (enum: AiModelTypeEnum)',
                               state_flag CHAR(1) DEFAULT '0' COMMENT 'State flag [0: Normal, 1: Disabled]',
                               sort INT DEFAULT 0 COMMENT 'Sort order',

    -- Chat configuration
                               temperature DECIMAL(3,2) COMMENT 'Temperature parameter',
                               max_tokens INT COMMENT 'Maximum tokens per response',
                               max_contexts INT COMMENT 'Maximum context messages',

    -- BaseDO fields
                               create_by VARCHAR(255) COMMENT 'Created by',
                               create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
                               update_by VARCHAR(255) COMMENT 'Updated by',
                               update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
                               del_flag CHAR(1) DEFAULT '0' COMMENT 'Delete flag [0: Normal, 1: Deleted]',

                               INDEX idx_key_id (key_id),
                               INDEX idx_platform (platform),
                               INDEX idx_type (type),
                               INDEX idx_state_flag (state_flag),
                               INDEX idx_sort (sort),
                               INDEX idx_create_time (create_time),
                               FOREIGN KEY (key_id) REFERENCES ai_api_key(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI Chat Model Table';


CREATE TABLE ai_model_agent_category (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Category ID',
                                         name VARCHAR(255) NOT NULL COMMENT 'Category name',
                                         icon VARCHAR(500) COMMENT 'Category icon URL',
                                         sort INT DEFAULT 0 COMMENT 'Sort order',

    -- BaseDO fields
                                         create_by VARCHAR(255) COMMENT 'Created by',
                                         create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
                                         update_by VARCHAR(255) COMMENT 'Updated by',
                                         update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
                                         del_flag CHAR(1) DEFAULT '0' COMMENT 'Delete flag [0: Normal, 1: Deleted]',

                                         INDEX idx_sort (sort),
                                         INDEX idx_create_time (create_time),
                                         UNIQUE KEY uk_name (name) COMMENT 'Unique category name'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI Model Agent Category Table';

CREATE TABLE ai_model_agent (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Agent ID',
                                user_id BIGINT NOT NULL COMMENT 'User ID',
                                model_id BIGINT NOT NULL COMMENT 'Model ID',
                                name VARCHAR(255) NOT NULL COMMENT 'Agent name',
                                avatar VARCHAR(500) COMMENT 'Agent avatar URL',
                                category_ids JSON COMMENT 'Category IDs array',
                                description TEXT COMMENT 'Agent description',
                                chat_prologue TEXT COMMENT 'Chat prologue',
                                system_message TEXT COMMENT 'System message/role context',
                                public_flag CHAR(1) DEFAULT '0' COMMENT 'Public flag [0: Private, 1: Public]',
                                state_flag CHAR(1) DEFAULT '1' COMMENT 'State flag [0: Disabled, 1: Enabled]',
                                sort INT DEFAULT 0 COMMENT 'Sort order',
                                knowledge_ids JSON COMMENT 'Knowledge base IDs array (references KnowledgeDO.id)',
                                tool_ids JSON COMMENT 'Tool IDs array (references ToolDO.id)',

    -- BaseDO fields
                                create_by VARCHAR(255) COMMENT 'Created by',
                                create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
                                update_by VARCHAR(255) COMMENT 'Updated by',
                                update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
                                del_flag CHAR(1) DEFAULT '0' COMMENT 'Delete flag [0: Normal, 1: Deleted]',

                                INDEX idx_user_id (user_id),
                                INDEX idx_model_id (model_id),
                                INDEX idx_public_flag (public_flag),
                                INDEX idx_state_flag (state_flag),
                                INDEX idx_sort (sort),
                                INDEX idx_create_time (create_time),
                                FOREIGN KEY (model_id) REFERENCES ai_chat_model(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI Model Agent Table';