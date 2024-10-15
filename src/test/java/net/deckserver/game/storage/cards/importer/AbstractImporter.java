package net.deckserver.game.storage.cards.importer;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public abstract class AbstractImporter<T> {

    private final Path dataPath;

    public AbstractImporter(Path dataPath) {
        this.dataPath = dataPath;
    }

    public List<T> read() throws Exception {
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
