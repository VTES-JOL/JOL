package net.deckserver.game.storage.cards.importer;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractImporter<T> {

    private final Path dataPath;

    public AbstractImporter(Path dataPath) {
        this.dataPath = dataPath;
    }

    public List<T> read() throws IOException {
        try (InputStream dataStream = Files.newInputStream(dataPath); InputStreamReader inputStreamReader = new InputStreamReader(dataStream); CSVReader csvReader = new CSVReaderBuilder(inputStreamReader).withSkipLines(1).build()) {
            List<String[]> lineData = csvReader.readAll();
            return lineData.stream().map(this::map).collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Exception reading library file: {}", e);
            throw e;
        }
    }

    public abstract T map(String[] lineData);
}
