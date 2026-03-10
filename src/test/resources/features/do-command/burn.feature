Feature: Burn command
  Players can burn cards from regions using the burn command.

  Scenario: Burn the top card of your library by number
    Given "Player2" has 0 cards in their ash heap
    And the top card of "Player2"'s library is "143"
    When "Player2" enters the command "burn library 1"
    Then the top card of "Player2"'s library is now "176"
    And "Player2" has 1 cards in their ash_heap
    And the last chat message contains "Player2 burns"
    And the last chat message contains "from their library."

  Scenario: Burn the top card of your library with top shortcut
    Given the top card of "Player2"'s library is "143"
    When "Player2" enters the command "burn library top"
    Then "Player2" has 1 cards in their ash_heap
    And the last chat message contains "from their library."

  Scenario: Burn a ready card
    Given "Player2" has 0 cards in their ash heap
    When "Player2" enters the command "burn ready 1"
    Then "Player2" has 1 cards in their ash_heap
    And the last chat message contains "from their ready region."

  Scenario: Burn a random ready card
    When "Player2" enters the command "burn ready random"
    Then "Player2" has 1 cards in their ash_heap
    And the last chat message contains "Player2 burns"
    And the last chat message contains "(picked randomly)"
    And the last chat message contains "from their ready region."

  Scenario: Burn another player's ready card
    Given "Player2" has 0 cards in their ash heap
    When "Player3" enters the command "burn Player2 ready 1"
    Then "Player2" has 1 cards in their ash_heap
    And the last chat message contains "Player3 burns"
    And the last chat message contains "from Player2's ready region"