package dsclient.modelimpl;

import java.util.*;

import nbclient.model.*;
import nbclient.model.state.SCard;
import nbclient.model.state.SLocation;

public class DsGame extends Notes implements Game {

	private String gname;
	private final LinkedList<Player> players = new LinkedList<Player>();
	private final LinkedList<LocBox> regions = new LinkedList<LocBox>();
	private final Map<String, SCard> cards = new HashMap<String, SCard>(500);
	private int index = 1;

	public void setName(String name) {
		gname = name;
	}

	public void addPlayer(String player) {
		players.add(new Player(player));
	}
	
	Player getPlayer(String player) {
		for(Iterator<Player> i = players.iterator();i.hasNext();) {
			Player p = i.next();
			if(p.name.equals(player)) return p;
		}
		return null;
	}

	public void orderPlayers(String[] order) {
		Collection<Player> newP = new LinkedList<Player>();
		for(int i = 0; i < order.length; i++) {
			newP.add(getPlayer(order[i]));
		}
		players.clear();
		players.addAll(newP);
	}

	public void addLocation(String regionName) {
		LocBox l = new LocBox(regionName, this);
		regions.add(l);
	}

	public void addLocation(String player, String regionName) {
		LocBox l = new LocBox(regionName, this);
		Player p = getPlayer(player);
		p.locs.add(l);
	}
	
	public String getName() {
		return gname;
	}

	public String[] getPlayers() {
		String[] ret = new String[players.size()];
		for(int i = 0; i < ret.length; i++)
			ret[i] = players.get(i).name;
		return ret;
	}

	public SLocation[] getPlayerLocations(String player) {
		Player p = getPlayer(player);
		return (Location[]) p.locs.toArray(new Location[0]);
	}

	public SLocation getPlayerLocation(String player, String regionName) {
		Player p = getPlayer(player);
		for(Iterator<LocBox> i = p.locs.iterator(); i.hasNext();) {
			SLocation l = i.next();
			if(l.getName().equals(regionName)) return l;
		}
		return null;
	}

	public SLocation getLocation(String regionName) {
		for(Iterator<LocBox> i = regions.iterator(); i.hasNext();) {
			SLocation l = i.next();
			if(l.getName().equals(regionName)) return l;
		}
		return null;
	}

	public String getPlayerRegionName(SLocation location) {
		if(regions.contains(location)) return null;
		return location.getName();
	}
	
	void addCard(SCard card) {
		cards.put(card.getId(),card);
	}
	
	int getNewId() {
		return index++;
	}

	public SCard getCard(String id) {
		return cards.get(id);
	}

	public nbclient.model.state.SCardContainer getRegionFromCard(SCard card) {
		DsCard p = (DsCard) card;
		while(p.getParent() instanceof DsCard) {
			p = (DsCard) p.getParent();
		}
		return p.getParent();
	}

	static class Player {
		
        final String name;
        final Collection<LocBox> locs = new LinkedList<LocBox>();
        
        Player(String name) {
        	this.name = name;
        }
	}
}
