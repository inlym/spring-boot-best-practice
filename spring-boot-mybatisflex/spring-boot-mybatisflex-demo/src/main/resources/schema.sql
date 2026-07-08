-- ================================ 用户信息表 ================================
-- 用于演示 MyBatis-Flex 基础 ORM 操作

create table user_info
(
    -- ================================ 通用字段 ================================
    id          bigint unsigned not null auto_increment comment '主键 ID',
    create_time timestamp(6)    not null default current_timestamp(6) comment '创建时间',
    update_time timestamp(6)    not null default current_timestamp(6) on update current_timestamp(6) comment '更新时间',
    delete_time timestamp(6)    null     default null comment '删除时间',

    -- ================================ 业务字段 ================================
    username    varchar(100) not null comment '用户名',
    email       varchar(200)          default null comment '邮箱',
    age         int unsigned          default null comment '年龄',

    primary key (id)
);
