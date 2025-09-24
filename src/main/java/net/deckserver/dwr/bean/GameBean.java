package net.deckserver.dwr.bean;

import java.util.List;

public record GameBean(boolean player, boolean admin, boolean judge, int refresh, String hand, String globalNotes,
                       String privateNotes, String label, String phase, boolean resetChat, boolean turnChanged,
                       List<String> turn, List<String> turns, String state, List<String> phases, List<String> ping,
                       List<String> pinged, String stamp, String name, int logLength, String currentPlayer) {

}
