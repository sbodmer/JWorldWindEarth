/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osmbuildings;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.ExtrudedPolygon;
import gov.nasa.worldwind.render.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.tinyrcp.App;
import org.w3c.dom.Element;
import org.worldwindearth.WWEFactory;
import org.worldwindearth.WWEPlugin;

/**
 *
 * @author sbodmer
 */
public class JOSMBuildingsWWEPlugin extends javax.swing.JPanel implements WWEPlugin, ChangeListener, ActionListener, SelectListener {

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
    //*** API
    //**************************************************************************
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
    public boolean hasLayerButton() {
        return true;
    }

    @Override
    public void layerMouseClicked(MouseEvent e, gov.nasa.worldwind.geom.Position pos) {
        //---
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
    public Object doAction(String message, Object argument, Object subject) {
        return null;
    }

    @Override
    public void setup(App app, Object argument) {
        this.app = app;

        BasicLayerFactory bl = new BasicLayerFactory();

        InputStream in = getClass().getResourceAsStream("/org/osmbuildings/Resources/Config/OSMBuildings.xml");
        // InputStream in = app.getLoader().getResourceAsStream("org/osmbuildings/Resources/Config/OSMBuildings.xml");
        layer = (OSMBuildingsLayer) bl.createFromConfigSource(in, null);
        layer.setName("OSMBuildings");
        layer.setExpiryTime(((JOSMBuildingsWWEFactory) factory).getExpireDays() * 24L * 60L * 60L * 1000L);
        // System.out.println("PICK:"+layer.isPickEnabled());
        // layer.setPickEnabled(true);
        ww.addSelectListener(this);

        SP_DefaultHeight.addChangeListener(this);
        CB_DrawProcessingBox.addActionListener(this);
        SP_MaxTiles.addChangeListener(this);
        SP_Opacity.addChangeListener(this);
        SP_Rows.addChangeListener(this);
        
        CB_DrawOutline.addActionListener(this);
        CB_ApplyRoofTextures.addActionListener(this);
        CB_FixedLighting.addActionListener(this);
        
        BT_Clear.addActionListener(this);
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
            layer.setOpacity(SP_Opacity.getValue() / 100d);
            CB_DrawOutline.setSelected(config.getAttribute("drawOutline").equals("true"));
            CB_ApplyRoofTextures.setSelected(config.getAttribute("applyRoofTextures").equals("true"));
            SP_Rows.setValue(Integer.parseInt(config.getAttribute("rows")));
            layer.setResolutionGrid((int) SP_Rows.getValue(), (int) SP_Rows.getValue());
            CB_FixedLighting.setSelected(config.getAttribute("fixedLighting").equals("true"));
            layer.setFixedLighting(config.getAttribute("fixedLighting").equals("true"));
            
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
        config.setAttribute("drawProcessingBox", "" + CB_DrawProcessingBox.isSelected());
        config.setAttribute("maxTiles", SP_MaxTiles.getValue().toString());
        config.setAttribute("opacity", "" + SP_Opacity.getValue());
        config.setAttribute("drawOutline", "" + CB_DrawOutline.isSelected());
        config.setAttribute("applyRoofTextures", "" + CB_ApplyRoofTextures.isSelected());
        config.setAttribute("rows", SP_Rows.getValue().toString());
        config.setAttribute("fixedLighting", ""+CB_FixedLighting.isSelected());
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

        } else if (e.getSource() == SP_Rows) {
            layer.setResolutionGrid((int) SP_Rows.getValue(),(int) SP_Rows.getValue());
            
        } else if (e.getSource() == SP_Opacity) {
            layer.setOpacity(SP_Opacity.getValue() / 100d);

        }
        ww.redraw();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("drawProcessingBox")) {
            layer.setDrawProcessingBox(CB_DrawProcessingBox.isSelected());

        } else if (e.getActionCommand().equals("drawOutline")) {
            layer.setDrawOutline(CB_DrawOutline.isSelected());

        } else if (e.getActionCommand().equals("applyRoofTextures")) {
            layer.setApplyRoofTextures(CB_ApplyRoofTextures.isSelected());

        } else if (e.getActionCommand().equals("fixedLighting")) {
            layer.setFixedLighting(CB_FixedLighting.isSelected());
            
        } else if (e.getActionCommand().equals("clear")) {
            layer.clearTiles();

        }
        ww.redraw();
    }

    //**************************************************************************
    //*** SelectListener
    //**************************************************************************
    @Override
    public void selected(SelectEvent event) {
        // System.out.println("EVENT:"+event);
        if (event.isLeftDoubleClick()) {
            // PickedObject po = event.getTopPickedObject();
            // System.out.println("Double click on " + po.getObject());
            String txt = "";
            List<PickedObject> list = event.getAllTopPickedObjects();
            for (int i = 0; i < list.size(); i++) {
                PickedObject po = list.get(i);
                Object o = po.getObject();
                txt += "Layer   : " + po.getParentLayer().getName() + "\n";
                txt += "Object  : " + o.getClass().getSimpleName() + "\n";
                if (o instanceof ExtrudedPolygon) {
                    ExtrudedPolygon p = (ExtrudedPolygon) o;
                    txt += "Comment : " + p.getValue(OSMBuildingsRenderable.AVKEY_OSMBUILDING_COMMENT) + "\n";
                    txt += "Feature ID   : " +p.getValue(OSMBuildingsRenderable.AVKEY_OSMBUILDING_FEATURE_ID)+"\n";
                    txt += "Inner bounds : "+p.getValue(OSMBuildingsRenderable.AVKEY_OSMBUILDING_HAS_INNER_BOUNDS)+"\n";
                    AVList props = (AVList) p.getValue(AVKey.PROPERTIES);
                    if (props != null) {
                        Iterator<Entry<String, Object>> it = props.getEntries().iterator();
                        while (it.hasNext()) {
                            Entry entry = it.next();
                            txt += "" + entry.getKey() + "=" + entry.getValue() + "\n";
                        }
                    }
                    
                } else if (o instanceof Polygon) {
                    Polygon p = (Polygon) o;
                    txt += "Comment : " + p.getValue(OSMBuildingsRenderable.AVKEY_OSMBUILDING_COMMENT) + "\n";
                    txt += "Feature ID   : " +p.getValue(OSMBuildingsRenderable.AVKEY_OSMBUILDING_FEATURE_ID)+"\n";
                    txt += "Inner bounds : "+p.getValue(OSMBuildingsRenderable.AVKEY_OSMBUILDING_HAS_INNER_BOUNDS)+"\n";
                    
                    AVList props = (AVList) p.getValue(AVKey.PROPERTIES);
                    if (props != null) {
                        Iterator<Entry<String, Object>> it = props.getEntries().iterator();
                        while (it.hasNext()) {
                            Entry entry = it.next();
                            txt += "" + entry.getKey() + "=" + entry.getValue() + "\n";
                        }
                    }
                    
                } else if (o instanceof Ellipsoid) {
                    Ellipsoid p = (Ellipsoid) o;
                    txt += "Comment : " + p.getValue(OSMBuildingsRenderable.AVKEY_OSMBUILDING_COMMENT) + "\n";
                    txt += "Feature ID   : " +p.getValue(OSMBuildingsRenderable.AVKEY_OSMBUILDING_FEATURE_ID)+"\n";
                    
                    AVList props = (AVList) p.getValue(AVKey.PROPERTIES);
                    if (props != null) {
                        Iterator<Entry<String, Object>> it = props.getEntries().iterator();
                        while (it.hasNext()) {
                            Entry entry = it.next();
                            txt += "" + entry.getKey() + "=" + entry.getValue() + "\n";
                        }
                    }
                }
                txt += "\n";
            }
            TA_Object.setText(txt);
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
        jLabel1 = new javax.swing.JLabel();
        SP_DefaultHeight = new javax.swing.JSpinner();
        CB_DrawProcessingBox = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        SP_MaxTiles = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        CB_DrawOutline = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        CB_ApplyRoofTextures = new javax.swing.JCheckBox();
        BT_Clear = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        TA_Object = new javax.swing.JTextArea();
        jLabel7 = new javax.swing.JLabel();
        SP_Rows = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        CB_FixedLighting = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        SP_Opacity = new javax.swing.JSlider();

        setLayout(new java.awt.BorderLayout());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Default height");
        jLabel1.setPreferredSize(new java.awt.Dimension(150, 26));

        SP_DefaultHeight.setModel(new javax.swing.SpinnerNumberModel(10, 0, 1000, 1));
        SP_DefaultHeight.setPreferredSize(new java.awt.Dimension(70, 26));

        CB_DrawProcessingBox.setActionCommand("drawProcessingBox");
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
        jLabel6.setText("Apply roof textures");
        jLabel6.setPreferredSize(new java.awt.Dimension(150, 26));

        CB_ApplyRoofTextures.setActionCommand("applyRoofTextures");
        CB_ApplyRoofTextures.setPreferredSize(new java.awt.Dimension(26, 26));

        BT_Clear.setText("Clear");
        BT_Clear.setActionCommand("clear");

        TA_Object.setColumns(20);
        TA_Object.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        TA_Object.setLineWrap(true);
        TA_Object.setRows(5);
        jScrollPane1.setViewportView(TA_Object);

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("DOuble Click on a polygon to see details");
        jLabel7.setPreferredSize(new java.awt.Dimension(150, 26));

        SP_Rows.setModel(new javax.swing.SpinnerNumberModel(3, 1, 256, 1));
        SP_Rows.setToolTipText("The grid resolution (rows x cols) of the center screen to get buildings data");
        SP_Rows.setPreferredSize(new java.awt.Dimension(70, 26));

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Resolution rows");
        jLabel4.setPreferredSize(new java.awt.Dimension(150, 26));

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("Fixed lighting");
        jLabel8.setPreferredSize(new java.awt.Dimension(150, 26));

        CB_FixedLighting.setSelected(true);
        CB_FixedLighting.setActionCommand("fixedLighting");
        CB_FixedLighting.setPreferredSize(new java.awt.Dimension(26, 26));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1)
                            .addComponent(BT_Clear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(CB_DrawOutline, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(CB_ApplyRoofTextures, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(SP_MaxTiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(19, 251, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(SP_Rows, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(CB_FixedLighting, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SP_Rows, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CB_DrawOutline, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CB_ApplyRoofTextures, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CB_FixedLighting, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 97, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(BT_Clear)
                .addContainerGap())
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
                .addComponent(SP_Opacity, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)
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
    protected javax.swing.JButton BT_Clear;
    protected javax.swing.JCheckBox CB_ApplyRoofTextures;
    protected javax.swing.JCheckBox CB_DrawOutline;
    protected javax.swing.JCheckBox CB_DrawProcessingBox;
    protected javax.swing.JCheckBox CB_FixedLighting;
    protected javax.swing.JSpinner SP_DefaultHeight;
    protected javax.swing.JSpinner SP_MaxTiles;
    protected javax.swing.JSlider SP_Opacity;
    protected javax.swing.JSpinner SP_Rows;
    protected javax.swing.JTextArea TA_Object;
    protected javax.swing.JLabel jLabel1;
    protected javax.swing.JLabel jLabel2;
    protected javax.swing.JLabel jLabel3;
    protected javax.swing.JLabel jLabel4;
    protected javax.swing.JLabel jLabel5;
    protected javax.swing.JLabel jLabel6;
    protected javax.swing.JLabel jLabel7;
    protected javax.swing.JLabel jLabel8;
    protected javax.swing.JPanel jPanel1;
    protected javax.swing.JPanel jPanel2;
    protected javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

}
