CREATE TABLE point_history
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    user_id       BIGINT NULL,
    point_type    VARCHAR(255) NULL,
    point_status  VARCHAR(255) NULL,
    created_at    datetime NULL,
    point_balance INT NOT NULL,
    CONSTRAINT pk_pointhistory PRIMARY KEY (id)
);

CREATE TABLE user
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    username      VARCHAR(255) NULL,
    point_balance INT NOT NULL,
    created_at    datetime NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

ALTER TABLE point_history
    ADD CONSTRAINT FK_POINTHISTORY_ON_USER FOREIGN KEY (user_id) REFERENCES user (id);