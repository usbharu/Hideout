insert into "USERS" (ID, NAME, DOMAIN, SCREEN_NAME, DESCRIPTION, PASSWORD, INBOX, OUTBOX, URL, PUBLIC_KEY, PRIVATE_KEY,
                     CREATED_AT, KEY_ID, FOLLOWING, FOLLOWERS)
VALUES (10, 'test-user10', 'example.com', 'Im test-user10.', 'THis account is test-user10.',
        '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8',
        'https://example.com/users/test-user10/inbox',
        'https://example.com/users/test-user10/outbox', 'https://example.com/users/test-user10',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        '-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----', 12345678,
        'https://example.com/users/test-user10#pubkey', 'https://example.com/users/test-user10/following',
        'https://example.com/users/test-user10/followers');

insert into POSTS (ID, "userId", OVERVIEW, TEXT, "createdAt", VISIBILITY, URL, "repostId", "replyId", SENSITIVE, AP_ID)
VALUES (1240, 10, null, 'test post', 12345680, 0, 'https://example.com/users/test-user10/posts/1240', null, null, false,
        'https://example.com/users/test-user10/posts/1240'),
       (1241, 10, null, 'test post', 12345680, 0, 'https://example.com/users/test-user10/posts/1241', null, 1240, false,
        'https://example.com/users/test-user10/posts/1241');
