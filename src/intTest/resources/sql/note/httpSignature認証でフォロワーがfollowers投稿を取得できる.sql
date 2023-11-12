insert into "USERS" (ID, NAME, DOMAIN, SCREEN_NAME, DESCRIPTION, PASSWORD, INBOX, OUTBOX, URL, PUBLIC_KEY, PRIVATE_KEY,
                     CREATED_AT, KEY_ID, FOLLOWING, FOLLOWERS)
VALUES (8, 'test-user8', 'example.com', 'Im test-user8.', 'THis account is test-user8.',
        '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8',
        'https://example.com/users/test-user8/inbox',
        'https://example.com/users/test-user8/outbox', 'https://example.com/users/test-user8',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        '-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----', 12345678,
        'https://example.com/users/test-user8#pubkey', 'https://example.com/users/test-user8/following',
        'https://example.com/users/test-user8/followers');

insert into "USERS" (ID, NAME, DOMAIN, SCREEN_NAME, DESCRIPTION, PASSWORD, INBOX, OUTBOX, URL, PUBLIC_KEY, PRIVATE_KEY,
                     CREATED_AT, KEY_ID, FOLLOWING, FOLLOWERS)
VALUES (9, 'test-user9', 'follower.example.com', 'Im test-user9.', 'THis account is test-user9.',
        null,
        'https://follower.example.com/users/test-user9/inbox',
        'https://follower.example.com/users/test-user9/outbox', 'https://follower.example.com/users/test-user9',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        null, 12345678,
        'https://follower.example.com/users/test-user9#pubkey',
        'https://follower.example.com/users/test-user9/following',
        'https://follower.example.com/users/test-user9/followers');

insert into USERS_FOLLOWERS (USER_ID, FOLLOWER_ID)
VALUES (8, 9);

insert into POSTS (ID, "userId", OVERVIEW, TEXT, "createdAt", VISIBILITY, URL, "repostId", "replyId", SENSITIVE, AP_ID)
VALUES (1239, 8, null, 'test post', 12345680, 2, 'https://example.com/users/test-user8/posts/1239', null, null, false,
        'https://example.com/users/test-user8/posts/1239');
