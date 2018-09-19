/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.groundcursor;

import gov.nasa.fog.*;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.TiledImageLayer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JPanel;
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
public class JGroundCursorWWEPlugin extends JPanel implements WWEPlugin, ActionListener, ChangeListener {

    App app = null;
    TinyFactory factory = null;
    WorldWindow ww = null;

    GroundCursorLayer layer = null;

    /**
     * Creates new form JTerminalsLayer
     */
    public JGroundCursorWWEPlugin(TinyFactory factory, WorldWindow ww) {
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

        SP_Level.addChangeListener(this);
        CB_DrawTileInfo.addActionListener(this);
        CB_DynamicLevel.addActionListener(this);

        //--- Create layer form defaut config
        layer = new GroundCursorLayer();
        layer.setName("Ground cursor");

    }

    @Override
    public void cleanup() {
        layer.dispose();

    }

    @Override
    public void saveConfig(Element config) {
        if (config == null) return;

        config.setAttribute("zoom", "" + SP_Level.getValue());
        config.setAttribute("drawTileInfo", "" + CB_DrawTileInfo.isSelected());

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
            SP_Level.setValue(Integer.parseInt(config.getAttribute("zoom")));
            CB_DrawTileInfo.setSelected(config.getAttribute("drawTileInfo").equals("true"));

            layer.setLevel(SP_Level.getValue());
            layer.setDrawTileInfo(CB_DrawTileInfo.isSelected());

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
        if (e.getActionCommand().equals("drawTileInfo")) {
            layer.setDrawTileInfo(CB_DrawTileInfo.isSelected());
            ww.redraw();

        } else if (e.getActionCommand().equals("dynamic")) {
            if (CB_DynamicLevel.isSelected()) {
                SP_Level.setEnabled(false);

            } else {
                SP_Level.setEnabled(true);
            }

            layer.setDynamicLevel(CB_DynamicLevel.isSelected());
        }
    }

    //**************************************************************************
    //*** ChangeListener
    //**************************************************************************
    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == SP_Level) {
            layer.setLevel(SP_Level.getValue());
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

        SP_Level = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        CB_DrawTileInfo = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        CB_DynamicLevel = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();

        SP_Level.setFont(new java.awt.Font("Monospaced", 0, 10)); // NOI18N
        SP_Level.setMajorTickSpacing(1);
        SP_Level.setMaximum(18);
        SP_Level.setMinimum(12);
        SP_Level.setMinorTickSpacing(1);
        SP_Level.setPaintLabels(true);
        SP_Level.setPaintTicks(true);
        SP_Level.setSnapToTicks(true);
        SP_Level.setToolTipText("Tile zoom");
        SP_Level.setValue(15);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Fixed level");
        jLabel1.setPreferredSize(new java.awt.Dimension(140, 26));

        CB_DrawTileInfo.setActionCommand("drawTileInfo");
        CB_DrawTileInfo.setPreferredSize(new java.awt.Dimension(26, 26));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Dynamic level");
        jLabel2.setPreferredSize(new java.awt.Dimension(140, 26));

        CB_DynamicLevel.setActionCommand("dynamic");
        CB_DynamicLevel.setPreferredSize(new java.awt.Dimension(26, 26));

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Draw tile info");
        jLabel3.setPreferredSize(new java.awt.Dimension(140, 26));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(CB_DynamicLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(SP_Level, javax.swing.GroupLayout.DEFAULT_SIZE, 236, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(CB_DrawTileInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addGap(6, 6, 6))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(SP_Level, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(CB_DynamicLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(CB_DrawTileInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(188, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox CB_DrawTileInfo;
    private javax.swing.JCheckBox CB_DynamicLevel;
    private javax.swing.JSlider SP_Level;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    // End of variables declaration//GEN-END:variables
}
