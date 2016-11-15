package deckserver.game.state.model;

import org.netbeans.modules.schema2beans.Common;

import java.util.Vector;

// BEGIN_NOI18N

public class Region extends org.netbeans.modules.schema2beans.BaseBean {

    static public final String NAME = "Name";    // NOI18N
    static public final String NOTATION = "Notation";    // NOI18N
    static public final String GAME_CARD = "GameCard";    // NOI18N
    private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(3, 6, 1);
    static Vector comparators = new Vector();

    public Region() {
        this(Common.USE_DEFAULT_VALUES);
    }

    public Region(int options) {
        super(comparators, runtimeVersion);
        // Properties (see root bean comments for the bean graph)
        initPropertyTables(3);
        this.createProperty("name",    // NOI18N
                NAME,
                Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                String.class);
        this.createProperty("notation",    // NOI18N
                NOTATION,
                Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY,
                Notation.class);
        this.createProperty("game-card",    // NOI18N
                GAME_CARD,
                Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY,
                GameCard.class);
        this.initialize(options);
    }

    //
    public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.add(c);
    }

    //
    public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.remove(c);
    }

    // Setting the default values of the properties
    void initialize(int options) {

    }

    //
    public String getName() {
        return (String) this.getValue(NAME);
    }

    // This attribute is mandatory
    public void setName(String value) {
        this.setValue(NAME, value);
    }

    // This attribute is an array, possibly empty
    public void setNotation(int index, Notation value) {
        this.setValue(NOTATION, index, value);
    }

    //
    public Notation getNotation(int index) {
        return (Notation) this.getValue(NOTATION, index);
    }

    //
    public Notation[] getNotation() {
        return (Notation[]) this.getValues(NOTATION);
    }

    // This attribute is an array, possibly empty
    public void setNotation(Notation[] value) {
        this.setValue(NOTATION, value);
    }

    // Return the number of properties
    public int sizeNotation() {
        return this.size(NOTATION);
    }

    // Add a new element returning its index in the list
    public int addNotation(Notation value) {
        int positionOfNewItem = this.addValue(NOTATION, value);
        return positionOfNewItem;
    }

    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removeNotation(Notation value) {
        return this.removeValue(NOTATION, value);
    }

    /**
     * Create a new bean using it's default constructor.
     * This does not add it to any bean graph.
     */
    public Notation newNotation() {
        return new Notation();
    }

    // This attribute is an array, possibly empty
    public void setGameCard(int index, GameCard value) {
        this.setValue(GAME_CARD, index, value);
    }

    //
    public GameCard getGameCard(int index) {
        return (GameCard) this.getValue(GAME_CARD, index);
    }

    //
    public GameCard[] getGameCard() {
        return (GameCard[]) this.getValues(GAME_CARD);
    }

    // This attribute is an array, possibly empty
    public void setGameCard(GameCard[] value) {
        this.setValue(GAME_CARD, value);
    }

    // Return the number of properties
    public int sizeGameCard() {
        return this.size(GAME_CARD);
    }

    // Add a new element returning its index in the list
    public int addGameCard(GameCard value) {
        int positionOfNewItem = this.addValue(GAME_CARD, value);
        return positionOfNewItem;
    }

    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removeGameCard(GameCard value) {
        return this.removeValue(GAME_CARD, value);
    }

    /**
     * Create a new bean using it's default constructor.
     * This does not add it to any bean graph.
     */
    public GameCard newGameCard() {
        return new GameCard();
    }

    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
    }

    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent) {
        String s;
        Object o;
        org.netbeans.modules.schema2beans.BaseBean n;
        str.append(indent);
        str.append("Name");    // NOI18N
        str.append(indent).append("\t");    // NOI18N
        str.append("<");    // NOI18N
        o = this.getName();
        str.append((o == null ? "null" : o.toString().trim()));    // NOI18N
        str.append(">\n");    // NOI18N
        this.dumpAttributes(NAME, 0, str, indent);

        str.append(indent);
        str.append("Notation[").append(this.sizeNotation()).append("]");    // NOI18N
        for (int i = 0; i < this.sizeNotation(); i++) {
            str.append(indent).append("\t");
            str.append("#").append(i).append(":");
            n = this.getNotation(i);
            if (n != null)
                n.dump(str, indent + "\t");    // NOI18N
            else
                str.append(indent).append("\tnull");    // NOI18N
            this.dumpAttributes(NOTATION, i, str, indent);
        }

        str.append(indent);
        str.append("GameCard[").append(this.sizeGameCard()).append("]");    // NOI18N
        for (int i = 0; i < this.sizeGameCard(); i++) {
            str.append(indent).append("\t");
            str.append("#").append(i).append(":");
            n = this.getGameCard(i);
            if (n != null)
                n.dump(str, indent + "\t");    // NOI18N
            else
                str.append(indent).append("\tnull");    // NOI18N
            this.dumpAttributes(GAME_CARD, i, str, indent);
        }

    }

    public String dumpBeanNode() {
        StringBuffer str = new StringBuffer();
        str.append("Region\n");    // NOI18N
        this.dump(str, "\n  ");    // NOI18N
        return str.toString();
    }
}

// END_NOI18N


/*
        The following schema file has been used for generation:

<?xml version="1.0" encoding="UTF-8"?>

<!--
    Document   : game-state.dtd
    Created on : September 7, 2003, 6:26 PM
    Author     : administrator
    Description:
        Purpose of the document follows.
-->

<!ELEMENT game-state (name, counter, player*, region*, notation*)>

<!ELEMENT name (#PCDATA)>

<!ELEMENT counter (#PCDATA)>

<!ELEMENT player (#PCDATA)>

<!ELEMENT region (name, notation*, game-card*)>

<!ELEMENT notation (name, value)>

<!ELEMENT value (#PCDATA)>

<!ELEMENT game-card (id, cardid, notation*)>

*/
