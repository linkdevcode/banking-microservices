DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS accounts;
DROP TABLE IF EXISTS roles;
DROP TABLE IF EXISTS users;

CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL,
    phone_number VARCHAR(10) NOT NULL,     -- Khớp với phoneNumber
    full_name VARCHAR(100),                -- Khớp với fullName
    status VARCHAR(20) NOT NULL,           -- Khớp với EUserStatus (Enum String)
    created_at DATETIME NOT NULL,          -- Khớp với createdAt
    password_changed_at TIMESTAMP NULL,    -- Khớp với passwordChangedAt
    CONSTRAINT uk_username UNIQUE (username),
    CONSTRAINT uk_email UNIQUE (email)
);

CREATE TABLE accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,                 -- Vừa là PK vừa trỏ sang users.id
    user_id BIGINT NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    balance DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(20) NOT NULL,           -- Khớp với EAccountStatus (Enum String)
    CONSTRAINT uk_account_number UNIQUE (account_number),
    CONSTRAINT fk_accounts_users FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_ur_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_ur_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);