insert into "actors" (ID, NAME, DOMAIN, SCREEN_NAME, DESCRIPTION, INBOX, OUTBOX, URL, PUBLIC_KEY, PRIVATE_KEY,
                      CREATED_AT, KEY_ID, FOLLOWING, FOLLOWERS, INSTANCE, LOCKED)
VALUES (3, 'test-user3', 'example.com', 'Im test user3.', 'THis account is test user3.',
        'https://example.com/users/test-user3/inbox',
        'https://example.com/users/test-user3/outbox', 'https://example.com/users/test-user3',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        '-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----', 12345678,
        'https://example.com/users/test-user3#pubkey', 'https://example.com/users/test-user3/following',
        'https://example.com/users/test-user3/followers', null, false);

insert into POSTS (ID, actor_id, OVERVIEW, TEXT, "CREATED_AT", VISIBILITY, URL, "REPOST_ID", "REPLY_ID", SENSITIVE,
                   AP_ID)
VALUES (1236, 3, null, 'test post', 12345680, 2, 'https://example.com/users/test-user3/posts/1236', null, null, false,
        'https://example.com/users/test-user3/posts/1236')
