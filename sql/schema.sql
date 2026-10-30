-- Database is created automatically (createDatabaseIfNotExist=true) and the tables by
-- Hibernate (ddl-auto=update). The DDL below is what Hibernate generates, for reference.
CREATE DATABASE IF NOT EXISTS employeedb;
USE employeedb;

CREATE TABLE IF NOT EXISTS id_cards (
  card_id     BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  card_number VARCHAR(30) NOT NULL,
  issue_date  DATE        NOT NULL,
  CONSTRAINT uk_id_cards_card_number UNIQUE (card_number)
);

-- One-to-One: employees.card_id is NOT NULL (every employee has a card) and
-- UNIQUE (a card belongs to only one employee).
CREATE TABLE IF NOT EXISTS employees (
  employee_id   BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  employee_name VARCHAR(100) NOT NULL,
  department    VARCHAR(50)  NOT NULL,
  card_id       BIGINT       NOT NULL,
  CONSTRAINT uk_employees_card_id UNIQUE (card_id),
  CONSTRAINT fk_employees_id_card FOREIGN KEY (card_id) REFERENCES id_cards (card_id)
);
