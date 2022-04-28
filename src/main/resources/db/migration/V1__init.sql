create table user_info
(
    check_name    varchar(16) primary key,
    token         varchar(512),
    email_address varchar(32)
);

create table check_config
(
    id          int primary key,
    host        varchar(256),
    check_url   varchar(256),
    login_url   varchar(256),
    main_param  varchar(512),
    ext_param   varchar(512),
    retry_count int
);

insert into check_config values (1, 'http://localhost:19020', 'https://h-api.jielong.co/api/Thread/EditCheckInRecord',
                                 'http://www.jielong.co/Portal/Login?key=', null, null, 99);