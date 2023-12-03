Feature: InboxCommonMockServer

  Background:
    * def assertInbox = Java.type(`federation.InboxCommonTest`)
    * def req = {req: []}

  Scenario: pathMatches('/users/{username}') && methodIs('get')
    * def remoteUrl =  'http://localhost:' + assertInbox.getRemotePort()
    * def username = pathParams.username
    * def userUrl = remoteUrl + '/users/' + username


    * def person =
    """
{
  "@context": [
    "https://www.w3.org/ns/activitystreams",
    "https://w3id.org/security/v1",
    {
      "manuallyApprovesFollowers": "as:manuallyApprovesFollowers",
      "toot": "http://joinmastodon.org/ns#",
      "featured": {
        "@id": "toot:featured",
        "@type": "@id"
      },
      "featuredTags": {
        "@id": "toot:featuredTags",
        "@type": "@id"
      },
      "alsoKnownAs": {
        "@id": "as:alsoKnownAs",
        "@type": "@id"
      },
      "movedTo": {
        "@id": "as:movedTo",
        "@type": "@id"
      },
      "schema": "http://schema.org#",
      "PropertyValue": "schema:PropertyValue",
      "value": "schema:value",
      "discoverable": "toot:discoverable",
      "Device": "toot:Device",
      "Ed25519Signature": "toot:Ed25519Signature",
      "Ed25519Key": "toot:Ed25519Key",
      "Curve25519Key": "toot:Curve25519Key",
      "EncryptedMessage": "toot:EncryptedMessage",
      "publicKeyBase64": "toot:publicKeyBase64",
      "deviceId": "toot:deviceId",
      "claim": {
        "@type": "@id",
        "@id": "toot:claim"
      },
      "fingerprintKey": {
        "@type": "@id",
        "@id": "toot:fingerprintKey"
      },
      "identityKey": {
        "@type": "@id",
        "@id": "toot:identityKey"
      },
      "devices": {
        "@type": "@id",
        "@id": "toot:devices"
      },
      "messageFranking": "toot:messageFranking",
      "messageType": "toot:messageType",
      "cipherText": "toot:cipherText",
      "suspended": "toot:suspended",
      "focalPoint": {
        "@container": "@list",
        "@id": "toot:focalPoint"
      }
    }
  ],
  "id": #(userUrl),
  "type": "Person",
  "following": #(userUrl + '/following'),
  "followers": #(userUrl  + '/followers'),
  "inbox": #(userUrl  + '/inbox'),
  "outbox": #(userUrl  + '/outbox'),
  "featured": #(userUrl  + '/collections/featured'),
  "featuredTags": #(userUrl  + '/collections/tags'),
  "preferredUsername": #(username),
  "name": #(username),
  "summary": "E2E Test User Jaga/Cotlin/Winter Boot/Ktol\nYonTude: https://example.com\nY(Tvvitter): https://example.com\n",
  "url": #(userUrl + '/@' + username),
  "manuallyApprovesFollowers": false,
  "discoverable": true,
  "published": "2016-03-16T00:00:00Z",
  "devices": #(userUrl  + '/collections/devices'),
  "alsoKnownAs": [
     #( 'https://example.com/users/' + username)
  ],
  "publicKey": {
    "id": #(userUrl  + '#main-key'),
    "owner": #(userUrl),
    "publicKeyPem": "-----BEGIN PUBLIC KEY-----\nMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAvmtKo0xYGXR4M0LQQhK4\nBkKpRvvUxrqGV6Ew4CBHSyzdnbFsiBqHUWz4JvRiQvAPqQ4jQFpxVZCPr9xx6lJp\nx7EAAKIdVTnBBV4CYfu7QPsRqtjbB5408q+mo5oUXNs8xg2tcC42p2SJ5CRJX/fr\nOgCZwo3LC9pOBdCQZ+tiiPmWNBTNby99JZn4D/xNcwuhV04qcPoHYD9OPuxxGyzc\naVJ2mqJmvi/lewQuR8qnUIbz+Gik+xvyG6LmyuDoa1H2LDQfQXYb62G70HsYdu7a\ndObvZovytp+kkjP/cUaIYkhhOAYqAA4zCwVRY4NHK0MAMq9sMoUfNJa8U+zR9NvD\noQIDAQAB\n-----END PUBLIC KEY-----\n"
  },
  "tag": [],
  "attachment": [
    {
      "type": "PropertyValue",
      "name": "Pixib Fan-Bridge",
      "value": "\u003ca href=\"https://example.com/hideout\" target=\"_blank\" rel=\"nofollow noopener noreferrer me\"\u003e\u003cspan class=\"invisible\"\u003ehttps://www.\u003c/span\u003e\u003cspan class=\"\"\u003eexample.com/hideout\u003c/span\u003e\u003cspan class=\"invisible\"\u003e\u003c/span\u003e\u003c/a\u003e"
    },
    {
      "type": "PropertyValue",
      "name": "GitHub",
      "value": "\u003ca href=\"https://github.com/usbharu/hideout\" target=\"_blank\" rel=\"nofollow noopener noreferrer me\"\u003e\u003cspan class=\"invisible\"\u003ehttps://\u003c/span\u003e\u003cspan class=\"\"\u003egithub.com/usbharu/hideout\u003c/span\u003e\u003cspan class=\"invisible\"\u003e\u003c/span\u003e\u003c/a\u003e"
    }
  ],
  "endpoints": {
    "sharedInbox": #(userUrl + 'inbox')
  },
  "icon": {
    "type": "Image",
    "mediaType": "image/jpeg",
    "url": "https://upload.wikimedia.org/wikipedia/commons/thumb/3/30/Destroyer_Vozbuzhdenyy.jpg/320px-Destroyer_Vozbuzhdenyy.jpg"
  },
  "image": {
    "type": "Image",
    "mediaType": "image/jpeg",
    "url": "https://upload.wikimedia.org/wikipedia/commons/thumb/6/63/Views_of_Mount_Fuji_from_%C5%8Cwakudani_20211202.jpg/320px-Views_of_Mount_Fuji_from_%C5%8Cwakudani_20211202.jpg"
  }
}

    """
    * set req.req[] = '/users/' + username
    * def response = person

  Scenario: pathMatches('/internal-assertion-api/requests') && methodIs('get')
    * def response = req

  Scenario: pathMatches('/internal-assertion-api/requests/deleteAll') && methodIs('post')
    * set req.req = []
    * def responseStatus = 200
