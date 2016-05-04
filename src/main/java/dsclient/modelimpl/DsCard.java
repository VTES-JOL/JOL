package dsclient.modelimpl;

import nbclient.model.Card;
import nbclient.model.state.SCardContainer;
import nbclient.vtesmodel.JolAdminFactory;

class DsCard extends CardBox implements Card {

	private String id;
	private String card;
	private CardBox parent;
	
	DsCard(String id, String card) {
		super(null);
		this.id = id;
		this.card = card;
	}
	
	DsGame getGame() {
		return parent.getGame();
	}

	public String getName() {
            try {
                return JolAdminFactory.INSTANCE.getAllCards().getCardById(card).getName();
            } catch (Throwable t) {
                System.err.println("Error finding card " + this.card);
                return "ERROR CARD";
            }
	}
	
	void setParent(CardBox parent) {
		this.parent = parent;
	}

	public SCardContainer getParent() {
		return parent;
	}

	public String getId() {
		return id;
	}

	public String getCardId() {
		return card;
	}

}
