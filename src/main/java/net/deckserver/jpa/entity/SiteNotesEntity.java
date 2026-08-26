package net.deckserver.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "site_notes")
public class SiteNotesEntity {

    public static final short SINGLETON_ID = 1;

    @Id
    @Column(name = "id")
    private short id = SINGLETON_ID;

    @Column(name = "notes", nullable = false, columnDefinition = "TEXT")
    private String notes = "";

    public short getId() { return id; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
