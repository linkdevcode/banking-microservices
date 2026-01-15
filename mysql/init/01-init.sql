-- =========================
-- DATABASES
-- =========================
CREATE DATABASE IF NOT EXISTS banking_user_db;
CREATE DATABASE IF NOT EXISTS banking_payment_db;
CREATE DATABASE IF NOT EXISTS banking_history_db;
CREATE DATABASE IF NOT EXISTS banking_batch_db;

-- =========================
-- USERS
-- =========================
CREATE USER IF NOT EXISTS 'user_service'@'%' IDENTIFIED BY 'user123';
CREATE USER IF NOT EXISTS 'payment_service'@'%' IDENTIFIED BY 'payment123';
CREATE USER IF NOT EXISTS 'history_service'@'%' IDENTIFIED BY 'history123';
CREATE USER IF NOT EXISTS 'batch_service'@'%' IDENTIFIED BY 'batch123';

-- =========================
-- GRANTS
-- =========================
GRANT ALL PRIVILEGES ON banking_user_db.* TO 'user_service'@'%';
GRANT ALL PRIVILEGES ON banking_payment_db.* TO 'payment_service'@'%';
GRANT ALL PRIVILEGES ON banking_history_db.* TO 'history_service'@'%';
GRANT ALL PRIVILEGES ON banking_batch_db.* TO 'batch_service'@'%';

FLUSH PRIVILEGES;
