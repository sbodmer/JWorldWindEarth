package org.worldwindearth;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author sbodmer
 */
public class WorldWindLayersTableModel extends AbstractTableModel {
    LayerList ll = null;
    
    public WorldWindLayersTableModel(LayerList ll) {
        super();
        this.ll = ll;
        
    }
    //**************************************************************************
    //*** TableModel
    //**************************************************************************
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return Boolean.class;
        }
        return Layer.class;
    }

    public String getColumnName(int column) {
        if (column == 0) {
            return "Active";
            
        } else if (column == 1) {
            return "Name";
            
        }
        return "";
        
    }
    
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return true;
        }
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex == -1) return null;
        Layer l = ll.get(rowIndex);
        if (columnIndex == 0) {
            return l.isEnabled();
            
        } else if (columnIndex == 1) {
            return l;
            
        }
        return null;
        
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (rowIndex == -1) return;
        
        Layer l = ll.get(rowIndex);
        if (columnIndex == 0) {
            Boolean b = (Boolean) aValue;
            l.setEnabled(b.booleanValue());          
            
        }
        
    }

    @Override
    public int getRowCount() {
        return ll.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }
    
}
