create table if not exists blocks
(
    id      bigserial primary key,
    user_id bigint not null,
    target  bigint not null,
    constraint fk_blocks_user_id__id foreign key (user_id) references users (id) on delete restrict on update restrict,
    constraint fk_blocks_target_id__id foreign key (target) references users (id) on delete restrict on update restrict
);
