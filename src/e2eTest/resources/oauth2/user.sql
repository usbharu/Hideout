insert into actors (ID, NAME, DOMAIN, SCREEN_NAME, DESCRIPTION, INBOX, OUTBOX, URL, PUBLIC_KEY, PRIVATE_KEY,
                    CREATED_AT, KEY_ID, FOLLOWING, FOLLOWERS, INSTANCE, LOCKED)
VALUES (1730415786666758144, 'test-user', 'localhost', 'Im test user.', 'THis account is test user.',
        'http://localhost/users/test-user/inbox',
        'http://localhost/users/test-user/outbox', 'http://localhost/users/test-user',
        '-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAi4mifRg6huAIn6DXk3Vn
5tkRC0AO32ZJvczwXr9xDj4HJvrSUHBAxIwwIeuCceAYtiuZk4JmEKydeB6SRkoO
Nty93XZXS1SMmiHCvWOY5YlpnfFU1kLqW3fkXcLNls4XmzujLt1i2sT8mYkENAsP
h6K4SRtmktOVYZOWcVEcfLGKbJvaDD/+lKikNC1XCouylfGV/bA/FPY5vuI+7cdM
Mjana28JdiWlPWSdzcxtCSgN+nGWPjk2WWm8K+wK2zXqMxA0U0p4odyyILBGALxX
zMqObIQvpwPh/t+b6ohem4eq70/0/SwDhd+IzHkT3x4UzG1oxSQS/juPkO7uuS8p
uwIDAQAB
-----END PUBLIC KEY-----
',
        '-----BEGIN PRIVATE KEY-----
MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCLiaJ9GDqG4Aif
oNeTdWfm2RELQA7fZkm9zPBev3EOPgcm+tJQcEDEjDAh64Jx4Bi2K5mTgmYQrJ14
HpJGSg423L3ddldLVIyaIcK9Y5jliWmd8VTWQupbd+Rdws2WzhebO6Mu3WLaxPyZ
iQQ0Cw+HorhJG2aS05Vhk5ZxURx8sYpsm9oMP/6UqKQ0LVcKi7KV8ZX9sD8U9jm+
4j7tx0wyNqdrbwl2JaU9ZJ3NzG0JKA36cZY+OTZZabwr7ArbNeozEDRTSnih3LIg
sEYAvFfMyo5shC+nA+H+35vqiF6bh6rvT/T9LAOF34jMeRPfHhTMbWjFJBL+O4+Q
7u65Lym7AgMBAAECggEADJLa7v3LbFLsxAGY22NFeRJPTF252VycwXshn9ANbnSd
bWBFqlTrKSrevXe82ekRIP09ygKCkvcS+3t5v9a1gDEU9MtQo2ubfdoT87/xS6G9
wCs6c1I1Twe3LtG6d9/bVbQiiLsPSNpeTrF/jPcAL780bvYGoK1rNQ85C7383Kl6
1nwZCD0itjkmzbO0nGMRCduW46OdQKiOMuEC7z0zwynH3cK3wGvdlKyLG4L3pPZm
1/Uz7AZTieqSCjSgcgmaut7dmS49e3j8ujfb3wcKscfHoofyqNWsW1xyU1WytO9a
QLh9wlqfvGlfwQWkY6z6uFmc4XfRVZSC8nic4cAW3QKBgQC4PYbR5AuylDcfc6Am
jpL5mcF6qEMnEPgnL9z5VvuLs1f/JEyx5VgzQreDOKc1KOxDX7Xhok4gpvIJv1fi
zimviszEmIpHdPvgS7mP2hu42bSIjwVaXpny5aEEZbB6HQ9pGDW/MSsgmb6x31Kx
o+sslpqf9cpalI35UPtkNaEJNwKBgQDB4tVUQ5gGPKllEfCN64B/B7wodWr5cUNU
UpUXdFPCu+HXnRen6GKLo+25wmCUGtcIuvCY1Xm+tL0Z7jrI+oOD4CL9ob7BJrPF
XCq0jUhaEzWFGp1SOa6n+35fWPkCfG4EwfsK8+PWoZsZc1eykMxIJmBln3vufuHz
qybfhy0VnQKBgD2tAxvyXmQar9VMjLk7k0IRUa6w80H5sUjVAgFKOA0NLZEQ4sfO
wdbvJ6W66mamW2k2ehmdjs/pcy8GKfKYF2ZXbbMGaYwAQm1UjDr2xb78yi3Iyv70
mk6wxlVFgW1vmwAQhbWKTSitryO2YeVrvUeA5yRTULk/78Mdc/qY5V7DAoGAAu3I
RzOWMlHsRSiWN66dDE4zm3DaotYBLF7q/aW2NjTcXoNy/ghWpMFfL/UtvE8DfJBG
XiirZCQazy94F90g63cRUD+HQCezg4G2629O7n1ny5DxW3Kfns3/xLT1XgI/Lzc2
8Z1pja53R1Ukt//T9isOPbrBBoNIKoQlXC8QkUkCgYEAsib3uOMAIOJab5jc8FSj
VG+Cg2H63J5DgUUwx2Y0DPENugdGyYzCDMVPBNaB0Ru1SpqbUjgqh+YHynunSVeu
hDXMOteeyeVHUGw8mvcCEt53uRYVNW/rzXTMqfLVxbsJZHCsJBtFpwcgD2w4NjS2
Ja15+ZWbOA4vJA9pOh3x4XM=
-----END PRIVATE KEY-----
', 1701398248417,
        'http://localhost/users/test-user#pubkey', 'http://localhost/users/test-user/following',
        'http://localhost/users/test-users/followers', null, false);

insert into user_details (actor_id, password, auto_accept_follow_request, auto_accept_followee_follow_request)
values ( 1730415786666758144
       , '$2a$10$/mWC/n7nC7X3l9qCEOKnredxne2zewoqEsJWTOdlKfg2zXKJ0F9Em', true, true)
