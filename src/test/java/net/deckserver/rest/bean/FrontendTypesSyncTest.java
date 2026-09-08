package net.deckserver.rest.bean;

import net.deckserver.storage.json.game.ChatData;
import net.deckserver.storage.json.game.CommandErrorData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Structural sync check between {@code src/main/webui/src/api/types.ts}
 * (hand-written mirrors of the Java REST beans — see that file's own header
 * comment) and the actual Java beans it mirrors.
 *
 * <p>The relationship is deliberately asymmetric: {@code types.ts} says it
 * only declares "the fields the React pages actually read", so a Java bean is
 * allowed to carry fields no TS interface mentions. What it must never do is
 * the reverse — a TS field with no matching Java field is a page reading
 * {@code undefined} off a real API response, typically silently (no compile
 * error; TypeScript trusts the hand-written interface). This test asserts
 * only that direction: every field a target TS interface declares exists,
 * under the same name, as a declared field on the mapped Java class.
 *
 * <p>Field <em>names</em> only — not types. These are Lombok
 * {@code @Getter}/{@code @Builder} classes and plain records with no
 * {@code @JsonProperty} renames, so the declared field name is also the JSON
 * property name Jackson serializes (boolean fields included: Lombok's
 * {@code isX()} getter still maps back to JSON key {@code x}). A name match
 * doesn't guarantee the TS type itself is right (e.g. {@code string} vs
 * {@code number}) — only that the field exists at all.
 *
 * <p>Add a row to {@link #mappings()} whenever a new TS interface is added
 * as a mirror of a Java bean.
 */
class FrontendTypesSyncTest {

    private static final Path TYPES_TS = Path.of("src/main/webui/src/api/types.ts");
    private static String source;

    @BeforeAll
    static void loadTypesFile() throws IOException {
        assertTrue(Files.exists(TYPES_TS), () -> "Expected to find " + TYPES_TS.toAbsolutePath()
                + " — this test must run with the repo root as the working directory.");
        source = Files.readString(TYPES_TS);
    }

    /** {tsInterfaceName, javaClass} — the beans actually exercised by the current game page work. */
    static Stream<Arguments> mappings() {
        return Stream.of(
                Arguments.of("NavBean", NavBean.class),
                Arguments.of("ChatData", ChatData.class),
                Arguments.of("CommandError", CommandErrorData.class),
                Arguments.of("GameSnapshot", GameSnapshot.class),
                Arguments.of("PlayerSnapshot", PlayerSnapshot.class),
                Arguments.of("RegionSnapshot", RegionSnapshot.class),
                Arguments.of("CardSnapshot", CardSnapshot.class),
                Arguments.of("JudgeRequestSnapshot", JudgeRequestBean.class),
                Arguments.of("PendingAction", PendingActionBean.class),
                Arguments.of("CardMode", PlayModeBean.class),
                Arguments.of("CardBean", CardBean.class)
        );
    }

    @ParameterizedTest(name = "{0} ↔ {1}")
    @MethodSource("mappings")
    void everyTsFieldExistsOnTheMappedJavaBean(String tsInterfaceName, Class<?> javaClass) {
        Set<String> tsFields = tsInterfaceFields(source, tsInterfaceName);
        assertFalse(tsFields.isEmpty(), () -> "Parsed zero fields from TS interface " + tsInterfaceName
                + " — the interface name may have moved/been renamed in types.ts, or the parser needs updating.");

        Set<String> javaFields = declaredFieldNames(javaClass);
        Set<String> missing = new TreeSet<>(tsFields);
        missing.removeAll(javaFields);

        assertTrue(missing.isEmpty(), () -> String.format(
                "types.ts's %s declares field(s) %s with no matching field on %s.%n"
                        + "  TS fields:   %s%n"
                        + "  Java fields: %s%n"
                        + "A page reading one of these off a real API response gets undefined at runtime.",
                tsInterfaceName, missing, javaClass.getName(), tsFields, javaFields));
    }

    @Test
    void everyMappedTsInterfaceIsStillPresentInTypesTs() {
        // Guards the mapping table itself: a renamed/removed TS interface should
        // fail loudly here rather than have its row silently parse to zero
        // fields (which the assertion above already catches) or, worse, match
        // some unrelated interface by accident.
        mappings().forEach(args -> {
            String tsInterfaceName = (String) args.get()[0];
            assertTrue(source.contains("export interface " + tsInterfaceName), () ->
                    "No `export interface " + tsInterfaceName + "` found in " + TYPES_TS);
        });
    }

    // ── TS parsing ───────────────────────────────────────────────────────

    /**
     * Field names declared directly in {@code export interface <name> { ... }}.
     * Handles brace nesting (e.g. a {@code Record<string, string>} value type,
     * which has no braces of its own) but assumes — true of every interface
     * this test targets — a flat body with no inline nested object-literal
     * types, only references to other named types.
     */
    private static Set<String> tsInterfaceFields(String tsSource, String interfaceName) {
        Matcher header = Pattern.compile("export interface " + Pattern.quote(interfaceName) + "\\s*\\{").matcher(tsSource);
        if (!header.find()) {
            return Set.of();
        }
        int start = header.end();
        int depth = 1;
        int i = start;
        while (depth > 0 && i < tsSource.length()) {
            char c = tsSource.charAt(i++);
            if (c == '{') depth++;
            else if (c == '}') depth--;
        }
        String body = tsSource.substring(start, i - 1);

        Pattern fieldLine = Pattern.compile("^\\s*([A-Za-z_][A-Za-z0-9_]*)\\??\\s*:");
        Set<String> fields = new LinkedHashSet<>();
        for (String line : body.split("\n")) {
            String trimmed = line.strip();
            if (trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*")) continue;
            Matcher m = fieldLine.matcher(line);
            if (m.find()) fields.add(m.group(1));
        }
        return fields;
    }

    // ── Java reflection ──────────────────────────────────────────────────

    private static Set<String> declaredFieldNames(Class<?> clazz) {
        Set<String> names = new LinkedHashSet<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) continue;
            names.add(field.getName());
        }
        return names;
    }
}
