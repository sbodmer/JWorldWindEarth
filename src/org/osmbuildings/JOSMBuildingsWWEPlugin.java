/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osmbuildings;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.tinyrcp.App;
import org.w3c.dom.Element;
import org.worldwindearth.WWEFactory;
import org.worldwindearth.WWEFactory;
import org.worldwindearth.WWEPlugin;

/**
 *
 * @author sbodmer
 */
public class JOSMBuildingsWWEPlugin extends javax.swing.JPanel implements WWEPlugin, ChangeListener, ActionListener {
    WWEFactory factory = null;
    App app = null;
    WorldWindow ww = null;
    
    OSMBuildingsLayer layer = null;
    
    /**
     * Creates new form OSMBuildingsWWELayerPlugin
     */
    public JOSMBuildingsWWEPlugin(WWEFactory factory, WorldWindow ww) {
        this.factory = factory;
        this.ww = ww;
        
        initComponents();
       
        
    }

    //**************************************************************************
    //*** WWEPlugin
    //**************************************************************************
    @Override
    public WWEFactory getPluginFactory() {
        return factory;
    }

    @Override
    public String getPluginName() {
        return layer.getName();
    }
    
    @Override
    public void setPluginName(String name) {
        layer.setName(name);
    }
    
    //**************************************************************************
    //***WWELayerPlugin
    //**************************************************************************
    @Override
    public Layer getLayer() {
        return layer;
    }

    @Override
    public JToggleButton getLayerButton() {
        return BT_Layer;
    }

    //**************************************************************************
    //*** WWEPlugin
    //**************************************************************************
    @Override
    public JComponent getVisualComponent() {
        return null;
    }

    @Override
    public JComponent getConfigComponent() {
        return this;
    }

    @Override
    public Object doAction(String message, Object argument) {
        return null;
    }

    @Override
    public void setup(App app, Object argument) {
        this.app = app;
     
        BasicLayerFactory bl = new BasicLayerFactory();
        
        InputStream in = getClass().getResourceAsStream("/org/osmbuildings/Resources/Config/OSMBuildings.xml");
        layer = (OSMBuildingsLayer) bl.createFromConfigSource(in, null);
        layer.setName("OSMBuildings");
        layer.setValue(AVKEY_WORLDWIND_LAYER_PLUGIN, this);
        
        SP_DefaultHeight.addChangeListener(this);
        CB_DrawProcessingBox.addActionListener(this);
        SP_MaxTiles.addChangeListener(this);
        SP_Opacity.addChangeListener(this);
        CB_DrawOutline.addActionListener(this);
        CB_ApplyTextures.addActionListener(this);
        
    }

    @Override
    public void configure(Element config) {
        if (config == null) return;
        
        try {
            SP_DefaultHeight.setValue(Integer.parseInt(config.getAttribute("defaultHeight")));
            layer.setDefaultBuildingHeight((int) SP_DefaultHeight.getValue());
            CB_DrawProcessingBox.setSelected(config.getAttribute("drawProcessingBox").equals("true"));
            layer.setDrawProcessingBox(CB_DrawProcessingBox.isSelected());
            SP_MaxTiles.setValue(Integer.parseInt(config.getAttribute("maxTiles")));
            layer.setMaxTiles((int) SP_MaxTiles.getValue());
            SP_Opacity.setValue(Integer.parseInt(config.getAttribute("opacity")));
            layer.setOpacity(SP_Opacity.getValue()/100d);
            CB_DrawOutline.setSelected(config.getAttribute("drawOutline").equals("true"));
            CB_ApplyTextures.setSelected(config.getAttribute("applyTextures").equals("true"));
            
        } catch (NumberFormatException ex) {
            //---
        }
        
    }
    
    @Override
    public void cleanup() {
        layer.dispose();
    }

    @Override
    public void saveConfig(Element config) {
        if (config == null) return;
        
        config.setAttribute("defaultHeight", SP_DefaultHeight.getValue().toString());
        config.setAttribute("drawProcessingBox", ""+CB_DrawProcessingBox.isSelected());
        config.setAttribute("maxTiles", SP_MaxTiles.getValue().toString());
        config.setAttribute("opacity", ""+SP_Opacity.getValue());
        config.setAttribute("drawOutline", ""+CB_DrawOutline.isSelected());
        config.setAttribute("applyTextures", ""+CB_ApplyTextures.isSelected());
    }

    @Override
    public Object getProperty(String name) {
        return null;
    }

    @Override
    public void setProperty(String name, Object value) {
        //---
    }
    
    //**************************************************************************
    //*** ChangeListener
    //**************************************************************************
    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == SP_DefaultHeight) {
            layer.setDefaultBuildingHeight((int) SP_DefaultHeight.getValue());
            layer.clearTiles();
            
        } else if (e.getSource() == SP_MaxTiles) {
            layer.setMaxTiles((int) SP_MaxTiles.getValue());
            
        }else if (e.getSource() == SP_Opacity) {
            layer.setOpacity(SP_Opacity.getValue()/100d);
            
        }
        ww.redraw();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("drawProcessingBox")) {
            layer.setDrawProcessingBox(CB_DrawProcessingBox.isSelected());
            
        } else if (e.getActionCommand().equals("drawOutline")) {
            layer.setDrawOutline(CB_DrawOutline.isSelected());
            
        } else if (e.getActionCommand().equals("applyTextures")) {
            layer.setApplyTextures(CB_ApplyTextures.isSelected());
            
        }
        ww.redraw();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        BT_Layer = new javax.swing.JToggleButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        SP_DefaultHeight = new javax.swing.JSpinner();
        CB_DrawProcessingBox = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        SP_MaxTiles = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        CB_DrawOutline = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        CB_ApplyTextures = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        SP_Opacity = new javax.swing.JSlider();

        BT_Layer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/osmbuildings/Resources/Icons/22x22/osmbuildings.png"))); // NOI18N
        BT_Layer.setPreferredSize(new java.awt.Dimension(32, 32));

        setLayout(new java.awt.BorderLayout());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Default height");
        jLabel1.setPreferredSize(new java.awt.Dimension(150, 26));

        SP_DefaultHeight.setModel(new javax.swing.SpinnerNumberModel(10, 0, 1000, 1));
        SP_DefaultHeight.setPreferredSize(new java.awt.Dimension(70, 26));

        CB_DrawProcessingBox.setSelected(true);
        CB_DrawProcessingBox.setPreferredSize(new java.awt.Dimension(26, 26));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Draw processing box");
        jLabel2.setPreferredSize(new java.awt.Dimension(150, 26));

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Maximum tiles");
        jLabel3.setPreferredSize(new java.awt.Dimension(150, 26));

        SP_MaxTiles.setModel(new javax.swing.SpinnerNumberModel(30, 1, 256, 1));
        SP_MaxTiles.setPreferredSize(new java.awt.Dimension(70, 26));

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Draw outline");
        jLabel5.setPreferredSize(new java.awt.Dimension(150, 26));

        CB_DrawOutline.setActionCommand("drawOutline");
        CB_DrawOutline.setPreferredSize(new java.awt.Dimension(26, 26));

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Apply textures");
        jLabel6.setPreferredSize(new java.awt.Dimension(150, 26));

        CB_ApplyTextures.setActionCommand("applyTextures");
        CB_ApplyTextures.setPreferredSize(new java.awt.Dimension(26, 26));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(CB_DrawProcessingBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(SP_DefaultHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(SP_MaxTiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(152, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(CB_DrawOutline, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(CB_ApplyTextures, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SP_DefaultHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(CB_DrawProcessingBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SP_MaxTiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CB_DrawOutline, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CB_ApplyTextures, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(91, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);

        SP_Opacity.setFont(new java.awt.Font("Monospaced", 0, 10)); // NOI18N
        SP_Opacity.setMajorTickSpacing(10);
        SP_Opacity.setMinorTickSpacing(5);
        SP_Opacity.setPaintLabels(true);
        SP_Opacity.setPaintTicks(true);
        SP_Opacity.setToolTipText("Opacity");
        SP_Opacity.setValue(100);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SP_Opacity, javax.swing.GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SP_Opacity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel2, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents

    

    

    


    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JToggleButton BT_Layer;
    protected javax.swing.JCheckBox CB_ApplyTextures;
    protected javax.swing.JCheckBox CB_DrawOutline;
    protected javax.swing.JCheckBox CB_DrawProcessingBox;
    protected javax.swing.JSpinner SP_DefaultHeight;
    protected javax.swing.JSpinner SP_MaxTiles;
    protected javax.swing.JSlider SP_Opacity;
    protected javax.swing.JLabel jLabel1;
    protected javax.swing.JLabel jLabel2;
    protected javax.swing.JLabel jLabel3;
    protected javax.swing.JLabel jLabel5;
    protected javax.swing.JLabel jLabel6;
    protected javax.swing.JPanel jPanel1;
    protected javax.swing.JPanel jPanel2;
    // End of variables declaration//GEN-END:variables

    

    
}
