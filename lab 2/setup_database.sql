-- Mini Banking System Database Setup

CREATE DATABASE IF NOT EXISTS minibank;
USE minibank;

CREATE TABLE IF NOT EXISTS account (
    acc_no INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    balance DOUBLE NOT NULL
);

-- Optional: Insert sample data
-- INSERT INTO account VALUES (1001, 'John Doe', 5000.00);
-- INSERT INTO account VALUES (1002, 'Jane Smith', 3000.00);

SELECT 'Database and table created successfully!' AS Status;
