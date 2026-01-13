INSERT INTO users (username, password, email, phone_number, full_name, status, created_at)
VALUES (
    'admin', 
    '$2a$10$fWvzswb.uYsbtl6TaH45hOL6SYnw0D3OnTAiVuzgvDkL8zwJm/fOa', 
    'admin@mail.com', 
    '0123456789', 
    'System Admin', 
    'ACTIVE', 
    NOW()
);

INSERT INTO user_roles (user_id, role_id) 
VALUES (
    (SELECT id FROM users WHERE username = 'admin'), 
    (SELECT id FROM roles WHERE name = 'ROLE_ADMIN')
);

INSERT INTO accounts (id, account_number, balance, currency, status)
VALUES (
    (SELECT id FROM users WHERE username = 'admin'), 
    'BANK-ADMIN-001', 
    0.00, 
    'VND', 
    'ACTIVE'
);