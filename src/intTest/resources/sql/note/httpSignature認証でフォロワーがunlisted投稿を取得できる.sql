insert into "USERS" (ID, NAME, DOMAIN, SCREEN_NAME, DESCRIPTION, PASSWORD, INBOX, OUTBOX, URL, PUBLIC_KEY, PRIVATE_KEY,
                     CREATED_AT, KEY_ID, FOLLOWING, FOLLOWERS, INSTANCE)
VALUES (6, 'test-user6', 'example.com', 'Im test-user6.', 'THis account is test-user6.',
        '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8',
        'https://example.com/users/test-user6/inbox',
        'https://example.com/users/test-user6/outbox', 'https://example.com/users/test-user6',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        '-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----', 12345678,
        'https://example.com/users/test-user6#pubkey', 'https://example.com/users/test-user6/following',
        'https://example.com/users/test-user6/followers', null);

insert into "USERS" (ID, NAME, DOMAIN, SCREEN_NAME, DESCRIPTION, PASSWORD, INBOX, OUTBOX, URL, PUBLIC_KEY, PRIVATE_KEY,
                     CREATED_AT, KEY_ID, FOLLOWING, FOLLOWERS, INSTANCE)
VALUES (7, 'test-user7', 'follower.example.com', 'Im test-user7.', 'THis account is test-user7.',
        null,
        'https://follower.example.com/users/test-user7/inbox',
        'https://follower.example.com/users/test-user7/outbox', 'https://follower.example.com/users/test-user7',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        null, 12345678,
        'https://follower.example.com/users/test-user7#pubkey',
        'https://follower.example.com/users/test-user7/following',
        'https://follower.example.com/users/test-user7/followers', null);

insert into relationships (user_id, target_user_id, following, blocking, muting, follow_request,
                           ignore_follow_request)
VALUES (7, 6, true, false, false, false, false);

insert into POSTS (ID, "USER_ID", OVERVIEW, TEXT, "CREATED_AT", VISIBILITY, URL, "REPOST_ID", "REPLY_ID", SENSITIVE,
                   AP_ID)
VALUES (1238, 6, null, 'test post', 12345680, 1, 'https://example.com/users/test-user6/posts/1238', null, null, false,
        'https://example.com/users/test-user6/posts/1238');
