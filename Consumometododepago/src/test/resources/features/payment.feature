Feature: Payment with Wompi API

  Scenario: Successful PSE Transaction Creation
    Given that the user has access to the Wompi API
    When querying the merchant information
    And creating a PSE transaction
    Then the response should be successful