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
import javax.swing.Icon;
import javax.swing.JComponent;
import org.tinyrcp.App;
import org.tinyrcp.TinyPlugin;
import org.w3c.dom.Element;
import org.worldwindearth.WWE;
import org.worldwindearth.WWEPlugin;
import org.worldwindearth.WWEFactory;

/**
 *
 * @author sbodmer
 */
public class JOSMBuildingsWWEFactory extends javax.swing.JPanel implements WWEFactory, ActionListener {

    public static final String COPYRIGHT_TEXT = "© Data OpenStreetMap · © 3D OSM Buildings";

    App app = null;

    /**
     * Creates new form OSMBuildingsWWELayerPluginFactory
     */
    public JOSMBuildingsWWEFactory() {
        initComponents();

        try {
            FileStore fs = WorldWind.getDataFileStore();
            URL url = fs.findFile(OSMBuildingsLayer.CACHE_FOLDER, false);
            File f = new File(url.toURI());
            f.mkdirs();
            TF_CachePath.setText(f.getPath());

        } catch (URISyntaxException ex) {
            TF_CachePath.setText(ex.getMessage());
            ex.printStackTrace();

        } catch (NullPointerException ex) {
            TF_CachePath.setText(ex.getMessage());
            ex.printStackTrace();
        }

    }

    //**************************************************************************
    //*** API
    //**************************************************************************
    public int getExpireDays() {
        return (int) SP_ExpireDays.getValue();

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

        BT_Cache.addActionListener(this);
        BT_Clear.addActionListener(this);
    }

    @Override
    public void configure(Element config) {
        if (config == null) return;

        try {
            SP_ExpireDays.setValue(Integer.parseInt(config.getAttribute("expireDays")));

        } catch (NumberFormatException ex) {

        }
    }

    @Override
    public TinyPlugin newPlugin(Object o) {
        return new JOSMBuildingsWWEPlugin(this, (WorldWindow) o);
    }

    @Override
    public void store(Element config) {
        if (config == null) return;

        config.setAttribute("expireDays", SP_ExpireDays.getValue().toString());
    }

    @Override
    public void destroy() {
        //---
    }

    @Override
    public Object getProperty(String property) {
        if (property.equals(PROPERTY_COPYRIGHT_TEXT)) return COPYRIGHT_TEXT;
        return null;
    }

    //**************************************************************************
    //*** ActionListener
    //**************************************************************************
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("cache")) {
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
        jPanel1 = new javax.swing.JPanel();
        BT_Cache = new javax.swing.JButton();
        TF_CacheSize = new javax.swing.JTextField();
        BT_Clear = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        SP_ExpireDays = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        TF_CachePath = new javax.swing.JTextField();

        LB_Name.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/osmbuildings/Resources/Icons/22x22/osmbuildings.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/osmbuildings/OSMBuildings"); // NOI18N
        LB_Name.setText(bundle.getString("factory_name")); // NOI18N

        setLayout(new java.awt.BorderLayout());

        BT_Cache.setText("Check cache size");
        BT_Cache.setActionCommand("cache");

        TF_CacheSize.setEditable(false);

        BT_Clear.setText("Clear cache");
        BT_Clear.setActionCommand("clear");

        jLabel2.setText("Expire date (days)");

        SP_ExpireDays.setModel(new javax.swing.SpinnerNumberModel(30, 0, 365, 1));
        SP_ExpireDays.setToolTipText("Set the tile cache expire days");

        jLabel3.setText("Cache path is");

        TF_CachePath.setEditable(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(TF_CachePath))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(BT_Cache)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(TF_CacheSize, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(BT_Clear))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(SP_ExpireDays, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 98, Short.MAX_VALUE)))
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
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(TF_CachePath, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(198, Short.MAX_VALUE))
        );

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JButton BT_Cache;
    protected javax.swing.JButton BT_Clear;
    protected javax.swing.JLabel LB_Description;
    protected javax.swing.JLabel LB_Name;
    protected javax.swing.JSpinner SP_ExpireDays;
    protected javax.swing.JTextField TF_CachePath;
    protected javax.swing.JTextField TF_CacheSize;
    protected javax.swing.JLabel jLabel2;
    protected javax.swing.JLabel jLabel3;
    protected javax.swing.JPanel jPanel1;
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
            for (int i = 0;i < fi.length;i++) si = si + calculateSize(fi[i]);

        }
        return si;
    }

    private void recursiveDelete(File f) {
        if (f.isFile()) {
            f.delete();

        } else if (f.isDirectory()) {
            File fi[] = f.listFiles();
            for (int i = 0;i < fi.length;i++) recursiveDelete(fi[i]);

        }
    }

}
