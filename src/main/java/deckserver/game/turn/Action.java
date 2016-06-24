/**
 * This generated bean class Action matches the schema element 'action'.
 * The root bean class is GameActions
 * <p>
 * Generated on Wed May 04 14:08:52 AEST 2016
 *
 * @Generated
 */

package deckserver.game.turn;

import org.netbeans.modules.schema2beans.Common;

import java.util.Vector;

// BEGIN_NOI18N

public class Action extends org.netbeans.modules.schema2beans.BaseBean {

    static public final String COUNTER = "Counter";    // NOI18N
    static public final String TEXT = "Text";    // NOI18N
    static public final String COMMAND = "Command";    // NOI18N
    private static final org.netbeans.modules.schema2beans.Version runtimeVersion = new org.netbeans.modules.schema2beans.Version(3, 6, 1);
    static Vector comparators = new Vector();

    public Action() {
        this(Common.USE_DEFAULT_VALUES);
    }

    public Action(int options) {
        super(comparators, runtimeVersion);
        // Properties (see root bean comments for the bean graph)
        initPropertyTables(3);
        this.createProperty("counter",    // NOI18N
                COUNTER,
                Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                String.class);
        this.createProperty("text",    // NOI18N
                TEXT,
                Common.TYPE_1 | Common.TYPE_STRING | Common.TYPE_KEY,
                String.class);
        this.createProperty("command",    // NOI18N
                COMMAND,
                Common.TYPE_0_N | Common.TYPE_STRING | Common.TYPE_KEY,
                String.class);
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
    public String getCounter() {
        return (String) this.getValue(COUNTER);
    }

    // This attribute is mandatory
    public void setCounter(String value) {
        this.setValue(COUNTER, value);
    }

    //
    public String getText() {
        return (String) this.getValue(TEXT);
    }

    // This attribute is mandatory
    public void setText(String value) {
        this.setValue(TEXT, value);
    }

    // This attribute is an array, possibly empty
    public void setCommand(int index, String value) {
        this.setValue(COMMAND, index, value);
    }

    //
    public String getCommand(int index) {
        return (String) this.getValue(COMMAND, index);
    }

    //
    public String[] getCommand() {
        return (String[]) this.getValues(COMMAND);
    }

    // This attribute is an array, possibly empty
    public void setCommand(String[] value) {
        this.setValue(COMMAND, value);
    }

    // Return the number of properties
    public int sizeCommand() {
        return this.size(COMMAND);
    }

    // Add a new element returning its index in the list
    public int addCommand(String value) {
        int positionOfNewItem = this.addValue(COMMAND, value);
        return positionOfNewItem;
    }

    //
    // Remove an element using its reference
    // Returns the index the element had in the list
    //
    public int removeCommand(String value) {
        return this.removeValue(COMMAND, value);
    }

    public void validate() throws org.netbeans.modules.schema2beans.ValidateException {
    }

    // Dump the content of this bean returning it as a String
    public void dump(StringBuffer str, String indent) {
        String s;
        Object o;
        org.netbeans.modules.schema2beans.BaseBean n;
        str.append(indent);
        str.append("Counter");    // NOI18N
        str.append(indent + "\t");    // NOI18N
        str.append("<");    // NOI18N
        o = this.getCounter();
        str.append((o == null ? "null" : o.toString().trim()));    // NOI18N
        str.append(">\n");    // NOI18N
        this.dumpAttributes(COUNTER, 0, str, indent);

        str.append(indent);
        str.append("Text");    // NOI18N
        str.append(indent + "\t");    // NOI18N
        str.append("<");    // NOI18N
        o = this.getText();
        str.append((o == null ? "null" : o.toString().trim()));    // NOI18N
        str.append(">\n");    // NOI18N
        this.dumpAttributes(TEXT, 0, str, indent);

        str.append(indent);
        str.append("Command[" + this.sizeCommand() + "]");    // NOI18N
        for (int i = 0; i < this.sizeCommand(); i++) {
            str.append(indent + "\t");
            str.append("#" + i + ":");
            str.append(indent + "\t");    // NOI18N
            str.append("<");    // NOI18N
            o = this.getCommand(i);
            str.append((o == null ? "null" : o.toString().trim()));    // NOI18N
            str.append(">\n");    // NOI18N
            this.dumpAttributes(COMMAND, i, str, indent);
        }

    }

    public String dumpBeanNode() {
        StringBuffer str = new StringBuffer();
        str.append("Action\n");    // NOI18N
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
