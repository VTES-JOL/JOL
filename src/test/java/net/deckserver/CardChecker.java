package net.deckserver;

import org.junit.Ignore;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Ignore
public class CardChecker {

    @Test
    public void loadCards() throws Exception {
        String basePath = "/Users/shannon/data";
        Path keyFile = Paths.get(basePath, "cards", "base.prop");
        Path textFile = Paths.get(basePath, "cards", "base.txt");
        assertTrue(keyFile.toFile().exists());
        assertTrue(textFile.toFile().exists());
        List<String> keys = Files.readAllLines(keyFile, Charset.forName("ISO-8859-1"));
        List<String> text = Files.readAllLines(textFile, Charset.forName("ISO-8859-1"));

        List<String> names = text.stream().filter(s -> s.startsWith("Name:")).map(s -> s.replaceAll("^Name: ", "")).collect(Collectors.toList());
        assertThat(names.size(), is(3723));

        // Map of Names -> keys
        Map<String, String> nameKeys = keys.stream().map(s -> s.split("=")).collect(Collectors.toMap(s -> s[1], s -> s[0]));
        nameKeys.keySet().removeAll(names);
        nameKeys.keySet().forEach(System.out::println);
    }
}
