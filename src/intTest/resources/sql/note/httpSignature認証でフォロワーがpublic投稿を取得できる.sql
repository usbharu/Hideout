insert into "USERS" (ID, NAME, DOMAIN, SCREEN_NAME, DESCRIPTION, PASSWORD, INBOX, OUTBOX, URL, PUBLIC_KEY, PRIVATE_KEY,
                     CREATED_AT, KEY_ID, FOLLOWING, FOLLOWERS, INSTANCE)
VALUES (4, 'test-user4', 'example.com', 'Im test user4.', 'THis account is test user4.',
        '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8',
        'https://example.com/users/test-user4/inbox',
        'https://example.com/users/test-user4/outbox', 'https://example.com/users/test-user4',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        '-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----', 12345678,
        'https://example.com/users/test-user4#pubkey', 'https://example.com/users/test-user4/following',
        'https://example.com/users/test-user4/followers', null);

insert into "USERS" (ID, NAME, DOMAIN, SCREEN_NAME, DESCRIPTION, PASSWORD, INBOX, OUTBOX, URL, PUBLIC_KEY, PRIVATE_KEY,
                     CREATED_AT, KEY_ID, FOLLOWING, FOLLOWERS, INSTANCE)
VALUES (5, 'test-user5', 'follower.example.com', 'Im test user5.', 'THis account is test user5.',
        null,
        'https://follower.example.com/users/test-user5/inbox',
        'https://follower.example.com/users/test-user5/outbox', 'https://follower.example.com/users/test-user5',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        null, 12345678,
        'https://follower.example.com/users/test-user5#pubkey',
        'https://follower.example.com/users/test-user5/following',
        'https://follower.example.com/users/test-user5/followers', null);

insert into USERS_FOLLOWERS (USER_ID, FOLLOWER_ID)
VALUES (4, 5);

insert into POSTS (ID, "userId", OVERVIEW, TEXT, "createdAt", VISIBILITY, URL, "repostId", "replyId", SENSITIVE, AP_ID)
VALUES (1237, 4, null, 'test post', 12345680, 0, 'https://example.com/users/test-user4/posts/1237', null, null, false,
        'https://example.com/users/test-user4/posts/1237');
