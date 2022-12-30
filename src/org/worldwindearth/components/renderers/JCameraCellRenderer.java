/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.components.renderers;

import java.awt.Color;
import java.awt.Component;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import org.tinyrcp.App;
import org.worldwindearth.components.Camera;

/**
 *
 * @author sbodmer
 */
public class JCameraCellRenderer extends javax.swing.JPanel implements ListCellRenderer<Camera> {

    App app = null;
    
    /**
     * Creates new form JCameraCellRenderer
     */
    public JCameraCellRenderer(App app) {
        this.app = app;
        
        initComponents();
    }

    //**************************************************************************
    //*** ListCellRenderer
    //**************************************************************************
    @Override
    public Component getListCellRendererComponent(JList list, Camera value, int index, boolean isSelected, boolean cellHasFocus) {
        Color fg = isSelected?list.getSelectionForeground():list.getForeground();
        
        LB_Title.setText(value.getTitle());
        LB_Title.setForeground(fg);
        
        LB_Thumbnail.setIcon(value.getThumbnail()==null?null:new ImageIcon(value.getThumbnail()));
        
        if (isSelected) {
            setBackground(list.getSelectionBackground());
            
        } else {
            setBackground(list.getBackground());
            
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

        LB_Thumbnail = new javax.swing.JLabel();
        LB_Title = new javax.swing.JLabel();

        setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        LB_Thumbnail.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LB_Thumbnail.setPreferredSize(new java.awt.Dimension(128, 84));
        add(LB_Thumbnail);

        LB_Title.setText("...");
        add(LB_Title);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel LB_Thumbnail;
    private javax.swing.JLabel LB_Title;
    // End of variables declaration//GEN-END:variables

    
}
