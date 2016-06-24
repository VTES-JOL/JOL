/**
 * This generated bean class GameActions matches the schema element 'game-actions'.
 * <p>
 * Generated on Wed May 04 14:08:52 AEST 2016
 * <p>
 * This class matches the root element of the DTD,
 * and is the root of the following bean graph:
 * <p>
 * gameActions <game-actions> : GameActions
 * name <name> : String
 * counter <counter> : String
 * gameCounter <game-counter> : String
 * turn <turn> : Turn[0,n]
 * sequence <sequence> : String
 * name <name> : String
 * counter <counter> : String
 * label <label> : String
 * action <action> : Action[0,n]
 * counter <counter> : String
 * text <text> : String
 * command <command> : String[0,n]
 *
 * @Generated
 */

package deckserver.game.turn;

import org.netbeans.modules.schema2beans.AttrProp;
import org.netbeans.modules.schema2beans.Common;
import org.netbeans.modules.schema2beans.GraphManager;
import org.netbeans.modules.schema2beans.Schema2BeansException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.*;
import java.util.Vector;

// BEGIN_NOI18N

public class GameActions extends org.netbeans.modules.schema2beans.BaseBean {

    static public final String NAME = "Name";    // NOI18N
    static public final String COUNTER = "Counter";    // NOI18N
    static public final String GAME_COUNTER = "GameCounter";    // NOI18N
    static public final String TURN = "Turn";    // NOI18N
    private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(3, 6, 1);
    static Vector comparators = new Vector();

    public GameActions() {
        this(null, Common.USE_DEFAULT_VALUES);
    }

    public GameActions(Node doc, int options) {
        this(Common.NO_DEFAULT_VALUES);
        try {
            initFromNode(doc, options);
        } catch (Schema2BeansException e) {
            throw new RuntimeException(e);
        }
    }

    public GameActions(int options) {
        super(comparators, runtimeVersion);
        initOptions(options);
    }

    //
    public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.add(c);
    }

    //
    public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
        comparators.remove(c);
    }

    //
    // This method returns the root of the bean graph
    // Each call creates a new bean graph from the specified DOM graph
    //
    public static GameActions createGraph(Node doc) {
        return new GameActions(doc, Common.NO_DEFAULT_VALUES);
    }

    public static GameActions createGraph(InputStream in) {
        return createGraph(in, false);
    }

    public static GameActions createGraph(InputStream in, boolean validate) {
        try {
            Document doc = GraphManager.createXmlDocument(in, validate);
            return createGraph(doc);
        } catch (Exception t) {
            throw new RuntimeException(Common.getMessage(
                    "DOMGraphCreateFailed_msg",
                    t));
        }
    }

    //
    // This method returns the root for a new empty bean graph
    //
    public static GameActions createGraph() {
        return new GameActions();
    }

    protected void initFromNode(Node doc, int options) throws Schema2BeansException {
        if (doc == null) {
            doc = GraphManager.createRootElementNode("game-actions");    // NOI18N
            if (doc == null)
                throw new Schema2BeansException(Common.getMessage(
                        "CantCreateDOMRoot_msg", "game-actions"));
        }
        Node n = GraphManager.getElementNode("game-actions", doc);    // NOI18N
        if (n == null)
            throw new Schema2BeansException(Common.getMessage(
                    "DocRootNotInDOMGraph_msg", "game-actions", doc.getFirstChild().getNodeName()));

        this.graphManager.setXmlDocument(doc);

        // Entry point of the createBeans() recursive calls
        this.createBean(n, this.graphManager());
        this.initialize(options);
    }

    protected void initOptions(int options) {
        // The graph manager is allocated in the bean root
        this.graphManager = new GraphManager(this);
        this.createRoot("game-actions", "GameActions",    // NOI18N
                Common.TYPE_1 | Common.TYPE_BEAN, GameActions.class);

        // Properties (see root bean comments for the bean graph)
        initPropertyTables(4);
        this.createProperty("name",    // NOI18N
                NAME,
                Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                String.class);
        this.createProperty("counter",    // NOI18N
                COUNTER,
                Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                String.class);
        this.createProperty("game-counter",    // NOI18N
                GAME_COUNTER,
                Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                String.class);
        this.createProperty("turn",    // NOI18N
                TURN,
                Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY,
                Turn.class);
        this.initialize(options);
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

    //
    public String getCounter() {
        return (String) this.getValue(COUNTER);
    }

    // This attribute is mandatory
    public void setCounter(String value) {
        this.setValue(COUNTER, value);
    }

    //
    public String getGameCounter() {
        return (String) this.getValue(GAME_COUNTER);
    }

    // This attribute is mandatory
    public void setGameCounter(String value) {
        this.setValue(GAME_COUNTER, value);
    }

    // This attribute is an array, possibly empty
    public void setTurn(int index, Turn value) {
        this.setValue(TURN, index, value);
    }

    //
    public Turn getTurn(int index) {
        return (Turn) this.getValue(TURN, index);
    }

    //
    public Turn[] getTurn() {
        return (Turn[]) this.getValues(TURN);
    }

    // This attribute is an array, possibly empty
    public void setTurn(Turn[] value) {
        this.setValue(TURN, value);
    }

    // Return the number of properties
    public int sizeTurn() {
        return this.size(TURN);
    }

    // Add a new element returning its index in the list
    public int addTurn(Turn value) {
        int positionOfNewItem = this.addValue(TURN, value);
        return positionOfNewItem;
    }

    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removeTurn(Turn value) {
        return this.removeValue(TURN, value);
    }

    /**
     * Create a new bean using it's default constructor.
     * This does not add it to any bean graph.
     */
    public Turn newTurn() {
        return new Turn();
    }

    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
    }

    // Special serializer: output XML as serialization
    private void writeObject(ObjectOutputStream out) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        write(baos);
        String str = baos.toString();
        out.writeUTF(str);
    }

    // Special deserializer: read XML as deserialization
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        try {
            init(comparators, runtimeVersion);
            String strDocument = in.readUTF();
            ByteArrayInputStream bais = new ByteArrayInputStream(strDocument.getBytes());
            Document doc = GraphManager.createXmlDocument(bais, false);
            initOptions(Common.NO_DEFAULT_VALUES);
            initFromNode(doc, Common.NO_DEFAULT_VALUES);
        } catch (Schema2BeansException e) {
            throw new RuntimeException(e);
        }
    }

    public void _setSchemaLocation(String location) {
        if (beanProp().getAttrProp("xsi:schemaLocation", true) == null) {
            createAttribute("xmlns:xsi", "xmlns:xsi", AttrProp.CDATA | AttrProp.IMPLIED, null, "http://www.w3.org/2001/XMLSchema-instance");
            setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            createAttribute("xsi:schemaLocation", "xsi:schemaLocation", AttrProp.CDATA | AttrProp.IMPLIED, null, location);
        }
        setAttributeValue("xsi:schemaLocation", location);
    }

    public String _getSchemaLocation() {
        if (beanProp().getAttrProp("xsi:schemaLocation", true) == null) {
            createAttribute("xmlns:xsi", "xmlns:xsi", AttrProp.CDATA | AttrProp.IMPLIED, null, "http://www.w3.org/2001/XMLSchema-instance");
            setAttributeValue("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            createAttribute("xsi:schemaLocation", "xsi:schemaLocation", AttrProp.CDATA | AttrProp.IMPLIED, null, null);
        }
        return getAttributeValue("xsi:schemaLocation");
    }

    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent) {
        String s;
        Object o;
        org.netbeans.modules.schema2beans.BaseBean n;
        str.append(indent);
        str.append("Name");    // NOI18N
        str.append(indent + "\t");    // NOI18N
        str.append("<");    // NOI18N
        o = this.getName();
        str.append((o == null ? "null" : o.toString().trim()));    // NOI18N
        str.append(">\n");    // NOI18N
        this.dumpAttributes(NAME, 0, str, indent);

        str.append(indent);
        str.append("Counter");    // NOI18N
        str.append(indent + "\t");    // NOI18N
        str.append("<");    // NOI18N
        o = this.getCounter();
        str.append((o == null ? "null" : o.toString().trim()));    // NOI18N
        str.append(">\n");    // NOI18N
        this.dumpAttributes(COUNTER, 0, str, indent);

        str.append(indent);
        str.append("GameCounter");    // NOI18N
        str.append(indent + "\t");    // NOI18N
        str.append("<");    // NOI18N
        o = this.getGameCounter();
        str.append((o == null ? "null" : o.toString().trim()));    // NOI18N
        str.append(">\n");    // NOI18N
        this.dumpAttributes(GAME_COUNTER, 0, str, indent);

        str.append(indent);
        str.append("Turn[" + this.sizeTurn() + "]");    // NOI18N
        for (int i = 0; i < this.sizeTurn(); i++) {
            str.append(indent + "\t");
            str.append("#" + i + ":");
            n = this.getTurn(i);
            if (n != null)
                n.dump(str, indent + "\t");    // NOI18N
            else
                str.append(indent + "\tnull");    // NOI18N
            this.dumpAttributes(TURN, i, str, indent);
        }

    }

    public String dumpBeanNode() {
        StringBuffer str = new StringBuffer();
        str.append("GameActions\n");    // NOI18N
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

<!ELEMENT game-actions (name, counter, game-counter, turn*)>

<!ELEMENT name (#PCDATA)>

<!ELEMENT counter (#PCDATA)>

<!ELEMENT game-counter (#PCDATA)>

<!ELEMENT turn (sequence, name, counter, label, action*)>

<!ELEMENT sequence (#PCDATA)>

<!ELEMENT label (#PCDATA)>

<!ELEMENT action (counter, text, command*)>

<!ELEMENT command (#PCDATA)>

<!ELEMENT text (#PCDATA)>

*/
