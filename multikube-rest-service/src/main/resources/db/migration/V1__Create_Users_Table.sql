CREATE TABLE users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    username   VARCHAR(50)  NOT NULL UNIQUE, -- Added UNIQUE constraint for username
    email      VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted    BOOLEAN   DEFAULT FALSE
);

CREATE TABLE user_secrets
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id  BIGINT       NOT NULL,
    password VARCHAR(255) NOT NULL,
    deleted  BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Table for Roles
CREATE TABLE roles
(
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE -- e.g., 'PROVIDER_ADMIN', 'TENANT_USER'
);

-- Junction table for many-to-many relationship between users and roles
CREATE TABLE user_roles
(
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

INSERT INTO roles (name) VALUES ('PROVIDER_ADMIN');
INSERT INTO roles (name) VALUES ('TENANT_ADMIN');
INSERT INTO roles (name) VALUES ('TENANT_USER');

INSERT INTO users (username, email) VALUES ('admin', 'admin@multikube.com');

-- Assuming 'password' is hashed using a strong hashing algorithm like BCrypt
INSERT INTO user_secrets (user_id, password) VALUES ( (SELECT id FROM users WHERE username = 'admin'), '$2a$10$am41//PgOLSm5PslDIFfCeXgx.Aope4LpZD/S6EyIAd5V2iKu5BN2');

INSERT INTO user_roles (user_id, role_id)
VALUES (
           (SELECT id FROM users WHERE username = 'admin'),
           (SELECT id FROM roles WHERE name = 'PROVIDER_ADMIN')
       );