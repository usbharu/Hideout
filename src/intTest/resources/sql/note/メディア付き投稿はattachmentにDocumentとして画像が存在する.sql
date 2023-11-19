insert into "USERS" (ID, NAME, DOMAIN, SCREEN_NAME, DESCRIPTION, PASSWORD, INBOX, OUTBOX, URL, PUBLIC_KEY, PRIVATE_KEY,
                     CREATED_AT, KEY_ID, FOLLOWING, FOLLOWERS, INSTANCE)
VALUES (11, 'test-user11', 'example.com', 'Im test-user11.', 'THis account is test-user11.',
        '5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8',
        'https://example.com/users/test-user11/inbox',
        'https://example.com/users/test-user11/outbox', 'https://example.com/users/test-user11',
        '-----BEGIN PUBLIC KEY-----...-----END PUBLIC KEY-----',
        '-----BEGIN PRIVATE KEY-----...-----END PRIVATE KEY-----', 12345678,
        'https://example.com/users/test-user11#pubkey', 'https://example.com/users/test-user11/following',
        'https://example.com/users/test-user11/followers', null);

insert into POSTS (ID, "USER_ID", OVERVIEW, TEXT, "CREATED_AT", VISIBILITY, URL, "REPOST_ID", "REPLY_ID", SENSITIVE,
                   AP_ID)
VALUES (1242, 11, null, 'test post', 12345680, 0, 'https://example.com/users/test-user11/posts/1242', null, null, false,
        'https://example.com/users/test-user11/posts/1242');

insert into MEDIA (ID, NAME, URL, REMOTE_URL, THUMBNAIL_URL, TYPE, BLURHASH, MIME_TYPE, DESCRIPTION)
VALUES (1, 'test-media', 'https://example.com/media/test-media.png', null, null, 0, null, 'image/png', null),
       (2, 'test-media2', 'https://example.com/media/test-media2.png', null, null, 0, null, 'image/png', null);

insert into POSTS_MEDIA(POST_ID, MEDIA_ID)
VALUES (1242, 1),
       (1242, 2);
