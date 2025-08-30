-- DB 생성
CREATE DATABASE IF NOT EXISTS paypoint;

-- User 테이블 생성
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(10) NOT NULL,
    point_balance INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- PointHistory 테이블 생성
CREATE TABLE IF NOT EXISTS point_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY ,
    user_id BIGINT NOT NULL,
    point_status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_point_history_user FOREIGN KEY (user_id) REFERENCES user(id)
);