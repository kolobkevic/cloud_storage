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