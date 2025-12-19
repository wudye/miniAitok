

-- RIMARY KEY 隐含唯一性（等同于一个不能为 NULL 的唯一索引），但 UNIQUE KEY 并不等同于 PRIMARY KEY。两者区别要点：
-- 唯一性和可空性
-- PRIMARY KEY 必须唯一且不能为 NULL。
-- UNIQUE KEY 保证唯一性，但列可以为 NULL（在 MySQL 中允许多个 NULL），表中可以有多个 UNIQUE 索引。
-- 表级限制数量
-- 表只能有一个 PRIMARY KEY。
-- 可以有任意多个 UNIQUE KEY。
-- 存储与索引类型（MySQL / InnoDB）
-- PRIMARY KEY 在 InnoDB 中作为聚簇索引（clustered index），行数据按照主键组织，查主键很快。
-- UNIQUE KEY 是二级索引，索引记录中会保存主键值以便回表查询。
-- 外键与语义
-- 外键通常引用 PRIMARY KEY（也可以引用有唯一索引的列）。
-- PRIMARY KEY 常用于表的主标识；UNIQUE 用于额外的业务唯一约束（如邮箱、用户名等）。

CREATE TABLE `member` (
                          `user_id` bigint NOT NULL AUTO_INCREMENT,
                          `user_name` varchar(100) NOT NULL COMMENT 'User account',
                          `nick_name` varchar(100) DEFAULT NULL COMMENT 'User nickname',
                          `email` varchar(255) NOT NULL COMMENT 'User email',
                          `telephone` varchar(50) DEFAULT NULL COMMENT 'Phone number',
                          `sex` varchar(2) DEFAULT NULL COMMENT 'User gender (0: female, 1: male, 2: unknown)',
                          `avatar` varchar(2048) DEFAULT NULL COMMENT 'Avatar URL',
                          `password` varchar(255) NOT NULL COMMENT 'Password',
                          `salt` varchar(255) DEFAULT NULL,
                          `status` varchar(10) DEFAULT NULL COMMENT 'Account status (0: normal, 1: disabled)',
                          `del_flag` varchar(10) DEFAUL 0 COMMENT 'Deletion flag (0: exists, 1: deleted)',
                          `login_ip` varchar(45) DEFAULT NULL COMMENT 'Last login IP',
                          `login_location` varchar(255) DEFAULT NULL COMMENT 'Last login location',
                          `login_date` DATETIME DEFAULT NULL COMMENT 'Last login time',
                          `create_by` varchar(100) DEFAULT NULL COMMENT 'Creator',
                          `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Creation time',
                          `update_by` varchar(10) DEFAULT NULL COMMENT 'Updater',
                          `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Update time',
                          PRIMARY KEY (`user_id`),
                          UNIQUE KEY `uk_user_name` (`user_name`),
                          KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Member information table';


-- 冗余：在大多数情况下这是重复约束，VARCHAR(255) 已经在列层面限制了长度。
-- 不同：LENGTH() 返回字节数（utf8mb4 下一个字符可占 up to 4 字节），所以用 LENGTH() 可能比列定义更严格——例如 255 个 4 字节字符会使 LENGTH() 成为 1020，触发 CHECK，但字符数仍在 255 内。
-- 行为差异：在非严格模式下，超长值可能被截断（有警告），而 CHECK 可以在数据库层面明确拒绝插入，避免隐式截断。
-- 兼容性：MySQL 5.x 曾经忽略 CHECK，MySQL 8.0+ 才支持。
-- 这行语句表示为表上的 user_id 列创建了一个普通（非唯一）索引，索引名为 idx_member_info_user_id。主要作用与要点：
-- 提高基于 user_id 的查找、JOIN、和排序（WHERE / JOIN / ORDER BY）的查询性能。
-- 不会强制唯一性（与 UNIQUE 不同）。
-- 会增加写入（INSERT/UPDATE/DELETE）开销和额外的存储空间。
--     若 user_id 是经常用于查询或关联的字段，建立此索引通常是有利的。
CREATE TABLE `member_info` (
                               `info_id` bigint NOT NULL AUTO_INCREMENT COMMENT 'id',
                               `user_id` bigint DEFAULT NULL COMMENT 'user id',
                               `back_image` varchar(255) DEFAULT NULL COMMENT 'Personal page background image',
                               `description` varchar(300) DEFAULT NULL COMMENT 'Personal description',
                               `birthday` datetime DEFAULT NULL COMMENT 'Birthday',
                               `country` VARCHAR(50) DEFAULT NULL,
                               `province` varchar(20) DEFAULT NULL COMMENT 'Province',
                               `city` varchar(30) DEFAULT NULL COMMENT 'City',
                               `region` varchar(30) DEFAULT NULL COMMENT 'District',
                               `adcode` varchar(6) DEFAULT NULL COMMENT 'Postal code',
                               `campus` varchar(64) DEFAULT NULL COMMENT 'School',
                               `like_show_status` varchar(255) DEFAULT NULL COMMENT 'Like video display status: 0-show, 1-hide',
                               `favorite_show_status` varchar(255) DEFAULT NULL COMMENT 'Favorite video display status: 0-show, 1-hide',
                               PRIMARY KEY (`info_id`),
                               CONSTRAINT `max_length_back_image` CHECK (LENGTH(`back_image`) <= 255),
                               CONSTRAINT `max_length_description` CHECK (LENGTH(`description`) <= 300),
                               CONSTRAINT `max_length_province` CHECK (LENGTH(`province`) <= 20),
                               CONSTRAINT `max_length_city` CHECK (LENGTH(`city`) <= 30),
                               CONSTRAINT `max_length_region` CHECK (LENGTH(`region`) <= 30),
                               CONSTRAINT `max_length_adcode` CHECK (LENGTH(`adcode`) <= 6),
                               CONSTRAINT `max_length_campus` CHECK (LENGTH(`campus`) <= 64)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Member information table';


CREATE TABLE `user_sensitive` (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'primary key',
                                  `sensitives` varchar(255) DEFAULT NULL COMMENT 'sensitive words',
                                  `created_time` datetime DEFAULT NULL COMMENT 'created time',
                                  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='User sensitive information table';
