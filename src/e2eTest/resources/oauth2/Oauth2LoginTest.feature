Feature: OAuth2 Login Test

  Background:
    * url baseUrl
    * configure driver = { type: 'chrome' }

  Scenario: スコープwrite readを持ったトークンの作成

    * def apps =
    """
    {
    "client_name": "oauth2-test-client-1",
    "redirect_uris": "https://example.com",
    "scopes": "write read"
    }
    """

    Given path '/api/v1/apps'
    And request apps
    When method post
    Then status 200

    * def client_id = response.client_id
    * def client_secret = response.client_secret

    * def authorizeEndpoint = baseUrl + '/oauth/authorize?response_type=code&redirect_uri=https://example.com&client_id=' + client_id + '&scope=write read'

    Given driver authorizeEndpoint
    And driver.input('#username','test-user')
    And driver.input('#password','password')
    When driver.submit().click('body > div > form > button')
    Then match driver.title == 'test'
