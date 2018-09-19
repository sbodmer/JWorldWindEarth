/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osmbuildings;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.event.Message;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.event.SelectListener;
import gov.nasa.worldwind.formats.geojson.GeoJSONDoc;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.pick.PickedObject;
import gov.nasa.worldwind.render.BasicLightingModel;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.ExtrudedPolygon;
import gov.nasa.worldwind.render.LightingModel;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Polygon;
import gov.nasa.worldwind.terrain.Tessellator;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import org.tinyrcp.App;
import org.w3c.dom.Element;
import org.worldwindearth.WWE;
import org.worldwindearth.WWEFactory;
import org.worldwindearth.WWEPlugin;

/**
 *
 * @author sbodmer
 */
public class JOSMBuildingsWWEPlugin extends javax.swing.JPanel implements WWEPlugin, ChangeListener, ActionListener, SelectListener, OSMBuildingsLayer.PreBuildingsRenderer, OSMBuildingsTileListener, MouseListener {

    WWEFactory factory = null;
    App app = null;
    WorldWindow ww = null;
    File lastDir = new File(System.getProperty("user.home"), "Sources/netbeans/JWorldWindEarth/Resources/GeoJSON");

    DefaultListModel model = new DefaultListModel();
    protected OSMBuildingsLayer layer = null;

    /**
     * Current selected provider (cnnot use combobox model which is shared for
     * all layers)
     */
    protected OSMBuildingProvider provider = null;

    protected Vec4 defaultSunDirection = null;
    protected Material defaultSunMat = null;

    /**
     * Creates new form OSMBuildingsWWELayerPlugin
     */
    public JOSMBuildingsWWEPlugin(WWEFactory factory, WorldWindow ww, DefaultComboBoxModel<OSMBuildingProvider> list) {
        this.factory = factory;
        this.ww = ww;

        initComponents();

        LI_Entries.setModel(model);

        CMB_Providers.setModel(list);

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

        InputStream in = getClass().getResourceAsStream("/org/osmbuildings/Resources/Config/OSMBuildings.xml");
        // InputStream in = app.getLoader().getResourceAsStream("org/osmbuildings/Resources/Config/OSMBuildings.xml");
        layer = (OSMBuildingsLayer) bl.createFromConfigSource(in, null);
        layer.setName("OSMBuildings");
        layer.setExpiryTime(((JOSMBuildingsWWEFactory) factory).getExpireDays() * 24L * 60L * 60L * 1000L);
        // System.out.println("PICK:"+layer.isPickEnabled());
        // layer.setPickEnabled(true);
        provider = (OSMBuildingProvider) CMB_Providers.getSelectedItem();
        if (provider == null) provider = new OSMBuildingProvider("www.osmbuildings.org","https://[abcd].data.osmbuildings.org/0.2/sx3pxpz6/tile/${Z}/${X}/${Y}.json", 15, 15);
        layer.setMinLevel(provider.getMinLevel());
        layer.setMaxLevel(provider.getMaxLevel());
        layer.setProvider(provider.getUrl());
        layer.addPreBuildingsRenderer(this);
        layer.addTileListener(this);
        ww.addSelectListener(this);

        LB_Provider.setText(provider.getTitle());

        SP_DefaultHeight.addChangeListener(this);
        CB_DrawProcessingBox.addActionListener(this);
        SP_MaxTiles.addChangeListener(this);
        SP_Opacity.addChangeListener(this);
        SP_Rows.addChangeListener(this);

        CB_DrawOutline.addActionListener(this);
        CB_ApplyRoofTextures.addActionListener(this);

        BT_Clear.addActionListener(this);
        BT_Delete.addActionListener(this);

        BT_Add.addActionListener(this);

        TA_Logs.addMouseListener(this);
        MN_ClearLogs.addActionListener(this);

        BT_Apply.addActionListener(this);

        LI_Entries.addMouseListener(this);
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
            CB_FixedLighting.setSelected(config.getAttribute("fixedLighting").equals("true"));

            String prov = config.getAttribute("provider");
            if (!prov.equals("")) {
                //--- Find  the provider
                DefaultComboBoxModel<OSMBuildingProvider> model = (DefaultComboBoxModel<OSMBuildingProvider>) CMB_Providers.getModel();

                for (int i = 0; i < model.getSize(); i++) {
                    OSMBuildingProvider pr = model.getElementAt(i);
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
        config.setAttribute("fixedLighting", "" + CB_FixedLighting.isSelected());
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
        if (e.getSource() == SP_DefaultHeight) {
            layer.setDefaultBuildingHeight((int) SP_DefaultHeight.getValue());
            layer.clearTiles();

        } else if (e.getSource() == SP_MaxTiles) {
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
        if (e.getActionCommand().equals("drawProcessingBox")) {
            layer.setDrawProcessingBox(CB_DrawProcessingBox.isSelected());

        } else if (e.getActionCommand().equals("drawOutline")) {
            layer.setDrawOutline(CB_DrawOutline.isSelected());

        } else if (e.getActionCommand().equals("applyRoofTextures")) {
            layer.setApplyRoofTextures(CB_ApplyRoofTextures.isSelected());

        } else if (e.getActionCommand().equals("clear")) {
            layer.clearTiles();

        } else if (e.getActionCommand().equals("clearLogs")) {
            TA_Logs.setText("");

        } else if (e.getActionCommand().equals("apply")) {
            provider = (OSMBuildingProvider) CMB_Providers.getSelectedItem();
            LB_Provider.setText(provider.getTitle());
            layer.setProvider(provider.getUrl());
            layer.setMinLevel(provider.getMinLevel());
            layer.setMaxLevel(provider.getMaxLevel());
            layer.clearTiles();

        } else if (e.getActionCommand().equals("add")) {
            //--- Add a custom geojson object
            JFileChooser jchooser = new JFileChooser(lastDir);
            jchooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isDirectory()) return true;
                    if (pathname.getName().endsWith(".json")) return true;
                    return false;
                }

                @Override
                public String getDescription() {
                    return "GeoJSON objects";
                }

            });
            int rep = jchooser.showOpenDialog(this);
            if (rep == JFileChooser.APPROVE_OPTION) {
                try {
                    File f = jchooser.getSelectedFile();
                    lastDir = f.getParentFile();
                    GeoJSONDoc doc = new GeoJSONDoc(f);
                    doc.parse();
                    GeoJSONEntry entry = new GeoJSONEntry(f.getName(), doc, null);
                    model.addElement(entry);
                    layer.addGeoJSONEntry(entry);
                    doc.close();

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        } else if (e.getActionCommand().equals("delete")) {
            GeoJSONEntry entry = LI_Entries.getSelectedValue();
            if (entry != null) {
                model.removeElement(entry);
                layer.removeGeoJSONEntry(entry);
            }

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
                    txt += "Feature ID   : " + p.getValue(OSMBuildingsRenderable.AVKEY_OSMBUILDING_FEATURE_ID) + "\n";
                    txt += "Inner bounds : " + p.getValue(OSMBuildingsRenderable.AVKEY_OSMBUILDING_HAS_INNER_BOUNDS) + "\n";
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
                    txt += "Feature ID   : " + p.getValue(OSMBuildingsRenderable.AVKEY_OSMBUILDING_FEATURE_ID) + "\n";
                    txt += "Inner bounds : " + p.getValue(OSMBuildingsRenderable.AVKEY_OSMBUILDING_HAS_INNER_BOUNDS) + "\n";

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
                    txt += "Feature ID   : " + p.getValue(OSMBuildingsRenderable.AVKEY_OSMBUILDING_FEATURE_ID) + "\n";

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

    //**************************************************************************
    //*** PreBuildingsRenderer
    //**************************************************************************
    @Override
    public void preBuildingsRender(DrawContext dc) {
        // Tessellator tes = dc.getGlobe().getTessellator();
        LightingModel lm = dc.getStandardLightingModel();
        if (lm instanceof BasicLightingModel) {
            BasicLightingModel blm = (BasicLightingModel) lm;
            if (defaultSunDirection == null) defaultSunDirection = blm.getLightDirection();
            if (defaultSunMat == null) defaultSunMat = blm.getLightMaterial();

            if (CB_FixedLighting.isSelected()) {
                blm.setLightDirection(defaultSunDirection);
                blm.setLightMaterial(defaultSunMat);

            } else {
                // blm.setLightDirection(Vec4.INFINITY);;
                //--- If in WWE Context do some stuff here
                Tessellator tes = dc.getGlobe().getTessellator();
                Vec4 sun = (Vec4) tes.getValue(WWE.TESSELATOR_KEY_SUN_DIRECTION);
                if (sun != null) {
                    Color color = (Color) tes.getValue(WWE.TESSELATOR_KEY_SUN_COLOR);
                    blm.setLightDirection(sun);
                    Color am = (Color) tes.getValue(WWE.TESSELATOR_KEY_SUN_AMBIENT_COLOR);
                    Material m = new Material(Color.WHITE, color, am, Color.BLACK, 0);
                    blm.setLightMaterial(m);

                } else {
                    blm.setLightDirection(defaultSunDirection);
                    blm.setLightMaterial(defaultSunMat);
                }
            }

        }

        //--- Move cursor to center of viewport
        // if (center != null) cursor.moveTo(new Position(center, 0));
    }

    //**************************************************************************
    //*** OSMBuildingsTileListener
    //**************************************************************************
    @Override
    public void osmBuildingsLoaded(OSMBuildingsTile btile) {
        //---
    }

    @Override
    public void osmBuildingsLoading(OSMBuildingsTile btile) {
        //---
    }

    @Override
    public void osmBuildingsLoadingFailed(OSMBuildingsTile btile, String reason) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int len = TA_Logs.getText().length();
                if (len > 65535) TA_Logs.setText("");

                TA_Logs.append("(E) " + btile.x + "x" + btile.y + " " + reason + "\n");
                TA_Logs.append("(E) " + btile.url + "\n");
                TA_Logs.setCaretPosition(TA_Logs.getText().length());
            }
        });
    }

    /**
     * Not used in this context
     *
     * @param id
     * @return
     */
    @Override
    public boolean osmBuildingsProduceRenderableForId(String id) {
        return false;
    }

    //**************************************************************************
    //*** MouseListener
    //**************************************************************************
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == LI_Entries) {
            if (e.getClickCount() >= 2) {
                GeoJSONEntry ge = LI_Entries.getSelectedValue();
                if (ge != null) {
                    try {
                        //--- Could be another renderable...
                        OSMBuildingsRenderable renderable = (OSMBuildingsRenderable) ge.getRenderable();
                        Position ref = renderable.getReferencePosition();
                        View v = ww.getView();
                        if (v instanceof BasicOrbitView) {
                            BasicOrbitView view = (BasicOrbitView) v;
                            view.addPanToAnimator(ref, view.getHeading(), view.getPitch(), view.getZoom());

                        } else {
                            v.goTo(ref, 1000);
                        }
                        
                    } catch (Exception ex) {

                    }
                }
            }
        }
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
        jLabel7 = new javax.swing.JLabel();
        SP_Rows = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        CB_FixedLighting = new javax.swing.JCheckBox();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        TA_Object = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        TA_Logs = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        TB_Tools = new javax.swing.JToolBar();
        BT_Add = new javax.swing.JButton();
        BT_Edit = new javax.swing.JButton();
        jSeparator9 = new javax.swing.JToolBar.Separator();
        BT_Delete = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        LI_Entries = new javax.swing.JList<>();
        jPanel2 = new javax.swing.JPanel();
        SP_Opacity = new javax.swing.JSlider();
        CMB_Providers = new javax.swing.JComboBox<>();
        BT_Apply = new javax.swing.JButton();
        LB_Provider = new javax.swing.JLabel();

        MN_ClearLogs.setText("Clear logs");
        MN_ClearLogs.setActionCommand("clearLogs");
        PU_Logs.add(MN_ClearLogs);

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

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setText("Double Click on a polygon to see details");
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

        TA_Object.setColumns(20);
        TA_Object.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        TA_Object.setLineWrap(true);
        TA_Object.setRows(5);
        jScrollPane1.setViewportView(TA_Object);

        jTabbedPane1.addTab("Selection", jScrollPane1);

        TA_Logs.setColumns(20);
        TA_Logs.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        TA_Logs.setRows(5);
        jScrollPane2.setViewportView(TA_Logs);

        jTabbedPane1.addTab("Logs", jScrollPane2);

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

        jPanel3.add(TB_Tools, java.awt.BorderLayout.PAGE_START);

        LI_Entries.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane4.setViewportView(LI_Entries);

        jPanel3.add(jScrollPane4, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Custom GeoJson", jPanel3);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(SP_MaxTiles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(19, 252, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(BT_Clear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jTabbedPane1)
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
                                        .addComponent(CB_ApplyRoofTextures, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(SP_Rows, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(CB_FixedLighting, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 246, Short.MAX_VALUE)))
                        .addContainerGap())))
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
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
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

        LB_Provider.setBackground(new java.awt.Color(144, 202, 249));
        LB_Provider.setFont(new java.awt.Font("DejaVu Sans", 1, 12)); // NOI18N
        LB_Provider.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
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
                    .addComponent(SP_Opacity, javax.swing.GroupLayout.DEFAULT_SIZE, 484, Short.MAX_VALUE)
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
    protected javax.swing.JButton BT_Add;
    protected javax.swing.JButton BT_Apply;
    protected javax.swing.JButton BT_Clear;
    protected javax.swing.JButton BT_Delete;
    protected javax.swing.JButton BT_Edit;
    protected javax.swing.JCheckBox CB_ApplyRoofTextures;
    protected javax.swing.JCheckBox CB_DrawOutline;
    protected javax.swing.JCheckBox CB_DrawProcessingBox;
    protected javax.swing.JCheckBox CB_FixedLighting;
    protected javax.swing.JComboBox<OSMBuildingProvider> CMB_Providers;
    protected javax.swing.JLabel LB_Provider;
    protected javax.swing.JList<GeoJSONEntry> LI_Entries;
    protected javax.swing.JMenuItem MN_ClearLogs;
    protected javax.swing.JPopupMenu PU_Logs;
    protected javax.swing.JSpinner SP_DefaultHeight;
    protected javax.swing.JSpinner SP_MaxTiles;
    protected javax.swing.JSlider SP_Opacity;
    protected javax.swing.JSpinner SP_Rows;
    protected javax.swing.JTextArea TA_Logs;
    protected javax.swing.JTextArea TA_Object;
    protected javax.swing.JToolBar TB_Tools;
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
    protected javax.swing.JPanel jPanel3;
    protected javax.swing.JScrollPane jScrollPane1;
    protected javax.swing.JScrollPane jScrollPane2;
    protected javax.swing.JScrollPane jScrollPane4;
    protected javax.swing.JToolBar.Separator jSeparator9;
    protected javax.swing.JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables

}
