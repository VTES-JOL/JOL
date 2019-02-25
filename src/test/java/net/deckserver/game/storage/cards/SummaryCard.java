package net.deckserver.game.storage.cards;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SummaryCard {

    private String id;
    private String jolId;
    private String amaranthId;
    private String type;
    private String text;
    private String htmlText;
    private String displayName;
    private Set<String> names;
    private boolean crypt;
    private boolean unique;
    private String group;
    private String sect;

    private SummaryCard() {

    }

    public SummaryCard(CryptCard cryptCard) {
        this.id = cryptCard.getId();
        this.jolId = cryptCard.getJolId();
        this.amaranthId = cryptCard.getAmaranthId();
        this.displayName = cryptCard.getDisplayName();
        this.names = cryptCard.getNames();
        this.type = cryptCard.getType();
        this.crypt = true;
        this.unique = cryptCard.isUnique();
        this.group = cryptCard.getGroup();
        this.sect = cryptCard.getSect();

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
        Optional.ofNullable(cryptCard.getDisciplines()).ifPresent(disciplines -> cardLines.add(disciplinesLabel + disciplines.stream().collect(Collectors.joining(" "))));
        Optional.ofNullable(cryptCard.getText()).ifPresent(cardLines::add);
        this.text = String.join("\n", cardLines);
        this.htmlText = String.join("<br/>", cardLines);
    }

    public SummaryCard(LibraryCard libraryCard) {
        this.id = libraryCard.getId();
        this.jolId = libraryCard.getKey();
        this.amaranthId = libraryCard.getJolId();
        this.displayName = libraryCard.getDisplayName();
        this.names = libraryCard.getNames();
        this.type = String.join("/", libraryCard.getType());
        this.crypt = false;
        this.unique = libraryCard.isUnique();

        List<String> cardLines = new ArrayList<>();
        Optional.ofNullable(libraryCard.getName()).ifPresent(name -> cardLines.add("Name: " + name));
        Optional.ofNullable(libraryCard.getBanned()).ifPresent(banned -> cardLines.add("-- Banned --"));
        Optional.ofNullable(libraryCard.getType()).ifPresent(type -> cardLines.add("Cardtype: " + String.join("/", type)));
        Optional.ofNullable(libraryCard.getClans()).ifPresent(clan -> cardLines.add("Clan: " + String.join("/", clan)));
        Optional.ofNullable(libraryCard.getBlood()).ifPresent(blood -> cardLines.add("Cost: " + blood + " blood"));
        Optional.ofNullable(libraryCard.getPool()).ifPresent(pool -> cardLines.add("Cost: " + pool + " pool"));
        Optional.ofNullable(libraryCard.getConviction()).ifPresent(conviction -> cardLines.add("Cost: " + conviction + " conviction"));
        Optional.ofNullable(libraryCard.getDisciplines()).ifPresent(disciplines -> cardLines.add("Discipline: " + disciplines.stream().collect(joining("/"))));
        Optional.ofNullable(libraryCard.getText()).ifPresent(cardLines::add);
        this.text = String.join("\n", cardLines);
        this.htmlText = String.join("<br/>", cardLines);
    }
}
