Feature: OAuth2 Login Test

  Background:
    * url baseUrl
    * configure driver = { type: 'chrome',start: true, headless: true, showDriverLog: true, addOptions: [ '--headless=new' ] }

  Scenario: スコープwrite readを持ったトークンの作成

    * def apps =
    """
    {
    "client_name": "oauth2-test-client-1",
    "redirect_uris": "https://usbharu.dev",
    "scopes": "write read"
    }
    """

    Given path '/api/v1/apps'
    And request apps
    When method post
    Then status 200

    * def client_id = response.client_id
    * def client_secret = response.client_secret

    * def authorizeEndpoint = baseUrl + '/oauth/authorize?response_type=code&redirect_uri=https://usbharu.dev&client_id=' + client_id + '&scope=write%20read'

    Given driver authorizeEndpoint
    And driver.input('#username','test-user')
    And driver.input('#password','password')

    When driver.submit().click('body > div > form > button')
    Then driver.waitForUrl(authorizeEndpoint + "&continue")
    And driver.click('#read')
    And driver.click('#write')

    When driver.submit().click('#submit-consent')
    Then driver.waitUntil("location.host == 'usbharu.dev'")

    * def code = script("new URLSearchParams(document.location.search).get('code')")

    Given path '/oauth/token'
    And form field client_id = client_id
    And form field client_secret = client_secret
    And form field redirect_uri = 'https://usbharu.dev'
    And form field grant_type = 'authorization_code'
    And form field code = code
    And form field scope = 'write read'
    When method post
    Then status 200

  Scenario: スコープread:statuses write:statusesを持ったトークンの作成

    * def apps =
    """
    {
    "client_name": "oauth2-test-client-2",
    "redirect_uris": "https://usbharu.dev",
    "scopes": "read:statuses write:statuses"
    }
    """

    Given path '/api/v1/apps'
    And request apps
    When method post
    Then status 200

    * def client_id = response.client_id
    * def client_secret = response.client_secret

    * def authorizeEndpoint = baseUrl + '/oauth/authorize?response_type=code&redirect_uri=https://usbharu.dev&client_id=' + client_id + '&scope=read:statuses+write:statuses'

    Given driver authorizeEndpoint
    And driver.input('#username','test-user')
    And driver.input('#password','password')

    When driver.submit().click('body > div > form > button')
    Then driver.waitForUrl(authorizeEndpoint + "&continue")
    And driver.click('/html/body/div/div[4]/div/form/div[1]/input')
    And driver.click('/html/body/div/div[4]/div/form/div[2]/input')

    When driver.submit().click('#submit-consent')
    Then driver.waitUntil("location.host == 'usbharu.dev'")

    * def code = script("new URLSearchParams(document.location.search).get('code')")

    Given path '/oauth/token'
    And form field client_id = client_id
    And form field client_secret = client_secret
    And form field redirect_uri = 'https://usbharu.dev'
    And form field grant_type = 'authorization_code'
    And form field code = code
    And form field scope = 'write read'
    When method post
    Then status 200
