create table if not exists instance
(
    id              bigint primary key,
    "name"          varchar(1000)  not null,
    description     varchar(5000)  not null,
    url          varchar(255) not null unique,
    icon_url        varchar(255)   not null,
    shared_inbox varchar(255) null unique,
    software        varchar(255)   not null,
    version         varchar(255)   not null,
    is_blocked      boolean        not null,
    is_muted        boolean        not null,
    moderation_note varchar(10000) not null,
    created_at      timestamp      not null
);
create table if not exists actors
(
    id          bigint primary key,
    "name"      varchar(300)   not null,
    "domain"    varchar(1000)  not null,
    screen_name varchar(300)   not null,
    description varchar(10000) not null,
    inbox  varchar(1000) not null unique,
    outbox varchar(1000) not null unique,
    url    varchar(1000) not null unique,
    public_key  varchar(10000) not null,
    private_key varchar(10000) null,
    created_at  bigint         not null,
    key_id      varchar(1000)  not null,
    "following" varchar(1000)  null,
    followers   varchar(1000)  null,
    "instance"  bigint         null,
    unique ("name", "domain"),
    constraint fk_actors_instance__id foreign key ("instance") references instance (id) on delete restrict on update restrict
);

create table if not exists user_details
(
    id                                  bigserial primary key,
    actor_id                            bigint       not null unique,
    password varchar(255) not null,
    auto_accept_follow_request          boolean      not null,
    auto_accept_followee_follow_request boolean      not null,
    constraint fk_user_details_actor_id__id foreign key (actor_id) references actors (id) on delete restrict on update restrict
);

create table if not exists media
(
    id            bigint primary key,
    "name"        varchar(255)  not null,
    url           varchar(255) not null unique,
    remote_url    varchar(255) null unique,
    thumbnail_url varchar(255) null unique,
    "type"        int           not null,
    blurhash      varchar(255)  null,
    mime_type     varchar(255)  not null,
    description   varchar(4000) null
);
create table if not exists meta_info
(
    id              bigint primary key,
    version         varchar(1000)   not null,
    kid             varchar(1000)   not null,
    jwt_private_key varchar(100000) not null,
    jwt_public_key  varchar(100000) not null
);
create table if not exists posts
(
    id          bigint primary key,
    actor_id bigint       not null,
    overview    varchar(100)          null,
    text        varchar(3000)         not null,
    created_at  bigint                not null,
    visibility  int     default 0     not null,
    url         varchar(500)          not null,
    repost_id   bigint                null,
    reply_id    bigint                null,
    "sensitive" boolean default false not null,
    ap_id    varchar(100) not null unique
);
alter table posts
    add constraint fk_posts_actor_id__id foreign key (actor_id) references actors (id) on delete restrict on update restrict;
alter table posts
    add constraint fk_posts_repostid__id foreign key (repost_id) references posts (id) on delete restrict on update restrict;
alter table posts
    add constraint fk_posts_replyid__id foreign key (reply_id) references posts (id) on delete restrict on update restrict;
create table if not exists posts_media
(
    post_id  bigint,
    media_id bigint,
    constraint pk_postsmedia primary key (post_id, media_id)
);
alter table posts_media
    add constraint fk_posts_media_post_id__id foreign key (post_id) references posts (id) on delete cascade on update cascade;
alter table posts_media
    add constraint fk_posts_media_media_id__id foreign key (media_id) references media (id) on delete cascade on update cascade;
create table if not exists reactions
(
    id       bigserial primary key,
    emoji_id bigint not null,
    post_id  bigint not null,
    actor_id bigint not null
);
alter table reactions
    add constraint fk_reactions_post_id__id foreign key (post_id) references posts (id) on delete restrict on update restrict;
alter table reactions
    add constraint fk_reactions_actor_id__id foreign key (actor_id) references actors (id) on delete restrict on update restrict;
create table if not exists timelines
(
    id             bigint primary key,
    user_id        bigint       not null,
    timeline_id    bigint       not null,
    post_id        bigint       not null,
    post_actor_id bigint not null,
    created_at     bigint       not null,
    reply_id       bigint       null,
    repost_id      bigint       null,
    visibility     int          not null,
    "sensitive"    boolean      not null,
    is_local       boolean      not null,
    is_pure_repost boolean      not null,
    media_ids      varchar(255) not null
);

create table if not exists application_authorization
(
    id                            varchar(255) primary key,
    registered_client_id          varchar(255)               not null,
    principal_name                varchar(255)               not null,
    authorization_grant_type      varchar(255)               not null,
    authorized_scopes             varchar(1000) default null null,
    "attributes"                  varchar(4000) default null null,
    "state"                       varchar(500)  default null null,
    authorization_code_value      varchar(4000) default null null,
    authorization_code_issued_at  timestamp     default null null,
    authorization_code_expires_at timestamp     default null null,
    authorization_code_metadata   varchar(2000) default null null,
    access_token_value            varchar(4000) default null null,
    access_token_issued_at        timestamp     default null null,
    access_token_expires_at       timestamp     default null null,
    access_token_metadata         varchar(2000) default null null,
    access_token_type             varchar(255)  default null null,
    access_token_scopes           varchar(1000) default null null,
    refresh_token_value           varchar(4000) default null null,
    refresh_token_issued_at       timestamp     default null null,
    refresh_token_expires_at      timestamp     default null null,
    refresh_token_metadata        varchar(2000) default null null,
    oidc_id_token_value           varchar(4000) default null null,
    oidc_id_token_issued_at       timestamp     default null null,
    oidc_id_token_expires_at      timestamp     default null null,
    oidc_id_token_metadata        varchar(2000) default null null,
    oidc_id_token_claims          varchar(2000) default null null,
    user_code_value               varchar(4000) default null null,
    user_code_issued_at           timestamp     default null null,
    user_code_expires_at          timestamp     default null null,
    user_code_metadata            varchar(2000) default null null,
    device_code_value             varchar(4000) default null null,
    device_code_issued_at         timestamp     default null null,
    device_code_expires_at        timestamp     default null null,
    device_code_metadata          varchar(2000) default null null
);
create table if not exists oauth2_authorization_consent
(
    registered_client_id varchar(100),
    principal_name       varchar(200),
    authorities          varchar(1000) not null,
    constraint pk_oauth2_authorization_consent primary key (registered_client_id, principal_name)
);
create table if not exists registered_client
(
    id                            varchar(100) primary key,
    client_id                     varchar(100)                            not null,
    client_id_issued_at           timestamp     default current_timestamp not null,
    client_secret                 varchar(200)  default null              null,
    client_secret_expires_at      timestamp     default null              null,
    client_name                   varchar(200)                            not null,
    client_authentication_methods varchar(1000)                           not null,
    authorization_grant_types     varchar(1000)                           not null,
    redirect_uris                 varchar(1000) default null              null,
    post_logout_redirect_uris     varchar(1000) default null              null,
    scopes                        varchar(1000)                           not null,
    client_settings               varchar(2000)                           not null,
    token_settings                varchar(2000)                           not null
);

create table if not exists relationships
(
    id                    bigserial primary key,
    actor_id        bigint not null,
    target_actor_id bigint not null,
    following             boolean not null,
    blocking              boolean not null,
    muting                boolean not null,
    follow_request        boolean not null,
    ignore_follow_request boolean not null,
    constraint fk_relationships_actor_id__id foreign key (actor_id) references actors (id) on delete restrict on update restrict,
    constraint fk_relationships_target_actor_id__id foreign key (target_actor_id) references actors (id) on delete restrict on update restrict,
    unique (actor_id, target_actor_id)
)
