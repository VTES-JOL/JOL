package net.deckserver.storage.json.system;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.text.DecimalFormat;

@Data
@NoArgsConstructor
@EqualsAndHashCode(of = "playerName")
public class PlayerResult {
    private String playerName;
    private String deckName;
    private Double victoryPoints;
    private boolean gameWin;

    public String getVP() {
        return DecimalFormat.getCompactNumberInstance().format(victoryPoints);
    }
}
