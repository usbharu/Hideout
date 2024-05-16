insert into filters (id, user_id, name, context, action)
VALUES (1, 1, 'test filter', 'home', 'warn');
insert into filter_keywords(id, filter_id, keyword, mode)
VALUES (1, 1, 'hoge', 'NONE')