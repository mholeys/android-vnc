Feature: AddServer
  Add a server to a server list

  Scenario: Access add from action bar
      Given I am on the ServerListActivity
      When I press add in the action bar
      Then I should see the AddServerActivity