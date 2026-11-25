-- Baseline schema:
CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT UKr43af9ap4edm43mmtq01oddj6 UNIQUE (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
CREATE TABLE categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    user_id BIGINT NULL,
    PRIMARY KEY (id),
    CONSTRAINT UKn22vnmml6mjq0j6i24cavceas UNIQUE (name, user_id),
    CONSTRAINT FKghuylkwuedgl2qahxjt8g41kb FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
CREATE TABLE recurring_expenses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    active BIT(1) NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    description VARCHAR(255) NOT NULL,
    frequency_days INT NOT NULL,
    next_occurrence_date DATE NOT NULL,
    category_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FKspdf7mta85amm05yrxfxnnjeo FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT FKlhobw6se4bynpqqbgvjjob8d8 FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
CREATE TABLE expenses (
    id INT NOT NULL AUTO_INCREMENT,
    amount DECIMAL(10, 2) NOT NULL,
    expense_date DATE NOT NULL,
    description VARCHAR(255) NOT NULL,
    category_id BIGINT NOT NULL,
    recurring_expense_id BIGINT NULL,
    user_id BIGINT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FKjao23ohq935a4qrorwwsen0lr FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT FK60junaafroquw2xhfsg4htbe6 FOREIGN KEY (recurring_expense_id) REFERENCES recurring_expenses (id),
    CONSTRAINT FKhpk0n2cbnfiuu5nrgl0ika3hq FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
CREATE TABLE budgets (
    id BIGINT NOT NULL AUTO_INCREMENT,
    amount DECIMAL(10, 2) NOT NULL,
    category_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT UKi2tekvq944to55i83r6uwfelw UNIQUE (user_id, category_id),
    CONSTRAINT FKn7qib00712y8dwelmqfwis6ka FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT FKln0tm5tgf3f9q3sp9sa5m8m7b FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;