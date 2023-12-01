Feature: test

  Background:
    * url baseUrl

  Scenario: test
    Given path '/api/v1/apps'
    When method get
    Then status 401
