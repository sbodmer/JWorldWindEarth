/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.wms;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.ogc.OGCBoundingBox;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerInfoURL;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import gov.nasa.worldwind.wms.WMSTiledImageLayer;
import static gov.nasa.worldwind.wms.WMSTiledImageLayer.wmsGetParamsFromCapsDoc;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
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
import org.tinyrcp.components.JMoveablePanel;
import org.w3c.dom.Element;
import org.worldwindearth.WWEFactory;
import org.worldwindearth.WWEPlugin;

/**
 * Create layer from config/Earth/BMNGWMSLayer2.xml
 *
 * @author sbodmer
 */
public class JWMSWWEPlugin extends JPanel implements WWEPlugin, ActionListener, WMSServer.WMSServerListener, ItemListener, ListSelectionListener, TableModelListener, ChangeListener {

    App app = null;
    WWEFactory factory = null;
    WorldWindow ww = null;
    JDesktopPane jdesktop = null;

    /**
     * The empty layer
     */
    WrappedWMSTiledImageLayer dummy = null;

    /**
     * The current valid layer
     */
    WMSTiledImageLayer layer = null;

    /**
     * Current choose server
     */
    WMSServer wms = null;

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
    public String toString() {
        return getPluginName();
    }

    @Override
    public String getPluginName() {
        return layer.getName();
    }

    @Override
    public void setPluginName(String name) {
        layer.setName(name);
        IF_Legend.setTitle(name);
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
        this.jdesktop = (JDesktopPane) arg;

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

        BT_Go.addActionListener(this);
        BT_Load.addActionListener(this);
        BT_Legend.addActionListener(this);

        //--- For debug purpose, use the wrapper one as first instance
        dummy = new WrappedWMSTiledImageLayer();
        dummy.setEnabled(false);
        dummy.setValue(WWEPlugin.AVKEY_WORLDWIND_LAYER_PLUGIN, this);

        layer = dummy;

        CMB_ImageFormat.setEnabled(false);

        SL_Opacity.addChangeListener(this);

        // IF_Legend.putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
        IF_Legend.setVisible(false);
        jdesktop.add(IF_Legend, JLayeredPane.PALETTE_LAYER);

    }

    @Override
    public void configure(Element config) {
        //--- Load the stored WMS server list
        try {
            if (config != null) {
                //--- Set opacity
                int opacity = Integer.parseInt(config.getAttribute("opacity"));
                SL_Opacity.setValue(opacity);
                layer.setOpacity(opacity / 100d);

                //--- Store the last selected WMS layers
                selectedLayers = config.getAttribute("selectedLayers");

            }

        } catch (Exception ex) {
            ex.printStackTrace();

        }
        String sw = config != null ? config.getAttribute("wms") : "";
        for (int i = 0; i < CMB_Server.getItemCount(); i++) {
            wms = (WMSServer) CMB_Server.getItemAt(i);
            if (wms.getTitle().equals(sw)) {
                LB_WMS.setText(wms.getTitle());
                wms.fetch(this, selectedLayers);
                break;

            }
        }

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
        config.setAttribute("wms", "" + wms.getTitle());
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

    @Override
    public void layerMouseClicked(MouseEvent e, gov.nasa.worldwind.geom.Position pos) {
        //---
    }

    //**************************************************************************
    //*** ActionListener
    //**************************************************************************
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("go")) {
            WMSServer w = (WMSServer) CMB_Server.getSelectedItem();
            int index = TB_Layers.getSelectedRow();
            if (index != -1) {
                WMSLayerCapabilities l = (WMSLayerCapabilities) TB_Layers.getValueAt(index, 1);
                try {
                    System.out.println("L:" + l.getTitle());

                    Sector s = l.getGeographicBoundingBox();
                    Position pos = Position.fromDegrees(s.getMinLatitude().getDegrees(), s.getMinLongitude().getDegrees());
                    BasicOrbitView view = (BasicOrbitView) ww.getView();
                    view.addPanToAnimator(pos, Angle.fromDegrees(0), Angle.fromDegrees(0), 10000);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        } else if (e.getActionCommand().equals("load")) {
            wms = (WMSServer) CMB_Server.getSelectedItem();
            wms.fetch(this, "");

        } else if (e.getActionCommand().equals("legend")) {
            IF_Legend.setVisible(true);
            IF_Legend.setBounds((jdesktop.getWidth() / 2) - 200, jdesktop.getHeight() - 200, 400, 200);

            /*
            JMoveablePanel jm = new JMoveablePanel();
            jm.setTitle("test");
            jm.setOpaque(false);
            jdesktop.add(jm, JLayeredPane.DEFAULT_LAYER);
            jm.setBounds(100,100,320,200);
            jm.setVisible(true);
             */
        }
    }

    //**************************************************************************
    //*** ItemListener
    //**************************************************************************
    @Override
    public void itemStateChanged(ItemEvent e) {

        if (e.getSource() == CMB_Server) {
            /*
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
                dummy.setEnabled(old.isEnabled());
                if (old != dummy) old.dispose();
                layer = dummy;

                WMSServer w = (WMSServer) CMB_Server.getSelectedItem();
                w.fetch(this, "");

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
             */
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
                // CMB_Server.setEnabled(false);
            }
        });
    }

    @Override
    public void wmsCapabilitiesLoaded(final WMSServer wms, final WMSCapabilities caps, final String defaultLayers) {
        final ItemListener listener = this;
        final TableModelListener tlistener = this;

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                PB_Waiting.setIndeterminate(false);
                // CMB_Server.setEnabled(true);

                try {
                    String txt = "WMS : " + caps.getVersion() + "\n";
                    txt += "" + caps.getServiceInformation().toString() + "\n";
                    txt += "\n";
                    // txt += "Abstract : " + caps.getServiceInformation().getServiceAbstract();
                    TA_Abstract.setText(txt);
                    TA_Abstract.setCaretPosition(0);

                    //--- Fill the image format
                    Iterator<String> fmts = caps.getImageFormats().iterator();
                    while (fmts.hasNext()) {
                        String prefix = fmts.next();

                        if (prefix.startsWith("image/jpeg")) CMB_ImageFormat.addItem(prefix);
                        if (prefix.startsWith("image/png")) CMB_ImageFormat.addItem(prefix);

                    }

                } catch (Exception ex) {
                    CMB_ImageFormat.addItem("image/png");

                }

                CMB_ImageFormat.addItemListener(listener);
                CMB_ImageFormat.setEnabled(true);

                String tokens[] = defaultLayers.split(",");
                ArrayList<String> selectedLayers = new ArrayList<>();
                for (int i = 0; i < tokens.length; i++) selectedLayers.add(tokens[i]);

                //--- Fill the exposed layers
                DefaultTableModel model = (DefaultTableModel) TB_Layers.getModel();
                model.removeTableModelListener(tlistener);
                model.setRowCount(0);
                try {
                    Iterator<WMSLayerCapabilities> it = caps.getNamedLayers().iterator();
                    while (it.hasNext()) {
                        WMSLayerCapabilities l = it.next();
                        // System.out.println("L:" + l.getTitle() + " => " + l.isLeaf() + "," + l.isNoSubsets() + "," + l.isOpaque() + "," + l.isQueryable());
                        if (l.isLeaf()) {
                            Object objs[] = {selectedLayers.contains(l.getName()), l};
                            model.addRow(objs);
                        }
                        // System.out.println("LAYER:"+l.getTitle());
                    }

                } catch (Exception ex) {
                    //---

                }
                //--- On Linux the table is not showed in some cases ???
                model.addTableModelListener(tlistener);
                TB_Layers.repaint();

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

                TA_Abstract.setText("Failed to load capabilities");
                TAB_Main.setSelectedIndex(1);

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

        IF_Legend = new javax.swing.JInternalFrame();
        jScrollPane4 = new javax.swing.JScrollPane();
        PN_Legends = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        CMB_ImageFormat = new javax.swing.JComboBox<>();
        jPanel4 = new javax.swing.JPanel();
        PB_Waiting = new javax.swing.JProgressBar();
        jPanel7 = new javax.swing.JPanel();
        BT_Go = new javax.swing.JButton();
        BT_Legend = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        CMB_Server = new javax.swing.JComboBox<>();
        BT_Load = new javax.swing.JButton();
        LB_WMS = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        TAB_Main = new javax.swing.JTabbedPane();
        PN_Layers = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TB_Layers = new javax.swing.JTable();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        TA_Layer = new javax.swing.JTextArea();
        PN_Abstract = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        TA_Abstract = new javax.swing.JTextArea();
        jPanel5 = new javax.swing.JPanel();
        SL_Opacity = new javax.swing.JSlider();

        IF_Legend.setClosable(true);
        IF_Legend.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        IF_Legend.setIconifiable(true);
        IF_Legend.setResizable(true);
        IF_Legend.setVisible(true);

        PN_Legends.setLayout(new javax.swing.BoxLayout(PN_Legends, javax.swing.BoxLayout.Y_AXIS));
        jScrollPane4.setViewportView(PN_Legends);

        IF_Legend.getContentPane().add(jScrollPane4, java.awt.BorderLayout.CENTER);

        setLayout(new java.awt.BorderLayout());

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel1.add(CMB_ImageFormat);

        jPanel3.add(jPanel1, java.awt.BorderLayout.EAST);

        PB_Waiting.setPreferredSize(new java.awt.Dimension(100, 26));
        jPanel4.add(PB_Waiting);

        jPanel3.add(jPanel4, java.awt.BorderLayout.CENTER);

        BT_Go.setText("Go");
        BT_Go.setToolTipText("Move to the geographic position of the highlightes layer");
        BT_Go.setActionCommand("go");
        jPanel7.add(BT_Go);

        BT_Legend.setText("Legend");
        BT_Legend.setActionCommand("legend");
        jPanel7.add(BT_Legend);

        jPanel3.add(jPanel7, java.awt.BorderLayout.WEST);

        add(jPanel3, java.awt.BorderLayout.PAGE_END);

        jPanel6.setLayout(new java.awt.BorderLayout());

        BT_Load.setText("Load");
        BT_Load.setToolTipText("Reload the WMS Capabilities");
        BT_Load.setActionCommand("load");

        LB_WMS.setBackground(new java.awt.Color(144, 202, 249));
        LB_WMS.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        LB_WMS.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LB_WMS.setText("...");
        LB_WMS.setOpaque(true);
        LB_WMS.setPreferredSize(new java.awt.Dimension(15, 26));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(LB_WMS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(CMB_Server, 0, 380, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BT_Load)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CMB_Server, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BT_Load))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(LB_WMS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel6.add(jPanel2, java.awt.BorderLayout.CENTER);

        add(jPanel6, java.awt.BorderLayout.NORTH);

        jPanel8.setLayout(new java.awt.BorderLayout());

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

        jTabbedPane1.setTabPlacement(javax.swing.JTabbedPane.LEFT);

        TA_Layer.setColumns(20);
        TA_Layer.setLineWrap(true);
        TA_Layer.setRows(5);
        jScrollPane3.setViewportView(TA_Layer);

        jTabbedPane1.addTab("Abstract", jScrollPane3);

        PN_Layers.add(jTabbedPane1, java.awt.BorderLayout.SOUTH);

        TAB_Main.addTab("Layers", PN_Layers);

        PN_Abstract.setLayout(new java.awt.BorderLayout());

        TA_Abstract.setColumns(20);
        TA_Abstract.setLineWrap(true);
        TA_Abstract.setRows(5);
        TA_Abstract.setWrapStyleWord(true);
        jScrollPane2.setViewportView(TA_Abstract);

        PN_Abstract.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        TAB_Main.addTab("Abstract", PN_Abstract);

        jPanel8.add(TAB_Main, java.awt.BorderLayout.CENTER);

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
                .addComponent(SL_Opacity, javax.swing.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SL_Opacity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.add(jPanel5, java.awt.BorderLayout.NORTH);

        add(jPanel8, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BT_Go;
    private javax.swing.JButton BT_Legend;
    private javax.swing.JButton BT_Load;
    private javax.swing.JComboBox<String> CMB_ImageFormat;
    private javax.swing.JComboBox<WMSServer> CMB_Server;
    private javax.swing.JInternalFrame IF_Legend;
    private javax.swing.JLabel LB_WMS;
    private javax.swing.JProgressBar PB_Waiting;
    private javax.swing.JPanel PN_Abstract;
    private javax.swing.JPanel PN_Layers;
    private javax.swing.JPanel PN_Legends;
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
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables

    /**
     * Apply the selected layers
     *
     */
    private void apply() {
        LB_WMS.setText(wms.getTitle());
        selectedLayers = "";
        PN_Legends.removeAll();
        for (int i = 0; i < TB_Layers.getRowCount(); i++) {
            Boolean selected = (Boolean) TB_Layers.getValueAt(i, 0);
            if (selected) {
                WMSLayerCapabilities l = (WMSLayerCapabilities) TB_Layers.getValueAt(i, 1);
                selectedLayers += "," + l.getName();
                JLabel jl = new JLabel(getLayerLegend(wms, l));
                PN_Legends.add(jl);

            }
        }
        PN_Legends.revalidate();
        PN_Legends.repaint();

        //--- Empty list not permitted
        if (selectedLayers.equals("")) return;
        selectedLayers = selectedLayers.substring(1);

        //--- Find index of current layer
        int index = ww.getModel().getLayers().indexOf(layer, 0);

        try {
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

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(getTopLevelAncestor(), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /*
    public static String buildWMSGetFeatureInfoUrl(String serverUrl, LayerInfo linfo, Position position) {
        String urlString = "";
        if (serverUrl.contains("?")) {
            urlString = serverUrl + "&amp;SERVICE=WMS";

        } else {
            urlString = serverUrl + "?SERVICE=WMS";
        }
        urlString += "&amp;VERSION=" + linfo.getWMSVersion();
        urlString += "&amp;REQUEST=GetFeatureInfo";
        urlString += "&amp;LAYERS=" + linfo.getName(); // real names
        urlString += "&amp;QUERY_LAYERS=" + linfo.getName(); // real name
        urlString += "&amp;WIDTH=512";
        urlString += "&amp;HEIGHT=512";
        urlString += "&amp;FORMAT=image/png";
        urlString += "&amp;X=" + position.getLongitude().degrees;
        urlString += "&amp;Y=" + position.getLatitude().degrees;
        urlString += "&amp;STYLES=";
        urlString += "&amp;SRS=EPSG:4326";
        urlString += "&amp;BBOX=" + buildBBoxWithPosition(position);
        return urlString;
    }
     */
    private static String buildBBoxWithPosition(Position p) {
        // BBOX=-88.59375,37.265625,-87.890625,37.96875
        double lon1 = p.getLongitude().degrees - 0.00001;
        double lon2 = p.getLongitude().degrees + 0.00001;
        double lat1 = p.getLatitude().degrees - 0.00001;
        double lat2 = p.getLatitude().degrees + 0.00001;
        return lon1 + "," + lat1 + "," + lon2 + "," + lat2;

    }

    private void getFeatureInfo(WMSServer server, WMSLayerCapabilities lcap, Position pos) {
        String serverUrl = server.api.toString();
        serverUrl += (serverUrl.contains("?") ? "&SERVICE=WMS" : "?SERVICE=WMS");
        serverUrl += "&VERSION=" + server.getCapabilities().getVersion();
        serverUrl += "&REQUEST=GetFeatureInfo";
        serverUrl += "&LAYERS=" + lcap.getName(); // real name
        serverUrl += "&QUERY_LAYERS=" + lcap.getName(); // real name
        serverUrl += "&WIDTH=512";
        serverUrl += "&HEIGHT=512";
        serverUrl += "&FORMAT=image/png";
        serverUrl += "&X=" + pos.getLongitude().degrees;
        serverUrl += "&Y=" + pos.getLatitude().degrees;
        serverUrl += "&STYLES=";
        serverUrl += "&SRS=EPSG:4326";
        serverUrl += "&BBOX=" + buildBBoxWithPosition(pos);
        System.out.println(serverUrl);

    }

    private ImageIcon getLayerLegend(WMSServer server, WMSLayerCapabilities lcap) {
        ImageIcon legend = null;
        try {
            String serverUrl = server.api.toString();
            serverUrl += (serverUrl.contains("?") ? "&SERVICE=WMS" : "?SERVICE=WMS");
            serverUrl += "&version=1.1.1";
            serverUrl += "&request=GetLegendGraphic";
            serverUrl += "&layer=" + lcap.getName(); // real name
            serverUrl += "&format=image/png";
            URL url = new URL(serverUrl);

            legend = new ImageIcon(url);

            int w = legend.getIconWidth();
            int h = legend.getIconHeight();

            if (w == -1 || h == -1) {
                //---
            }

        } catch (MalformedURLException ex) {
            //---
        }
        return legend;
    }
}
