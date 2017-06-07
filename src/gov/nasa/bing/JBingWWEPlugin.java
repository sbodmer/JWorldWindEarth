/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.bing;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.TiledImageLayer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.tinyrcp.App;
import org.tinyrcp.TinyFactory;
import org.w3c.dom.Element;
import org.worldwindearth.WWEPlugin;

/**
 * Create layer from config/Earth/BingImagery.xml
 *
 * @author sbodmer
 */
public class JBingWWEPlugin extends JPanel implements WWEPlugin, ActionListener, ChangeListener {

    App app = null;
    TinyFactory factory = null;
    WorldWindow ww = null;

    TiledImageLayer layer = null;

    /**
     * Creates new form JTerminalsLayer
     */
    public JBingWWEPlugin(TinyFactory factory, WorldWindow ww) {
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

        //--- Create layer form defaut config/Earth/BMNGWMSLayer2.xml
        BasicLayerFactory bl = new BasicLayerFactory();
        layer = (TiledImageLayer) bl.createFromConfigSource("config/Earth/BingImagery.xml", null);
        layer.setName("Bing sat images");
        layer.setValue(AVKEY_WORLDWIND_LAYER_PLUGIN, this);

        SP_Opacity.addChangeListener(this);
        CB_DrawTileIds.addActionListener(this);
        CB_DrawTileBoundaries.addActionListener(this);
        CB_DrawTileVolume.addActionListener(this);
        
    }

    @Override
    public void configure(Element config) {
        if (config == null) return;
        
        try {
            SP_Opacity.setValue(Integer.parseInt(config.getAttribute("opacity")));
            CB_DrawTileIds.setSelected(config.getAttribute("drawTileIds").equals("true"));
            CB_DrawTileBoundaries.setSelected(config.getAttribute("drawTileBoundaries").equals("true"));
            CB_DrawTileVolume.setSelected(config.getAttribute("drawTileVolume").equals("true"));
            
            layer.setOpacity(SP_Opacity.getValue()/100d);
            layer.setDrawTileIDs(CB_DrawTileIds.isSelected());
            layer.setDrawTileBoundaries(CB_DrawTileBoundaries.isSelected());
            layer.setDrawBoundingVolumes(CB_DrawTileVolume.isSelected());
            
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
        
        config.setAttribute("opacity", ""+SP_Opacity.getValue());
        config.setAttribute("drawTileIds", ""+CB_DrawTileIds.isSelected());
        config.setAttribute("drawTileBoundaries", ""+CB_DrawTileBoundaries.isSelected());
        config.setAttribute("drawTileVolume", ""+CB_DrawTileVolume.isSelected());
        
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
    public Object doAction(String action, Object argument) {
        return null;
    }

    @Override
    public void setPluginName(String name) {
        //---
    }

    @Override
    public JComponent getVisualComponent() {
        return null;
    }

    //**************************************************************************
    //*** WorldWindLayerPlugin
    //**************************************************************************
    @Override
    public Layer getLayer() {
        return layer;
    }

    @Override
    public JToggleButton getLayerButton() {
        return null;
    }

    //**************************************************************************
    //*** ActionListener
    //**************************************************************************
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("drawTileIds")) {
            layer.setDrawTileIDs(CB_DrawTileIds.isSelected());
            ww.redraw();
            
        } else if (e.getActionCommand().equals("drawTileBoundaries")) {
            layer.setDrawTileBoundaries(CB_DrawTileBoundaries.isSelected());
            ww.redraw();
            
        } else if (e.getActionCommand().equals("drawTileVolume")) {
            layer.setDrawBoundingVolumes(CB_DrawTileVolume.isSelected());
            ww.redraw();
        }
    }

    //**************************************************************************
    //*** ChangeListener
    //**************************************************************************
    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == SP_Opacity) {
            layer.setOpacity(SP_Opacity.getValue()/100d);
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
        SP_Opacity = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        CB_DrawTileIds = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        CB_DrawTileBoundaries = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        CB_DrawTileVolume = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        SP_Opacity.setFont(new java.awt.Font("Monospaced", 0, 10)); // NOI18N
        SP_Opacity.setMajorTickSpacing(10);
        SP_Opacity.setMinorTickSpacing(5);
        SP_Opacity.setPaintLabels(true);
        SP_Opacity.setPaintTicks(true);
        SP_Opacity.setValue(100);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Draw tile ID");
        jLabel1.setPreferredSize(new java.awt.Dimension(140, 26));

        CB_DrawTileIds.setActionCommand("drawTileIds");
        CB_DrawTileIds.setPreferredSize(new java.awt.Dimension(26, 26));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel2.setText("Draw tile boundaries");
        jLabel2.setPreferredSize(new java.awt.Dimension(140, 26));

        CB_DrawTileBoundaries.setActionCommand("drawTileBoundaries");
        CB_DrawTileBoundaries.setPreferredSize(new java.awt.Dimension(26, 26));

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel3.setText("Draw tile volume");
        jLabel3.setPreferredSize(new java.awt.Dimension(140, 26));

        CB_DrawTileVolume.setActionCommand("drawTileVolume");
        CB_DrawTileVolume.setPreferredSize(new java.awt.Dimension(26, 26));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(SP_Opacity, javax.swing.GroupLayout.DEFAULT_SIZE, 395, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(CB_DrawTileIds, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(CB_DrawTileBoundaries, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(CB_DrawTileVolume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SP_Opacity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CB_DrawTileIds, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CB_DrawTileBoundaries, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CB_DrawTileVolume, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(162, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.NORTH);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 407, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 135, Short.MAX_VALUE)
        );

        add(jPanel2, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox CB_DrawTileBoundaries;
    private javax.swing.JCheckBox CB_DrawTileIds;
    private javax.swing.JCheckBox CB_DrawTileVolume;
    private javax.swing.JSlider SP_Opacity;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    // End of variables declaration//GEN-END:variables

}
