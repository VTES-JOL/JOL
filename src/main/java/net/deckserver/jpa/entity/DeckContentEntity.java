package net.deckserver.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "deck_content")
public class DeckContentEntity {

    @Id
    @Column(name = "deck_id", nullable = false)
    private String deckId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    public DeckContentEntity() {}

    public DeckContentEntity(String deckId, String content) {
        this.deckId = deckId;
        this.content = content;
    }

    public String getDeckId() { return deckId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DeckContentEntity that)) return false;
        return Objects.equals(deckId, that.deckId);
    }

    @Override
    public int hashCode() { return Objects.hashCode(deckId); }
}
