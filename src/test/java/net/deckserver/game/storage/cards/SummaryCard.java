package net.deckserver.game.storage.cards;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SummaryCard {

    private String id;
    private String amaranthId;
    private String type;
    private String text;
    private String htmlText;
    private String originalText; //Original: as in the CSV
    private String displayName;
    private Set<String> names;
    private boolean crypt;
    private boolean unique;
    private boolean burnOption;
    private String group;
    private String sect;
    private List<String> clans;

    //Library only
    private String preamble;
    private List<LibraryCardMode> modes;
    private boolean doNotReplace;
    private boolean multiMode;
    private String cost;

    //Crypt only
    private Integer capacity;
    private List<String> disciplines;

    private String title;

    private String votes;

    private SummaryCard() {

    }

    public SummaryCard(CryptCard cryptCard) {
        this.id = cryptCard.getId();
        this.displayName = cryptCard.getDisplayName();
        this.names = cryptCard.getNames();
        this.type = cryptCard.getType();
        this.crypt = true;
        this.unique = cryptCard.isUnique();
        this.group = cryptCard.getGroup();
        this.sect = cryptCard.getSect();
        this.clans = Collections.singletonList(cryptCard.getClan());
        this.title = cryptCard.getTitle();
        this.votes = cryptCard.getVotes();

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
        Optional.ofNullable(cryptCard.getDisciplines())
                .map(disciplines -> disciplines.stream().map(s -> "[" + s + "]").collect(Collectors.joining(" ")))
                .ifPresent(disciplines -> cardLines.add(disciplinesLabel + disciplines));
        Optional.ofNullable(cryptCard.getText()).ifPresent(cardLines::add);
        this.text = String.join("\n", cardLines);
        this.htmlText = String.join("<br/>", cardLines);

        this.capacity = cryptCard.getCapacity();
        this.disciplines = cryptCard.getDisciplines();
        this.originalText = cryptCard.getText();
    }

    public SummaryCard(LibraryCard libraryCard) {
        this.id = libraryCard.getId();
        this.displayName = libraryCard.getDisplayName();
        this.names = libraryCard.getNames();
        this.type = String.join("/", libraryCard.getType());
        this.crypt = false;
        this.unique = libraryCard.isUnique();
        Optional.ofNullable(libraryCard.getBurnOption()).ifPresent(burnOption -> this.burnOption = burnOption);

        this.preamble = libraryCard.getPreamble();
        this.modes = libraryCard.getModes();
        this.doNotReplace = libraryCard.isDoNotReplace();
        this.multiMode = libraryCard.isMultiMode();

        if (!StringUtils.isBlank(libraryCard.getPool()))
            this.cost = libraryCard.getPool() + " pool";
        else if (!StringUtils.isBlank(libraryCard.getBlood()))
            this.cost = libraryCard.getBlood() + " blood";
        else if (!StringUtils.isBlank(libraryCard.getConviction()))
            this.cost = libraryCard.getConviction() + " conviction";

        this.clans = libraryCard.getClans();

        List<String> cardLines = new ArrayList<>();
        Optional.ofNullable(libraryCard.getName()).ifPresent(name -> cardLines.add("Name: " + name));
        Optional.ofNullable(libraryCard.getBanned()).ifPresent(banned -> cardLines.add("-- Banned --"));
        Optional.ofNullable(libraryCard.getType()).ifPresent(type -> cardLines.add("Cardtype: " + String.join("/", type)));
        Optional.ofNullable(libraryCard.getClans()).ifPresent(clan -> cardLines.add("Clan: " + String.join("/", clan)));
        Optional.ofNullable(libraryCard.getBlood()).ifPresent(blood -> cardLines.add("Cost: " + blood + " blood"));
        Optional.ofNullable(libraryCard.getPool()).ifPresent(pool -> cardLines.add("Cost: " + pool + " pool"));
        Optional.ofNullable(libraryCard.getBurnOption()).ifPresent(burnOption -> cardLines.add("Burn Option"));
        Optional.ofNullable(libraryCard.getConviction()).ifPresent(conviction -> cardLines.add("Cost: " + conviction + " conviction"));
        Optional.ofNullable(libraryCard.getDisciplines()).ifPresent(disciplines -> cardLines.add("Discipline: " + disciplines.stream().collect(joining("/"))));
        Optional.ofNullable(libraryCard.getText()).ifPresent(cardLines::add);
        this.text = String.join("\n", cardLines);
        this.htmlText = String.join("<br/>", cardLines);
        this.originalText = libraryCard.getText();
    }
}
