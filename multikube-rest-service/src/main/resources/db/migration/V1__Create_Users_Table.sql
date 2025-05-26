-- Table to represent Tenant organizations/entities
CREATE TABLE tenants
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(255) NOT NULL UNIQUE, -- Unique name for the tenant organization (e.g., "System", "Customer A Inc.")
    description TEXT,
    is_active   BOOLEAN   DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert the "System" tenant
INSERT INTO tenants (name, description, is_active)
VALUES ('System', 'Internal system tenant for provider administrators and system operations.', TRUE);

-- Table for Users
CREATE TABLE users
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id  BIGINT       NOT NULL,                                                            -- Foreign key to the tenants table, now NOT NULL
    username   VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active  BOOLEAN   DEFAULT TRUE,
    CONSTRAINT fk_user_tenant FOREIGN KEY (tenant_id) REFERENCES tenants (id) ON DELETE RESTRICT -- Prevent tenant deletion if users are still assigned
);

CREATE TABLE user_secrets
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id  BIGINT       NOT NULL,
    password VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Table for Roles
CREATE TABLE roles
(
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE -- e.g., 'PROVIDER_ADMIN', 'TENANT_ADMIN', 'TENANT_USER'
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

-- Seed initial roles
INSERT INTO roles (name)
VALUES ('PROVIDER_ADMIN');
INSERT INTO roles (name)
VALUES ('TENANT_ADMIN');
INSERT INTO roles (name)
VALUES ('TENANT_USER');

-- Seed initial admin user, now associated with the "System" tenant
INSERT INTO users (tenant_id, username, email)
VALUES ((SELECT id from tenants WHERE name = 'System'),
        'admin',
        'admin@multikube.com');

-- Seed admin user's password
-- Assuming 'password' is hashed using a strong hashing algorithm like BCrypt.
-- The example hash is for the password "password". Replace with a strong, unique password for production.
INSERT INTO user_secrets (user_id, password)
VALUES ((SELECT id FROM users WHERE username = 'admin'),
        '$2a$10$am41//PgOLSm5PslDIFfCeXgx.Aope4LpZD/S6EyIAd5V2iKu5BN2' -- Example hash for "password"
       );

-- Assign 'PROVIDER_ADMIN' role to the admin user
INSERT INTO user_roles (user_id, role_id)
VALUES ((SELECT id FROM users WHERE username = 'admin'),
        (SELECT id FROM roles WHERE name = 'PROVIDER_ADMIN'));