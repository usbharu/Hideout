insert into "USERS" (ID, NAME, DOMAIN, SCREEN_NAME, DESCRIPTION, PASSWORD, INBOX, OUTBOX, URL, PUBLIC_KEY, PRIVATE_KEY,
                     CREATED_AT, KEY_ID, FOLLOWING, FOLLOWERS, INSTANCE)
VALUES (3, 'test-user3', 'example.com', 'Im test user3.', 'THis account is test user3.',
        '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8',
        'https://example.com/users/test-user3/inbox',
        'https://example.com/users/test-user3/outbox', 'https://example.com/users/test-user3',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        '-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----', 12345678,
        'https://example.com/users/test-user3#pubkey', 'https://example.com/users/test-user3/following',
        'https://example.com/users/test-user3/followers', null);

insert into POSTS (ID, "userId", OVERVIEW, TEXT, "createdAt", VISIBILITY, URL, "repostId", "replyId", SENSITIVE, AP_ID)
VALUES (1236, 3, null, 'test post', 12345680, 2, 'https://example.com/users/test-user3/posts/1236', null, null, false,
        'https://example.com/users/test-user3/posts/1236')
