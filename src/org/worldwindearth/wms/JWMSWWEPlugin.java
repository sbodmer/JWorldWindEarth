/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.wms;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.TiledImageLayer;
import gov.nasa.worldwind.ogc.OGCCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSCapabilityInformation;
import gov.nasa.worldwind.util.DataConfigurationUtils;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.wms.CapabilitiesRequest;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URI;
import java.net.URL;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import org.tinyrcp.App;
import org.w3c.dom.Element;
import org.worldwindearth.WWEFactory;
import org.worldwindearth.WWEPlugin;

/**
 * Create layer from config/Earth/BMNGWMSLayer2.xml
 *
 * @author sbodmer
 */
public class JWMSWWEPlugin extends JPanel implements WWEPlugin, ActionListener, ItemListener {

    App app = null;
    WWEFactory factory = null;
    WorldWindow ww = null;

    WrappedWMSTiledImageLayer layer = null;

    /**
     * Creates new form WMS layer
     *
     * @param factory
     */
    public JWMSWWEPlugin(WWEFactory factory, WorldWindow ww) {
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
        return layer.getName();
    }

    @Override
    public void setPluginName(String name) {
        layer.setName(name);
    }

    @Override
    public JComponent getConfigComponent() {
        return this;
    }

    @Override
    public WWEFactory getPluginFactory() {
        return factory;
    }

    @Override
    public void setup(App app, Object arg) {
        this.app = app;

        /*
        //--- Create layer form defaut config
        BasicLayerFactory bl = new BasicLayerFactory();
        layer = (TiledImageLayer) bl.createFromConfigSource("config/WMSLayerTemplate.xml", null);
        layer.setName("Generic WMS");
        layer.setValue(AVKEY_WORLDWIND_LAYER_PLUGIN, this);
         */
        
        AVListImpl av =new AVListImpl();
        av.setValue(AVKey.NUM_LEVELS, 19);
        av.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, LatLon.fromDegrees(90, 180));
        av.setValue(AVKey.SECTOR, Sector.FULL_SPHERE);
        av.setValue(AVKey.TILE_WIDTH, 256);
        av.setValue(AVKey.TILE_HEIGHT, 256);
        av.setValue(AVKey.FORMAT_SUFFIX,".png");
        av.setValue(AVKey.DATA_CACHE_NAME,"WMS");
        av.setValue(AVKey.DATASET_NAME, "generic");
        LevelSet.SectorResolution[] sectorLimits = { 
            new LevelSet.SectorResolution(Sector.FULL_SPHERE, 19)
        };
        av.setValue(AVKey.SECTOR_RESOLUTION_LIMITS, sectorLimits);
        layer = new WrappedWMSTiledImageLayer(av);
        layer.setEnabled(false);
        
        CMB_Server.addItemListener(this);
    }

    @Override
    public void configure(Element config) {
        //--- Load the stored WMS server list
        CMB_Server.removeItemListener(this);
        try {
            CMB_Server.addItem(new WMSServer("<none>", null));
            CMB_Server.addItem(new WMSServer("Switzerland / BGDI", new URI("http://wms.geo.admin.ch/")));// ?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities")));

        } catch (Exception ex) {
            ex.printStackTrace();

        }
        CMB_Server.addItemListener(this);
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
    public Object doAction(String action, Object argument) {
        return null;
    }

    @Override
    public JComponent getVisualComponent() {
        return null;
    }

    //**************************************************************************
    //*** WWELayerPlugin
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
        //---
    }

    //**************************************************************************
    //*** ItemListener
    //**************************************************************************
    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            PB_Waiting.setIndeterminate(true);
            
            WMSServer w = (WMSServer) CMB_Server.getSelectedItem();
            try {
                AVListImpl av = new AVListImpl();
                av.setValue(AVKey.SERVICE_NAME, "WMS");
                av.setValue(AVKey.GET_CAPABILITIES_URL, w.getApi().toString());
                // this.setParms(AVKey.R"REQUEST", "GetCapabilities");
                // this.setParam("VERSION", "1.3.0");
                URL url = DataConfigurationUtils.getOGCGetCapabilitiesURL(av);
                System.out.println("URL:"+url);
                WMSCapabilities cap = WMSCapabilities.retrieve(w.getApi());
                System.out.println("CAP:"+(cap==null?"NULL":"NOT NULL"));
                System.out.println("FORMAT:"+cap.getImageFormats());
                
                // System.out.println("CAP:"+cap.getRequestURL("GetCapabilities", "1.1.1", "GET"));
                WMSCapabilityInformation info = cap.getCapabilityInformation();
                System.out.println("INFO:"+info.getImageFormats());
                
            } catch (Exception ex) {
                ex.printStackTrace();
                
            }
            PB_Waiting.setIndeterminate(false);
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
        jScrollPane1 = new javax.swing.JScrollPane();
        TB_Layers = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        CMB_Server = new javax.swing.JComboBox<>();
        jPanel3 = new javax.swing.JPanel();
        PB_Waiting = new javax.swing.JProgressBar();

        setLayout(new java.awt.BorderLayout());

        TB_Layers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Active", "Layer"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(TB_Layers);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                .addContainerGap())
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(CMB_Server, 0, 388, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(CMB_Server, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel2, java.awt.BorderLayout.NORTH);

        jPanel3.add(PB_Waiting);

        add(jPanel3, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<WMSServer> CMB_Server;
    private javax.swing.JProgressBar PB_Waiting;
    private javax.swing.JTable TB_Layers;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

}
