insert into "actors" (ID, NAME, DOMAIN, SCREEN_NAME, DESCRIPTION, INBOX, OUTBOX, URL, PUBLIC_KEY, PRIVATE_KEY,
                      CREATED_AT, KEY_ID, FOLLOWING, FOLLOWERS, INSTANCE, LOCKED)
VALUES (2, 'test-user2', 'example.com', 'Im test user.', 'THis account is test user.',
        'https://example.com/users/test-user2/inbox',
        'https://example.com/users/test-user2/outbox', 'https://example.com/users/test-user2',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        '-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----', 12345678,
        'https://example.com/users/test-user2#pubkey', 'https://example.com/users/test-user2/following',
        'https://example.com/users/test-user2s/followers', null, false);
