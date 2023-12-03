Feature: Follow Accept Test

  Background:
    * url baseUrl
    * def assertionUtil = Java.type('AssertionUtil')

  Scenario: Follow Accept Test

    * def follow =
    """
    {"type": "Follow","actor": #(remoteUrl + '/users/test-follower'),"object": #(baseUrl + '/users/test-user')}
    """

    Given path '/inbox'
    And header Signature = 'keyId="https://test-hideout.usbharu.dev/users/c#pubkey", algorithm="rsa-sha256", headers="x-request-id tpp-redirect-uri digest psu-id", signature="e/91pFiI5bRffP33EMrqoI5A0xjkg3Ar0kzRGHC/1RsLrDW0zV50dHly/qJJ5xrYHRlss3+vd0mznTLBs1X0hx0uXjpfvCvwclpSi8u+sqn+Y2bcQKzf7ah0vAONQd6zeTYW7e/1kDJreP43PsJyz29KZD16Yop8nM++YeEs6C5eWiyYXKoQozXnfmTOX3y6bhxfKKQWVcxA5aLOTmTZRYTsBsTy9zn8NjDQaRI/0gcyYPqpq+2g8j2DbyJu3Z6zP6VmwbGGlQU/s9Pa7G5LqUPH/sBMSlIeqh+Hvm2pL7B3/BMFvGtTD+e2mR60BFnLIxMYx+oX4o33J2XkFIODLQ=="'
    And request follow
    When method post
    Then status 202

    And retry until assertionUtil.assertUserExist('test-follower',remoteUrl)

    * url remoteUrl

    Given path '/internal-assertion-api/requests'
    When method get
    Then status 200
    And match response.req contains ['/users/test-follower']

    * url baseUrl
