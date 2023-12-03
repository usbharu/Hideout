Feature: Inbox Common Test

  Background:
    * url baseUrl

  Scenario: inboxにHTTP Signature付きのリクエストがあったらリモートに取得しに行く

    * url remoteUrl

    Given path '/internal-assertion-api/requests/deleteAll'
    When method post
    Then status 200

    * url baseUrl

    * def inbox =
    """
    { "type": "Follow" }
    """

    Given path `/inbox`
    And request inbox
#    And header Signature = 'keyId="'+ remoteUrl +'/users/test-user#pubkey", algorithm="rsa-sha256", headers="(request-target)", signature="a"'
    And header Signature = 'keyId="'+ remoteUrl +'/users/test-user#pubkey", algorithm="rsa-sha256", headers="(request-target) date host digest", signature="FfpkmBogW70FMo94yovGpl15L/m4bDjVIFb9mSZUstPE3H00nHiqNsjAq671qFMJsGOO1uWfLEExcdvzwTiC3wuHShzingvxQUbTgcgRTRZcHbtrOZxT8hYHGndpCXGv/NOLkfXDtZO9v5u0fnA2yJFokzyPHOPJ1cJliWlXP38Bl/pO4H5rBLQBZKpM2jYIjMyI78G2rDXNHEeGrGiyfB5SKb3H6zFQL+X9QpXUI4n0f07VsnwaDyp63oUopmzNUyBEuSqB+8va/lbfcWwrxpZnKGzQRZ+VBcV7jDoKGNOP9/O1xEI2CwB8sh+h6KVHdX3EQEvO1slaaLzcwRRqrQ=="'
    When method post
    Then status 202

    * def assertInbox = Java.type(`federation.InboxCommonTest`)

    And assertInbox.assertUserExist('test-user',remoteUrl)

    * url remoteUrl

    Given path '/internal-assertion-api/requests'
    When method get
    Then status 200

    * url baseUrl

    * print response
    Then match response.req == ['/users/test-user']


  Scenario: inboxにHTTP Signatureがないリクエストがきたら401を返す

    * def inbox =
    """
    {"type": "Follow"}
    """

    Given path '/inbox'
    And request inbox
    When method post
    Then status 401


  Scenario: user-inboxにHTTP Signature付きのリクエストがあったらリモートに取得しに行く

    * url remoteUrl

    Given path '/internal-assertion-api/requests/deleteAll'
    When method post
    Then status 200

    * url baseUrl

    * def inbox =
    """
    { "type": "Follow" }
    """

    Given path `/inbox`
    And request inbox
#    And header Signature = 'keyId="'+ remoteUrl +'/users/test-user#pubkey", algorithm="rsa-sha256", headers="(request-target)", signature="a"'
    And header Signature = 'keyId="'+ remoteUrl +'/users/test-user2#pubkey", algorithm="rsa-sha256", headers="(request-target) date host digest", signature="FfpkmBogW70FMo94yovGpl15L/m4bDjVIFb9mSZUstPE3H00nHiqNsjAq671qFMJsGOO1uWfLEExcdvzwTiC3wuHShzingvxQUbTgcgRTRZcHbtrOZxT8hYHGndpCXGv/NOLkfXDtZO9v5u0fnA2yJFokzyPHOPJ1cJliWlXP38Bl/pO4H5rBLQBZKpM2jYIjMyI78G2rDXNHEeGrGiyfB5SKb3H6zFQL+X9QpXUI4n0f07VsnwaDyp63oUopmzNUyBEuSqB+8va/lbfcWwrxpZnKGzQRZ+VBcV7jDoKGNOP9/O1xEI2CwB8sh+h6KVHdX3EQEvO1slaaLzcwRRqrQ=="'
    When method post
    Then status 202

    * def assertInbox = Java.type(`federation.InboxCommonTest`)

    And assertInbox.assertUserExist('test-user2',remoteUrl)


    * url remoteUrl

    Given path '/internal-assertion-api/requests'
    When method get
    Then status 200

    * url baseUrl

    * print response
    Then match response.req == ['/users/test-user2']

  Scenario: user-inboxにHTTP Signatureがないリクエストがきたら401を返す

    * def inbox =
    """
    {"type": "Follow"}
    """

    Given path '/inbox'
    And request inbox
    When method post
    Then status 401
