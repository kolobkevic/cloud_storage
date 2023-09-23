INSERT INTO roles
(description, name, role_type, created_at, updated_at)
VALUES('Super Admin', 'Root', 'ADMIN', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());

INSERT INTO roles
(description, name, role_type, created_at, updated_at)
VALUES('Base role for users', 'User', 'USER', CURRENT_TIMESTAMP(), CURRENT_TIMESTAMP());