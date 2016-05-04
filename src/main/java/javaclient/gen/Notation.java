/**
 *	This generated bean class Notation matches the schema element 'notation'.
 *  The root bean class is GameState
 *
 *	Generated on Wed May 04 14:08:52 AEST 2016
 * @Generated
 */

package javaclient.gen;

import org.netbeans.modules.schema2beans.Common;

import java.util.Vector;

// BEGIN_NOI18N

public class Notation extends org.netbeans.modules.schema2beans.BaseBean
{

	static Vector comparators = new Vector();
	private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(3, 6, 1);

	static public final String NAME = "Name";	// NOI18N
	static public final String VALUE = "Value";	// NOI18N

	public Notation() {
		this(Common.USE_DEFAULT_VALUES);
	}

	public Notation(int options)
	{
		super(comparators, runtimeVersion);
		// Properties (see root bean comments for the bean graph)
		initPropertyTables(2);
		this.createProperty("name", 	// NOI18N
			NAME, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
		this.createProperty("value", 	// NOI18N
			VALUE, 
			Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY, 
			String.class);
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
	public void setValue(String value) {
		this.setValue(VALUE, value);
	}

	//
	public String getValue() {
		return (String)this.getValue(VALUE);
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
		str.append("Name");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getName();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(NAME, 0, str, indent);

		str.append(indent);
		str.append("Value");	// NOI18N
		str.append(indent+"\t");	// NOI18N
		str.append("<");	// NOI18N
		o = this.getValue();
		str.append((o==null?"null":o.toString().trim()));	// NOI18N
		str.append(">\n");	// NOI18N
		this.dumpAttributes(VALUE, 0, str, indent);

	}
	public String dumpBeanNode(){
		StringBuffer str = new StringBuffer();
		str.append("Notation\n");	// NOI18N
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
