insert into "actors" (ID, NAME, DOMAIN, SCREEN_NAME, DESCRIPTION, INBOX, OUTBOX, URL, PUBLIC_KEY, PRIVATE_KEY,
                      CREATED_AT, KEY_ID, FOLLOWING, FOLLOWERS, INSTANCE, LOCKED)
VALUES (4, 'test-user4', 'example.com', 'Im test user4.', 'THis account is test user4.',
        'https://example.com/users/test-user4/inbox',
        'https://example.com/users/test-user4/outbox', 'https://example.com/users/test-user4',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        '-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----', 12345678,
        'https://example.com/users/test-user4#pubkey', 'https://example.com/users/test-user4/following',
        'https://example.com/users/test-user4/followers', null, false),
       (5, 'test-user5', 'follower.example.com', 'Im test user5.', 'THis account is test user5.',
        'https://follower.example.com/users/test-user5/inbox',
        'https://follower.example.com/users/test-user5/outbox', 'https://follower.example.com/users/test-user5',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        null, 12345678,
        'https://follower.example.com/users/test-user5#pubkey',
        'https://follower.example.com/users/test-user5/following',
        'https://follower.example.com/users/test-user5/followers', null, false);

insert into relationships (actor_id, target_actor_id, following, blocking, muting, follow_request,
                           ignore_follow_request)
VALUES (5, 4, true, false, false, false, false);

insert into POSTS (ID, "actor_ID", OVERVIEW, TEXT, "CREATED_AT", VISIBILITY, URL, "REPOST_ID", "REPLY_ID", SENSITIVE,
                   AP_ID)
VALUES (1237, 4, null, 'test post', 12345680, 0, 'https://example.com/users/test-user4/posts/1237', null, null, false,
        'https://example.com/users/test-user4/posts/1237');
