/**
 *	This generated bean class GameState matches the schema element 'game-state'.
 *
 *	Generated on Wed May 04 14:08:52 AEST 2016
 *
 *	This class matches the root element of the DTD,
 *	and is the root of the following bean graph:
 *
 *	gameState <game-state> : GameState
 *		name <name> : String
 *		counter <counter> : String
 *		player <player> : String[0,n]
 *		region <region> : Region[0,n]
 *			name <name> : String
 *			notation <notation> : Notation[0,n]
 *				name <name> : String
 *				value <value> : String
 *			gameCard <game-card> : GameCard[0,n]
 *				id <id> : String
 *				cardid <cardid> : String
 *				notation <notation> : Notation[0,n]
 *					name <name> : String
 *					value <value> : String
 *		notation <notation> : Notation[0,n]
 *			name <name> : String
 *			value <value> : String
 *
 * @Generated
 */

package javaclient.gen;

import org.netbeans.modules.schema2beans.AttrProp;
import org.netbeans.modules.schema2beans.Common;
import org.netbeans.modules.schema2beans.GraphManager;
import org.netbeans.modules.schema2beans.Schema2BeansException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.*;
import java.util.Vector;

// BEGIN_NOI18N

public class GameState extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(3, 6, 1);

	static public final String NAME = "Name";	// NOI18N
	static public final String COUNTER = "Counter";	// NOI18N
	static public final String PLAYER = "Player";	// NOI18N
	static public final String REGION = "Region";	// NOI18N
	static public final String NOTATION = "Notation";	// NOI18N

	public GameState() {
		this(null, Common.USE_DEFAULT_VALUES);
	}

	public GameState(Node doc, int options) {
		this(Common.NO_DEFAULT_VALUES);
		try {
			initFromNode(doc, options);
		}
		catch (Schema2BeansException e) {
			throw new RuntimeException(e);
		}
	}
	protected void initFromNode(Node doc, int options) throws Schema2BeansException
	{
		if (doc == null)
		{
			doc = GraphManager.createRootElementNode("game-state");	// NOI18N
			if (doc == null)
				throw new Schema2BeansException(Common.getMessage(
					"CantCreateDOMRoot_msg", "game-state"));
		}
		Node n = GraphManager.getElementNode("game-state", doc);	// NOI18N
		if (n == null)
			throw new Schema2BeansException(Common.getMessage(
				"DocRootNotInDOMGraph_msg", "game-state", doc.getFirstChild().getNodeName()));

		this.graphManager.setXmlDocument(doc);

		// Entry point of the createBeans() recursive calls
		this.createBean(n, this.graphManager());
		this.initialize(options);
	}
	public GameState(int options)
	{
		super(comparators, runtimeVersion);
		initOptions(options);
	}
	protected void initOptions(int options)
	{
		// The graph manager is allocated in the bean root
		this.graphManager = new GraphManager(this);
		this.createRoot("game-state", "GameState",	// NOI18N
			Common.TYPE_1 | Common.TYPE_BEAN, GameState.class);

		// Properties (see root bean comments for the bean graph)
		initPropertyTables(5);
		this.createProperty("name", 	// NOI18N
			NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("counter", 	// NOI18N
			COUNTER, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("player", 	// NOI18N
			PLAYER, 
			Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("region", 	// NOI18N
			REGION, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Region.class);
		this.createProperty("notation", 	// NOI18N
			NOTATION, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Notation.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setName(String value) {
		this.setValue(NAME, value);
	}

	//
	public String getName() {
		return (String)this.getValue(NAME);
	}

	// This attribute is mandatory
	public void setCounter(String value) {
		this.setValue(COUNTER, value);
	}

	//
	public String getCounter() {
		return (String)this.getValue(COUNTER);
	}

	// This attribute is an array, possibly empty
	public void setPlayer(int index, String value) {
		this.setValue(PLAYER, index, value);
	}

	//
	public String getPlayer(int index) {
		return (String)this.getValue(PLAYER, index);
	}

	// This attribute is an array, possibly empty
	public void setPlayer(String[] value) {
		this.setValue(PLAYER, value);
	}

	//
	public String[] getPlayer() {
		return (String[])this.getValues(PLAYER);
	}

	// Return the number of properties
	public int sizePlayer() {
		return this.size(PLAYER);
	}

	// Add a new element returning its index in the list
	public int addPlayer(String value) {
		int positionOfNewItem = this.addValue(PLAYER, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removePlayer(String value) {
		return this.removeValue(PLAYER, value);
	}

	// This attribute is an array, possibly empty
	public void setRegion(int index, Region value) {
		this.setValue(REGION, index, value);
	}

	//
	public Region getRegion(int index) {
		return (Region)this.getValue(REGION, index);
	}

	// This attribute is an array, possibly empty
	public void setRegion(Region[] value) {
		this.setValue(REGION, value);
	}

	//
	public Region[] getRegion() {
		return (Region[])this.getValues(REGION);
	}

	// Return the number of properties
	public int sizeRegion() {
		return this.size(REGION);
	}

	// Add a new element returning its index in the list
	public int addRegion(Region value) {
		int positionOfNewItem = this.addValue(REGION, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeRegion(Region value) {
		return this.removeValue(REGION, value);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public Region newRegion() {
		return new Region();
	}

	// This attribute is an array, possibly empty
	public void setNotation(int index, Notation value) {
		this.setValue(NOTATION, index, value);
	}

	//
	public Notation getNotation(int index) {
		return (Notation)this.getValue(NOTATION, index);
	}

	// This attribute is an array, possibly empty
	public void setNotation(Notation[] value) {
		this.setValue(NOTATION, value);
	}

	//
	public Notation[] getNotation() {
		return (Notation[])this.getValues(NOTATION);
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
	public static GameState createGraph(Node doc) {
		return new GameState(doc, Common.NO_DEFAULT_VALUES);
	}

	public static GameState createGraph(InputStream in) {
		return createGraph(in, false);
	}

	public static GameState createGraph(InputStream in, boolean validate) {
		try {
			Document doc = GraphManager.createXmlDocument(in, validate);
			return createGraph(doc);
		}
		catch (Exception t) {
			throw new RuntimeException(Common.getMessage(
				"DOMGraphCreateFailed_msg",
				t));
		}
	}

	//
	// This method returns the root for a new empty bean graph
	//
	public static GameState createGraph() {
		return new GameState();
	}

	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
	}

	// Special serializer: output XML as serialization
	private void writeObject(ObjectOutputStream out) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		write(baos);
		String str = baos.toString();;
		// System.out.println("str='"+str+"'");
		out.writeUTF(str);
	}
	// Special deserializer: read XML as deserialization
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException{
		try{
			init(comparators, runtimeVersion);
			String strDocument = in.readUTF();
			// System.out.println("strDocument='"+strDocument+"'");
			ByteArrayInputStream bais = new ByteArrayInputStream(strDocument.getBytes());
			Document doc = GraphManager.createXmlDocument(bais, false);
			initOptions(Common.NO_DEFAULT_VALUES);
			initFromNode(doc, Common.NO_DEFAULT_VALUES);
		}
		catch (Schema2BeansException e) {
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
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Name");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(NAME, 0, str, indent);

		str.append(indent);
		str.append("Counter");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getCounter();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(COUNTER, 0, str, indent);

		str.append(indent);
		str.append("Player["+this.sizePlayer()+"]");	// NOI18N
		for(int i=0; i<this.sizePlayer(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			str.append(indent+"\t");	// NOI18N
			str.append("<");	// NOI18N
			o = this.getPlayer(i);
			str.append((o==null?"null":o.toString().trim()));	// NOI18N
			str.append(">\n");	// NOI18N
			this.dumpAttributes(PLAYER, i, str, indent);
		}

		str.append(indent);
		str.append("Region["+this.sizeRegion()+"]");	// NOI18N
		for(int i=0; i<this.sizeRegion(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getRegion(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(REGION, i, str, indent);
		}

		str.append(indent);
		str.append("Notation["+this.sizeNotation()+"]");	// NOI18N
		for(int i=0; i<this.sizeNotation(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getNotation(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(NOTATION, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("GameState\n");	// NOI18N
		this.dump(str, "\n  ");	// NOI18N
		return str.toString();
	}}

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
