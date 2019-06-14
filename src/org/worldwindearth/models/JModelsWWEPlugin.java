/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.models;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.util.BasicDragger;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import java.awt.CardLayout;
import java.awt.IllegalComponentStateException;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.InputStream;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import org.tinyrcp.App;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.worldwindearth.WWEFactory;
import org.worldwindearth.WWEPlugin;
import org.worldwindearth.components.DragSurfacePolygon;

/**
 *
 * @author sbodmer
 */
public class JModelsWWEPlugin extends javax.swing.JPanel implements WWEPlugin, ChangeListener, ActionListener, MouseListener, ModelLoaderListener, ListSelectionListener, org.worldwindearth.components.DragSurfacePolygon.DragSurfacePolygonListener {

    WWEFactory factory = null;
    App app = null;
    WorldWindow ww = null;
    File lastDir = new File(System.getProperty("user.home"), "Temp/PaysDeBrest/70_104_OBJ/70_104_BATI_TEXTURE/70_104_COMPLET_TEXTURE");

    protected JDesktopPane desktop = null;
    protected ModelsLayer layer = null;

    protected Vec4 defaultSunDirection = null;
    protected Material defaultSunMat = null;

    protected Model selected = null;
    protected DragSurfacePolygon cross = null;

    protected BasicDragger dragger = null;
    
    // protected ScreenAnnotation loading = null;

    /**
     * Creates new form OSMBuildingsWWELayerPlugin
     */
    public JModelsWWEPlugin(WWEFactory factory, WorldWindow ww) {
        this.factory = factory;
        this.ww = ww;

        initComponents();

        //--- Visual representation fine tuning (8 decimals)
        SP_Latitude.setEditor(new JSpinner.NumberEditor(SP_Latitude, "0.000000000"));
        SP_Longitude.setEditor(new JSpinner.NumberEditor(SP_Longitude, "0.000000000"));
        SP_Scale.setEditor(new JSpinner.NumberEditor(SP_Scale, "0.0000"));
        
        dragger = new BasicDragger(ww);
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
        // System.out.println("MOUSE CLICKED:"+e+" POS:"+pos);
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
        this.desktop = (JDesktopPane) argument;

        BasicLayerFactory bl = new BasicLayerFactory();

        InputStream in = getClass().getResourceAsStream("/org/worldwindearth/models/Resources/Config/Models.xml");
        layer = (ModelsLayer) bl.createFromConfigSource(in, null);
        layer.setName("3D Models");
        layer.setModelLoadingListener(this);
        // System.out.println("PICK:"+layer.isPickEnabled());
        // layer.setPickEnabled(true);

        BT_Clear.addActionListener(this);
        BT_Delete.addActionListener(this);
        BT_Edit.addActionListener(this);
        BT_Add.addActionListener(this);

        TA_Logs.addMouseListener(this);
        MN_ClearLogs.addActionListener(this);

        TB_Models.addMouseListener(this);
        TB_Models.getSelectionModel().addListSelectionListener(this);
        TB_Models.getColumnModel().getColumn(0).setMaxWidth(32);
        TB_Models.getColumnModel().getColumn(1).setCellRenderer(new JModelCellRenderer());
        SP_Latitude.addChangeListener(this);
        SP_Longitude.addChangeListener(this);
        SP_Altitude.addChangeListener(this);
        SP_Azimuth.addChangeListener(this);
        SP_Elevation.addChangeListener(this);
        SP_Roll.addChangeListener(this);
        SP_Scale.addChangeListener(this);

        // 
        // layer.setPickEnabled(true);
        /*
        AnnotationAttributes defaultAttributes = new AnnotationAttributes();
        defaultAttributes.setCornerRadius(10);
        defaultAttributes.setInsets(new Insets(8, 8, 8, 8));
        defaultAttributes.setBackgroundColor(new Color(0f, 0f, 0f, .5f));
        defaultAttributes.setTextColor(Color.WHITE);
        defaultAttributes.setDrawOffset(new Point(25, 25));
        defaultAttributes.setDistanceMinScale(.5);
        defaultAttributes.setDistanceMaxScale(2);
        defaultAttributes.setDistanceMinOpacity(.5);
        defaultAttributes.setLeaderGapWidth(14);
        defaultAttributes.setDrawOffset(new Point(20, 40));
        loading = new ScreenAnnotation("--- Loading model, the interface will freeze during loading ---", new Point(20, 50));
        loading.getAttributes().setDefaults(defaultAttributes);
        loading.getAttributes().setCornerRadius(0);
        loading.getAttributes().setSize(new Dimension(200, 0));
        loading.getAttributes().setAdjustWidthToText(AVKey.SIZE_FIXED); // use strict dimension width - 200
        loading.getAttributes().setDrawOffset(new Point(100, 0)); // screen point is annotation bottom left corner
        loading.getAttributes().setHighlightScale(1);             // No highlighting either
        layer.addAnnotation(loading);
        */
        
        //--- Create the drag surface polygon
        double fac = 0.0001;
        cross = new DragSurfacePolygon(DragSurfacePolygon.ARROWS, fac, 0d, 0d);
        cross.setDragSurfacePolygonListener(this);
        ShapeAttributes attrs = new BasicShapeAttributes();
        attrs.setInteriorMaterial(Material.BLACK);
        attrs.setOutlineMaterial(Material.WHITE);
        attrs.setInteriorOpacity(0.7);
        attrs.setOutlineOpacity(1);
        attrs.setOutlineWidth(5);
        cross.setAttributes(attrs);
        layer.addRenderable(cross);
        cross.setVisible(false);
    }

    @Override
    public void configure(Element config) {
        if (config == null) return;

        DefaultTableModel mo = (DefaultTableModel) TB_Models.getModel();
        NodeList nl = config.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeName().equals("Model")) {
                Element e = (Element) nl.item(i);
                try {
                    File f = new File(e.getAttribute("path"));
                    Model m = new Model(f);
                    m.setLatitude(Double.parseDouble(e.getAttribute("lat")));
                    m.setLongitude(Double.parseDouble(e.getAttribute("lon")));
                    m.setAltitude(Double.parseDouble(e.getAttribute("alt")));
                    m.setAzimuth(Double.parseDouble(e.getAttribute("azimuth")));
                    m.setElevation(Double.parseDouble(e.getAttribute("elevation")));
                    m.setRoll(Double.parseDouble(e.getAttribute("roll")));
                    m.setTitle(e.getFirstChild().getNodeValue());
                    m.setScale(Double.parseDouble(e.getAttribute("scale")));

                    boolean show = e.getAttribute("show").equals("true");

                    Object obj[] = {show ? Boolean.TRUE : Boolean.FALSE, m};
                    mo.addRow(obj);
                    layer.addModel(m);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        }

    }

    @Override
    public void cleanup() {
        layer.dispose();

        layer.setPickEnabled(false);
    }

    @Override
    public void saveConfig(Element config) {
        if (config == null) return;

        // config.setAttribute("opacity", "" + SP_Opacity.getValue());

        for (int i = 0; i < TB_Models.getRowCount(); i++) {
            Boolean show = (Boolean) TB_Models.getValueAt(i, 0);
            Model m = (Model) TB_Models.getValueAt(i, 1);
            Element e = config.getOwnerDocument().createElement("Model");
            e.setAttribute("lat", "" + m.getLatitude());
            e.setAttribute("lon", "" + m.getLongitude());
            e.setAttribute("alt", "" + m.getAltitude());
            e.setAttribute("azimuth", "" + m.getAzimuth());
            e.setAttribute("elevation", "" + m.getElevation());
            e.setAttribute("roll", "" + m.getRoll());
            e.setAttribute("scale", "" + m.getScale());
            e.setAttribute("show", "" + show);
            e.setAttribute("path", m.getFile().getPath());
            e.appendChild(config.getOwnerDocument().createTextNode(m.getTitle()));
            config.appendChild(e);
        }
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
        if (e.getSource() == SP_Latitude) {
            //--- One by one, so the widget event are correctly fired
            int index = TB_Models.getSelectedRow();
            if (index == -1) return;
            Model m = (Model) TB_Models.getValueAt(index, 1);
            double lat = (double) SP_Latitude.getValue();
            m.setLatitude(lat);
            //--- This call will move the surface polygon in all case even
            //--- if already moved by drag framework
            cross.moveTo(Position.fromDegrees(selected.getLatitude(), selected.getLongitude()));

        } else if (e.getSource() == SP_Longitude) {
            int index = TB_Models.getSelectedRow();
            if (index == -1) return;
            Model m = (Model) TB_Models.getValueAt(index, 1);
            double lon = (double) SP_Longitude.getValue();
            m.setLongitude(lon);
            cross.moveTo(Position.fromDegrees(selected.getLatitude(), selected.getLongitude()));

        } else if (e.getSource() == SP_Altitude) {
            int index = TB_Models.getSelectedRow();
            if (index == -1) return;
            Model m = (Model) TB_Models.getValueAt(index, 1);
            double alt = (double) SP_Altitude.getValue();
            m.setAltitude(alt);

        } else if (e.getSource() == SP_Azimuth) {
            int index = TB_Models.getSelectedRow();
            if (index == -1) return;
            Model m = (Model) TB_Models.getValueAt(index, 1);
            double h = (double) SP_Azimuth.getValue();
            m.setAzimuth(h);

        } else if (e.getSource() == SP_Elevation) {
            int index = TB_Models.getSelectedRow();
            if (index == -1) return;
            Model m = (Model) TB_Models.getValueAt(index, 1);
            double p = (double) SP_Elevation.getValue();
            m.setElevation(p);

        } else if (e.getSource() == SP_Roll) {
            int index = TB_Models.getSelectedRow();
            if (index == -1) return;
            Model m = (Model) TB_Models.getValueAt(index, 1);
            double r = (double) SP_Roll.getValue();
            m.setRoll(r);

        } else if (e.getSource() == SP_Scale) {
            int index = TB_Models.getSelectedRow();
            if (index == -1) return;
            Model m = (Model) TB_Models.getValueAt(index, 1);
            double r = (double) SP_Scale.getValue();
            m.setScale(r);

        }
        ww.redraw();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("clear")) {
            layer.clearModels();
            DefaultTableModel m = (DefaultTableModel) TB_Models.getModel();
            m.setRowCount(0);
            SP_Latitude.setValue(0);
            SP_Longitude.setValue(0);
            SP_Altitude.setValue(0);

        } else if (e.getActionCommand().equals("clearLogs")) {
            TA_Logs.setText("");

        } else if (e.getActionCommand().equals("edit")) {
            int index = TB_Models.getSelectedRow();
            if (index > -1) {
                Model m = (Model) TB_Models.getValueAt(index, 1);
                String nt = JOptionPane.showInputDialog(this, "Title", m.getTitle());
                if (nt != null) m.setTitle(nt);
                TB_Models.repaint();
            }

        } else if (e.getActionCommand().equals("add")) {
            //--- Add a custom geojson object
            JFileChooser jchooser = new JFileChooser(lastDir);
            jchooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isDirectory()) return true;
                    if (pathname.getName().endsWith(".obj")) return true;
                    return false;
                }

                @Override
                public String getDescription() {
                    return "Supported models (.obj)";
                }

            });
            int rep = jchooser.showOpenDialog(this);
            if (rep == JFileChooser.APPROVE_OPTION) {
                try {
                    File f = jchooser.getSelectedFile();
                    lastDir = f.getParentFile();
                    Rectangle vp = ww.getView().getViewport();
                    Position pos = ww.getView().computePositionFromScreenPoint(vp.width / 2, vp.height / 2);
                    double ele = ww.getModel().getGlobe().getElevation(pos.latitude, pos.longitude);

                    Model m = new Model(pos.getLatitude().degrees, pos.getLongitude().degrees, ele, f);
                    Object obj[] = {Boolean.TRUE, m};
                    DefaultTableModel mo = (DefaultTableModel) TB_Models.getModel();
                    mo.addRow(obj);
                    layer.addModel(m);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        } else if (e.getActionCommand().equals("delete")) {
            int index = TB_Models.getSelectedRow();
            if (index > -1) {
                Model m = (Model) TB_Models.getValueAt(index, 1);
                layer.removeModel(m);
                DefaultTableModel dmodel = (DefaultTableModel) TB_Models.getModel();
                dmodel.removeRow(index);
                m.setRenderable(null);
            }

        }
        ww.redraw();
    }

    //**************************************************************************
    //*** MouseListener
    //**************************************************************************
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == TB_Models) {
            int index = TB_Models.getSelectedRow();
            if (index == -1) return;
            Model m = (Model) TB_Models.getValueAt(index, 1);
            // SP_Longitude.setValue(m.getLongitude());
            // SP_Latitude.setValue(m.getLatitude());
            // SP_Altitude.setValue(m.getAltitude());
            m.setVisible((boolean) TB_Models.getValueAt(index, 0));
            if (e.getClickCount() >= 2) {
                try {
                    View v = ww.getView();
                    Position ref = Position.fromDegrees(m.getLatitude(), m.getLongitude(), m.getAltitude());
                    if (v instanceof BasicOrbitView) {
                        BasicOrbitView view = (BasicOrbitView) v;
                        view.addPanToAnimator(ref, view.getHeading(), view.getPitch(), view.getZoom());

                    } else {
                        v.goTo(ref, 1000);
                    }

                } catch (Exception ex) {
                    //---
                }

            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        try {
            if (e.isPopupTrigger()) {
                PU_Logs.show(TA_Logs, e.getX(), e.getY());
            }

        } catch (IllegalComponentStateException ex) {
            //--- Not on screen

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
    //*** ModelLoaderListener
    //**************************************************************************
    @Override
    public void modelLoading(final String file, final String message) {
        //---
        // GL2 gl = ww.getContext().getGL().getGL2();
        // PN_Loading.setBounds(desktop.getWidth()/2-150, desktop.getHeight()/2-32, 300, 64);
        // desktop.add(PN_Loading);
        // PN_Loading.setVisible(true);
    }

    @Override
    public void modelLoaded(String file, String message) {
        // desktop.remove(PN_Loading);
        PN_Loading.setVisible(false);
    }

    @Override
    public void modelLoadingFailed(String file, String message) {
        // desktop.remove(PN_Loading);
    }

    //**************************************************************************
    //*** ListSelectionModel
    //**************************************************************************
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getValueIsAdjusting() == true) return;

        ww.removeSelectListener(dragger);
        //--- Remove old surface quad
        CardLayout layout = (CardLayout) PN_Data.getLayout();
        int index = TB_Models.getSelectedRow();
        if (index != -1) {
            layout.show(PN_Data, "coordinates");
            selected = (Model) TB_Models.getValueAt(index, 1);
            SP_Latitude.setValue(selected.getLatitude());
            SP_Longitude.setValue(selected.getLongitude());
            SP_Altitude.setValue(selected.getAltitude());
            SP_Azimuth.setValue(selected.getAzimuth());
            SP_Elevation.setValue(selected.getElevation());
            SP_Roll.setValue(selected.getRoll());
            SP_Scale.setValue(selected.getScale());

            ww.addSelectListener(dragger);
            cross.moveTo(Position.fromDegrees(selected.getLatitude(), selected.getLongitude()));
            cross.setVisible(true);

        } else {
            layout.show(PN_Data, "empty");
            selected = null;
            cross.setVisible(false);
        }
    }

    //**************************************************************************
    //*** DragSurfacePolygonListener
    //**************************************************************************
    @Override
    public void surfacePolygonDragged(DragSurfacePolygon poly, Position newPosition) {
        //--- Setting widget will trigger the stateChange call which will move
        //--- the model
        SP_Latitude.setValue(newPosition.latitude.getDegrees());
        SP_Longitude.setValue(newPosition.longitude.getDegrees());

        if (CB_StickToTerrain.isSelected()) {
            double ele = ww.getModel().getGlobe().getElevation(newPosition.latitude, newPosition.longitude);
            SP_Altitude.setValue(ele);
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

        PU_Logs = new javax.swing.JPopupMenu();
        MN_ClearLogs = new javax.swing.JMenuItem();
        PN_Loading = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        TB_Tools = new javax.swing.JToolBar();
        BT_Add = new javax.swing.JButton();
        BT_Edit = new javax.swing.JButton();
        jSeparator9 = new javax.swing.JToolBar.Separator();
        BT_Delete = new javax.swing.JButton();
        BT_Clear = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        TB_Models = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        TA_Logs = new javax.swing.JTextArea();
        PN_Data = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        PN_Coordinates = new javax.swing.JPanel();
        SP_Latitude = new javax.swing.JSpinner();
        SP_Longitude = new javax.swing.JSpinner();
        SP_Altitude = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        SP_Azimuth = new javax.swing.JSpinner();
        SP_Elevation = new javax.swing.JSpinner();
        SP_Roll = new javax.swing.JSpinner();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        SP_Scale = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        CB_StickToTerrain = new javax.swing.JCheckBox();

        MN_ClearLogs.setText("Clear logs");
        MN_ClearLogs.setActionCommand("clearLogs");
        PU_Logs.add(MN_ClearLogs);

        PN_Loading.setBackground(java.awt.Color.orange);

        jLabel2.setText("Object loading in progress, please wait...");
        PN_Loading.add(jLabel2);

        setLayout(new java.awt.BorderLayout());

        jLabel10.setBackground(java.awt.Color.orange);
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel10.setText("!!! Loading textures can freeze the interface, so be patient !!!");
        jLabel10.setOpaque(true);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(jPanel2, java.awt.BorderLayout.PAGE_START);

        jPanel3.setLayout(new java.awt.BorderLayout());

        TB_Tools.setBorder(null);
        TB_Tools.setFloatable(false);

        BT_Add.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Add.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/add.png"))); // NOI18N
        BT_Add.setToolTipText("add");
        BT_Add.setActionCommand("add");
        BT_Add.setPreferredSize(new java.awt.Dimension(26, 26));
        TB_Tools.add(BT_Add);

        BT_Edit.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Edit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/edit.png"))); // NOI18N
        BT_Edit.setToolTipText("edit");
        BT_Edit.setActionCommand("edit");
        BT_Edit.setFocusable(false);
        BT_Edit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_Edit.setPreferredSize(new java.awt.Dimension(26, 26));
        BT_Edit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        TB_Tools.add(BT_Edit);
        TB_Tools.add(jSeparator9);

        BT_Delete.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Delete.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/remove.png"))); // NOI18N
        BT_Delete.setToolTipText("delete");
        BT_Delete.setActionCommand("delete");
        BT_Delete.setPreferredSize(new java.awt.Dimension(26, 26));
        TB_Tools.add(BT_Delete);

        BT_Clear.setText("Clear");
        BT_Clear.setActionCommand("clear");
        BT_Clear.setFocusable(false);
        BT_Clear.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_Clear.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        TB_Tools.add(BT_Clear);

        jPanel3.add(TB_Tools, java.awt.BorderLayout.PAGE_START);

        TB_Models.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "", "Name"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Boolean.class, java.lang.Object.class
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
        TB_Models.setRowHeight(26);
        jScrollPane3.setViewportView(TB_Models);

        jPanel3.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("3D Models", jPanel3);

        TA_Logs.setColumns(20);
        TA_Logs.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        TA_Logs.setRows(5);
        jScrollPane2.setViewportView(TA_Logs);

        jTabbedPane1.addTab("Logs", jScrollPane2);

        add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        PN_Data.setLayout(new java.awt.CardLayout());

        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Select a model and move it if needed");
        PN_Data.add(jLabel9, "empty");

        SP_Latitude.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        SP_Latitude.setModel(new javax.swing.SpinnerNumberModel(0.0d, null, null, 0.01d));
        SP_Latitude.setPreferredSize(new java.awt.Dimension(120, 26));

        SP_Longitude.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        SP_Longitude.setModel(new javax.swing.SpinnerNumberModel(0.0d, null, null, 0.01d));
        SP_Longitude.setPreferredSize(new java.awt.Dimension(120, 28));

        SP_Altitude.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        SP_Altitude.setModel(new javax.swing.SpinnerNumberModel(0.0d, null, null, 1.0d));
        SP_Altitude.setPreferredSize(new java.awt.Dimension(120, 28));

        jLabel1.setText("Latitude");
        jLabel1.setPreferredSize(new java.awt.Dimension(80, 26));

        jLabel3.setText("Longitude");
        jLabel3.setPreferredSize(new java.awt.Dimension(80, 26));

        jLabel4.setText("Altitude");
        jLabel4.setPreferredSize(new java.awt.Dimension(80, 26));

        SP_Azimuth.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        SP_Azimuth.setModel(new javax.swing.SpinnerNumberModel(0.0d, null, null, 1.0d));
        SP_Azimuth.setToolTipText("Heading");
        SP_Azimuth.setPreferredSize(new java.awt.Dimension(60, 26));

        SP_Elevation.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        SP_Elevation.setModel(new javax.swing.SpinnerNumberModel(0.0d, null, null, 1.0d));
        SP_Elevation.setToolTipText("Heading");
        SP_Elevation.setPreferredSize(new java.awt.Dimension(60, 26));

        SP_Roll.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        SP_Roll.setModel(new javax.swing.SpinnerNumberModel(0.0d, null, null, 1.0d));
        SP_Roll.setToolTipText("Heading");
        SP_Roll.setPreferredSize(new java.awt.Dimension(60, 26));

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel5.setText("A");
        jLabel5.setToolTipText("Azimuth (heading)");
        jLabel5.setPreferredSize(new java.awt.Dimension(30, 26));

        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel6.setText("E");
        jLabel6.setToolTipText("Elevation (pitch)");
        jLabel6.setPreferredSize(new java.awt.Dimension(30, 26));

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel7.setText("R");
        jLabel7.setToolTipText("Roll");
        jLabel7.setPreferredSize(new java.awt.Dimension(30, 26));

        SP_Scale.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        SP_Scale.setModel(new javax.swing.SpinnerNumberModel(1.0d, 0.0d, null, 0.1d));
        SP_Scale.setToolTipText("Size");
        SP_Scale.setPreferredSize(new java.awt.Dimension(100, 26));

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        jLabel8.setText("S");
        jLabel8.setToolTipText("Scale");
        jLabel8.setPreferredSize(new java.awt.Dimension(30, 26));

        CB_StickToTerrain.setText("Stick to terrain");
        CB_StickToTerrain.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        javax.swing.GroupLayout PN_CoordinatesLayout = new javax.swing.GroupLayout(PN_Coordinates);
        PN_Coordinates.setLayout(PN_CoordinatesLayout);
        PN_CoordinatesLayout.setHorizontalGroup(
            PN_CoordinatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PN_CoordinatesLayout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(PN_CoordinatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PN_CoordinatesLayout.createSequentialGroup()
                        .addGroup(PN_CoordinatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PN_CoordinatesLayout.createSequentialGroup()
                                .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(SP_Altitude, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(PN_CoordinatesLayout.createSequentialGroup()
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(SP_Longitude, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(6, 6, 6)
                        .addGroup(PN_CoordinatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PN_CoordinatesLayout.createSequentialGroup()
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(SP_Roll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(PN_CoordinatesLayout.createSequentialGroup()
                                .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(SP_Elevation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(CB_StickToTerrain))))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PN_CoordinatesLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(SP_Latitude, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(SP_Azimuth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(SP_Scale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        PN_CoordinatesLayout.setVerticalGroup(
            PN_CoordinatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PN_CoordinatesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PN_CoordinatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SP_Latitude, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SP_Azimuth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SP_Scale, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PN_CoordinatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SP_Longitude, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SP_Elevation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(CB_StickToTerrain))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PN_CoordinatesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SP_Altitude, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SP_Roll, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        PN_Data.add(PN_Coordinates, "coordinates");

        add(PN_Data, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JButton BT_Add;
    protected javax.swing.JButton BT_Clear;
    protected javax.swing.JButton BT_Delete;
    protected javax.swing.JButton BT_Edit;
    protected javax.swing.JCheckBox CB_StickToTerrain;
    protected javax.swing.JMenuItem MN_ClearLogs;
    protected javax.swing.JPanel PN_Coordinates;
    protected javax.swing.JPanel PN_Data;
    protected javax.swing.JPanel PN_Loading;
    protected javax.swing.JPopupMenu PU_Logs;
    protected javax.swing.JSpinner SP_Altitude;
    protected javax.swing.JSpinner SP_Azimuth;
    protected javax.swing.JSpinner SP_Elevation;
    protected javax.swing.JSpinner SP_Latitude;
    protected javax.swing.JSpinner SP_Longitude;
    protected javax.swing.JSpinner SP_Roll;
    protected javax.swing.JSpinner SP_Scale;
    protected javax.swing.JTextArea TA_Logs;
    protected javax.swing.JTable TB_Models;
    protected javax.swing.JToolBar TB_Tools;
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
    protected javax.swing.JPanel jPanel2;
    protected javax.swing.JPanel jPanel3;
    protected javax.swing.JScrollPane jScrollPane2;
    protected javax.swing.JScrollPane jScrollPane3;
    protected javax.swing.JToolBar.Separator jSeparator9;
    protected javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables

}
