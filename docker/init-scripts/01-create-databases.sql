-- EDAMS 数据库初始化脚本
-- 创建所有微服务需要的数据库

-- 基础服务数据库
CREATE DATABASE IF NOT EXISTS edams_nacos CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS edams_gateway CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS edams_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS edams_user CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS edams_permission CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 数据管理服务数据库
CREATE DATABASE IF NOT EXISTS edams_asset CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS edams_analytics CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS edams_lifecycle CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS edams_knowledge CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 业务服务数据库
CREATE DATABASE IF NOT EXISTS edams_llm CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS edams_notification CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS edams_workflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS edams_aiops CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS edams_chatbot CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 新增服务数据库
CREATE DATABASE IF NOT EXISTS edams_analysis CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS edams_collaboration CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS edams_report CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 授予权限
GRANT ALL PRIVILEGES ON edams_%.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON edams_%.* TO 'edams'@'%';
FLUSH PRIVILEGES;
