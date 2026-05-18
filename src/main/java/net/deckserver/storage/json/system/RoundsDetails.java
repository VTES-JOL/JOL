package net.deckserver.storage.json.system;

import net.deckserver.services.HistoryService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

public class RoundsDetails {

    public static String exportPastGamesAsCsv(Map<Integer, Map<Integer, List<TournamentPlayer>>> rounds) {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader("Round", "Table", "Player")
                .setQuoteMode(QuoteMode.ALL)
                .build();

        try {
            StringWriter writer = new StringWriter();
            CSVPrinter printer = new CSVPrinter(writer, format);
            if (rounds.isEmpty()) {
                return "NO TABLES AVAILABLE";
            }
            rounds.forEach((round, tableMap) -> {
                tableMap.forEach((table, players) -> {
                    players.forEach(player -> {
                        try {
                            printer.printRecord(round, table, player.getName());
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                });
            });
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
