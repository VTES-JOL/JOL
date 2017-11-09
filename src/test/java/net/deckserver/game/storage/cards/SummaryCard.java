package net.deckserver.game.storage.cards;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.joining;

@Data
@NoArgsConstructor
public class SummaryCard {

    private String id;
    private String key;
    private String type;
    private String text;
    private String displayName;
    private Set<String> names;

    public SummaryCard(CryptCard cryptCard) {
        this.id = cryptCard.getId();
        this.key = cryptCard.getKey();
        this.displayName = cryptCard.getDisplayName();
        this.names = cryptCard.getNames();
        this.type = cryptCard.getType();

        List<String> cardLines = new ArrayList<>();
        boolean vampire = cryptCard.getType().equals("Vampire");
        String clanLabel = vampire ? "Clan: " : "Creed: ";
        String disciplinesLabel = vampire ? "Disciplines: " : "Virtues: ";
        Optional.ofNullable(cryptCard.getName()).ifPresent(name -> cardLines.add("Name: " + name));
        Optional.ofNullable(cryptCard.getBanned()).ifPresent(banned -> cardLines.add("-- Banned --"));
        Optional.ofNullable(cryptCard.getType()).ifPresent(type -> cardLines.add("Cardtype: " + type));
        Optional.ofNullable(cryptCard.getClan()).ifPresent(clan -> cardLines.add(clanLabel + clan));
        Optional.ofNullable(cryptCard.getAdvanced()).ifPresent(advanced -> cardLines.add("Level: Advanced"));
        Optional.ofNullable(cryptCard.getGroup()).ifPresent(group -> cardLines.add("Group: " + group));
        Optional.ofNullable(cryptCard.getCapacity()).ifPresent(capacity -> cardLines.add("Capacity: " + capacity));
        Optional.ofNullable(cryptCard.getDisciplineList()).ifPresent(disciplines -> cardLines.add(disciplinesLabel + disciplines));
        Optional.ofNullable(cryptCard.getText()).ifPresent(cardLines::add);
        this.text = cardLines.stream().collect(joining("\n"));
    }

    public SummaryCard(LibraryCard libraryCard) {
        this.id = libraryCard.getId();
        this.key = libraryCard.getKey();
        this.displayName = libraryCard.getDisplayName();
        this.names = libraryCard.getNames();
        this.type = libraryCard.getType().stream().collect(joining("/"));

        List<String> cardLines = new ArrayList<>();
        Optional.ofNullable(libraryCard.getName()).ifPresent(name -> cardLines.add("Name: " + name));
        Optional.ofNullable(libraryCard.getBanned()).ifPresent(banned -> cardLines.add("-- Banned --"));
        Optional.ofNullable(libraryCard.getType()).ifPresent(type -> cardLines.add("Cardtype: " + type.stream().collect(joining("/"))));
        Optional.ofNullable(libraryCard.getClans()).ifPresent(clan -> cardLines.add("Clan: " + clan.stream().collect(joining("/"))));
        Optional.ofNullable(libraryCard.getBlood()).ifPresent(blood -> cardLines.add("Cost: " + blood + " blood"));
        Optional.ofNullable(libraryCard.getPool()).ifPresent(pool -> cardLines.add("Cost: " + pool + " pool"));
        Optional.ofNullable(libraryCard.getConviction()).ifPresent(conviction -> cardLines.add("Cost: " + conviction + " conviction"));
        Optional.ofNullable(libraryCard.getDisciplines()).ifPresent(disciplines -> cardLines.add("Discipline: " + disciplines.stream().collect(joining("/"))));
        Optional.ofNullable(libraryCard.getText()).ifPresent(cardLines::add);
        this.text = cardLines.stream().collect(joining("\n"));
    }
}
