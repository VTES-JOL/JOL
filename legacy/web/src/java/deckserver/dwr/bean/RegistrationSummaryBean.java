package deckserver.dwr.bean;

import java.util.*;

import nbclient.vtesmodel.JolAdminFactory;

import deckserver.rich.*;

public class RegistrationSummaryBean {
	
	Map<String, PlSummaryBean> registrations = new HashMap<String, PlSummaryBean>();

	public RegistrationSummaryBean(AdminBean abean, String name) {
		GameModel game = abean.getGameModel(name);
		JolAdminFactory admin = abean.getAdmin();
		String[] players = admin.getPlayers();
		for(int i = 0; i < players.length; i++) {
			if(admin.isInvited(name, players[i]) || admin.getGameDeck(name, players[i]) != null) {
				registrations.put(players[i], new PlSummaryBean(game,players[i]));
			}
		}
	}

	public Map<String, PlSummaryBean> getRegistrations() {
		return registrations;
	}
}
