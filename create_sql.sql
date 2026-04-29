-- 创建库
create database if not exists chronicle;
use chronicle;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userName     varchar(256)                           null comment '用户昵称',
    userAvatar   varchar(1024)                          null comment '用户头像',
    userProfile  varchar(512)                           null comment '用户简介',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin',
    editTime     datetime     default CURRENT_TIMESTAMP not null comment '编辑时间',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    UNIQUE KEY uk_userAccount (userAccount),
    INDEX idx_userName (userName)
) comment '用户' collate = utf8mb4_unicode_ci;

-- 表名: entries (记录表)
CREATE TABLE IF NOT EXISTS entries
(
    id         bigint PRIMARY KEY AUTO_INCREMENT COMMENT 'id',
    userId     bigint                             not null comment '用户ID',
    content    varchar(512)                       NOT NULL COMMENT '记录内容',
    entryType  varchar(20)                        NOT NULL COMMENT '记录类型 枚举值:Do\Idea\Think\Rule',
    checked    tinyint  default 0                 not null comment '是否勾选',
    completionRate tinyint default 0              not null comment '完成百分比(0-100)',
    createTime datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete   tinyint  default 0                 not null comment '是否删除',
    UNIQUE KEY uk_id (id),
    INDEX idx_userId (userId),
    INDEX idx_entryType (entryType),
    INDEX idx_createTime (createTime DESC),
    INDEX idx_userId_entryType (userId, entryType)
) comment '记录表' collate = utf8mb4_unicode_ci;

