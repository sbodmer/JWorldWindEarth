/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.landsat;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.TiledImageLayer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import org.w3c.dom.Element;
import org.tinyrcp.App;
import org.tinyrcp.TinyFactory;
import org.worldwindearth.WWEPlugin;

/**
 * Create layer from default xml
 * 
 * @author sbodmer
 */
public class JLandsatWWEPlugin extends JPanel implements WWEPlugin, ActionListener {
    App app = null;
    TinyFactory factory = null;
    WorldWindow ww = null;
    
    TiledImageLayer layer = null;
    
    /**
     * Creates new form JTerminalsLayer
     */
    public JLandsatWWEPlugin(TinyFactory factory, WorldWindow ww) {
        super();
        this.factory = factory;
        this.ww = ww;
        
        initComponents();
        
        
    }

    
    
    //**************************************************************************
    //*** Plugin
    //**************************************************************************
    @Override
    public String getPluginName() {
        return factory.getFactoryName();
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
    public void setup(App app, Object arg) {
        this.app = app;
        
        //--- Create layer form defaut config
        BasicLayerFactory bl = new BasicLayerFactory();
        layer = (TiledImageLayer) bl.createFromConfigSource("config/Earth/LandsatI3WMSLayer2.xml", null);
        layer.setName("Landsat");
        
    }

    @Override
    public void cleanup() {
        layer.dispose();
        
    }

    @Override
    public void saveConfig(Element config) {
        //---
    }

    @Override
    public Object getProperty(String property) {
        return null;
    }

    @Override
    public void setProperty(String property, Object value) {
        //---
    }

    @Override
    public Object doAction(String action, Object argument, Object subject) {
        return null;
    }

    @Override
    public void setPluginName(String name) {
        layer.setName(name);
    }

    @Override
    public JComponent getVisualComponent() {
        return null;
    }

    @Override
    public void configure(Element config) {
        //---
    }
    
    //**************************************************************************
    //*** WorldWindLayerPlugin
    //**************************************************************************
    @Override
    public Layer getLayer() {
        return layer;
    }
    
    @Override
    public boolean hasLayerButton() {
        return false;
    }
    
    //**************************************************************************
    //*** ActionListener
    //**************************************************************************
    @Override
    public void actionPerformed(ActionEvent e) {
        //---
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
