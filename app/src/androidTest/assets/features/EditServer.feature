Feature: AddServer
  Edit a server in the server list

  Scenario Outline: Get to the edit activity by via context menu, and back
    Given I am on the ServerListActivity
    And ServerList should contain a server with "<name>" "<address>" "<port>"
    When I long press <name> in the Server list
    And I press Edit
    Then I should see the EditServerActivity
    And I go back

    Examples:
    | name      | address         | password    | port |
    | server2   | server          | password    | 5900 |
    | server3   | 192.168.0.1     | password    | 5900 |
    | asdrvs    | 192.168.0.1     | password    | 5900 |

  Scenario Outline: Get to the edit activity by via context menu, and cancel
    Given I am on the ServerListActivity
    And ServerList should contain a server with "<name>" "<address>" "<port>"
    When I long press <name> in the Server list
    And I press Edit
    Then I should see the EditServerActivity
    And I press cancel

    Examples:
      | name      | address         | password    | port |
      | server2   | server          | password    | 5900 |
      | server3   | 192.168.0.1     | password    | 5900 |
      | asdrvs    | 192.168.0.1     | password    | 5900 |

  Scenario Outline: Change a servers name
    Given I am on the ServerListActivity
    When I long press <name> in the Server list
    And I press Edit
    Then I should see the EditServerActivity
    When I fill in Name with <new-name>
    And I press Save
    Then I should see the ServerListActivity
    And ServerList should contain a server with "<new-name>" "<address>" "<port>"
    And ServerList should not contain a server with "<name>" "<address>" "<port>"

    Examples:
    | name      | new-name  | address         | password    | port |
    | server2   | server9   | server          | password    | 5900 |
    | server3   | server10  | 192.168.0.1     | password    | 5900 |
    | asdrvs    | server11  | 192.168.0.1     | password    | 5900 |