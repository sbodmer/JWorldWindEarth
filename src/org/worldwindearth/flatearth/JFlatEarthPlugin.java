/*
 * JNotesPanel.java
 *
 * Created on July 26, 2006, 3:09 PM
 */
package org.worldwindearth.flatearth;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.globes.EarthFlat;
import gov.nasa.worldwind.globes.projections.ProjectionMercator;
import gov.nasa.worldwind.terrain.ZeroElevationModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.util.*;
import javax.swing.*;
import org.tinyrcp.App;
import org.tinyrcp.TinyFactory;
import org.w3c.dom.*;
import org.tinyrcp.TinyPlugin;
import org.worldwindearth.WWEFactory;
import org.worldwindearth.components.JPlanet;

/**
 * 
 * @author sbodmer
 */
public class JFlatEarthPlugin extends JPanel implements TinyPlugin {

    App app = null;
    ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
    TinyFactory factory = null;
    
    /**
     * Main planet panel
     */
    JPlanet jplanet = null;
    
    String name = "";
    /**
     * Creates new form JTerminals
     */
    public JFlatEarthPlugin(TinyFactory factory) {
        this.factory = factory;
        jplanet = new JPlanet();
        name =  factory.getFactoryName();
        
        initComponents();
        
        add(jplanet, BorderLayout.CENTER);
    }

    //**************************************************************************
    //*** API
    //**************************************************************************
    public void addActionListener(ActionListener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    public void removeActionListener(ActionListener listener) {
        listeners.remove(listener);
    }

    @Override
    public String toString() {
        return getPluginName();
    }
    
    //***************************************************************************
    //*** WWEPlugin
    //***************************************************************************    
    @Override
    public String getPluginName() {
        return name;
    }

    @Override
    public void setPluginName(String name) {
        this.name = name.equals("")?factory.getFactoryName():name;
    }

    @Override
    public JComponent getConfigComponent() {
        return null;
    }

    @Override
    public TinyFactory getPluginFactory() {
        return factory;
    }

    
    @Override
    public void setup(App app, Object obj) {
        this.app = app;
        
        //--- Flat world
        Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
        // wwd.getSceneController().setVerticalExaggeration(10d);
        
        EarthFlat ef = new EarthFlat();
        ef.setElevationModel(new ZeroElevationModel());
        m.setGlobe(ef);
        ef.setProjection(new ProjectionMercator());
        
        jplanet.initialize(app, m, WWEFactory.PLANET_EARTH);
    }
    
    @Override
    public void configure(Element config) {
        if (config == null) {
            try {
                //--- Load default config
                InputStream in = app.getLoader().getResourceAsStream("org/worldwindearth/flatearth/Resources/Configs/FlatEarth.xml");
                config = app.getDocumentBuilder().parse(in).getDocumentElement();
                
            } catch (Exception ex) {

            }

        }
        jplanet.configure(config);
        jplanet.revalidate();
        jplanet.repaint();
        
    }

    @Override
    public void cleanup() {
        jplanet.destroy();

        listeners.clear();
        
    }

    /**
     * Store current view position and frame location
     *
     * @param config
     */
    @Override
    public void saveConfig(Element config) {
        if (config == null) return;

        jplanet.save(config);

    }

    @Override
    public JComponent getVisualComponent() {
        return this;
    }

    @Override
    public void setProperty(String name, Object obj) {
        //---
    }

    @Override
    public Object getProperty(String name) {
        return null;
    }

    @Override
    public Object doAction(String action, Object argument, Object subject) {
        return null;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btgblayers = new javax.swing.ButtonGroup();
        PU_Layers = new javax.swing.JPopupMenu();
        MN_NewLayers = new javax.swing.JMenu();
        jSeparator7 = new javax.swing.JSeparator();
        MN_Rename = new javax.swing.JMenuItem();
        MN_RemoveLayer = new javax.swing.JMenuItem();
        PU_More = new javax.swing.JPopupMenu();
        MN_Screens = new javax.swing.JMenu();
        MN_Fullscreen = new javax.swing.JCheckBoxMenuItem();
        MN_ScreenIdentifier = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        btgscreens = new javax.swing.ButtonGroup();

        MN_NewLayers.setText("New layer");
        PU_Layers.add(MN_NewLayers);
        PU_Layers.add(jSeparator7);

        MN_Rename.setText("Rename");
        MN_Rename.setActionCommand("renameLayer");
        PU_Layers.add(MN_Rename);

        MN_RemoveLayer.setText("Remove");
        MN_RemoveLayer.setActionCommand("removeLayer");
        PU_Layers.add(MN_RemoveLayer);

        MN_Screens.setText("Screens");

        MN_Fullscreen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, 0));
        MN_Fullscreen.setText("Fullscreen");
        MN_Fullscreen.setActionCommand("fullscreen");
        MN_Screens.add(MN_Fullscreen);

        MN_ScreenIdentifier.setText("Screen identifier");
        MN_ScreenIdentifier.setActionCommand("screenIdentifier");
        MN_Screens.add(MN_ScreenIdentifier);
        MN_Screens.add(jSeparator6);

        PU_More.add(MN_Screens);

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBoxMenuItem MN_Fullscreen;
    private javax.swing.JMenu MN_NewLayers;
    private javax.swing.JMenuItem MN_RemoveLayer;
    private javax.swing.JMenuItem MN_Rename;
    private javax.swing.JMenuItem MN_ScreenIdentifier;
    private javax.swing.JMenu MN_Screens;
    private javax.swing.JPopupMenu PU_Layers;
    private javax.swing.JPopupMenu PU_More;
    private javax.swing.ButtonGroup btgblayers;
    private javax.swing.ButtonGroup btgscreens;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    // End of variables declaration//GEN-END:variables

}
