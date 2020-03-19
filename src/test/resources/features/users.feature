Feature: User functionalities

  Background:
    Given our spring application is running

  Scenario: Getting no users
    When I ask for a user with Id "6e9f59fa-cc85-4096-9165-7a3661fd6bc0"
    Then I should return no users

  Scenario: Getting users
    And A user has been created with ID
      | Username   | Email                      | Day of Birth   | ID                                   |
      | smithj     | john.smith@fakemail.com    | 1990-10-03     | 6e9f59fa-cc85-4096-9165-7a3661fd6bc0 |
    When I ask for a user with Id "6e9f59fa-cc85-4096-9165-7a3661fd6bc0"
    Then I should get following user
      | Username   | Email                      | Day of Birth   |
      | smithj     | john.smith@fakemail.com    | 1990-10-03     |
