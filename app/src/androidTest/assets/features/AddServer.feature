Feature: AddServer
  Add a server to a server list

  Scenario: Access add from action bar
    Given I am on the ServerListActivity
    When I press add in the action bar
    Then I should see the AddServerActivity

  Scenario Outline: Add server
    Given I am on the AddServerActivity
    When I fill in name with <name>
    And I fill in address with <address>
    And I fill in port with <port>
    And I fill in password with <password>
    And I press Add
    Then I should see the ServerListActivity
    Then ServerList should contain a server with "<name>" "<address>" "<port>"

    Examples:
      | name      | address         | password    | port |
      | server    | server          | password    | 5900 |
      | server    | 192.168.0.1     | password    | 5900 |
      | asdrvs    | 192.168.0.1     | password    | 5900 |