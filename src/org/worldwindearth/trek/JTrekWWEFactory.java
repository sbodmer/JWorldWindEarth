/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.trek;

import gov.nasa.worldwind.WorldWindow;
import javax.swing.Icon;
import javax.swing.JComponent;
import org.tinyrcp.App;
import org.tinyrcp.TinyPlugin;
import org.w3c.dom.Element;
import org.worldwindearth.WWEFactory;

/**
 * 
 * @author sbodmer
 */
public class JTrekWWEFactory extends javax.swing.JPanel implements WWEFactory {
    App app = null;
    
    /**
     * 
     */
    public JTrekWWEFactory() {

        initComponents();


    }

    //**************************************************************************
    //*** API
    //**************************************************************************
   

    //**************************************************************************
    //*** WWEPluginFactory
    //**************************************************************************
    @Override
    public Icon getFactoryIcon(int size) {
        return LB_Name.getIcon();
    }

    @Override
    public String getFactoryName() {
        return LB_Name.getText();
    }

    @Override
    public String getFactoryDescription() {
        return LB_Description.getText();
    }

    @Override
    public String getFactoryCategory() {
        return PLUGIN_CATEGORY_WORLDWIND_LAYER;
    }

    @Override
    public String getFactoryFamily() {
        return PLUGIN_FAMILY_WORLDWIND_LAYER_MEASURES;
    }

    @Override
    public JComponent getFactoryConfigComponent() {
        return null;
    }

    @Override
    public void initialize(App app) {
        this.app = app;

       
    }

    @Override
    public void configure(Element config) {
        if (config == null) return;

        
    }

    @Override
    public TinyPlugin newPlugin(Object o) {
        return new JTrekWWEPlugin(this, (WorldWindow) o);
    }

    @Override
    public void store(Element config) {
        if (config == null) return;

        
    }

    @Override
    public void destroy() {
       //---
    }

    @Override
    public Object getProperty(String property) {
        return null;
    }

    public boolean doesFactorySupport(Object obj) {
        if (obj != null) return obj.toString().equals(WWEFactory.PLANET_EARTH);
        return false;
    }

   

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        LB_Name = new javax.swing.JLabel();
        LB_Description = new javax.swing.JLabel();

        LB_Name.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/trek/Resources/Icons/22x22/trek.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/worldwindearth/trek/Trek"); // NOI18N
        LB_Name.setText(bundle.getString("factory_name")); // NOI18N

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JLabel LB_Description;
    protected javax.swing.JLabel LB_Name;
    // End of variables declaration//GEN-END:variables

}