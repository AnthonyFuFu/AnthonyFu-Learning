CREATE DATABASE IF NOT EXISTS anthonyfu_learning;
USE anthonyfu_learning;

-- 如果表存在就刪掉
DROP TABLE IF EXISTS `member`;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS `s3_file`;

CREATE TABLE `member` (
 MEM_ID INT AUTO_INCREMENT NOT NULL PRIMARY KEY,
 MEM_NAME VARCHAR(45) NOT NULL,
 MEM_ACCOUNT VARCHAR(50) NOT NULL,
 MEM_PASSWORD VARCHAR(20) NOT NULL
);
-- 插入測試資料
INSERT INTO `member` (MEM_NAME, MEM_ACCOUNT, MEM_PASSWORD)
VALUES ('anthonyfu', 'anthonyfu', '123456');


CREATE TABLE `user` (
 USR_ID INT AUTO_INCREMENT NOT NULL PRIMARY KEY,
 USR_NAME VARCHAR(45) NOT NULL,
 USR_ACCOUNT VARCHAR(50) NOT NULL,
 USR_PASSWORD VARCHAR(20) NOT NULL
);
-- 插入測試資料
INSERT INTO `user` (USR_NAME, USR_ACCOUNT, USR_PASSWORD)
VALUES ('anthonyfu', 'anthonyfu', '123456');

CREATE TABLE `s3_file`
(
    `serial_id`        VARCHAR(64)      NOT NULL PRIMARY KEY,
    `node_type`        CHAR(5)          NOT NULL,
    `f_type`           VARCHAR(20)      NOT NULL,
    `f_path`           VARCHAR(500)     NOT NULL,
    `f_name`           VARCHAR(255)     NOT NULL,
    `f_save_name`      VARCHAR(255)     NULL,
    `f_size`           BIGINT UNSIGNED  NULL,
    `ref_id`           VARCHAR(64)      NULL,
    `access_start`     DATETIME         NULL,
    `start_mile`       DECIMAL(18,3)    NULL,
    `access_end`       DATETIME         NULL,
    `end_mile`         DECIMAL(18,3)    NULL,
    `access_type`      VARCHAR(20)      NULL,
    `access_user`      VARCHAR(50)      NULL,
    `over_write_id`    VARCHAR(64)      NULL,
    `release_user`     VARCHAR(50)      NULL,
    `release_user_name` VARCHAR(100)    NULL,
    `release_time`     DATETIME         NULL,
    `his_ref_id`       VARCHAR(64)      NULL,
    `virtual_path`     VARCHAR(500)     NULL,
    `source`           CHAR(1)          NULL
)