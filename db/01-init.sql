-- 本機開發用初始化腳本，僅供 localhost / docker-compose 開發環境使用
-- 密碼為開發用預留值，正式環境請改用環境變數或密鑰管理，勿沿用此值
CREATE DATABASE IF NOT EXISTS tododb DEFAULT CHARSET=utf8mb4;
CREATE USER IF NOT EXISTS 'todolist'@'%' IDENTIFIED BY 'todolist_local_dev_only';
ALTER USER 'todolist'@'%' IDENTIFIED BY 'todolist_local_dev_only';
GRANT SELECT, INSERT, UPDATE, DELETE ON tododb.* TO 'todolist'@'%';
FLUSH PRIVILEGES;
USE tododb;

CREATE TABLE IF NOT EXISTS todo(
	id BIGINT AUTO_INCREMENT PRIMARY KEY,
	title VARCHAR(100) NOT NULL,
	description TEXT,
	completed BOOLEAN DEFAULT FALSE NOT NULL,
	created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
	updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL
);