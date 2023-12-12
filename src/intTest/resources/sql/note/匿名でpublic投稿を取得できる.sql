insert into actors (ID, NAME, DOMAIN, SCREEN_NAME, DESCRIPTION, INBOX, OUTBOX, URL, PUBLIC_KEY, PRIVATE_KEY,
                    CREATED_AT, KEY_ID, FOLLOWING, FOLLOWERS, INSTANCE, LOCKED)
VALUES (1, 'test-user', 'example.com', 'Im test user.', 'THis account is test user.',
        'https://example.com/users/test-user/inbox',
        'https://example.com/users/test-user/outbox', 'https://example.com/users/test-user',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        '-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----', 12345678,
        'https://example.com/users/test-user#pubkey', 'https://example.com/users/test-user/following',
        'https://example.com/users/test-users/followers', null, false);

insert into POSTS (ID, actor_id, OVERVIEW, TEXT, "CREATED_AT", VISIBILITY, URL, "REPOST_ID", "REPLY_ID", SENSITIVE,
                   AP_ID)
VALUES (1234, 1, null, 'test post', 12345680, 0, 'https://example.com/users/test-user/posts/1234', null, null, false,
        'https://example.com/users/test-user/posts/1234')
