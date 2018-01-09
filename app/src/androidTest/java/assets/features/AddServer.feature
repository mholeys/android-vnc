Feature: AddServer
  Add a server to a server list

  Scenario Outline: Access the add server activity from action bar
    Given I am on the ServerListActivity
    When I press add in the action bar
    Then I should see the AddServerActivity

    Examples:

