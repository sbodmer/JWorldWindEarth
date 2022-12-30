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
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.w3c.dom.Element;
import org.tinyrcp.App;
import org.tinyrcp.TinyFactory;
import org.worldwindearth.WWEPlugin;

/**
 * Create layer from default xml
 * 
 * @author sbodmer
 */
public class JLandsatWWEPlugin extends JPanel implements WWEPlugin, ActionListener, ChangeListener {
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
        return this;
    }

    @Override
    public TinyFactory getPluginFactory() {
        return factory;
    }

    @Override
    public void setup(App app, Object arg) {
        this.app = app;
        
        SL_Opacity.addChangeListener(this);
        
        
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
        if (config == null) return;
        
        config.setAttribute("opacity", ""+SL_Opacity.getValue());
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
        if (config == null) return;
        
        try {
            SL_Opacity.setValue(Integer.parseInt(config.getAttribute("opacity")));
            layer.setOpacity(SL_Opacity.getValue()/100d);
            layer.setUseTransparentTextures(layer.getOpacity()==1?false:true);
            
        } catch (NumberFormatException ex) {
           //--- 
        }
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
    
    @Override
    public void layerMouseClicked(MouseEvent e, gov.nasa.worldwind.geom.Position pos) {
        //---
    }
    
    //**************************************************************************
    //*** ActionListener
    //**************************************************************************
    @Override
    public void actionPerformed(ActionEvent e) {
        //---
    }
    
    //**************************************************************************
    //*** ChangeListener
    //**************************************************************************
    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == SL_Opacity) {
            layer.setOpacity(SL_Opacity.getValue()/100d);
            layer.setUseTransparentTextures(layer.getOpacity()==1?false:true);
            ww.redraw();
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        SL_Opacity = new javax.swing.JSlider();

        setLayout(new java.awt.BorderLayout());

        SL_Opacity.setFont(new java.awt.Font("Arial", 0, 9)); // NOI18N
        SL_Opacity.setMajorTickSpacing(10);
        SL_Opacity.setMinorTickSpacing(5);
        SL_Opacity.setPaintLabels(true);
        SL_Opacity.setPaintTicks(true);
        SL_Opacity.setToolTipText("Transparency");
        SL_Opacity.setValue(100);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SL_Opacity, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(SL_Opacity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        add(jPanel1, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSlider SL_Opacity;
    private javax.swing.JPanel jPanel1;
    // End of variables declaration//GEN-END:variables
}
