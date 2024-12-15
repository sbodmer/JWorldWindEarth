/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osmbuildings;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.cache.FileStore;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Properties;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import org.osmbuildings.tileserver.OSMBuildingsTileServer;
import org.osmbuildings.tileserver.OSMBuildingsTileServerListener;
import org.tinyrcp.App;
import org.tinyrcp.TinyPlugin;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.worldwindearth.WWEFactory;
import org.worldwindearth.components.SVGIcon;

/**
 * http://[abcd].data.osmbuildings.org/0.2/sx3pxpz6/tile/${Z}/${X}/${Y}.json
 *
 * @author sbodmer
 */
public class JOSMBuildingsWWEFactory extends javax.swing.JPanel implements WWEFactory, ActionListener, OSMBuildingsTileServerListener {

    public static final String COPYRIGHT_TEXT = "© Data OpenStreetMap · © 3D OSM Buildings";

    DefaultComboBoxModel<OSMBuildingProvider> list = new DefaultComboBoxModel<>();
    App app = null;
    NumberFormat nf = NumberFormat.getNumberInstance();

    WorldWindow ww = null;
    javax.swing.Timer timer = null;

    /**
     * Local provider
     */
    OSMBuildingsTileServer server = null;

    /**
     * Creates new form OSMBuildingsWWELayerPluginFactory
     */
    public JOSMBuildingsWWEFactory() {

        initComponents();

        try {
            FileStore fs = WorldWind.getDataFileStore();
            URL url = fs.findFile(OSMBuildingsLayer.CACHE_FOLDER, false);
            if (url == null) {
                File tmp = fs.getLocations().get(0);
                File f = new File(tmp, OSMBuildingsLayer.CACHE_FOLDER);
                f.mkdirs();
                TF_CachePath.setText(f.getPath());

            } else {
                File f = new File(url.toURI());
                TF_CachePath.setText(f.getPath());
            }

        } catch (URISyntaxException ex) {
            TF_CachePath.setText(ex.getMessage());
            ex.printStackTrace();

        } catch (NullPointerException ex) {
            TF_CachePath.setText(ex.getMessage());
            ex.printStackTrace();
        }

        timer = new javax.swing.Timer(1000, this);

    }

    //**************************************************************************
    //*** API
    //**************************************************************************
    public int getExpireDays() {
        return (int) SP_ExpireDays.getValue();

    }

    public DefaultTableModel getProviders() {
        return (DefaultTableModel) TB_Providers.getModel();
    }

    //**************************************************************************
    //*** WWEPluginFactory
    //**************************************************************************
    @Override
    public Icon getFactoryIcon(int size) {
        return LB_Name.getIcon();
    }

    @Override
    public String getFactoryName() {
        return LB_Name.getText();
    }

    @Override
    public String getFactoryDescription() {
        return LB_Description.getText();
    }

    @Override
    public String getFactoryCategory() {
        return PLUGIN_CATEGORY_WORLDWIND_LAYER;
    }

    @Override
    public String getFactoryFamily() {
        return PLUGIN_FAMILY_WORLDWIND_LAYER_BUILDINGS;
    }

    @Override
    public JComponent getFactoryConfigComponent() {
        return this;
    }

    @Override
    public void initialize(App app) {
        this.app = app;

        BT_Add.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/add.svg"));
        BT_Up.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/up.svg"));
        BT_Down.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/down.svg"));
        BT_Edit.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/edit.svg"));
        BT_Delete.setIcon(SVGIcon.newIcon(22, "/org/worldwindearth/Resources/Icons/trash.svg"));
        
        BT_Add.addActionListener(this);
        BT_Delete.addActionListener(this);
        BT_Edit.addActionListener(this);
        BT_Down.addActionListener(this);
        BT_Up.addActionListener(this);
        BT_ClearLogs.addActionListener(this);

        
        TB_Providers.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        CB_LocalProvider.addActionListener(this);

        BT_Cache.addActionListener(this);
        BT_Clear.addActionListener(this);

        timer.start();
    }

    @Override
    public void configure(Element config) {
        if (config == null) return;

        try {
            SP_ExpireDays.setValue(Integer.parseInt(config.getAttribute("expireDays")));

        } catch (NumberFormatException ex) {
            //---
        }

        DefaultTableModel model = (DefaultTableModel) TB_Providers.getModel();
        model.setRowCount(0);
        NodeList nl = config.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Node n = nl.item(i);
            if (n.getNodeName().equals("Provider")) {
                Element e = (Element) n;
                String url = "";
                if (e.getFirstChild() != null) url = e.getFirstChild().getNodeValue();
                OSMBuildingProvider pr = new OSMBuildingProvider(e.getAttribute("title"), url, Integer.parseInt(e.getAttribute("minLevel")), Integer.parseInt(e.getAttribute("maxLevel")));
                list.addElement(pr);
                Object obj[] = {pr.getTitle(), pr.getUrl(), pr.getMinLevel(), pr.getMaxLevel()};
                model.addRow(obj);

            } else if (n.getNodeName().equals("Local")) {
                Element e = (Element) n;
                CB_LocalProvider.setSelected(e.getAttribute("localProviderEnabled").equals("true"));
                try {
                    SP_LocalProviderPort.setValue(Integer.parseInt(e.getAttribute("localProviderPort")));
                    //--- Let the hard coded one be the source
                    // TF_LocalProviderAPI.setText(e.getFirstChild().getNodeValue());

                } catch (Exception ex) {

                }
            }

        }
        if (model.getRowCount() == 0) {
            OSMBuildingProvider pr = new OSMBuildingProvider("www.osmbuildings.org", "http://[abcd].data.osmbuildings.org/0.2/sx3pxpz6/tile/${Z}/${X}/${Y}.json", 15, 15);
            list.addElement(pr);
            Object obj[] = {pr.getTitle(), pr.getUrl(), pr.getMinLevel(), pr.getMaxLevel()};
            model.addRow(obj);
        }

        TF_LocalProviderURL.setText("http://localhost:" + SP_LocalProviderPort.getValue() + "/osmbuildings/${Z}/${X}/${Y}.json");

        //--- If local provider is enabled, start it here
        if (CB_LocalProvider.isSelected()) {
            Properties p = new Properties();
            p.setProperty("CACHE_FOLDER", "");  //--- No cache, let layer handle the cache
            p.setProperty("CACHE_KEEP_DELAY", "30");
            p.setProperty("HOST", "localhost");
            p.setProperty("PORT", "" + SP_LocalProviderPort.getValue());
            p.setProperty("OSM_API", TF_LocalProviderAPI.getText());

            if (server == null) {
                server = new OSMBuildingsTileServer(p, this);
                server.start();
            }
        }
    }

    @Override
    public TinyPlugin newPlugin(Object o) {
        this.ww = (WorldWindow) o;
        
        return new JOSMBuildingsWWEPlugin(this, (WorldWindow) o, list);
    }

    @Override
    public void store(Element config) {
        if (config == null) return;

        config.setAttribute("expireDays", SP_ExpireDays.getValue().toString());

        Element e = config.getOwnerDocument().createElement("Local");
        e.setAttribute("localProviderEnabled", "" + CB_LocalProvider.isSelected());
        e.setAttribute("localProviderPort", "" + SP_LocalProviderPort.getValue());
        e.appendChild(config.getOwnerDocument().createTextNode(TF_LocalProviderAPI.getText()));
        config.appendChild(e);

        DefaultTableModel model = (DefaultTableModel) TB_Providers.getModel();
        for (int i = 0; i < model.getRowCount(); i++) {
            e = config.getOwnerDocument().createElement("Provider");
            e.appendChild(config.getOwnerDocument().createTextNode(model.getValueAt(i, 1).toString()));
            e.setAttribute("title", model.getValueAt(i, 0).toString().replace('&', ' '));
            e.setAttribute("minLevel", model.getValueAt(i, 2).toString());
            e.setAttribute("maxLevel", model.getValueAt(i, 3).toString());
            config.appendChild(e);
        }
    }

    @Override
    public void destroy() {
        //---
        timer.stop();
        if (server != null) server.interrupt();
    }

    @Override
    public Object getProperty(String property) {
        if (property.equals(PROPERTY_COPYRIGHT_TEXT)) return COPYRIGHT_TEXT;
        return null;
    }

    public boolean doesFactorySupport(Object obj) {
        if (obj != null) return obj.toString().equals(WWEFactory.PLANET_EARTH);
        return false;
    }

    //**************************************************************************
    //*** ActionListener
    //**************************************************************************
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timer) {
            //--- Check local provider status
            if (server == null) {
                LB_LocalProviderStatus.setText("...");

            } else if (server.isAlive()) {
                LB_LocalProviderStatus.setText("<html><font color=\"green\">running</font></html>");
                long loaded = server.getLoaded();
                int kb = (int) (loaded / 1024);

                LB_Loaded.setText("" + nf.format(kb) + "kb");

            } else {
                LB_LocalProviderStatus.setText("<html><font color=\"red\">stopped</font></html>");

            }

        } else if (e.getActionCommand().equals("cache")) {
            //--- Check the cache size
            try {
                FileStore fs = WorldWind.getDataFileStore();
                URL url = fs.findFile(OSMBuildingsLayer.CACHE_FOLDER, false);
                long size = calculateSize(new File(url.toURI()));
                TF_CacheSize.setText((size / (1024 * 1024)) + " MB");

            } catch (URISyntaxException ex) {
                ex.printStackTrace();

            } catch (NullPointerException ex) {
                ex.printStackTrace();

            }

        } else if (e.getActionCommand().equals("clear")) {
            //--- Clear the cache
            try {
                FileStore fs = WorldWind.getDataFileStore();
                URL url = fs.findFile(OSMBuildingsLayer.CACHE_FOLDER, false);
                File f = new File(url.toURI());
                recursiveDelete(f);
                long size = calculateSize(new File(url.toURI()));
                TF_CacheSize.setText((size / (1024 * 1024)) + " MB");

            } catch (URISyntaxException ex) {
                ex.printStackTrace();

            }

        } else if (e.getActionCommand().equals("delete")) {
            DefaultTableModel model = (DefaultTableModel) TB_Providers.getModel();
            int index = TB_Providers.getSelectedRow();
            if (index == -1) return;

            OSMBuildingProvider pr = list.getElementAt(index);
            list.removeElement(pr);
            model.removeRow(index);

        } else if (e.getActionCommand().equals("edit")) {
            int index = TB_Providers.getSelectedRow();
            if (index == -1) return;

            edit(index);

        } else if (e.getActionCommand().equals("add")) {
            int index = TB_Providers.getSelectedRow();
            if (index == -1) index = 0;
            add(index);

        } else if (e.getActionCommand().equals("clearLogs")) {
            TA_Logs.setText("");

        } else if (e.getActionCommand().equals("down")) {
            int index = TB_Providers.getSelectedRow();
            if (index >= list.getSize() - 1) return;

            DefaultTableModel model = (DefaultTableModel) TB_Providers.getModel();

            OSMBuildingProvider pr = list.getElementAt(index);
            list.removeElement(pr);
            model.removeRow(index);

            list.insertElementAt(pr, index + 1);
            model.insertRow(index + 1, new Object[]{pr.getTitle(), pr.getUrl(), pr.getMinLevel(), pr.getMaxLevel()});

            //--- Select row again
            TB_Providers.getSelectionModel().addSelectionInterval(index + 1, index + 1);

        } else if (e.getActionCommand().equals("up")) {
            int index = TB_Providers.getSelectedRow();
            if (index <= 0) return;

            DefaultTableModel model = (DefaultTableModel) TB_Providers.getModel();

            OSMBuildingProvider pr = list.getElementAt(index);
            list.removeElement(pr);
            model.removeRow(index);

            list.insertElementAt(pr, index - 1);
            model.insertRow(index - 1, new Object[]{pr.getTitle(), pr.getUrl(), pr.getMinLevel(), pr.getMaxLevel()});

            //--- Select row again
            TB_Providers.getSelectionModel().addSelectionInterval(index - 1, index - 1);

        } else if (e.getActionCommand().equals("localProviderEnabled")) {
            if (CB_LocalProvider.isSelected()) {
                SP_LocalProviderPort.setEnabled(false);
                TF_LocalProviderAPI.setEnabled(false);
                TF_LocalProviderURL.setText("http://localhost:" + SP_LocalProviderPort.getValue() + "/osmbuildings/${Z}/${X}/${Y}.json");

                Properties p = new Properties();
                p.setProperty("CACHE_FOLDER", "");  //--- No cache, let local layer handle the cache
                p.setProperty("CACHE_KEEP_DELAY", "30");
                p.setProperty("HOST", "localhost");
                p.setProperty("PORT", "" + SP_LocalProviderPort.getValue());
                p.setProperty("OSM_API", TF_LocalProviderAPI.getText());

                server = new OSMBuildingsTileServer(p, this);
                server.start();

            } else {
                SP_LocalProviderPort.setEnabled(true);
                TF_LocalProviderAPI.setEnabled(true);

                if (server != null) server.interrupt();
            }
        }
    }

    //**************************************************************************
    //*** OSMBuildingsTileServerListener
    //**************************************************************************
    @Override
    public void buildingsLogs(int state, String line) {
        if (TA_Logs.getText().length() > 65535) TA_Logs.setText("");
        if (state == 0) {
            TA_Logs.append("(I) " + line + "\n");
            LB_Log.setText(line);

        } else if (state == 1) {
            TA_Logs.append("(E) " + line + "\n");
            LB_Log.setText("<html><font color=\"red\">" + line + "</font></html>");

        } else if (state == 2) {
            TA_Logs.append("(W) " + line + "\n");
            LB_Log.setText("<html><font color=\"red\">" + line + "</font></html>");
            
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

        LB_Name = new javax.swing.JLabel();
        LB_Description = new javax.swing.JLabel();
        PN_ProviderData = new javax.swing.JPanel();
        TF_Title = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        TF_Url = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        SP_MinLevel = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        SP_MaxLevel = new javax.swing.JSpinner();
        jPanel1 = new javax.swing.JPanel();
        BT_Cache = new javax.swing.JButton();
        TF_CacheSize = new javax.swing.JTextField();
        BT_Clear = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        SP_ExpireDays = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        TF_CachePath = new javax.swing.JTextField();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        PN_Providers = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        TB_Providers = new javax.swing.JTable();
        TB_Tools = new javax.swing.JToolBar();
        BT_Add = new javax.swing.JButton();
        BT_Up = new javax.swing.JButton();
        BT_Down = new javax.swing.JButton();
        BT_Edit = new javax.swing.JButton();
        jSeparator9 = new javax.swing.JToolBar.Separator();
        BT_Delete = new javax.swing.JButton();
        PN_LocalProvider = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        CB_LocalProvider = new javax.swing.JCheckBox();
        SP_LocalProviderPort = new javax.swing.JSpinner();
        TF_LocalProviderURL = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        TF_LocalProviderAPI = new javax.swing.JTextField();
        jLabel10 = new javax.swing.JLabel();
        LB_LocalProviderStatus = new javax.swing.JLabel();
        LB_Loaded = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        TA_Logs = new javax.swing.JTextArea();
        BT_ClearLogs = new javax.swing.JButton();
        LB_Log = new javax.swing.JLabel();

        LB_Name.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/osmbuildings/Resources/Icons/22x22/osmbuildings.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/osmbuildings/OSMBuildings"); // NOI18N
        LB_Name.setText(bundle.getString("factory_name")); // NOI18N

        TF_Title.setText("www.osmbuildings.org");

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel1.setText("Title");
        jLabel1.setPreferredSize(new java.awt.Dimension(100, 26));

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel4.setText("URL");
        jLabel4.setPreferredSize(new java.awt.Dimension(100, 26));

        TF_Url.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        TF_Url.setText("http://");
        TF_Url.setToolTipText("<html>\nThe building structure (GeoJSON) provider, the following sequence will be replaced\n<table>\n<tr><td>${Z}</td><td>The zoom level (ex. 15)</td></tr>\n<tr><td>${X}</td><td>The X tile column</td></tr>\n<tr><td>${Y}</td><td>The Y tile row</td></tr>\n</table>\nTo have random server use \"[ab...]\" notation, which will use a or b or ... in a random way.<br>\nEx: http://[abcd].data.osmbuildings.org/0.2/sx3pxpz6/tile/${Z}/${X}/${Y}.json\n</html>");

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("Min level");
        jLabel5.setPreferredSize(new java.awt.Dimension(100, 26));

        SP_MinLevel.setModel(new javax.swing.SpinnerNumberModel(15, 14, 18, 1));

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("Max Level");
        jLabel6.setPreferredSize(new java.awt.Dimension(100, 26));

        SP_MaxLevel.setModel(new javax.swing.SpinnerNumberModel(15, 14, 20, 1));

        javax.swing.GroupLayout PN_ProviderDataLayout = new javax.swing.GroupLayout(PN_ProviderData);
        PN_ProviderData.setLayout(PN_ProviderDataLayout);
        PN_ProviderDataLayout.setHorizontalGroup(
            PN_ProviderDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PN_ProviderDataLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PN_ProviderDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PN_ProviderDataLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(TF_Title))
                    .addGroup(PN_ProviderDataLayout.createSequentialGroup()
                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(TF_Url, javax.swing.GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE))
                    .addGroup(PN_ProviderDataLayout.createSequentialGroup()
                        .addGroup(PN_ProviderDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PN_ProviderDataLayout.createSequentialGroup()
                                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(SP_MinLevel, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(PN_ProviderDataLayout.createSequentialGroup()
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(SP_MaxLevel, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        PN_ProviderDataLayout.setVerticalGroup(
            PN_ProviderDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PN_ProviderDataLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PN_ProviderDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_Title, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PN_ProviderDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TF_Url, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PN_ProviderDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SP_MinLevel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PN_ProviderDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SP_MaxLevel, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setLayout(new java.awt.BorderLayout());

        BT_Cache.setText("Check cache size");
        BT_Cache.setActionCommand("cache");

        TF_CacheSize.setEditable(false);

        BT_Clear.setText("Clear cache");
        BT_Clear.setActionCommand("clear");

        jLabel2.setText("Expire date (days)");
        jLabel2.setPreferredSize(new java.awt.Dimension(200, 26));

        SP_ExpireDays.setModel(new javax.swing.SpinnerNumberModel(30, 0, 365, 1));
        SP_ExpireDays.setToolTipText("Set the tile cache expire days, after the delay the buildinga are resolved again");

        jLabel3.setText("Cache path is");
        jLabel3.setPreferredSize(new java.awt.Dimension(200, 26));

        TF_CachePath.setEditable(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(TF_CachePath))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(BT_Cache, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(TF_CacheSize, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(BT_Clear))
                            .addComponent(SP_ExpireDays, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 332, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BT_Cache)
                    .addComponent(TF_CacheSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BT_Clear))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SP_ExpireDays, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TF_CachePath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.NORTH);

        PN_Providers.setPreferredSize(new java.awt.Dimension(456, 200));
        PN_Providers.setLayout(new java.awt.BorderLayout());

        TB_Providers.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {"local", "http://localhost:8088/osmbuildings/${Z}/${X}/${Y}.json",  new Integer(15),  new Integer(15)},
                {"osmbuildings.org", "http://[abcd].data.osmbuildings.org/0.2/sx3pxpz6/tile/${Z}/${X}/${Y}.json",  new Integer(15),  new Integer(15)},
                {"les-studios-inexistants.ch", "http://kaitan.les-studios-inexistants.ch:8088/osmbuildings/${Z}/${X}/${Y}.json",  new Integer(15),  new Integer(15)}
            },
            new String [] {
                "Title", "URL", "Min level", "Max level"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class, java.lang.Integer.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        TB_Providers.setRowHeight(24);
        jScrollPane2.setViewportView(TB_Providers);

        PN_Providers.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        TB_Tools.setBorder(null);

        BT_Add.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Add.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/add.png"))); // NOI18N
        BT_Add.setToolTipText("add");
        BT_Add.setActionCommand("add");
        BT_Add.setPreferredSize(new java.awt.Dimension(32, 32));
        TB_Tools.add(BT_Add);

        BT_Up.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Up.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/up.png"))); // NOI18N
        BT_Up.setToolTipText("move up");
        BT_Up.setActionCommand("up");
        BT_Up.setFocusable(false);
        BT_Up.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_Up.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_Up.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        TB_Tools.add(BT_Up);

        BT_Down.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Down.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/down.png"))); // NOI18N
        BT_Down.setToolTipText("move down");
        BT_Down.setActionCommand("down");
        BT_Down.setFocusable(false);
        BT_Down.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_Down.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_Down.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        TB_Tools.add(BT_Down);

        BT_Edit.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Edit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/edit.png"))); // NOI18N
        BT_Edit.setToolTipText("edit");
        BT_Edit.setActionCommand("edit");
        BT_Edit.setFocusable(false);
        BT_Edit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_Edit.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_Edit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        TB_Tools.add(BT_Edit);
        TB_Tools.add(jSeparator9);

        BT_Delete.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Delete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/remove.png"))); // NOI18N
        BT_Delete.setToolTipText("delete");
        BT_Delete.setActionCommand("delete");
        BT_Delete.setPreferredSize(new java.awt.Dimension(32, 32));
        TB_Tools.add(BT_Delete);

        PN_Providers.add(TB_Tools, java.awt.BorderLayout.PAGE_START);

        jTabbedPane1.addTab("Providers", PN_Providers);

        jLabel7.setText("<html>To use a client side embeded provider, enable it here and use the specified URL as provider.<br>\nThe OSM API bandwidth is limited, so after a while an error will occur and no more data will be downloaded.<br>\nJust wait a moment before trying again.</html>");

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("Local Http port");
        jLabel8.setPreferredSize(new java.awt.Dimension(200, 26));

        CB_LocalProvider.setSelected(true);
        CB_LocalProvider.setText("Enabled");
        CB_LocalProvider.setActionCommand("localProviderEnabled");

        SP_LocalProviderPort.setModel(new javax.swing.SpinnerNumberModel(8088, 1024, 65535, 1));
        SP_LocalProviderPort.setEnabled(false);
        SP_LocalProviderPort.setPreferredSize(new java.awt.Dimension(100, 26));

        TF_LocalProviderURL.setEditable(false);
        TF_LocalProviderURL.setText("http://localhost:8088/${Z}/${X}/${Y}.json");

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel9.setText("OSM Server API URL");
        jLabel9.setPreferredSize(new java.awt.Dimension(200, 26));

        TF_LocalProviderAPI.setText("https://www.overpass-api.de/api/xapi?");
        TF_LocalProviderAPI.setEnabled(false);

        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel10.setText("Enabled");
        jLabel10.setPreferredSize(new java.awt.Dimension(200, 26));

        LB_LocalProviderStatus.setText("...");

        LB_Loaded.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        LB_Loaded.setText("0");
        LB_Loaded.setToolTipText("Loaded bytes from OSM Api (is limited per IP)");

        TA_Logs.setColumns(20);
        TA_Logs.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        TA_Logs.setRows(5);
        jScrollPane1.setViewportView(TA_Logs);

        BT_ClearLogs.setText("Clear logs");
        BT_ClearLogs.setActionCommand("clearLogs");

        LB_Log.setText("...");
        LB_Log.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        LB_Log.setPreferredSize(new java.awt.Dimension(52, 26));

        javax.swing.GroupLayout PN_LocalProviderLayout = new javax.swing.GroupLayout(PN_LocalProvider);
        PN_LocalProvider.setLayout(PN_LocalProviderLayout);
        PN_LocalProviderLayout.setHorizontalGroup(
            PN_LocalProviderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PN_LocalProviderLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PN_LocalProviderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                    .addGroup(PN_LocalProviderLayout.createSequentialGroup()
                        .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(CB_LocalProvider)
                        .addGap(18, 18, 18)
                        .addComponent(LB_LocalProviderStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(LB_Loaded, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(PN_LocalProviderLayout.createSequentialGroup()
                        .addGroup(PN_LocalProviderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PN_LocalProviderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PN_LocalProviderLayout.createSequentialGroup()
                                .addComponent(SP_LocalProviderPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(TF_LocalProviderURL))
                            .addComponent(TF_LocalProviderAPI)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PN_LocalProviderLayout.createSequentialGroup()
                        .addComponent(LB_Log, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(BT_ClearLogs)))
                .addContainerGap())
        );
        PN_LocalProviderLayout.setVerticalGroup(
            PN_LocalProviderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PN_LocalProviderLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PN_LocalProviderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(PN_LocalProviderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(CB_LocalProvider, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(LB_LocalProviderStatus)
                        .addComponent(LB_Loaded)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PN_LocalProviderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SP_LocalProviderPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TF_LocalProviderURL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PN_LocalProviderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(TF_LocalProviderAPI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 259, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PN_LocalProviderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BT_ClearLogs)
                    .addComponent(LB_Log, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Local Provider", PN_LocalProvider);

        add(jTabbedPane1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JButton BT_Add;
    protected javax.swing.JButton BT_Cache;
    protected javax.swing.JButton BT_Clear;
    protected javax.swing.JButton BT_ClearLogs;
    protected javax.swing.JButton BT_Delete;
    protected javax.swing.JButton BT_Down;
    protected javax.swing.JButton BT_Edit;
    protected javax.swing.JButton BT_Up;
    protected javax.swing.JCheckBox CB_LocalProvider;
    protected javax.swing.JLabel LB_Description;
    protected javax.swing.JLabel LB_Loaded;
    protected javax.swing.JLabel LB_LocalProviderStatus;
    protected javax.swing.JLabel LB_Log;
    protected javax.swing.JLabel LB_Name;
    protected javax.swing.JPanel PN_LocalProvider;
    protected javax.swing.JPanel PN_ProviderData;
    protected javax.swing.JPanel PN_Providers;
    protected javax.swing.JSpinner SP_ExpireDays;
    protected javax.swing.JSpinner SP_LocalProviderPort;
    protected javax.swing.JSpinner SP_MaxLevel;
    protected javax.swing.JSpinner SP_MinLevel;
    protected javax.swing.JTextArea TA_Logs;
    protected javax.swing.JTable TB_Providers;
    protected javax.swing.JToolBar TB_Tools;
    protected javax.swing.JTextField TF_CachePath;
    protected javax.swing.JTextField TF_CacheSize;
    protected javax.swing.JTextField TF_LocalProviderAPI;
    protected javax.swing.JTextField TF_LocalProviderURL;
    protected javax.swing.JTextField TF_Title;
    protected javax.swing.JTextField TF_Url;
    protected javax.swing.JLabel jLabel1;
    protected javax.swing.JLabel jLabel10;
    protected javax.swing.JLabel jLabel2;
    protected javax.swing.JLabel jLabel3;
    protected javax.swing.JLabel jLabel4;
    protected javax.swing.JLabel jLabel5;
    protected javax.swing.JLabel jLabel6;
    protected javax.swing.JLabel jLabel7;
    protected javax.swing.JLabel jLabel8;
    protected javax.swing.JLabel jLabel9;
    protected javax.swing.JPanel jPanel1;
    protected javax.swing.JScrollPane jScrollPane1;
    protected javax.swing.JScrollPane jScrollPane2;
    protected javax.swing.JToolBar.Separator jSeparator9;
    protected javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables

    //**************************************************************************
    //*** Private
    //**************************************************************************
    private long calculateSize(File f) {
        long si = 0;
        if (f.isFile()) {
            si = f.length();

        } else if (f.isDirectory()) {
            File fi[] = f.listFiles();
            for (int i = 0; i < fi.length; i++) si = si + calculateSize(fi[i]);

        }
        return si;
    }

    private void recursiveDelete(File f) {
        if (f.isFile()) {
            f.delete();

        } else if (f.isDirectory()) {
            File fi[] = f.listFiles();
            for (int i = 0; i < fi.length; i++) recursiveDelete(fi[i]);

        }
    }

    /**
     * Open input dialog to ask for wms server modification
     */
    private void edit(int index) {
        OSMBuildingProvider pr = list.getElementAt(index);

        TF_Title.setText(pr.getTitle());
        TF_Url.setText(pr.getUrl());
        SP_MinLevel.setValue(pr.getMinLevel());
        SP_MaxLevel.setValue(pr.getMaxLevel());

        int rep = JOptionPane.showConfirmDialog(this, PN_ProviderData, "Provider", JOptionPane.OK_CANCEL_OPTION);
        if (rep == JOptionPane.OK_OPTION) {
            DefaultTableModel model = (DefaultTableModel) TB_Providers.getModel();
            pr.setTitle(TF_Title.getText().trim());
            pr.setUrl(TF_Url.getText().trim());
            pr.setMinLevel((Integer) SP_MinLevel.getValue());
            pr.setMaxLevel((Integer) SP_MaxLevel.getValue());
            model.setValueAt(pr.getTitle(), index, 0);
            model.setValueAt(pr.getUrl(), index, 1);
            model.setValueAt(pr.getMinLevel(), index, 2);
            model.setValueAt(pr.getMaxLevel(), index, 3);

        }
    }

    private void add(int index) {
        TF_Title.setText("");
        TF_Url.setText("http://[abcd].data.osmbuildings.org/0.2/sx3pxpz6/tile/${Z}/${X}/${Y}.json");
        SP_MinLevel.setValue(15);
        SP_MaxLevel.setValue(15);

        int rep = JOptionPane.showConfirmDialog(this, PN_ProviderData, "Provider", JOptionPane.OK_CANCEL_OPTION);
        if (rep == JOptionPane.OK_OPTION) {
            OSMBuildingProvider pr = new OSMBuildingProvider(TF_Title.getText().trim(), TF_Url.getText().trim(), (Integer) SP_MinLevel.getValue(), (Integer) SP_MaxLevel.getValue());
            list.insertElementAt(pr, index);
            DefaultTableModel model = (DefaultTableModel) TB_Providers.getModel();

            model.insertRow(index, new Object[]{pr.getTitle(), pr.getUrl(), pr.getMinLevel(), pr.getMaxLevel()});

        }

    }

}
