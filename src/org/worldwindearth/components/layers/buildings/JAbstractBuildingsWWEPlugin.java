/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.components.layers.buildings;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ScreenCredit;
import gov.nasa.worldwind.render.ScreenCreditImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.tinyrcp.App;
import org.w3c.dom.Element;
import org.worldwindearth.WWEFactory;
import org.worldwindearth.WWEPlugin;

/**
 * Main class to implement to have building representation on the world
 * 
 * @author sbodmer
 */
public abstract class JAbstractBuildingsWWEPlugin extends javax.swing.JPanel implements WWEPlugin, ChangeListener, ActionListener, MouseListener, BuildingsTileListener {

    protected WWEFactory factory = null;
    protected App app = null;
    protected WorldWindow ww = null;
    
    protected File lastDir = new File(System.getProperty("user.home"), "Sources/netbeans/JWorldWindEarth/Resources/GeoJSON");

    protected BuildingsLayer layer = null;

    /**
     * Current selected provider (cannot use combobox model which is shared for
     * all layers)
     */
    protected BuildingsProvider provider = null;

    protected Vec4 defaultSunDirection = null;
    protected Material defaultSunMat = null;

    //--- The loaded tiles
    protected DefaultListModel<BuildingsTile> model = new DefaultListModel<>();
    
    //--- The new layer config
    protected String configPath = "/org/worldwindearth/layers/buildings/Resources/Config/Buildings.xml";
    
    //--- Model visual refresh
    javax.swing.Timer timer = null;
    
    /**
     * Creates new form BuildingsWWELayerPlugin
     */
    public JAbstractBuildingsWWEPlugin(WWEFactory factory, WorldWindow ww, DefaultComboBoxModel<BuildingsProvider> list) {
        this.factory = factory;
        this.ww = ww;

        initComponents();

        LI_Tiles.setModel(model);
        LI_Tiles.setCellRenderer(new JBuildingsTileRenderer());
        
        CMB_Providers.setModel(list);
        timer = new javax.swing.Timer(1000, this);
        
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
        if (message.equals(DO_ACTION_VIEWPORT_NEEDS_REFRESH)) {
            //--- Simulate the stop event, so the layer will refresh the buildings
            Message msg = new Message(View.VIEW_STOPPED, ww);
            layer.onMessage(msg);
        }
        return null;
    }

    @Override
    public void setup(App app, Object argument) {
        this.app = app;

        BasicLayerFactory bl = new BasicLayerFactory();

        InputStream in = getClass().getResourceAsStream(configPath);
        layer = (BuildingsLayer) bl.createFromConfigSource(in, null);
        layer.setName("Buildings");
        layer.setExpiryTime(((JAbstractBuildingsWWEFactory) factory).getExpireDays() * 24L * 60L * 60L * 1000L);
        layer.setBuildingTileListener(this);
        
        ImageIcon ic = (ImageIcon) factory.getFactoryIcon(32);
        BufferedImage buf = new BufferedImage(22,22, BufferedImage.TYPE_INT_RGB);
        buf.getGraphics().drawImage(ic.getImage(), 0,0,null);
        ScreenCredit sc = new ScreenCreditImage(getPluginName(), buf);
        // sc.setLink("http://www.osmbuildings.org");
        sc.setOpacity(1);
        layer.setScreenCreditImage(sc);
        
        // System.out.println("PICK:"+layer.isPickEnabled());
        // layer.setPickEnabled(true);
        provider = (BuildingsProvider) CMB_Providers.getSelectedItem();
        if (provider == null) provider = new BuildingsProvider("local","file:///usr/share/worldwindearth/buildings/${Z}/${X}/${Y}/buildings.xml", 15, 15);
        layer.setMinLevel(provider.getMinLevel());
        layer.setMaxLevel(provider.getMaxLevel());
        layer.setProvider(provider.getUrl());
        LB_Provider.setText(provider.getTitle());

        CB_DrawProcessingBox.addActionListener(this);
        SP_MaxTiles.addChangeListener(this);
        SP_Opacity.addChangeListener(this);
        SP_Rows.addChangeListener(this);
        CB_Draggable.addActionListener(this);
        
       
        BT_Clear.addActionListener(this);
        
        TA_Logs.addMouseListener(this);
        MN_ClearLogs.addActionListener(this);

        BT_Apply.addActionListener(this);

        timer.start();
    }

    @Override
    public void configure(Element config) {
        if (config == null) return;

        try {
            CB_DrawProcessingBox.setSelected(config.getAttribute("drawProcessingBox").equals("true"));
            layer.setDrawProcessingBox(CB_DrawProcessingBox.isSelected());
            SP_MaxTiles.setValue(Integer.parseInt(config.getAttribute("maxTiles")));
            layer.setMaxTiles((int) SP_MaxTiles.getValue());
            SP_Opacity.setValue(Integer.parseInt(config.getAttribute("opacity")));
            layer.setOpacity(SP_Opacity.getValue() / 100d);
            SP_Rows.setValue(Integer.parseInt(config.getAttribute("rows")));
            CB_Draggable.setSelected(config.getAttribute("draggable").equals("true"));
            layer.setDraggable(CB_Draggable.isSelected());
            
            String prov = config.getAttribute("provider");
            if (!prov.equals("")) {
                //--- Find  the provider
                DefaultComboBoxModel<BuildingsProvider> model = (DefaultComboBoxModel<BuildingsProvider>) CMB_Providers.getModel();

                for (int i = 0; i < model.getSize(); i++) {
                    BuildingsProvider pr = model.getElementAt(i);
                    if (pr.getTitle().equals(prov)) {
                        this.provider = pr;
                        break;
                    }
                }
                LB_Provider.setText(provider.getTitle());
                layer.setProvider(provider.getUrl());
                layer.setMinLevel(provider.getMinLevel());
                layer.setMaxLevel(provider.getMaxLevel());
            }
            layer.setRows((int) SP_Rows.getValue());
            layer.setCols((int) SP_Rows.getValue());

        } catch (NumberFormatException ex) {
            //---
        }

    }

    @Override
    public void cleanup() {
        timer.stop();
        layer.dispose();
        
    }

    @Override
    public void saveConfig(Element config) {
        if (config == null) return;

        config.setAttribute("drawProcessingBox", "" + CB_DrawProcessingBox.isSelected());
        config.setAttribute("maxTiles", SP_MaxTiles.getValue().toString());
        config.setAttribute("opacity", "" + SP_Opacity.getValue());
        config.setAttribute("rows", SP_Rows.getValue().toString());
        config.setAttribute("draggable", "" + CB_Draggable.isSelected());
        config.setAttribute("provider", provider.getTitle());

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
        if (e.getSource() == SP_MaxTiles) {
            layer.setMaxTiles((int) SP_MaxTiles.getValue());

        } else if (e.getSource() == SP_Rows) {
            layer.setRows((int) SP_Rows.getValue());
            layer.setCols((int) SP_Rows.getValue());

        } else if (e.getSource() == SP_Opacity) {
            layer.setOpacity(SP_Opacity.getValue() / 100d);

        }
        ww.redraw();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timer) {
            LI_Tiles.repaint();
            
        } else if (e.getActionCommand().equals("drawProcessingBox")) {
            layer.setDrawProcessingBox(CB_DrawProcessingBox.isSelected());

        } else if (e.getActionCommand().equals("draggable")) {
            layer.setDraggable(CB_Draggable.isSelected());
            
        } else if (e.getActionCommand().equals("clear")) {
            layer.clearTiles();

        } else if (e.getActionCommand().equals("clearLogs")) {
            TA_Logs.setText("");

        } else if (e.getActionCommand().equals("apply")) {
            provider = (BuildingsProvider) CMB_Providers.getSelectedItem();
            LB_Provider.setText(provider.getTitle());
            layer.setProvider(provider.getUrl());
            layer.setMinLevel(provider.getMinLevel());
            layer.setMaxLevel(provider.getMaxLevel());
            layer.clearTiles();

        }
        ww.redraw();
    }

    
    //**************************************************************************
    //*** MouseListener
    //**************************************************************************
    @Override
    public void mouseClicked(MouseEvent e) {
        //---
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.isPopupTrigger()) {
            PU_Logs.show(TA_Logs, e.getX(), e.getY());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //---
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        //---
    }

    @Override
    public void mouseExited(MouseEvent e) {
        //---
    }

    //**************************************************************************
    //*** BuildingTileListener
    //**************************************************************************
    
    @Override
    public void buildingLoaded(BuildingsTile btile) {
        
    }

    @Override
    public void buildingLoading(BuildingsTile btile, long current, long total) {
        if (!model.contains(btile)) {
            model.addElement(btile);
        }
        
    }

    @Override
    public void buildingLoadingFailed(BuildingsTile btile, String reason) {
        // if (model.contains(btile)) model.removeElement(btile);
        
    }
    
    @Override
    public void buildingRemoved(BuildingsTile btile) {
        if (model.contains(btile)) model.removeElement(btile);
        
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PU_Logs = new javax.swing.JPopupMenu();
        MN_ClearLogs = new javax.swing.JMenuItem();
        jPanel1 = new javax.swing.JPanel();
        CB_DrawProcessingBox = new javax.swing.JCheckBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        SP_MaxTiles = new javax.swing.JSpinner();
        BT_Clear = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        SP_Rows = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        LI_Tiles = new javax.swing.JList<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        TA_Logs = new javax.swing.JTextArea();
        jLabel9 = new javax.swing.JLabel();
        CB_Draggable = new javax.swing.JCheckBox();
        jPanel2 = new javax.swing.JPanel();
        SP_Opacity = new javax.swing.JSlider();
        CMB_Providers = new javax.swing.JComboBox<>();
        BT_Apply = new javax.swing.JButton();
        LB_Provider = new javax.swing.JLabel();

        MN_ClearLogs.setText("Clear logs");
        MN_ClearLogs.setActionCommand("clearLogs");
        PU_Logs.add(MN_ClearLogs);

        setLayout(new java.awt.BorderLayout());

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

        BT_Clear.setText("Clear");
        BT_Clear.setActionCommand("clear");

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("...");
        jLabel7.setPreferredSize(new java.awt.Dimension(150, 26));

        SP_Rows.setModel(new javax.swing.SpinnerNumberModel(3, 1, 256, 1));
        SP_Rows.setToolTipText("The grid resolution (rows x cols) of the center screen to get buildings data");
        SP_Rows.setPreferredSize(new java.awt.Dimension(70, 26));

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("Resolution rows");
        jLabel4.setPreferredSize(new java.awt.Dimension(150, 26));

        LI_Tiles.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(LI_Tiles);

        jTabbedPane1.addTab("Tiles", jScrollPane3);

        TA_Logs.setEditable(false);
        TA_Logs.setColumns(20);
        TA_Logs.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        TA_Logs.setRows(5);
        jScrollPane2.setViewportView(TA_Logs);

        jTabbedPane1.addTab("Logs", jScrollPane2);

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("Draggable");
        jLabel9.setPreferredSize(new java.awt.Dimension(150, 26));

        CB_Draggable.setActionCommand("draggable");
        CB_Draggable.setPreferredSize(new java.awt.Dimension(26, 26));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(BT_Clear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTabbedPane1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(CB_DrawProcessingBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 155, Short.MAX_VALUE)
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(CB_Draggable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(SP_MaxTiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(SP_Rows, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(CB_DrawProcessingBox, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CB_Draggable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SP_MaxTiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SP_Rows, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 394, Short.MAX_VALUE)
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

        CMB_Providers.setEditable(true);

        BT_Apply.setText("Apply");
        BT_Apply.setActionCommand("apply");

        LB_Provider.setText("...");
        LB_Provider.setOpaque(true);
        LB_Provider.setPreferredSize(new java.awt.Dimension(15, 26));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(CMB_Providers, 0, 1, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BT_Apply))
                    .addComponent(SP_Opacity, javax.swing.GroupLayout.DEFAULT_SIZE, 537, Short.MAX_VALUE)
                    .addComponent(LB_Provider, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CMB_Providers, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BT_Apply))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(LB_Provider, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(SP_Opacity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel2, java.awt.BorderLayout.PAGE_START);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JButton BT_Apply;
    protected javax.swing.JButton BT_Clear;
    protected javax.swing.JCheckBox CB_Draggable;
    protected javax.swing.JCheckBox CB_DrawProcessingBox;
    protected javax.swing.JComboBox<BuildingsProvider> CMB_Providers;
    protected javax.swing.JLabel LB_Provider;
    protected javax.swing.JList<BuildingsTile> LI_Tiles;
    protected javax.swing.JMenuItem MN_ClearLogs;
    protected javax.swing.JPopupMenu PU_Logs;
    protected javax.swing.JSpinner SP_MaxTiles;
    protected javax.swing.JSlider SP_Opacity;
    protected javax.swing.JSpinner SP_Rows;
    protected javax.swing.JTextArea TA_Logs;
    protected javax.swing.JLabel jLabel2;
    protected javax.swing.JLabel jLabel3;
    protected javax.swing.JLabel jLabel4;
    protected javax.swing.JLabel jLabel7;
    protected javax.swing.JLabel jLabel9;
    protected javax.swing.JPanel jPanel1;
    protected javax.swing.JPanel jPanel2;
    protected javax.swing.JScrollPane jScrollPane2;
    protected javax.swing.JScrollPane jScrollPane3;
    protected javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables

    

}
