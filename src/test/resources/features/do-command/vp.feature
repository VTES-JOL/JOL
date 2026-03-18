Feature: Victory point command

  Scenario Outline: Update victory points
    Given "<target>" has <before> victory points
    When "<player>" enters the command "<command>"
    Then "<target>" has <after> victory points
    And the last chat message contains "<message>"

    Examples:
      | player  | command        | target  | before | after | message                                   |
      | Player2 | vp +1          | Player2 | 0.0    | 1.0   | Player2 has gained 1 victory points.      |
      | Player1 | vp Player3 +1  | Player3 | 0.0    | 1.0   | Player3 has gained 1 victory points.      |
      | Player2 | vp withdraw    | Player2 | 0.0    | 0.5   | withdraws and gains 0.5 victory points. |

  Scenario Outline: Reject invalid victory point commands
    Given "<target>" has <before> victory points
    When "<player>" enters the command "<command>"
    Then the command fails
    And "<target>" has <after> victory points

    Examples:
      | player  | command       | target  | before | after |
      | Player2 | vp            | Player2 | 0.0    | 0.0   |
      | Player1 | vp Player1 0  | Player1 | 0.0    | 0.0   |