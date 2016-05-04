/**
 *	This generated bean class Turn matches the schema element 'turn'.
 *  The root bean class is GameActions
 *
 *	Generated on Wed May 04 14:08:52 AEST 2016
 * @Generated
 */

package nbclient.model.gen.v2;

import org.netbeans.modules.schema2beans.Common;

import java.util.Vector;

// BEGIN_NOI18N

public class Turn extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(3, 6, 1);

	static public final String SEQUENCE = "Sequence";	// NOI18N
	static public final String NAME = "Name";	// NOI18N
	static public final String COUNTER = "Counter";	// NOI18N
	static public final String LABEL = "Label";	// NOI18N
	static public final String ACTION = "Action";	// NOI18N

	public Turn() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public Turn(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(5);
		this.createProperty("sequence", 	// NOI18N
			SEQUENCE, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("name", 	// NOI18N
			NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("counter", 	// NOI18N
			COUNTER, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("label", 	// NOI18N
			LABEL, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("action", 	// NOI18N
			ACTION, 
			Common.TYPE_0_N | Common.TYPE_BEAN | Common.TYPE_KEY, 
			Action.class);
		this.initialize(options);
	}

	// Setting the default values of the properties
	void initialize(int options) {

	}

	// This attribute is mandatory
	public void setSequence(String value) {
		this.setValue(SEQUENCE, value);
	}

	//
	public String getSequence() {
		return (String)this.getValue(SEQUENCE);
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

	// This attribute is mandatory
	public void setLabel(String value) {
		this.setValue(LABEL, value);
	}

	//
	public String getLabel() {
		return (String)this.getValue(LABEL);
	}

	// This attribute is an array, possibly empty
	public void setAction(int index, Action value) {
		this.setValue(ACTION, index, value);
	}

	//
	public Action getAction(int index) {
		return (Action)this.getValue(ACTION, index);
	}

	// This attribute is an array, possibly empty
	public void setAction(Action[] value) {
		this.setValue(ACTION, value);
	}

	//
	public Action[] getAction() {
		return (Action[])this.getValues(ACTION);
	}

	// Return the number of properties
	public int sizeAction() {
		return this.size(ACTION);
	}

	// Add a new element returning its index in the list
	public int addAction(nbclient.model.gen.v2.Action value) {
		int positionOfNewItem = this.addValue(ACTION, value);
		return positionOfNewItem;
	}

	//
	// Remove an element using its reference
	// Returns the index the element had in the list
	//
	public int removeAction(nbclient.model.gen.v2.Action value) {
		return this.removeValue(ACTION, value);
	}

	/**
	 * Create a new bean using it's default constructor.
	 * This does not add it to any bean graph.
	 */
	public Action newAction() {
		return new Action();
	}

	//
	public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.add(c);
	}

	//
	public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator c) {
		comparators.remove(c);
	}
	public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
	}

	// Dump the content of this bean returning it as a String
	public void dump(StringBuffer str, String indent){
		String s;
		Object o;
		org.netbeans.modules.schema2beans.BaseBean n;
		str.append(indent);
		str.append("Sequence");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getSequence();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(SEQUENCE, 0, str, indent);

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
		str.append("Label");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getLabel();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(LABEL, 0, str, indent);

		str.append(indent);
		str.append("Action["+this.sizeAction()+"]");	// NOI18N
		for(int i=0; i<this.sizeAction(); i++)
		{
			str.append(indent+"\t");
			str.append("#"+i+":");
			n = (org.netbeans.modules.schema2beans.BaseBean) this.getAction(i);
			if (n != null)
				n.dump(str, indent + "\t");	// NOI18N
			else
				str.append(indent+"\tnull");	// NOI18N
			this.dumpAttributes(ACTION, i, str, indent);
		}

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("Turn\n");	// NOI18N
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
