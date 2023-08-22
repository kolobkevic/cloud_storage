DROP TABLE IF EXISTS users;
CREATE TABLE users (
                       id                 bigint          NOT NULL AUTO_INCREMENT,
                       email              varchar(128)    NOT NULL UNIQUE,
                       first_name         varchar(50)     NOT NULL,
                       last_name          varchar(50)     NOT NULL,
                       password           varchar(64)     DEFAULT NULL,
                       created_at         datetime(5)     DEFAULT NULL,
                       updated_at         datetime(5)     DEFAULT NULL,
                       PRIMARY KEY (id)
);

DROP TABLE IF EXISTS roles;
CREATE TABLE roles (
                       id                 bigint          NOT NULL AUTO_INCREMENT,
                       description        varchar(150)    NOT NULL,
                       name               varchar(30)     NOT NULL UNIQUE ,
                       role_type          varchar(10)     NOT NULL,
                       created_at         datetime(5)     DEFAULT NULL,
                       updated_at         datetime(5)     DEFAULT NULL,
                       PRIMARY KEY (id)
);

DROP TABLE IF EXISTS users_roles;
CREATE TABLE users_roles (
                             user_id      bigint         NOT NULL,
                             role_id      bigint         NOT NULL,
                             PRIMARY KEY (user_id, role_id),
                             KEY `K_users_roles_role` (role_id),
                             CONSTRAINT `FK_users_roles_user_id` FOREIGN KEY (user_id) REFERENCES users (id),
                             CONSTRAINT `FK_users_roles_role_id` FOREIGN KEY (role_id) REFERENCES roles (id)
);