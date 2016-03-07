package deckserver.dwr.bean;

public final class GameBean {

	int refresh = -1;

	String hand = null;

	String global = null;

	String text = null;
	
	String label = null;
	
	String[] turn = null;

	String[] turns = null;
	
	String[] pingkeys = null;
	
	String[] pingvalues = null;

	String state = null;

	String[] phases = null;
	
	String[] collapsed = null;

	private boolean admin;
	
	private boolean resetChat;
	
	private boolean turnChanged;

	private boolean player;

	private String stamp;

	public String[] getCollapsed() {
		return collapsed;
	}

	public GameBean(boolean isPlayer, boolean isAdmin, int refresh, String hand, String global, String text,
			String label, boolean resetChat, boolean turnChanged, String[] turn, String[] turns, String state, String[] phases,
			String[] pingkeys, String[] pingvalues, String[] collapsed,String stamp) {
		this(pingkeys,pingvalues,refresh);
		this.player = isPlayer;
		this.admin = isAdmin;
		this.hand = hand;
		this.global = global;
		this.text = text;
		this.label = label;
		this.resetChat = resetChat;
		this.turnChanged = turnChanged;
		this.turn = turn;
		this.turns = turns;
		this.state = state;
		this.phases = phases;
		this.collapsed = collapsed;
		this.stamp = stamp;
	}
	
	public GameBean(String[] pingkeys, String[] pingvalues, int refresh) {
		this.pingkeys = pingkeys;
		this.pingvalues = pingvalues;
		this.refresh = refresh;
	}

	public String getHand() {
		return hand;
	}

	public String getGlobal() {
		return global;
	}

	public String[] getPhases() {
		return phases;
	}

	public int getRefresh() {
		return refresh;
	}

	public String getState() {
		return state;
	}

	public String getText() {
		return text;
	}

	public String[] getTurns() {
		return turns;
	}

	public String getLabel() {
		return label;
	}
	
	public boolean getTurnChanged() {
		return turnChanged;
	}
	
	public String[] getTurn() {
		return turn;
	}
	
	public boolean isResetChat() {
		return resetChat;
	}

	public boolean isAdmin() {
		return admin;
	}

	public boolean isPlayer() {
		return player;
	}

	public String[] getPingkeys() {
		return pingkeys;
	}

	public String[] getPingvalues() {
		return pingvalues;
	}

	public String getStamp() {
		return stamp;
	}

}
