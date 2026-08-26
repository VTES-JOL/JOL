package net.deckserver.services;

import net.deckserver.rest.bean.ChatEntryBean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetEnvironmentVariable;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@SetEnvironmentVariable(key = "ENABLE_TEST_MODE", value = "true")
@ExtendWith(JolServiceExtension.class)
class GlobalChatServiceTest {

    @Test
    void sendsAndReadsBackAChatMessage() {
        GlobalChatService.chat("Player1", "hello from the smoke test");
        List<ChatEntryBean> recent = GlobalChatService.getRecentChats(10);
        assertThat(recent, hasItem(hasProperty("message", equalTo("hello from the smoke test"))));
    }

    @Test
    void tracksPerPlayerUnseenCursor() {
        GlobalChatService.chat("Player2", "unseen message one");
        List<ChatEntryBean> unseen = GlobalChatService.getUnseenChats("Player3");
        assertThat(unseen, hasItem(hasProperty("message", equalTo("unseen message one"))));

        // having just been marked seen, the same player sees nothing new
        List<ChatEntryBean> secondRead = GlobalChatService.getUnseenChats("Player3");
        assertThat(secondRead, not(hasItem(hasProperty("message", equalTo("unseen message one")))));
    }
}
