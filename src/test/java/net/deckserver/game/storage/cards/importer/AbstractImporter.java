package net.deckserver.game.storage.cards.importer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractImporter<T> {

    private final InputStream dataStream;
    private List<CardAlias> aliases = new ArrayList<>();

    public AbstractImporter(InputStream dataStream) {
        this.dataStream = dataStream;
    }

    public AbstractImporter(InputStream dataStream, InputStream aliasStream) {
        this.dataStream = dataStream;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            CollectionType cardAliasCollectionType = objectMapper.getTypeFactory().constructCollectionType(List.class, CardAlias.class);
            this.aliases = objectMapper.readValue(aliasStream, cardAliasCollectionType);
        } catch (IOException e) {
            log.error("Unable to read alias file", e);
        }
    }

    public List<T> read() throws IOException {
        try (InputStreamReader inputStreamReader = new InputStreamReader(dataStream); CSVReader csvReader = new CSVReader(inputStreamReader, CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, 1)) {
            List<String[]> lineData = csvReader.readAll();
            return lineData.stream().map(this::map).collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Exception reading library file: {}", e);
            throw e;
        }
    }

    public List<CardAlias> getAliases() {
        return this.aliases;
    }

    public abstract T map(String[] lineData);
}
