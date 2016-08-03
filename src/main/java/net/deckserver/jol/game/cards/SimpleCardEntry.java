package net.deckserver.jol.game.cards;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * Created by shannon on 27/07/2016.
 */
public class SimpleCardEntry implements CardEntry {

    private CardType type;
    private String cardId;
    private String name;
    private String baseName;
    private String[] fullText;
    private String text;
    private String group;
    private boolean isAdvanced;

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String getType() {
        return type.getLabel();
    }

    @Override
    public String getCardId() {
        return cardId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getBaseName() {
        return baseName;
    }

    @Override
    public String[] getFullText() {
        return fullText;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public boolean isCrypt() {
        return EnumSet.of(CardType.VAMPIRE, CardType.IMBUED).contains(type);
    }

    @Override
    public String toString() {
        return "SimpleCardEntry{" +
                "type=" + type +
                ", cardId='" + cardId + '\'' +
                ", name='" + name + '\'' +
                ", baseName='" + baseName + '\'' +
                ", text='" + text + '\'' +
                ", group='" + group + '\'' +
                '}';
    }

    public static class Builder {

        private SimpleCardEntry entry = new SimpleCardEntry();

        public Builder setType(CardType type) {
            entry.type = type;
            return this;
        }

        public Builder setId(String cardId) {
            entry.cardId = cardId;
            return this;
        }

        public Builder setName(String name) {
            entry.name = name;
            return this;
        }

        public Builder setFullText(String[] fullText) {
            entry.fullText = fullText;
            return this;
        }

        public Builder setGroup(String group) {
            entry.group = group;
            return this;
        }

        public Builder setAdvanced(boolean isAdvanced) {
            entry.isAdvanced = isAdvanced;
            return this;
        }

        public SimpleCardEntry build() {
            entry.text = Arrays.toString(entry.fullText);
            if (entry.isAdvanced) {
                entry.baseName = entry.name;
                entry.name = entry.name + " (advanced)";
            }
            return entry;
        }
    }
}
