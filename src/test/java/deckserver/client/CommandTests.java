package deckserver.client;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by shannon on 23/11/16.
 */
public class CommandTests {

    private static String SPLIT_PATTERN = ";";

    @Test
    public void chainedCommands() throws Exception {
        String chainedCommand = "transfer 3 +3; play vamp 3";
        StringTokenizer tokenizer = new StringTokenizer(chainedCommand, SPLIT_PATTERN);
        List<String> tokenList = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            tokenList.add(tokenizer.nextToken());
        }
        String[] tokenArray = tokenList.toArray(new String[tokenList.size()]);
        String[] splitArray = chainedCommand.split(SPLIT_PATTERN);

        System.out.println(Arrays.toString(tokenArray));
        System.out.println(Arrays.toString(splitArray));

    }
}
