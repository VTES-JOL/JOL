package net.deckserver.game.storage.cards;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.deckserver.storage.json.cards.CardSummary;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class SummaryCard {

    private String id;
    private String icon;
    private String type;
    private String htmlText;
    private String originalText;
    private String displayName;
    private String name;
    private Set<String> names;
    private boolean crypt;
    private boolean unique;
    private Boolean burnOption;
    private String group;
    private String sect;
    private String path;
    private List<String> clans;
    private boolean banned;
    private boolean playTest;
    private Set<String> sets;

    //Library only
    private String preamble;
    private List<LibraryCardMode> modes;
    private Boolean doNotReplace;
    private Boolean multiMode;
    private String cost;

    //Crypt only
    private Integer capacity;
    private List<String> disciplines;
    private Boolean advanced;
    private Boolean infernal;

    private String title;

    private String votes;

    public SummaryCard(CryptCard cryptCard) {
        this.id = cryptCard.getId();
        this.displayName = cryptCard.getDisplayName();
        this.name = cryptCard.getName();
        this.names = cryptCard.getNames();
        this.type = cryptCard.getType();
        this.crypt = true;
        this.unique = cryptCard.isUnique();
        this.group = cryptCard.getGroup();
        this.sect = cryptCard.getSect();
        this.path = cryptCard.getPath();
        this.clans = Collections.singletonList(cryptCard.getClan());
        this.title = cryptCard.getTitle();
        this.votes = cryptCard.getVotes();
        this.banned = cryptCard.isBanned();
        this.playTest = cryptCard.isPlayTest();
        this.sets = cryptCard.getSets();
        this.advanced = cryptCard.isAdvanced();
        this.infernal = cryptCard.isInfernal();

        List<String> cardLines = new ArrayList<>();
        boolean vampire = cryptCard.getType().equals("Vampire");
        String clanLabel = vampire ? "Clan: " : "Creed: ";
        String disciplinesLabel = vampire ? "Disciplines: " : "Virtues: ";
        Optional.ofNullable(cryptCard.getName()).ifPresent(name -> cardLines.add("Name: " + name));
        if (cryptCard.isBanned()) {
            cardLines.add("-- Banned --");
        }
        Optional.of(cryptCard.getType()).ifPresent(type -> cardLines.add("Cardtype: " + type));
        Optional.ofNullable(cryptCard.getClan()).ifPresent(clan -> cardLines.add(clanLabel + clan));
        if (cryptCard.isAdvanced()) {
            cardLines.add("Level: Advanced");
        }
        Optional.ofNullable(cryptCard.getGroup()).ifPresent(group -> cardLines.add("Group: " + group));
        Optional.ofNullable(cryptCard.getCapacity()).ifPresent(capacity -> cardLines.add("Capacity: " + capacity));
        Optional.ofNullable(cryptCard.getDisciplines())
                .map(disciplines -> disciplines.stream().map(s -> "[" + s + "]").collect(Collectors.joining(" ")))
                .ifPresent(disciplines -> cardLines.add(disciplinesLabel + disciplines));
        Optional.ofNullable(cryptCard.getText()).ifPresent(cardLines::add);
        this.htmlText = String.join("<br/>", cardLines);
        this.originalText = cryptCard.getText();
        this.capacity = cryptCard.getCapacity();
        this.disciplines = cryptCard.getDisciplines();
    }

    public SummaryCard(LibraryCard libraryCard) {
        this.id = libraryCard.getId();
        this.displayName = libraryCard.getDisplayName();
        this.name = libraryCard.getName();
        this.names = libraryCard.getNames();
        this.type = String.join("/", libraryCard.getType());
        this.crypt = false;
        this.unique = libraryCard.isUnique();
        Optional.ofNullable(libraryCard.getBurnOption()).ifPresent(burnOption -> this.burnOption = burnOption);
        this.banned = libraryCard.isBanned();
        this.playTest = libraryCard.isPlayTest();
        this.sets = libraryCard.getSets();

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
        this.path = libraryCard.getPath();

        List<String> cardLines = new ArrayList<>();
        Optional.ofNullable(libraryCard.getName()).ifPresent(name -> cardLines.add("Name: " + name));
        if (libraryCard.isBanned()) {
            cardLines.add("-- Banned --");
        }
        Optional.ofNullable(libraryCard.getType()).ifPresent(type -> cardLines.add("Cardtype: " + String.join("/", type)));
        Optional.ofNullable(libraryCard.getClans()).ifPresent(clan -> cardLines.add("Clan: " + String.join("/", clan)));
        Optional.ofNullable(libraryCard.getPath()).ifPresent(path -> cardLines.add("Path: " + path));
        Optional.ofNullable(libraryCard.getBlood()).ifPresent(blood -> cardLines.add("Cost: " + blood + " blood"));
        Optional.ofNullable(libraryCard.getPool()).ifPresent(pool -> cardLines.add("Cost: " + pool + " pool"));
        Optional.ofNullable(libraryCard.getBurnOption()).ifPresent(burnOption -> cardLines.add("Burn Option"));
        Optional.ofNullable(libraryCard.getConviction()).ifPresent(conviction -> cardLines.add("Cost: " + conviction + " conviction"));
        Optional.ofNullable(libraryCard.getDisciplines()).ifPresent(disciplines -> cardLines.add("Discipline: " + String.join("/", disciplines)));
        Optional.ofNullable(libraryCard.getText()).map(text -> text.replaceAll("\\n", "<br/>")).ifPresent(cardLines::add);
        this.htmlText = String.join("<br/>", cardLines);
        this.originalText = libraryCard.getText().replaceAll("\\n", "<br/>");
    }

    public CardSummary toCardSummary() {
        CardSummary cardSummary = new CardSummary();
        cardSummary.setId(id);
        cardSummary.setDisplayName(displayName);
        cardSummary.setName(name);
        cardSummary.setNames(names);
        cardSummary.setType(type);
        cardSummary.setCrypt(crypt);
        cardSummary.setUnique(unique);
        cardSummary.setGroup(group);
        cardSummary.setSect(sect);
        cardSummary.setPath(path);
        cardSummary.setClans(clans);
        cardSummary.setTitle(title);
        cardSummary.setVotes(votes);
        cardSummary.setBanned(banned);
        cardSummary.setPlayTest(playTest);
        cardSummary.setSets(sets);
        cardSummary.setBanned(banned);
        cardSummary.setAdvanced(Optional.ofNullable(advanced).orElse(false));
        cardSummary.setInfernal(Optional.ofNullable(infernal).orElse(false));
        cardSummary.setHtmlText(htmlText);
        cardSummary.setCapacity(capacity);
        cardSummary.setDisciplines(disciplines);
        return cardSummary;
    }
}
