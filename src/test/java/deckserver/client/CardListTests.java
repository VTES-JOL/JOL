package deckserver.client;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertNotNull;

public class CardListTests {

    @Test
    public void readCardText() throws Exception {
        String tempDir = System.getProperty("java.io.tmpdir");
        System.out.println(tempDir);
        Charset utf8charset = Charset.forName("UTF-8");
        Charset iso88591charset = Charset.forName("ISO-8859-1");

        Path detailsPath = Paths.get("src/test/resources/cards/cardlist.txt");
        assertNotNull(detailsPath);

        byte[] rawData = Files.readAllBytes(detailsPath);

        ByteBuffer inputBuffer = ByteBuffer.wrap(rawData);

        CharBuffer data = utf8charset.decode(inputBuffer);

        ByteBuffer outputBuffer = iso88591charset.encode(data);

        byte[] outputData = outputBuffer.array();

        Path outputPath = Paths.get("target/base.txt");
        Files.write(outputPath, outputData);

    }
}
