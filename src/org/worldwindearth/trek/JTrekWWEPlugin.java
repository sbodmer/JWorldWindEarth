/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.trek;

import org.osm.*;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.Layer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import org.tinyrcp.App;
import org.tinyrcp.TinyFactory;
import org.w3c.dom.Element;
import org.worldwindearth.WWEPlugin;

/**
 * Stars dome
 * 
 * @author sbodmer
 */
public class JTrekWWEPlugin extends JPanel implements WWEPlugin, ActionListener, ChangeListener, ListSelectionListener {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    
    App app = null;
    TinyFactory factory = null;
    WorldWindow ww = null;
    
    OSMLayer layer = new OSMLayer();
    
    File lastDir = new File(System.getProperty("user.home"));
    
    /**
     * 
     * @param factory
     */
    public JTrekWWEPlugin(TinyFactory factory, WorldWindow ww) {
        super();
        this.factory = factory;
        this.ww = ww;
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        initComponents();
        
        TB_GPX.getTableHeader().getColumnModel().getColumn(0).setMaxWidth(32);
        
        TB_GPX.getSelectionModel().addListSelectionListener(this);
    }

    
    
    //**************************************************************************
    //*** Plugin
    //**************************************************************************
    @Override
    public String getPluginName() {
        return layer.getName();
    }
   
    @Override
    public TinyFactory getPluginFactory() {
        return factory;
    }

    @Override
    public void setup(App app, Object arg) {
        this.app = app;
        
        SL_Opacity.addChangeListener(this);
        BT_Import.addActionListener(this);
        BT_Export.addActionListener(this);
        
        layer.setName("Trek");
        
    }

    @Override
    public void cleanup() {
        layer.dispose();
        
    }

    @Override
    public void saveConfig(Element config) {
        if (config == null) return;
        
        config.setAttribute("opacity", ""+SL_Opacity.getValue());
                
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
    public JComponent getConfigComponent() {
        return this;
    }

    @Override
    public void configure(Element config) {
        if (config == null) return;
        
        try {
            int opacity = Integer.parseInt(config.getAttribute("opacity"));
            SL_Opacity.setValue(opacity);
            layer.setOpacity((double) (opacity/100d));
            layer.setUseTransparentTextures(opacity==100?false:true);
            
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
        if (e.getActionCommand().equals("import")) {
            JFileChooser jf =new JFileChooser(lastDir);
            jf.setMultiSelectionEnabled(false);
            jf.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jf.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    if (f.getName().toLowerCase().endsWith(".gpx")) return true;
                    if (f.isDirectory()) return true;
                    return false;
                            
                }

                @Override
                public String getDescription() {
                    return ".gpx files";
                }
                
            });
            int rep = jf.showOpenDialog(getTopLevelAncestor());
            if (rep == JFileChooser.APPROVE_OPTION) {
                File f = jf.getSelectedFile();
                lastDir = f.getParentFile();
            
                GPX g = new GPX(f);
                Object obj[] = { Boolean.TRUE, g};
                DefaultTableModel model = (DefaultTableModel) TB_GPX.getModel();
                model.addRow(obj);
            }
            
        } else if (e.getActionCommand().equals("export")) {
            
        }
    }
    
    //**************************************************************************
    //*** ChangeListener
    //**************************************************************************
    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == SL_Opacity) {
            int opacity = SL_Opacity.getValue();
            layer.setOpacity(opacity/100d);
            layer.setUseTransparentTextures(opacity==100?false:true);
            ww.redraw();
            
        }
    }
    
    //**************************************************************************
    //*** ListSelectionListener
    //**************************************************************************
    @Override
    public void valueChanged(ListSelectionEvent e) {
        
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
        SL_Opacity = new javax.swing.JSlider();
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        BT_New = new javax.swing.JButton();
        BT_Edit = new javax.swing.JButton();
        BT_Import = new javax.swing.JButton();
        BT_Export = new javax.swing.JButton();
        jSeparator9 = new javax.swing.JToolBar.Separator();
        BT_Delete = new javax.swing.JButton();
        jPanel11 = new javax.swing.JPanel();
        LB_Track = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        TB_GPX = new javax.swing.JTable();
        jPanel4 = new javax.swing.JPanel();
        jToolBar2 = new javax.swing.JToolBar();
        BT_DeletePoint = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        TB_Waypoints = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        TB_Route = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        TB_Track = new javax.swing.JTable();

        setLayout(new java.awt.BorderLayout());

        SL_Opacity.setFont(new java.awt.Font("Monospaced", 0, 10)); // NOI18N
        SL_Opacity.setMajorTickSpacing(10);
        SL_Opacity.setMinorTickSpacing(5);
        SL_Opacity.setPaintLabels(true);
        SL_Opacity.setPaintTicks(true);
        SL_Opacity.setToolTipText("Transparency");
        SL_Opacity.setValue(100);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SL_Opacity, javax.swing.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SL_Opacity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel2, java.awt.BorderLayout.NORTH);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel3.setPreferredSize(new java.awt.Dimension(456, 200));
        jPanel3.setLayout(new java.awt.BorderLayout());

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);

        BT_New.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_New.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/add.png"))); // NOI18N
        BT_New.setToolTipText("new layer");
        BT_New.setActionCommand("new");
        BT_New.setFocusable(false);
        BT_New.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_New.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_New.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(BT_New);

        BT_Edit.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Edit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/edit.png"))); // NOI18N
        BT_Edit.setToolTipText("rename layer");
        BT_Edit.setActionCommand("edit");
        BT_Edit.setFocusable(false);
        BT_Edit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_Edit.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_Edit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(BT_Edit);

        BT_Import.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Import.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/edit.png"))); // NOI18N
        BT_Import.setToolTipText("import");
        BT_Import.setActionCommand("import");
        BT_Import.setFocusable(false);
        BT_Import.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_Import.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_Import.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(BT_Import);

        BT_Export.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Export.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/edit.png"))); // NOI18N
        BT_Export.setToolTipText("Export");
        BT_Export.setActionCommand("export");
        BT_Export.setFocusable(false);
        BT_Export.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_Export.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_Export.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(BT_Export);
        jToolBar1.add(jSeparator9);

        BT_Delete.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Delete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/remove.png"))); // NOI18N
        BT_Delete.setToolTipText("delete layer");
        BT_Delete.setActionCommand("delete");
        BT_Delete.setFocusable(false);
        BT_Delete.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_Delete.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_Delete.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(BT_Delete);

        jPanel3.add(jToolBar1, java.awt.BorderLayout.NORTH);

        jPanel11.setMaximumSize(new java.awt.Dimension(32767, 24));
        jPanel11.setOpaque(false);
        jPanel11.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        LB_Track.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LB_Track.setText("...");
        jPanel11.add(LB_Track);

        jPanel3.add(jPanel11, java.awt.BorderLayout.SOUTH);

        TB_GPX.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Show", "GPX"
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
        jScrollPane5.setViewportView(TB_GPX);

        jPanel3.add(jScrollPane5, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel3, java.awt.BorderLayout.NORTH);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);

        BT_DeletePoint.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_DeletePoint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/remove.png"))); // NOI18N
        BT_DeletePoint.setToolTipText("delete layer");
        BT_DeletePoint.setActionCommand("delete");
        BT_DeletePoint.setFocusable(false);
        BT_DeletePoint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_DeletePoint.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_DeletePoint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar2.add(BT_DeletePoint);

        jPanel4.add(jToolBar2, java.awt.BorderLayout.NORTH);

        TB_Waypoints.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title", "Lat", "Lon", "Elevation"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Double.class, java.lang.Double.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                true, true, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(TB_Waypoints);

        jTabbedPane1.addTab("Waypoints", jScrollPane3);

        TB_Route.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane4.setViewportView(TB_Route);

        jTabbedPane1.addTab("Routes", jScrollPane4);

        TB_Track.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Time", "Lat", "Lon", "Elevation"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Double.class, java.lang.Double.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                true, true, true, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(TB_Track);

        jTabbedPane1.addTab("Track", jScrollPane2);

        jPanel4.add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        jPanel1.add(jPanel4, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BT_Delete;
    private javax.swing.JButton BT_DeletePoint;
    private javax.swing.JButton BT_Edit;
    private javax.swing.JButton BT_Export;
    private javax.swing.JButton BT_Import;
    private javax.swing.JButton BT_New;
    private javax.swing.JLabel LB_Track;
    private javax.swing.JSlider SL_Opacity;
    private javax.swing.JTable TB_GPX;
    private javax.swing.JTable TB_Route;
    private javax.swing.JTable TB_Track;
    private javax.swing.JTable TB_Waypoints;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JToolBar.Separator jSeparator9;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    // End of variables declaration//GEN-END:variables

    

    

    

    
}
