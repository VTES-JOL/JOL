package net.deckserver.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import net.deckserver.game.enums.DeckFormat;
import net.deckserver.storage.json.system.DeckInfo;
import net.deckserver.storage.json.system.RegistrationStatus;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * Regression coverage for the exact persist()/load() shape used by {@link DeckService} and
 * {@link RegistrationService}: writing a Guava {@link Table} straight through Jackson
 * ({@code objectMapper.writeValue(path, table)}), then reading it back as a
 * {@code Map<String, Map<String, V>>}. This only works because {@code jackson-datatype-guava}
 * is on the classpath and gets picked up by {@code PersistedService.objectMapper}'s
 * {@code findAndRegisterModules()} - a bare {@code new ObjectMapper()} would instead serialize
 * the Table as its bean properties ({"empty":false}), silently losing all table contents. If
 * that dependency, or the {@code findAndRegisterModules()} call, is ever removed, this test
 * catches it.
 */
class GuavaTablePersistenceTest {

    // Mirrors PersistedService.objectMapper's setup exactly, since that registration is what
    // makes raw-Table serialization work at all.
    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.findAndRegisterModules();
    }

    @Test
    void deckInfoTableRoundTripsThroughRawObjectMapperWriteValue() throws Exception {
        Table<String, String, DeckInfo> table = HashBasedTable.create();
        table.put("Player1", "My Deck", new DeckInfo("id-1", "My Deck", DeckFormat.MODERN, Set.of("STANDARD")));
        table.put("Player1", "Other Deck", new DeckInfo("id-2", "Other Deck", DeckFormat.TAGGED, Set.of()));
        table.put("Player2", "Their Deck", new DeckInfo("id-3", "Their Deck", DeckFormat.MODERN, Set.of("PLAYTEST")));

        String json = MAPPER.writeValueAsString(table);

        TypeFactory typeFactory = MAPPER.getTypeFactory();
        MapType deckMapType = typeFactory.constructMapType(Map.class, String.class, DeckInfo.class);
        Map<String, Map<String, DeckInfo>> loaded = MAPPER.readValue(json,
                typeFactory.constructMapType(ConcurrentHashMap.class, typeFactory.constructType(String.class), deckMapType));

        Table<String, String, DeckInfo> reloaded = HashBasedTable.create();
        loaded.forEach((player, decks) -> decks.forEach((deckName, info) -> reloaded.put(player, deckName, info)));

        assertThat(reloaded, is(table));
    }

    @Test
    void registrationStatusTableRoundTripsThroughRawObjectMapperWriteValue() throws Exception {
        Table<String, String, RegistrationStatus> table = HashBasedTable.create();
        RegistrationStatus status = new RegistrationStatus(OffsetDateTime.parse("2026-01-01T00:00:00Z"));
        table.put("Some Game", "Player1", status);

        String json = MAPPER.writeValueAsString(table);

        TypeFactory typeFactory = MAPPER.getTypeFactory();
        MapType registrationMapType = typeFactory.constructMapType(Map.class, String.class, RegistrationStatus.class);
        Map<String, Map<String, RegistrationStatus>> loaded = MAPPER.readValue(json,
                typeFactory.constructMapType(ConcurrentHashMap.class, typeFactory.constructType(String.class), registrationMapType));

        Table<String, String, RegistrationStatus> reloaded = HashBasedTable.create();
        loaded.forEach((game, players) -> players.forEach((player, s) -> reloaded.put(game, player, s)));

        assertThat(reloaded.get("Some Game", "Player1").getTimestamp(), is(status.getTimestamp()));
    }
}
