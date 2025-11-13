package net.deckserver.game.storage.cards.importer;

import lombok.extern.slf4j.Slf4j;
import net.deckserver.game.storage.cards.BaseCard;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public abstract class AbstractImporter<T extends BaseCard> {

    private final Path basePath;
    private final String filePrefix;

    public AbstractImporter(Path basePath, String filePrefix) {
        this.basePath = basePath;
        this.filePrefix = filePrefix;
    }

    public List<T> read() throws Exception {
        Path dataPath = basePath.resolve(filePrefix + ".csv");
        try (FileReader in = new FileReader(dataPath.toFile())) {
            Iterable<CSVRecord> records = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build()
                    .parse(in);
            return StreamSupport.stream(records.spliterator(), false)
                    .map(CSVRecord::values)
                    .map(this::map)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Exception reading library file:", e);
            throw e;
        }
    }

    public abstract T map(String[] lineData);
}
