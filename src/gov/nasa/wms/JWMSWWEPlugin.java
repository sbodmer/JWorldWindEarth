/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.wms;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import static gov.nasa.worldwind.wms.WMSTiledImageLayer.wmsGetParamsFromCapsDoc;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import javax.swing.ComboBoxModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
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
public class JWMSWWEPlugin extends JPanel implements WWEPlugin, ActionListener, ItemListener, WMSServer.WMSServerListener, ListSelectionListener, TableModelListener, ChangeListener {

    App app = null;
    WWEFactory factory = null;
    WorldWindow ww = null;

    /**
     * The empty layer
     */
    WrappedWMSTiledImageLayer dummy = null;
    
    /**
     * The current valid layer
     */
    WMSTiledImageLayer layer = null;

    /**
     * The WMS layers selected
     */
    String selectedLayers = ""; 
    
    /**
     * Creates new form WMS layer
     *
     * @param factory
     * @param ww
     * @param list
     */
    public JWMSWWEPlugin(WWEFactory factory, WorldWindow ww, ComboBoxModel<WMSServer> list) {
        super();
        this.factory = factory;
        this.ww = ww;

        initComponents();

        CMB_Server.setModel(list);
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
        TB_Layers.getModel().addTableModelListener(this);
        /*
        //--- Create layer from defaut config
        BasicLayerFactory bl = new BasicLayerFactory();
        layer = (TiledImageLayer) bl.createFromConfigSource("config/WMSLayerTemplate.xml", null);
        layer.setName("Generic WMS");
        layer.setValue(AVKEY_WORLDWIND_LAYER_PLUGIN, this);
         */
        
        //--- For debug purpose, use the wrapper one as first instance
        dummy = new WrappedWMSTiledImageLayer();
        dummy.setEnabled(false);
        dummy.setValue(WWEPlugin.AVKEY_WORLDWIND_LAYER_PLUGIN, this);
        CMB_Server.addItemListener(this);
        layer = dummy;

        CMB_ImageFormat.setEnabled(false);

        SL_Opacity.addChangeListener(this);
    }

    @Override
    public void configure(Element config) {
        //--- Load the stored WMS server list
        CMB_Server.removeItemListener(this);
        try {
            if (config != null) {
                //--- Set opacity
                int opacity = Integer.parseInt(config.getAttribute("opacity"));
                SL_Opacity.setValue(opacity);
                layer.setOpacity(opacity / 100d);
                
                //--- Check the last selected one
                int index = Integer.parseInt(config.getAttribute("selectedIndex"));
                CMB_Server.setSelectedIndex(index);
                
                //--- Store the last selected WMS layers
                selectedLayers = config.getAttribute("selectedLayers");
                
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();

        }
        if (CMB_Server.getItemCount() > 0) {
            WMSServer wms = (WMSServer) CMB_Server.getSelectedItem();
            wms.fetch(this, selectedLayers);
        }
        CMB_Server.addItemListener(this);

    }

    @Override
    public void cleanup() {
        dummy.dispose();
        layer.dispose();

    }

    @Override
    public void saveConfig(Element config) {
        if (config == null) return;

        config.setAttribute("opacity", "" + SL_Opacity.getValue());
        config.setAttribute("selectedIndex", ""+CMB_Server.getSelectedIndex());
        config.setAttribute("selectedLayers", selectedLayers);
        
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
    public boolean hasLayerButton() {
        return true;
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
        if (e.getSource() == CMB_Server) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                //--- A new WMS Server was selected
                PB_Waiting.setIndeterminate(true);

                DefaultTableModel model = (DefaultTableModel) TB_Layers.getModel();
                model.setRowCount(0);
                TA_Abstract.setText("");
                TA_Layer.setText("");

                CMB_ImageFormat.removeItemListener(this);
                CMB_ImageFormat.removeAllItems();
                CMB_ImageFormat.setEnabled(false);

                //--- Set the dummy layer to clear the tiles
                int index = ww.getModel().getLayers().indexOf(layer, 0);
                //--- Replace old layer
                Layer old = ww.getModel().getLayers().set(index, dummy);
                if (old != dummy) old.dispose();
                layer = dummy;

                WMSServer w = (WMSServer) CMB_Server.getSelectedItem();
                w.fetch(this, "");

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

        } else if (e.getSource() == CMB_ImageFormat) {
            apply();

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
    public void wmsCapabilitiesLoaded(final WMSServer wms, final WMSCapabilities caps, final String defaultLayers) {
        final ItemListener listener = this;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                PB_Waiting.setIndeterminate(false);
                CMB_Server.setEnabled(true);

                TA_Abstract.setText(caps.getServiceInformation().getServiceAbstract());

                //--- Fill the image format
                Iterator<String> fmts = caps.getImageFormats().iterator();
                while (fmts.hasNext()) {
                    String prefix = fmts.next();

                    if (prefix.startsWith("image/jpeg")) CMB_ImageFormat.addItem(prefix);
                    if (prefix.startsWith("image/png")) CMB_ImageFormat.addItem(prefix);

                }

                CMB_ImageFormat.addItemListener(listener);
                CMB_ImageFormat.setEnabled(true);

                String tokens[] = defaultLayers.split(",");
                ArrayList<String> selectedLayers = new ArrayList<>();
                for (int i=0;i<tokens.length;i++) selectedLayers.add(tokens[i]);
                
                //--- Fill the exposed layers
                DefaultTableModel model = (DefaultTableModel) TB_Layers.getModel();
                model.setRowCount(0);
                Iterator<WMSLayerCapabilities> it = caps.getNamedLayers().iterator();
                while (it.hasNext()) {
                    WMSLayerCapabilities l = it.next();
                    // System.out.println("L:"+l.getTitle());
                    if (l.isLeaf()) {
                        Object objs[] = {selectedLayers.contains(l.getName()), l};
                        model.addRow(objs);
                    }
                    // System.out.println("LAYER:"+l.getTitle());
                }
                //--- If empty, try to apply anyway
                apply();
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
                //--- Display some information about the layer
                WMSLayerCapabilities l = (WMSLayerCapabilities) TB_Layers.getValueAt(index, 1);
                TA_Layer.setText(l.getLayerAbstract());

            } else {
                TA_Layer.setText("");
            }

        }
    }

    //**************************************************************************
    //*** TableModelListener
    //**************************************************************************
    @Override
    public void tableChanged(TableModelEvent e) {
        if (e.getSource() == TB_Layers.getModel()) {
            if ((e.getColumn() == 0) && (e.getType() == TableModelEvent.UPDATE)) {
                //--- Selection
                apply();
            }
        }
    }

    //**************************************************************************
    //*** ChangeListener
    //**************************************************************************
    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == SL_Opacity) {
            double alpha = SL_Opacity.getValue() / 100d;
            layer.setOpacity(alpha);

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

        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        CMB_ImageFormat = new javax.swing.JComboBox<>();
        jPanel4 = new javax.swing.JPanel();
        PB_Waiting = new javax.swing.JProgressBar();
        TAB_Main = new javax.swing.JTabbedPane();
        PN_Layers = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TB_Layers = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        TA_Layer = new javax.swing.JTextArea();
        PN_Abstract = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        TA_Abstract = new javax.swing.JTextArea();
        jPanel6 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        SL_Opacity = new javax.swing.JSlider();
        jPanel2 = new javax.swing.JPanel();
        CMB_Server = new javax.swing.JComboBox<>();

        setLayout(new java.awt.BorderLayout());

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel1.add(CMB_ImageFormat);

        jPanel3.add(jPanel1, java.awt.BorderLayout.EAST);

        PB_Waiting.setPreferredSize(new java.awt.Dimension(100, 26));
        jPanel4.add(PB_Waiting);

        jPanel3.add(jPanel4, java.awt.BorderLayout.CENTER);

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

        PN_Abstract.setLayout(new java.awt.BorderLayout());

        TA_Abstract.setColumns(20);
        TA_Abstract.setLineWrap(true);
        TA_Abstract.setRows(5);
        TA_Abstract.setWrapStyleWord(true);
        jScrollPane2.setViewportView(TA_Abstract);

        PN_Abstract.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        TAB_Main.addTab("Abstract", PN_Abstract);

        add(TAB_Main, java.awt.BorderLayout.CENTER);

        jPanel6.setLayout(new java.awt.BorderLayout());

        SL_Opacity.setFont(new java.awt.Font("Monospaced", 0, 10)); // NOI18N
        SL_Opacity.setMajorTickSpacing(10);
        SL_Opacity.setMinorTickSpacing(5);
        SL_Opacity.setPaintLabels(true);
        SL_Opacity.setPaintTicks(true);
        SL_Opacity.setToolTipText("Opacity");
        SL_Opacity.setValue(100);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SL_Opacity, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SL_Opacity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.add(jPanel5, java.awt.BorderLayout.NORTH);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(CMB_Server, 0, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(CMB_Server, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.add(jPanel2, java.awt.BorderLayout.CENTER);

        add(jPanel6, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> CMB_ImageFormat;
    private javax.swing.JComboBox<WMSServer> CMB_Server;
    private javax.swing.JProgressBar PB_Waiting;
    private javax.swing.JPanel PN_Abstract;
    private javax.swing.JPanel PN_Layers;
    private javax.swing.JSlider SL_Opacity;
    private javax.swing.JTabbedPane TAB_Main;
    private javax.swing.JTextArea TA_Abstract;
    private javax.swing.JTextArea TA_Layer;
    private javax.swing.JTable TB_Layers;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    // End of variables declaration//GEN-END:variables

    /**
     * Apply the selected layers
     *
     */
    private void apply() {
        //--- Create the new layer
        selectedLayers = "";
        for (int i = 0; i < TB_Layers.getRowCount(); i++) {
            Boolean selected = (Boolean) TB_Layers.getValueAt(i, 0);
            if (selected) {
                WMSLayerCapabilities l = (WMSLayerCapabilities) TB_Layers.getValueAt(i, 1);
                selectedLayers += "," + l.getName();
            }
        }

        //--- Empty list not permitted
        if (selectedLayers.equals("")) return;
        selectedLayers = selectedLayers.substring(1);
        
        //--- Find index of current layer
        int index = ww.getModel().getLayers().indexOf(layer, 0);

        WMSServer wms = (WMSServer) CMB_Server.getSelectedItem();
        
        AVListImpl params = new AVListImpl();
        params.setValue(AVKey.LAYER_NAMES, selectedLayers);
        params.setValue(AVKey.IMAGE_FORMAT, CMB_ImageFormat.getSelectedItem().toString());
        // params.setValue(AVKey.DATASET_NAME, App.MD5(names));

        wmsGetParamsFromCapsDoc(wms.getCapabilities(), params);
        layer = new WMSTiledImageLayer(params);
        layer.setValue(WWEPlugin.AVKEY_WORLDWIND_LAYER_PLUGIN, this);
        layer.setOpacity(SL_Opacity.getValue() / 100d);
        
        /*
        Iterator<Map.Entry<String, Object>> it = params.getEntries().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Object> entry = it.next();
            System.out.println("" + entry.getKey() + " = " + entry.getValue());
            // setValue(e.getKey(), e.getValue());
        }
        */
        
        //--- Replace old layer
        Layer old = ww.getModel().getLayers().set(index, layer);
        layer.setEnabled(old.isEnabled());
        if (old != dummy) old.dispose();
    }
}
