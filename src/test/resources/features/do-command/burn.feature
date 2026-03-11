Feature: Burn command

  Scenario Outline: Burn cards from different places
    Given "<player>" will enter the command "<command>"
    And the card in "<from>" is "<target card>"
    And the ash heap for "<from>" is empty
    When "<player>" enters the command "<command>"
    Then 1 card has been burned from "<from>"
    And the last chat message contains "<message>"
    And the last chat message contains "<target card>"

    Examples:
      | player  | command              | from              | target card           | message                     |
      | Player2 | burn library 4       | Player2's library | Far Mastery           | from their library.         |
      | Player2 | burn ready 1         | Player2's ready   | Talley, The Hound     | from their ready region.    |
      | Player3 | burn Player2 ready 1 | Player2's ready   | Talley, The Hound     | from Player2's ready region |

  Scenario: Burn a random ready card
    Given "Player2" has 2 cards in their "ready"
    And "Player2" has 0 cards in their "ash heap"
    When "Player2" enters the command "burn ready random"
    Then "Player2" has 1 cards in their "ready"
    And "Player2" has 1 cards in their "ash heap"
    And the last chat message contains "Player2 burns"
    And the last chat message contains "(picked randomly)"
    And the last chat message contains "from their ready region."