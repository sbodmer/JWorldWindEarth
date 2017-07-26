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
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerStyle;
import gov.nasa.worldwind.util.DataConfigurationUtils;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.wms.Capabilities;
import gov.nasa.worldwind.wms.CapabilitiesRequest;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import org.tinyrcp.App;
import org.w3c.dom.Element;
import org.worldwindearth.WWEFactory;
import org.worldwindearth.WWEPlugin;

/**
 * Create layer from config/Earth/BMNGWMSLayer2.xml
 *
 * @author sbodmer
 */
public class JWMSWWEPlugin extends JPanel implements WWEPlugin, ActionListener, ItemListener, WMSServer.WMSServerListener, ListSelectionListener {

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

        TB_Layers.getColumnModel().getColumn(0).setMaxWidth(32);
        TB_Layers.getColumnModel().getColumn(1).setCellRenderer(new WMSLayerCapabilitiesCellRenderer());
        TB_Layers.getSelectionModel().addListSelectionListener(this);
        TB_Layers.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        /*
        //--- Create layer form defaut config
        BasicLayerFactory bl = new BasicLayerFactory();
        layer = (TiledImageLayer) bl.createFromConfigSource("config/WMSLayerTemplate.xml", null);
        layer.setName("Generic WMS");
        layer.setValue(AVKEY_WORLDWIND_LAYER_PLUGIN, this);
         */
        AVListImpl av = new AVListImpl();
        av.setValue(AVKey.NUM_LEVELS, 19);
        av.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, LatLon.fromDegrees(90, 180));
        av.setValue(AVKey.SECTOR, Sector.FULL_SPHERE);
        av.setValue(AVKey.TILE_WIDTH, 256);
        av.setValue(AVKey.TILE_HEIGHT, 256);
        av.setValue(AVKey.FORMAT_SUFFIX, ".png");
        av.setValue(AVKey.DATA_CACHE_NAME, "WMS");
        av.setValue(AVKey.DATASET_NAME, "generic");
        LevelSet.SectorResolution[] sectorLimits = {
            new LevelSet.SectorResolution(Sector.FULL_SPHERE, 19)
        };
        av.setValue(AVKey.SECTOR_RESOLUTION_LIMITS, sectorLimits);
        layer = new WrappedWMSTiledImageLayer(av);
        layer.setEnabled(false);
        layer.setValue(WWEPlugin.AVKEY_WORLDWIND_LAYER_PLUGIN, this);
        CMB_Server.addItemListener(this);
    }

    @Override
    public void configure(Element config) {
        //--- Load the stored WMS server list
        CMB_Server.removeItemListener(this);
        try {
            // CMB_Server.addItem(new WMSServer("<none>", null, null));
            CMB_Server.addItem(new WMSServer("Switzerland / BGDI", new URI("http://wms.geo.admin.ch/"), this));// ?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetCapabilities")));
            CMB_Server.addItem(new WMSServer("Switzerland / Geneva / Orthophoto 2011", new  URI("http://ge.ch/ags2/services/Orthophotos_2011/MapServer/WMSServer"), this));
            CMB_Server.addItem(new WMSServer("Switzerland / Geneva / Plan", new URI("http://ge.ch/ags2/services/Plan_Officiel/MapServer/WMSServer"), this));
            //--- Select first one
            CMB_Server.getItemAt(0).fetch();

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
            w.fetch();

            /*
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
             */
        }
    }

    //**************************************************************************
    //*** WMSListener
    //**************************************************************************
    @Override
    public void wmsCapabilitiesLoading(WMSServer wms) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                PB_Waiting.setIndeterminate(true);
                CMB_Server.setEnabled(false);
            }
        });
    }

    @Override
    public void wmsCapabilitiesLoaded(final WMSServer wms, final WMSCapabilities caps) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                PB_Waiting.setIndeterminate(false);
                CMB_Server.setEnabled(true);

                TA_Abstract.setText(caps.getServiceInformation().getServiceAbstract());
                
                
                Iterator<String> fmts = caps.getImageFormats().iterator();
                while (fmts.hasNext()) {
                    String prefix = fmts.next();
                    System.out.println("image:"+prefix);
                }
                
                
                DefaultTableModel model = (DefaultTableModel) TB_Layers.getModel();
                model.setRowCount(0);
                Iterator<WMSLayerCapabilities> it = caps.getNamedLayers().iterator();
                while (it.hasNext()) {
                    WMSLayerCapabilities l = it.next();
                    Set<WMSLayerStyle> styles = l.getStyles();
                    if (styles.size() > 0) {
                        Object objs[] = {false, l};
                        model.addRow(objs);
                    }
                    // System.out.println("LAYER:"+l.getTitle());
                }
            }
        });

    }

    @Override
    public void wmsCapabilitiesFailed(final WMSServer wms, final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                PB_Waiting.setIndeterminate(false);
                CMB_Server.setEnabled(true);

                DefaultTableModel model = (DefaultTableModel) TB_Layers.getModel();
                model.setRowCount(0);
            }
        });
    }

    //**************************************************************************
    //*** ListSelectionListener
    //**************************************************************************
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == TB_Layers.getSelectionModel()) {
            if (e.getValueIsAdjusting() == true) return;
            
            int index = TB_Layers.getSelectedRow();
            if (index != -1) {
                WMSLayerCapabilities l = (WMSLayerCapabilities) TB_Layers.getValueAt(index, 1);
                TA_Layer.setText(l.getLayerAbstract());
                
            } else {
                TA_Layer.setText("");
            }
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

        jPanel2 = new javax.swing.JPanel();
        CMB_Server = new javax.swing.JComboBox<>();
        jPanel3 = new javax.swing.JPanel();
        PB_Waiting = new javax.swing.JProgressBar();
        TAB_Main = new javax.swing.JTabbedPane();
        PN_Layers = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TB_Layers = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        TA_Layer = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        TA_Abstract = new javax.swing.JTextArea();

        setLayout(new java.awt.BorderLayout());

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

        PB_Waiting.setPreferredSize(new java.awt.Dimension(150, 26));
        jPanel3.add(PB_Waiting);

        add(jPanel3, java.awt.BorderLayout.PAGE_END);

        PN_Layers.setLayout(new java.awt.BorderLayout());

        TB_Layers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

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
        TB_Layers.setRowHeight(22);
        jScrollPane1.setViewportView(TB_Layers);

        PN_Layers.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        TA_Layer.setColumns(20);
        TA_Layer.setLineWrap(true);
        TA_Layer.setRows(5);
        jScrollPane3.setViewportView(TA_Layer);

        PN_Layers.add(jScrollPane3, java.awt.BorderLayout.SOUTH);

        TAB_Main.addTab("Layers", PN_Layers);

        jPanel1.setLayout(new java.awt.BorderLayout());

        TA_Abstract.setColumns(20);
        TA_Abstract.setLineWrap(true);
        TA_Abstract.setRows(5);
        TA_Abstract.setWrapStyleWord(true);
        jScrollPane2.setViewportView(TA_Abstract);

        jPanel1.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        TAB_Main.addTab("Abstract", jPanel1);

        add(TAB_Main, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<WMSServer> CMB_Server;
    private javax.swing.JProgressBar PB_Waiting;
    private javax.swing.JPanel PN_Layers;
    private javax.swing.JTabbedPane TAB_Main;
    private javax.swing.JTextArea TA_Abstract;
    private javax.swing.JTextArea TA_Layer;
    private javax.swing.JTable TB_Layers;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    // End of variables declaration//GEN-END:variables

    

}
