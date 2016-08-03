package net.deckserver.jol.game.cards.search.file;

import net.deckserver.jol.game.cards.CardEntry;
import net.deckserver.jol.game.cards.CardType;
import net.deckserver.jol.game.cards.SimpleCardEntry;
import net.deckserver.jol.game.cards.search.CardRepository;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Created by shannon on 26/07/2016.
 */
public class FileCardRepository implements CardRepository {

    private static final Logger logger = getLogger(FileCardRepository.class);

    private final String mapFilePath;
    private final String detailsFilePath;

    // Map of Name -> ID
    private Map<String, String> idMap = new HashMap<>();

    // Map of Id -> Entry
    private Map<String, CardEntry> cardMap = new HashMap<>();

    public FileCardRepository(String mapFilePath, String detailsFilePath) {
        this.mapFilePath = mapFilePath;
        this.detailsFilePath = detailsFilePath;
        refresh();
    }

    private Map<String, String> loadMap() {
        Map<String, String> newIdMap = new HashMap<>();
        try {
            File mapFile = new File(mapFilePath);
            Properties map = new Properties();
            map.load(new FileReader(mapFile));
            Set<String> keys = map.stringPropertyNames();
            for (String key : keys) {
                String[] values = map.getProperty(key).split("\\|");
                for (String value : values) {
                    newIdMap.put(value.trim(), key);
                }
            }
            return newIdMap;
        } catch (IOException e) {
            throw new RuntimeException("Unable to open card map file", e);
        }
    }

    private Map<String, CardEntry> loadDetails() {
        Map<String, CardEntry> newCardMap = new HashMap<>();
        try {
            File detailsFile = new File(detailsFilePath);
            BufferedReader reader = new BufferedReader(new FileReader(detailsFile));
            List<String> cardLines = new ArrayList<>();
            for (String line; (line = reader.readLine()) != null; ) {
                String trimmed = line.trim();
                if (trimmed.equals("")) {
                    CardEntry entry = createCardEntry(cardLines);
                    newCardMap.put(entry.getCardId(), entry);
                    cardLines = new ArrayList<>();
                } else {
                    cardLines.add(trimmed);
                }
            }
            return newCardMap;
        } catch (IOException e) {
            throw new RuntimeException("Unable to open card details file", e);
        }
    }

    private CardEntry createCardEntry(List<String> cardLines) {
        SimpleCardEntry.Builder builder = new SimpleCardEntry.Builder();
        List<String> fullTextLines = new ArrayList<>();
        for (String line : cardLines) {
            line = line.trim();
            LineType type = LineType.of(line);
            String lineData = LineType.stripLine(type, line).trim();
            switch (type) {
                case NAME:
                    String id = idMap.get(lineData);
                    builder.setName(lineData);
                    builder.setId(id);
                    break;
                case TYPE:
                    builder.setType(CardType.of(lineData));
                    break;
                case GROUP:
                    builder.setGroup(lineData);
                    break;
                case ADVANCED:
                    builder.setAdvanced(true);
                    break;
                default:
                    break;
            }
            fullTextLines.add(line);
        }
        builder.setFullText(fullTextLines.toArray(new String[fullTextLines.size()]));
        return builder.build();
    }

    @Override
    public Collection<CardEntry> findAll() {
        return cardMap.values();
    }

    @Override
    public CardEntry findById(String id) {
        return cardMap.get(id);
    }

    @Override
    public Collection<CardEntry> findByName(String name) {
        return null;
    }

    @Override
    public Collection<CardEntry> findByType(String query, EnumSet<CardType> typeFilter) {
        return null;
    }

    @Override
    public void refresh() {
        idMap = loadMap();
        cardMap = loadDetails();
    }

    private enum LineType {
        NAME("Name:"),
        TYPE("Cardtype:"),
        ARTIST("Artist:"),
        GROUP("Group:"),
        CLAN("Clan:"),
        CAPACITY("Capacity:"),
        DISCIPLINE("Discipline:"),
        ADVANCED("Level:"),
        NONE("");

        private String header;

        LineType(String header) {
            this.header = header;
        }

        public static LineType of(String line) {
            for (LineType type : LineType.values()) {
                if (line.startsWith(type.header)) {
                    return type;
                }
            }
            return NONE;
        }

        public static String stripLine(LineType type, String data) {
            if (NONE.equals(type)) {
                return data;
            } else {
                return data.replaceAll(type.header, "");
            }
        }

    }
}

