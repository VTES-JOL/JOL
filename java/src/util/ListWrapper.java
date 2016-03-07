/*
 * ListWrapper.java
 *
 * Created on September 25, 2003, 3:45 PM
 */

package util;

import java.awt.event.*;
import javax.swing.*;

/**
 *
 * @author  administrator
 */
public class ListWrapper extends AbstractListModel {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 5445032289138295825L;
	Object[] values;
    protected JList list;
    
    /** Creates a new instance of ListWrapper */
    public ListWrapper(JScrollPane ui,Object[] initValues) {
        values = initValues;
        list = new JList(this);
        list.setPrototypeCellValue("AAAAAAAAAAAAAAAAAAAAAAA");
        MouseListener mouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int index = list.locationToIndex(e.getPoint());
                doClick(index);
                if(e.getClickCount() > 1) doDoubleClick(index);
            }
        };
        list.addMouseListener(mouseListener);
        ui.getViewport().setView(list);
    }
    
    public void setValues(Object[] newValues) {
        int size = Math.max(values.length,newValues.length);
        values = newValues;
        fireContentsChanged(this,0,size - 1);
    }
    
    protected void doClick(int member) {
    }
    
    protected void doDoubleClick(int member) {
    }
    
    public int[] getSelectedElements() {
       return list.getSelectedIndices();
    }
    
    public int getSize() {
        return values.length;
    }
    
    public Object getElementAt(int index) {
        return values[index];
    }
    
}
