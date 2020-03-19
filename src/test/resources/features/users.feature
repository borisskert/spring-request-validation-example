Feature: User functionalities

  Background:
    Given our spring application is running

  Scenario: Getting no users
    When I ask for a user with Id "6e9f59fa-cc85-4096-9165-7a3661fd6bc0"
    Then I should return no users

  Scenario: Getting users
    Given A user exists with ID
      | Username   | Email                      | Day of Birth   | ID                                   |
      | smithj     | john.smith@fakemail.com    | 1990-10-03     | 6e9f59fa-cc85-4096-9165-7a3661fd6bc0 |
    When I ask for a user with Id "6e9f59fa-cc85-4096-9165-7a3661fd6bc0"
    Then I should get following user
      | Username   | Email                      | Day of Birth   |
      | smithj     | john.smith@fakemail.com    | 1990-10-03     |

  Scenario: Create user with generated ID
    When I create a user
      | Username   | Email                      | Day of Birth   |
      | mustermann | max.musterman@fakemail.com | 1945-05-08     |
    And I get the user location after creation
    And I ask for the user by location
    Then The location should get following user
      | Username   | Email                      | Day of Birth   |
      | mustermann | max.musterman@fakemail.com | 1945-05-08     |
