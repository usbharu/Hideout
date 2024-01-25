insert into actors (id, name, domain, screen_name, description, inbox, outbox, url, public_key, private_key, created_at,
                    key_id, following, followers, instance, locked, following_count, followers_count, posts_count,
                    last_post_at)
VALUES (1, 'test-user', 'example.com', 'Im test user.', 'THis account is test user.',
        'https://example.com/users/test-user/inbox',
        'https://example.com/users/test-user/outbox', 'https://example.com/users/test-user',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        '-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----', 12345678,
        'https://example.com/users/test-user#pubkey', 'https://example.com/users/test-user/following',
        'https://example.com/users/test-users/followers', null, false, 0, 0, 0, null);

insert into POSTS (id, actor_id, overview, content, text, created_at, visibility, url, repost_id, reply_id, sensitive,
                   ap_id,
                   deleted)
VALUES (1234, 1, null, '<p>test post</p>', 'test post', 12345680, 0, 'https://example.com/users/test-user/posts/1234',
        null, null, false,
        'https://example.com/users/test-user/posts/1234', false)
