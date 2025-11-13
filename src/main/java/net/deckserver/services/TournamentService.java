package net.deckserver.services;

public class TournamentService extends PersistedService {

    private TournamentService() {
        super("TournamentService", 10);
    }

    public static PersistedService getInstance() {
        return null;
    }

    @Override
    protected void persist() {

    }

    @Override
    protected void load() {

    }
}
