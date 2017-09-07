/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.geocode;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.worldwindearth.components.layers.ScreenProjectionLayer.ScreenProjectable;

/**
 *
 * @author sbodmer
 */
public class JBalloonCellRenderer extends javax.swing.JPanel implements ListCellRenderer<JBalloon> {

    /**
     * Creates new form JReverseCellRenderer
     */
    public JBalloonCellRenderer() {
        initComponents();
    }

    //**************************************************************************
    //*** ListCellRenderer
    //**************************************************************************
    @Override
    public Component getListCellRendererComponent(JList<? extends JBalloon> list, JBalloon value, int index, boolean isSelected, boolean cellHasFocus) {
        Color fg = isSelected ? list.getSelectionForeground() : list.getForeground();
        setForeground(fg);

        ScreenProjectable proj = value.getScreenProjectable();
        if (proj instanceof Result) {
            Result r = (Result) proj;
            
            LB_Producer.setIcon(r.producer.getPluginFactory().getFactoryIcon(22));
            LB_Summary.setText(r.summary);
        }
        
        if (isSelected) {
            setBackground(list.getSelectionBackground());

        } else {
            setBackground(Color.white);
        }

        return this;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        LB_Producer = new javax.swing.JLabel();
        LB_Summary = new javax.swing.JLabel();

        setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        LB_Producer.setPreferredSize(new java.awt.Dimension(22, 22));
        add(LB_Producer);

        LB_Summary.setText("...");
        add(LB_Summary);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JLabel LB_Producer;
    protected javax.swing.JLabel LB_Summary;
    // End of variables declaration//GEN-END:variables

}
