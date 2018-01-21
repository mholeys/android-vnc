Feature: AddServer
  Add a server to a server list
  
  Scenario: Access add from action bar, and back
    Given I am on the ServerListActivity
    When I press add in the action bar
    And I navigate up
    Then I should see the ServerListActivity

  Scenario: Access add from action bar, and cancel
    Given I am on the ServerListActivity
    When I press add in the action bar
    And I press cancel
    Then I should see the ServerListActivity

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

#  Scenario Outline: Add server bad port
#    Given I am on the AddServerActivity
#    When I fill in name with <name>
#    And I fill in address with <address>
#    And I fill in port with <port>
#    And I fill in password with <password>
#    And I press Add
#    Then I should see the ServerListActivity
#    Then ServerList should contain a server with "<name>" "<address>" "<port>"
#    # TODO: change to failure
#
#    Examples:
#      | name      | address         | password    | port   |
#      | server    | server          | password    | a      |
#      | server    | 192.168.0.1     | password    | -1     |
#      | asdrvs    | 192.168.0.1     | password    | 999999 |

#  Scenario Outline: Add server no name
#    Given I am on the AddServerActivity
#    When I fill in name with <name>
#    And I fill in address with <address>
#    And I fill in port with <port>
#    And I fill in password with <password>
#    And I press Add
#    Then I should see the ServerListActivity
#    Then ServerList should contain a server with "<name>" "<address>" "<port>"
#    # TODO: change to failure
#
#    Examples:
#      | name      | address         | password    | port   |
#      |           | server          | password    | 5901   |
#      |           | 192.168.0.1     | password    | 5901   |
#      |           | 192.168.0.1     | password    | 5901   |

