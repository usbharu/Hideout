create table if not exists filters
(
    id      bigint primary key not null,
    user_id bigint             not null,
    name    varchar(255)       not null,
    context varchar(500)       not null,
    action  varchar(255)       not null,
    constraint fk_filters_user_id__id foreign key (user_id) references actors (id) on delete cascade on update cascade
);

create table if not exists filter_keywords
(
    id        bigint primary key not null,
    filter_id bigint             not null,
    keyword   varchar(1000)      not null,
    mode      varchar(100)       not null,
    constraint fk_filter_keywords_filter_id__id foreign key (filter_id) references filters (id) on delete cascade on update cascade
);