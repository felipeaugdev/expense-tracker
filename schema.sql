-- Expense Tracker Database Schema Setup
CREATE DATABASE IF NOT EXISTS expense_tracker;
USE expense_tracker;
CREATE TABLE IF NOT EXISTS expenses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    amount DECIMAL(10, 2) NOT NULL,
    expense_date DATE NOT NULL,
    description VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL
);