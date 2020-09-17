drop table if exists account;

create table account (
    account_id uuid not null primary key,
    username varchar(320) not null unique,
    password varchar(100) not null,
    email varchar(320) not null unique
);
