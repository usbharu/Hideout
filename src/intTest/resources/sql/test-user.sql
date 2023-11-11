insert into "USERS" (ID, NAME, DOMAIN, SCREEN_NAME, DESCRIPTION, PASSWORD, INBOX, OUTBOX, URL, PUBLIC_KEY, PRIVATE_KEY,
                     CREATED_AT, KEY_ID, FOLLOWING, FOLLOWERS)
VALUES (1, 'test-user', 'example.com', 'Im test user.', 'THis account is test user.',
        '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8', 'https://example.com/users/test-user/inbox',
        'https://example.com/users/test-user/outbox', 'https://example.com/users/test-user',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        '-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----', 12345678,
        'https://example.com/users/test-user#pubkey', 'https://example.com/users/test-user/following',
        'https://example.com/users/test-users/followers');
