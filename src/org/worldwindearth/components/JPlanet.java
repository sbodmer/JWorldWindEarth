/*
 * JNotesPanel.java
 *
 * Created on July 26, 2006, 3:09 PM
 */
package org.worldwindearth.components;

import org.worldwindearth.WWEInputHandler;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.event.InputHandler;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.util.BasicDragger;
import gov.nasa.worldwind.view.firstperson.BasicFlyView;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.tinyrcp.App;
import org.tinyrcp.AppActionEvent;
import org.tinyrcp.TinyFactory;
import org.w3c.dom.*;
import org.worldwindearth.WWEPlugin;
import org.tinyrcp.TinyPlugin;
import org.worldwindearth.BasicWalkFlyView;
import org.worldwindearth.JWWEPluginCellRenderer;
import static org.worldwindearth.WWE.STORAGE_DATA_JPLANETS;
import static org.worldwindearth.WWE.STORAGE_DATA_JPLANETS_RELATIONS;
import org.worldwindearth.WWEFactory;
import static org.worldwindearth.WWEPlugin.AVKEY_WORLDWIND_LAYER_PLUGIN;
import org.worldwindearth.WorldWindLayersTableModel;
import org.worldwindearth.components.renderers.JCameraCellRenderer;
import org.worldwindearth.components.renderers.JCameraSmallCellRenderer;

/**
 * Main WorldWind panel
 *
 * @author sbodmer
 */
public class JPlanet extends JPanel implements KeyListener, ComponentListener, ActionListener, ListSelectionListener, ChangeListener, MouseListener, MouseWheelListener, MouseMotionListener, TableModelListener, RenderingListener, Transferable {

    public static DataFlavor DATAFLAVOR_JPLANET = null;

    NumberFormat nf = new DecimalFormat("#.########");

    App app = null;
    ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
    ResourceBundle bundle = null;

    //--- Signature to store relation between JPlanet panels
    String signature = "" + hashCode();

    /**
     * World wind window
     */
    // WorldWindowGLCanvas wwd = null;
    WorldWindowGLJPanel wwd = null;

    JPlanetTransferHandler cth = null;

    /**
     * Initial camera position for the BasicOrbitView
     */
    Position center = null;
    double zoom = 1000d;    //--- distance from eye to center of view
    double heading = 0.0d;
    double pitch = 0.0d;
    double roll = 0.0d;
    DefaultListModel<Camera> cameras = new DefaultListModel<Camera>();

    /**
     * The linked planets (when move occured here, forward same move to other
     * planets)
     *
     */
    ArrayList<JPlanet> attachedPlanets = new ArrayList<>();
    JPlanet parentPlanet = null;

    /**
     * The layers list
     */
    WorldWindLayersTableModel layers = null;

    /**
     * The possible views
     */
    BasicOrbitView orbit = null;
    BasicFlyView fly = null;
    BasicWalkFlyView walk = null;

    WWEInputHandler wweih = new WWEInputHandler();

    /**
     * The worldwindmodel
     */
    Model m = null;

    /**
     * Custom layer plugin, the key is the plugin hash code
     */
    HashMap<String, WWEPlugin> plugins = new HashMap<>();

    /**
     * The refresh tick
     */
    javax.swing.Timer timer = null;

    /**
     * New viewport broadcast timer
     */
    javax.swing.Timer vtimer = null;

    /**
     * First person timer
     */
    javax.swing.Timer fptimer = null;

    /**
     * Full screen frame
     */
    JFrame jframe = null;         //--- Fullscreen frame

    /**
     * The current selected layer
     */
    WWEPlugin selected = null;

    ListCellRenderer<Camera> cameraCellRenderer = null;
    ListCellRenderer<Camera> cameraSmallCellRenderer = null;

    JCross PN_Cross = null;

    /**
     * The current key pressed
     */
    int KEY_W = 8;
    int KEY_S = 4;
    int KEY_A = 2;
    int KEY_D = 1;
    boolean shift = false;
    int wsad = 0000;
    int iteration = 0;

    /**
     * Creates new form JTerminals
     */
    public JPlanet() {
        bundle = ResourceBundle.getBundle("org.worldwindearth.components.Planet");

        initComponents();

        PN_Cross = new JCross();
        DP_Main.add(PN_Cross, JLayeredPane.POPUP_LAYER);
        PN_Cross.setVisible(false);
        LB_FPKeys.setVisible(false);

        BT_Configure.setVisible(false);
        PN_Cameras.setVisible(false);
        LI_Cameras.setModel(cameras);

        MN_ClearAttached.addActionListener(this);

        //--- Set a custom desktop manager, which will block the layers internal frame if sticky
        TB_Layers.getSelectionModel().addListSelectionListener(this);
        TB_Layers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //--- Find available screens
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gd = ge.getScreenDevices();
        for (int i = 0; i < gd.length; i++) {
            JRadioButtonMenuItem jitem = new JRadioButtonMenuItem(gd[i].getIDstring());
            jitem.setActionCommand("screen");
            jitem.addActionListener(this);
            jitem.putClientProperty("gd", gd[i]);
            MN_Screens.add(jitem);
            btgscreens.add(jitem);
            if (i == 0) jitem.setSelected(true);
        }
        //--- Add window fullscreen option
        JRadioButtonMenuItem jitem = new JRadioButtonMenuItem("Window");
        jitem.setActionCommand("screen");
        jitem.addActionListener(this);
        MN_Screens.add(jitem);
        btgscreens.add(jitem);

        //--- Initial view and position display on the bottom
        timer = new javax.swing.Timer(1000, this);
        //timer.setInitialDelay(5000);

        //--- View port timer
        vtimer = new javax.swing.Timer(1000, this);
        vtimer.setRepeats(false);

        //--- First persont timer (key press polling)
        fptimer = new javax.swing.Timer(100, this);

    }

    //**************************************************************************
    //*** API
    //**************************************************************************
    public void addActionListener(ActionListener listener) {
        if (!listeners.contains(listener)) listeners.add(listener);
    }

    public void removeActionListener(ActionListener listener) {
        listeners.remove(listener);
    }

    /**
     * Link a JPlanet panel (camera view).
     *
     * THis component movement will be forwared to all attached planet
     *
     * @param linked
     */
    public void addAttachedPlanet(JPlanet linked) {
        if (!attachedPlanets.contains(linked)) {
            attachedPlanets.add(linked);

        }

    }

    public boolean removeAttachedPlanet(JPlanet linked) {
        return attachedPlanets.remove(linked);
    }

    /**
     * Set the parent JPlanet which this component is attached to
     *
     * @param jp
     */
    public void setParentPlanet(JPlanet jp) {
        this.parentPlanet = jp;
        BT_Attach.setVisible(jp == null);
    }

    public JPlanet getParentPlanet() {
        return parentPlanet;
    }

    /**
     * Return this panel signature (for relation)
     *
     * @return
     */
    public String getSignature() {
        return signature;
    }

    public WorldWindowGLJPanel getWorldWindowGLJPanel() {
        return wwd;
    }

    /**
     * The passed obj is the planet string (WWEFactory.PLUGIN_PLANET.xxx)
     */
    public void initialize(App app, Model model, String planet) {
        this.app = app;
        this.m = model;

        if (DATAFLAVOR_JPLANET == null) {
            try {
                Thread.currentThread().setContextClassLoader(app.getLoader());
                DATAFLAVOR_JPLANET = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + "; class=org.worldwindearth.components.JPlanet");

            } catch (Exception ex) {
                ex.printStackTrace();

            }
        }

        wwd = new WorldWindowGLJPanel();
        wwd.setModel(m);
        orbit = new BasicOrbitView() {
            @Override
            public double computeNearClipDistance() {
                return 10d; //--- set near clipping distance in meters
            }
        };
        fly = new BasicFlyView();
        walk = new BasicWalkFlyView();
        // WWEViewInputHandler vhh = new WWEViewInputHandler();
        // view.setViewInputHandler(vhh);
        wwd.setView(orbit);
        wwd.setInputHandler(wweih);
        wwd.addRenderingListener(this);

        
        wweih.addMouseListener(this);
        wweih.addMouseMotionListener(this);
        wweih.addMouseWheelListener(this);

        cameraCellRenderer = new JCameraCellRenderer(app);
        cameraSmallCellRenderer = new JCameraSmallCellRenderer(app);
        LI_Cameras.setCellRenderer(cameraCellRenderer);
        LI_Cameras.addMouseListener(this);
        BT_Cameras.addActionListener(this);
        BT_NewCamera.addActionListener(this);
        BT_RemoveCamera.addActionListener(this);
        BT_UpdateCamera.addActionListener(this);
        BT_CameraUp.addActionListener(this);
        BT_CameraDown.addActionListener(this);
        BT_SmallCamera.addActionListener(this);

        BT_Fly.addActionListener(this);
        BT_Orbit.addActionListener(this);
        BT_Walk.addActionListener(this);

        BT_AddLayer.addActionListener(this);
        BT_RemoveLayer.addActionListener(this);
        BT_LayerUp.addActionListener(this);
        BT_LayerDown.addActionListener(this);
        BT_Configure.addActionListener(this);

        BT_More.addActionListener(this);

        BT_ScrollLeft.addActionListener(this);
        BT_ScrollRight.addActionListener(this);

        BT_RenameLayer.addActionListener(this);

        DP_Main.add(wwd, new Integer(-10)); //--- Lowest priority
        DP_Main.addComponentListener(this);
        // DP_Main.addMouseListener(this);

        PU_More.add(app.createFactoryMenus(app.getString("word_panels", "App"), TinyFactory.PLUGIN_CATEGORY_PANEL, TinyFactory.PLUGIN_FAMILY_PANEL, planet, this), 0);
        PU_More.add(app.createFactoryMenus(app.getString("word_containers", "App"), TinyFactory.PLUGIN_CATEGORY_PANEL, TinyFactory.PLUGIN_FAMILY_CONTAINER, planet, this), 1);

        MN_HideStatusBar.addActionListener(this);
        MN_HideLayers.addActionListener(this);
        MN_HideTopBar.addActionListener(this);
        MN_SwitchSide.addActionListener(this);

        MN_Fullscreen.addActionListener(this);
        MN_ScreenIdentifier.addActionListener(this);
        MN_GoTo.addActionListener(this);

        SP_VerticalExageration.addChangeListener(this);
        CB_Wireframe.addActionListener(this);

        InputHandler ih = wwd.getInputHandler();
        ih.addKeyListener(this);
        ih.addMouseListener(this);
        ih.addMouseWheelListener(this);

        //--- Register drag and drop action for camera (view) link
        cth = new JPlanetTransferHandler(this);
        BT_Attach.addMouseMotionListener(this);
        BT_Attach.addMouseListener(this);
        BT_Attach.putClientProperty("jplanet", this);
        BT_Attach.setTransferHandler(cth);
        DP_Main.setTransferHandler(cth);

        //--- Register the JPlanet is global storage
        HashMap<String, JPlanet> hm = (HashMap<String, JPlanet>) app.fetchData(STORAGE_DATA_JPLANETS);
        if (hm == null) {
            hm = new HashMap<String, JPlanet>();
            app.storeData(STORAGE_DATA_JPLANETS, hm);
        }
        hm.put(signature, this);

    }

    public void configure(Element config) {
        HashMap<String, JPlanet> hm = (HashMap<String, JPlanet>) app.fetchData(STORAGE_DATA_JPLANETS);

        //------------------------------------------------------------------------
        //--- Find "layer" category in jars and create the new layer menu
        //------------------------------------------------------------------------
        MN_NewLayers.removeAll();
        JMenu jmenu = app.createFactoryMenus("Worldwind", WWEFactory.PLUGIN_CATEGORY_WORLDWIND_LAYER, WWEFactory.PLUGIN_FAMILY_WORLDWIND_LAYER_WORLDWIND, this);
        MN_NewLayers.add(jmenu, 0);
        jmenu = app.createFactoryMenus("Nasa", WWEFactory.PLUGIN_CATEGORY_WORLDWIND_LAYER, WWEFactory.PLUGIN_FAMILY_WORLDWIND_LAYER_NASA, this);
        MN_NewLayers.add(jmenu, 1);
        jmenu = app.createFactoryMenus("Map Tiles", WWEFactory.PLUGIN_CATEGORY_WORLDWIND_LAYER, WWEFactory.PLUGIN_FAMILY_WORLDWIND_LAYER_MAPTILES, this);
        MN_NewLayers.add(jmenu, 2);
        jmenu = app.createFactoryMenus("Web Map Server (WMS)", WWEFactory.PLUGIN_CATEGORY_WORLDWIND_LAYER, WWEFactory.PLUGIN_FAMILY_WORLDWIND_LAYER_WMS, this);
        MN_NewLayers.add(jmenu, 3);
        jmenu = app.createFactoryMenus("Buildings", WWEFactory.PLUGIN_CATEGORY_WORLDWIND_LAYER, WWEFactory.PLUGIN_FAMILY_WORLDWIND_LAYER_BUILDINGS, this);
        MN_NewLayers.add(jmenu, 4);
        jmenu = app.createFactoryMenus("Graticules", WWEFactory.PLUGIN_CATEGORY_WORLDWIND_LAYER, WWEFactory.PLUGIN_FAMILY_WORLDWIND_LAYER_GRATICULE, this);
        MN_NewLayers.add(jmenu, 5);
        jmenu = app.createFactoryMenus("Search", WWEFactory.PLUGIN_CATEGORY_WORLDWIND_LAYER, WWEFactory.PLUGIN_FAMILY_WORLDWIND_LAYER_SEARCH, this);
        MN_NewLayers.add(jmenu, 6);
        jmenu = app.createFactoryMenus("Measures", WWEFactory.PLUGIN_CATEGORY_WORLDWIND_LAYER, WWEFactory.PLUGIN_FAMILY_WORLDWIND_LAYER_MEASURES, this);
        MN_NewLayers.add(jmenu, 7);
        
        //--- Prepare table
        layers = new WorldWindLayersTableModel(m.getLayers());
        TB_Layers.setModel(layers);
        TB_Layers.setDefaultRenderer(Layer.class, new JWWEPluginCellRenderer(app));
        TB_Layers.getColumnModel().getColumn(0).setPreferredWidth(26);
        TB_Layers.getColumnModel().getColumn(0).setWidth(26);
        TB_Layers.getColumnModel().getColumn(0).setMaxWidth(26);
        TB_Layers.getColumnModel().getColumn(0).setMinWidth(26);

        //--- Instantiate layers and panels
        if (config != null) {
            //--- Store the jplanet in global storage
            signature = config.getAttribute("signature");
            if (signature.equals("")) signature = "" + hashCode();
            hm.put(signature, this);

            NodeList nl = config.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i).getNodeName().equals("Center")) {
                    try {
                        //--- Set viewport
                        Element c = (Element) nl.item(i);
                        double y = Double.parseDouble(c.getAttribute("lat"));
                        double x = Double.parseDouble(c.getAttribute("lon"));
                        double alt = Double.parseDouble(c.getAttribute("alt"));

                        center = Position.fromDegrees(y, x, alt);

                        heading = Double.parseDouble(c.getAttribute("heading"));
                        pitch = Double.parseDouble(c.getAttribute("pitch"));
                        roll = Double.parseDouble(c.getAttribute("roll"));
                        zoom = Double.parseDouble(c.getAttribute("zoom"));

                    } catch (NumberFormatException ex) {
                        ex.printStackTrace();

                    } catch (NullPointerException ex) {
                        ex.printStackTrace();
                    }

                } else if (nl.item(i).getNodeName().equals("Cameras")) {
                    NodeList nl2 = ((Element) nl.item(i)).getChildNodes();
                    for (int j = 0; j < nl2.getLength(); j++) {
                        if (nl2.item(j).getNodeName().equals("Camera")) {
                            Element e = (Element) nl2.item(j);

                            try {
                                double lat = Double.parseDouble(e.getAttribute("lat"));
                                double lon = Double.parseDouble(e.getAttribute("lon"));
                                double alt = Double.parseDouble(e.getAttribute("alt"));
                                double heading = Double.parseDouble(e.getAttribute("heading"));
                                double pitch = Double.parseDouble(e.getAttribute("pitch"));
                                double zoom = Double.parseDouble(e.getAttribute("zoom"));
                                Position p = Position.fromDegrees(lat, lon, alt);
                                Image thumb = null;
                                try {
                                    byte b[] = Base64.getDecoder().decode(e.getFirstChild().getNodeValue());
                                    thumb = Toolkit.getDefaultToolkit().createImage(b);

                                } catch (NullPointerException ex) {
                                    //--- No image
                                }
                                Camera c = new Camera(e.getAttribute("title"), p, heading, pitch, zoom, thumb);
                                cameras.addElement(c);

                            } catch (NumberFormatException ex) {

                            }
                        }
                    }

                } else if (nl.item(i).getNodeName().equals("Layers")) {
                    try {
                        //--- Set layers internal frame position
                        Element e = (Element) nl.item(i);
                        int loc = Integer.parseInt(e.getAttribute("mainDividerLocation"));
                        SP_Main.setDividerLocation(loc);
                        SP_Main.setLastDividerLocation(300);
                        boolean hidden = e.getAttribute("hideLayers").equals("true");
                        MN_HideLayers.setSelected(hidden);

                        loc = Integer.parseInt(e.getAttribute("dividerLocation"));
                        SP_Layers.setDividerLocation(loc);
                        SP_Layers.setLastDividerLocation(250);

                    } catch (NumberFormatException ex) {
                        // ex.printStackTrace();

                    }

                } else if (nl.item(i).getNodeName().equals("WorldWindLayer")) {
                    Element e = (Element) nl.item(i);
                    TinyFactory fac = app.getFactory(e.getAttribute("factory"));
                    if (fac != null) {
                        WWEPlugin p = (WWEPlugin) fac.newPlugin(wwd);
                        if (p != null) {
                            p.setup(app, DP_Main);
                            p.configure(e);
                            Layer l = p.getLayer();
                            l.setValue(AVKEY_WORLDWIND_LAYER_PLUGIN, p);
                            p.setPluginName(e.getAttribute("name"));
                            Boolean b = Boolean.valueOf(e.getAttribute("active"));
                            l.setEnabled(b);
                            plugins.put("" + p.hashCode(), p);

                            installLayer(p, -1);

                        } else {
                            System.err.println("(E) WorldWindLayerPlugin could not be instantiated (" + fac.getFactoryName() + ")");

                        }

                    } else {
                        System.err.println("(E) No factory found for WorldWindPluginLayer " + e.getAttribute("factory"));

                    }

                } else if (nl.item(i).getNodeName().equals("Panel")) {
                    //--- Instanciate panels
                    Element e = (Element) nl.item(i);
                    TinyFactory fac = app.getFactory(e.getAttribute("factory"));
                    if (fac != null) {
                        TinyPlugin p = fac.newPlugin(null);
                        if (p != null) {
                            p.setup(app, DP_Main);
                            p.configure(e);

                            JComponent jcomp = p.getVisualComponent();
                            jcomp.putClientProperty("plugin", p);
                            JInternalFrame iframe = new JInternalFrame(p.getPluginName());
                            iframe.setClosable(true);
                            iframe.setMaximizable(true);
                            iframe.setResizable(true);
                            iframe.setIconifiable(true);
                            iframe.getContentPane().add(jcomp);
                            iframe.addInternalFrameListener(new InternalFrameAdapter() {
                                @Override
                                public void internalFrameClosing(InternalFrameEvent e) {
                                    JInternalFrame ifr = e.getInternalFrame();
                                    JComponent jcomp = (JComponent) ifr.getContentPane().getComponent(0);
                                    ifr.removeAll();

                                    TinyPlugin p = (TinyPlugin) jcomp.getClientProperty("plugin");
                                    if (p != null) p.cleanup();

                                    ifr.setVisible(false);
                                    ifr.dispose();
                                }
                            });
                            DP_Main.add(iframe, JLayeredPane.PALETTE_LAYER);
                            iframe.setBounds(100, 100, 320, 240);
                            try {
                                int x = Integer.parseInt(e.getAttribute("x"));
                                int y = Integer.parseInt(e.getAttribute("y"));
                                int w = Integer.parseInt(e.getAttribute("width"));
                                int h = Integer.parseInt(e.getAttribute("height"));
                                iframe.setBounds(x, y, w, h);

                            } catch (NumberFormatException ex) {
                                //---
                            }
                            iframe.setVisible(true);

                        } else {
                            System.err.println("(E) WorldWindLayerPlugin could not be instantiated (" + fac.getFactoryName() + ")");

                        }
                    }

                } else if (nl.item(i).getNodeName().equals("AttachedPlanet")) {
                    Element e = (Element) nl.item(i);

                    //--- Store the relations for later us
                    HashMap<String, ArrayList<String>> li = (HashMap<String, ArrayList<String>>) app.fetchData(STORAGE_DATA_JPLANETS_RELATIONS);
                    if (li == null) {
                        li = new HashMap<>();
                        app.storeData(STORAGE_DATA_JPLANETS_RELATIONS, li);
                    }
                    ArrayList<String> pl = li.get(signature);
                    if (pl == null) {
                        pl = new ArrayList<>();
                        li.put(signature, pl);
                    }
                    pl.add(e.getAttribute("signature"));
                }
            }

        }

        //--- Refresh list last
        layers.fireTableDataChanged();
        layers.addTableModelListener(this);

        //--- If layers are hidden, hide the split bar
        boolean hide = MN_HideLayers.isSelected();
        if (hide) {
            PN_Left.setVisible(false);
            SP_Main.setDividerSize(0);

        }
        SP_Main.revalidate();

        timer.start();
    }

    public void destroy() {
        timer.stop();
        vtimer.stop();
        fptimer.stop();

        JInternalFrame fr[] = DP_Main.getAllFrames();
        for (int i = 0; i < fr.length; i++) {
            JComponent jcomp = (JComponent) fr[i].getContentPane().getComponent(0);
            fr[0].removeAll();

            //--- If plugin, cleanup
            TinyPlugin p = (TinyPlugin) jcomp.getClientProperty("plugin");
            if (p != null) p.cleanup();

            fr[i].setVisible(false);
            fr[i].dispose();
        }

        m.getLayers().clear();
        InputHandler ih = wwd.getInputHandler();
        ih.removeKeyListener(this);
        ih.removeMouseListener(this);
        ih.removeMouseWheelListener(this);

        //--- Clear plugins
        Iterator<WWEPlugin> it = plugins.values().iterator();
        while (it.hasNext()) {
            WWEPlugin p = it.next();
            JComponent jcomp = p.getConfigComponent();
            if (jcomp != null) PN_LayersData.remove(jcomp);
            p.cleanup();

        }
        plugins.clear();

        listeners.clear();
        remove(wwd);

        wwd.shutdown();
        wwd.destroy();

    }

    /**
     * Store current view position and frame location
     *
     * @param config
     */
    public void save(Element config) {
        if (config == null) return;
        config.setAttribute("signature", signature);

        Element e = config.getOwnerDocument().createElement("Layers");
        e.setAttribute("mainDividerLocation", "" + SP_Main.getDividerLocation());
        e.setAttribute("dividerLocation", "" + SP_Layers.getDividerLocation());
        e.setAttribute("hideLayers", "" + MN_HideLayers.isSelected());
        config.appendChild(e);

        e = config.getOwnerDocument().createElement("Center");
        BasicOrbitView view = (BasicOrbitView) wwd.getView();
        Position eye = view.getCenterPosition();
        e.setAttribute("lon", "" + eye.longitude.degrees);
        e.setAttribute("lat", "" + eye.latitude.degrees);
        e.setAttribute("alt", "" + eye.elevation);
        e.setAttribute("heading", "" + view.getHeading().degrees);
        e.setAttribute("pitch", "" + view.getPitch().degrees);
        e.setAttribute("roll", "" + view.getRoll().degrees);
        e.setAttribute("zoom", "" + view.getZoom());
        config.appendChild(e);

        //--- Add the saved cameras
        e = config.getOwnerDocument().createElement("Cameras");
        for (int i = 0; i < cameras.size(); i++) {
            Camera c = cameras.get(i);
            Position center = c.getCenterPosition();

            Element e2 = config.getOwnerDocument().createElement("Camera");
            e2.setAttribute("title", c.getTitle());
            e2.setAttribute("lon", "" + center.longitude.degrees);
            e2.setAttribute("lat", "" + center.latitude.degrees);
            e2.setAttribute("alt", "" + center.elevation);
            e2.setAttribute("heading", "" + c.getHeading());
            e2.setAttribute("pitch", "" + c.getPitch());
            e2.setAttribute("zoom", "" + c.getZoom());

            try {
                //--- Dump the thumbnail as png
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                Image thumb = c.getThumbnail();
                //--- Convert to BufferedImage to be saved
                BufferedImage newImage = new BufferedImage(thumb.getWidth(null), thumb.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = newImage.createGraphics();
                g.drawImage(thumb, 0, 0, null);
                g.dispose();
                ImageIO.write(newImage, "png", bout);
                e2.appendChild(config.getOwnerDocument().createTextNode(Base64.getEncoder().encodeToString(bout.toByteArray())));

            } catch (IOException ex) {
                //---
            }
            e.appendChild(e2);
        }
        config.appendChild(e);

        //--- Get the layer configs
        LayerList ll = m.getLayers();
        for (int i = 0; i < ll.size(); i++) {
            Layer l = ll.get(i);
            WWEPlugin p = (WWEPlugin) l.getValue(WWEPlugin.AVKEY_WORLDWIND_LAYER_PLUGIN);
            if (p != null) {
                Element wwp = config.getOwnerDocument().createElement("WorldWindLayer");
                wwp.setAttribute("active", "" + l.isEnabled());
                wwp.setAttribute("factory", p.getPluginFactory().getClass().getName());
                wwp.setAttribute("name", p.getPluginName());
                p.saveConfig(wwp);
                config.appendChild(wwp);
            }
        }

        //--- Get the panel configs
        JInternalFrame ji[] = DP_Main.getAllFrames();
        for (int i = 0; i < ji.length; i++) {
            JInternalFrame j = ji[i];

            JComponent jcomp = (JComponent) j.getContentPane().getComponent(0);

            TinyPlugin p = (TinyPlugin) jcomp.getClientProperty("plugin");
            if (p == null) continue;

            e = config.getOwnerDocument().createElement("Panel");
            e.setAttribute("factory", p.getPluginFactory().getClass().getName());
            e.setAttribute("name", p.getPluginName());
            e.setAttribute("x", "" + j.getX());
            e.setAttribute("y", "" + j.getY());
            e.setAttribute("width", "" + j.getWidth());
            e.setAttribute("height", "" + j.getHeight());
            p.saveConfig(e);
            config.appendChild(e);
        }

        //--- Store the JPlanet relations
        for (JPlanet jp : attachedPlanets) {
            e = config.getOwnerDocument().createElement("AttachedPlanet");
            e.setAttribute("signature", jp.getSignature());
            config.appendChild(e);
        }
    }

    //**************************************************************************
    //*** ActionListener
    //**************************************************************************
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == timer) {
            if (center != null) {
                //--- First time the window is fully realized with correct size
                //--- Perform initial broadcast messages
                Iterator<WWEPlugin> it = plugins.values().iterator();
                while (it.hasNext()) {
                    WWEPlugin p = it.next();
                    p.doAction(TinyPlugin.DO_ACTION_NEWSIZE, DP_Main.getSize(), null);
                    p.doAction(p.getLayer().isEnabled() ? WWEPlugin.DO_ACTION_LAYER_ENABLED : WWEPlugin.DO_ACTION_LAYER_DISABLED, null, null);

                }

                /*
                //--- Position animator
                SmoothInterpolator inter = new SmoothInterpolator(5000);
                PropertyAccessor.PositionAccessor ac = new PropertyAccessor.PositionAccessor() {
                    @Override
                    public Position getPosition() {
                        return wwd.getView().getEyePosition();
                    }

                    @Override
                    public boolean setPosition(Position value) {
                        wwd.getView().setEyePosition(value);
                        return true;
                    }
                };
                PositionAnimator pos = new PositionAnimator(inter, wwd.getView().getCurrentEyePosition(), eye, ac);
                
                //--- Heading animator
                PropertyAccessor.AngleAccessor haa = new PropertyAccessor.AngleAccessor() {
                    @Override
                    public Angle getAngle() {
                        return wwd.getView().getHeading();
                    }

                    @Override
                    public boolean setAngle(Angle value) {
                        wwd.getView().setHeading(value);
                        return true;
                    }
                    
                };
                AngleAnimator ha = new AngleAnimator(inter, wwd.getView().getHeading(), Angle.fromDegrees(heading), haa);
                
                //--- Pitch animator
                PropertyAccessor.AngleAccessor paa = new PropertyAccessor.AngleAccessor() {
                    @Override
                    public Angle getAngle() {
                        return wwd.getView().getPitch();
                    }

                    @Override
                    public boolean setAngle(Angle value) {
                        wwd.getView().setPitch(value);
                        return true;
                    }
                    
                };
                AngleAnimator pa = new AngleAnimator(inter, wwd.getView().getPitch(), Angle.fromDegrees(pitch), paa);
                
                //--- Roll animatir
                PropertyAccessor.AngleAccessor raa = new PropertyAccessor.AngleAccessor() {
                    @Override
                    public Angle getAngle() {
                        return wwd.getView().getRoll();
                    }

                    @Override
                    public boolean setAngle(Angle value) {
                        wwd.getView().setRoll(value);
                        return true;
                    }
                    
                };
                AngleAnimator ra = new AngleAnimator(inter, wwd.getView().getRoll(), Angle.fromDegrees(roll), raa);
                
                // System.out.println("EYE:"+eye+" center:"+orientation);
                // CompoundAnimator cn = new CompoundAnimator(inter,pos);// ,ha,pa,ra);
                // wwd.getView().addAnimator(cn);
                // cn.start();
                 */
                BasicOrbitView view = (BasicOrbitView) wwd.getView();
                view.addPanToAnimator(center, Angle.fromDegrees(heading), Angle.fromDegrees(pitch), zoom);

                // wwd.getView().goTo(eye, eye.elevation);
                center = null;

            } else {

            }

            //--- Display eye position on the bottom of the window
            Position cam = wwd.getView().getEyePosition();
            if (cam != null) {
                TF_Altitude.setText("" + (int) cam.getElevation());
                TF_Latitude.setText("" + nf.format(cam.getLatitude().degrees));
                TF_Longitude.setText("" + nf.format(cam.getLongitude().degrees));
                PB_Downloading.setIndeterminate(WorldWind.getRetrievalService().hasActiveTasks());
            }
            // Position ce = wwd.getView().computePositionFromScreenPoint(wwd.getWidth()/2, wwd.getHeight()/2);
            // System.out.println("LAT:"+ce.getLongitude()+" => "+cam.getLongitude());
            wwd.redraw();

        } else if (e.getSource() == vtimer) {
            //--- Forward to plugin the motion stop
            Iterator<WWEPlugin> it = plugins.values().iterator();
            while (it.hasNext()) it.next().doAction(WWEPlugin.DO_ACTION_VIEWPORT_NEEDS_REFRESH, null, null);

        } else if (e.getSource() == fptimer) {
            View v = wwd.getView();
            Vec4 fw = v.getForwardVector();
            Vec4 eye = v.getEyePoint();
            Vec4 neye = eye;
            Angle heading = v.getHeading();
            if ((wsad & KEY_W) == KEY_W) {
                //--- Forward
                neye = eye.add3(fw.multiply3(5 * (shift ? 5 : 1)));

            }
            if ((wsad & KEY_S) == KEY_S) {
                //--- Backward
                neye = eye.subtract3(fw.multiply3(5 * (shift ? 5 : 1)));

            }
            if ((wsad & KEY_A) == KEY_A) {
                //--- Left
                heading = Angle.fromDegrees(heading.getDegrees() - (shift ? 10 : 1));

            }
            if ((wsad & KEY_D) == KEY_D) {
                //--- Right
                heading = Angle.fromDegrees(heading.getDegrees() + (shift ? 10 : 1));

            }
            v.setEyePosition(m.getGlobe().computePositionFromPoint(neye));
            v.setHeading(heading);
            wwd.redraw();

            //--- Forward each 10s the end of drag to broadcast to layers a new
            //--- viewport context
            iteration++;
            if ((iteration % 50) == 0) {
                Iterator<WWEPlugin> it = plugins.values().iterator();
                while (it.hasNext()) it.next().doAction(WWEPlugin.DO_ACTION_VIEWPORT_NEEDS_REFRESH, null, null);

            }

        } else if (e.getActionCommand().equals("newPlugin")) {
            JMenuItem ji = (JMenuItem) e.getSource();
            TinyFactory factory = (TinyFactory) ji.getClientProperty("factory");

            //--- If it's a new layer
            if (factory instanceof WWEFactory) {
                int index = TB_Layers.getSelectedRow();
                JComponent jcomp = (JComponent) e.getSource();
                String name = JOptionPane.showInputDialog(this, "Layer name", factory.getFactoryName());
                if (name != null) {
                    WWEPlugin p = (WWEPlugin) factory.newPlugin(wwd);
                    p.setup(app, DP_Main);
                    p.configure(null);
                    Layer l = p.getLayer();
                    l.setValue(AVKEY_WORLDWIND_LAYER_PLUGIN, p);
                    l.setName(name);
                    l.setEnabled(true);
                    plugins.put("" + p.hashCode(), p);

                    installLayer(p, index);

                    p.doAction(WWEPlugin.DO_ACTION_LAYER_ENABLED, null, null);
                }
                layers.fireTableDataChanged();

            } else {
                String name = JOptionPane.showInputDialog(this, "Name", factory.getFactoryName());
                if (name == null) return;

                TinyPlugin p = factory.newPlugin(null);
                p.setup(app, null);
                p.configure(null);
                p.setPluginName(name);

                JComponent jcomp = p.getVisualComponent();
                jcomp.putClientProperty("plugin", p);
                JInternalFrame iframe = new JInternalFrame(p.getPluginName());
                iframe.setClosable(true);
                iframe.setMaximizable(true);
                iframe.setResizable(true);
                iframe.setIconifiable(true);
                iframe.getContentPane().add(jcomp);
                iframe.addInternalFrameListener(new InternalFrameAdapter() {
                    @Override
                    public void internalFrameClosing(InternalFrameEvent e) {
                        JInternalFrame ifr = e.getInternalFrame();
                        JComponent jcomp = (JComponent) ifr.getContentPane().getComponent(0);
                        ifr.removeAll();

                        TinyPlugin p = (TinyPlugin) jcomp.getClientProperty("plugin");
                        if (p != null) p.cleanup();

                        ifr.setVisible(false);
                        ifr.dispose();
                    }
                });
                DP_Main.add(iframe, JLayeredPane.PALETTE_LAYER);
                iframe.setBounds(100, 100, 320, 240);
                iframe.setVisible(true);
            }

        } else if (e.getActionCommand().equals("hideStatusBar")) {
            PN_StatusBar.setVisible(!MN_HideStatusBar.isSelected());

        } else if (e.getActionCommand().equals("hideTopBar")) {
            PN_TopBar.setVisible(!MN_HideTopBar.isSelected());

        } else if (e.getActionCommand().equals("hideLayers")) {
            boolean hide = MN_HideLayers.isSelected();
            if (hide) {
                SP_Main.setLastDividerLocation(SP_Main.getDividerLocation());
                PN_Left.setVisible(false);
                SP_Main.setDividerSize(0);

            } else {
                PN_Left.setVisible(true);
                SP_Main.setDividerLocation(SP_Main.getLastDividerLocation());
                SP_Main.setDividerSize(10);

            }
            SP_Main.revalidate();

        } else if (e.getActionCommand().equals("switch")) {
            JComponent left = (JComponent) SP_Main.getLeftComponent();
            JComponent right = (JComponent) SP_Main.getRightComponent();
            int lo = SP_Main.getDividerLocation();
            SP_Main.remove(left);
            SP_Main.remove(right);
            SP_Main.setLeftComponent(right);
            SP_Main.setRightComponent(left);
            SP_Main.setDividerLocation(SP_Main.getWidth() - lo);

        } else if (e.getActionCommand().equals("renameLayer")) {
            int index = TB_Layers.getSelectedRow();
            if (index != -1) {
                Layer l = (Layer) TB_Layers.getValueAt(index, 1);
                String newname = JOptionPane.showInputDialog("Layer name", l.getName());
                if (newname != null) {
                    WWEPlugin p = (WWEPlugin) l.getValue(WWEPlugin.AVKEY_WORLDWIND_LAYER_PLUGIN);
                    if (p != null) {
                        p.setPluginName(newname);

                    } else {
                        l.setName(newname);
                    }
                }

            }
            TB_Layers.repaint();

        } else if (e.getActionCommand().equals("removeLayer")) {
            int index = TB_Layers.getSelectedRow();
            if (index != -1) {
                Layer l = (Layer) TB_Layers.getValueAt(index, 1);
                LayerList ll = m.getLayers();
                ll.remove(l);

                WWEPlugin p = (WWEPlugin) l.getValue(WWEPlugin.AVKEY_WORLDWIND_LAYER_PLUGIN);
                if (p != null) {
                    //--- Remove the config panel
                    JComponent jcomp = p.getConfigComponent();
                    if (jcomp != null) PN_LayersData.remove(jcomp);

                    //--- Remove the main visual on the layered pane
                    jcomp = p.getVisualComponent();
                    if (jcomp != null) DP_Main.remove(jcomp);

                    //--- Remove the button
                    if (p.hasLayerButton()) {
                        for (int i = 0; i < PN_LayersButtons.getComponentCount(); i++) {
                            JToggleButton jbutton = (JToggleButton) PN_LayersButtons.getComponent(i);
                            TinyPlugin tmp = (TinyPlugin) jbutton.getClientProperty("plugin");
                            if (tmp == p) {
                                jbutton.removeActionListener(this);
                                btgblayers.remove(jbutton);
                                PN_LayersButtons.remove(jbutton);
                                break;
                            }
                        }
                    }
                    PN_LayersButtons.revalidate();

                    plugins.remove(p);
                    p.cleanup();

                }

            }
            layers.fireTableDataChanged();

        } else if (e.getActionCommand().equals("activeLayer")) {
            WWEPlugin p = (WWEPlugin) ((JToggleButton) e.getSource()).getClientProperty("plugin");

            for (int i = 0; i < TB_Layers.getRowCount(); i++) {
                Layer tmp = (Layer) TB_Layers.getValueAt(i, 1);
                if (tmp == p.getLayer()) {
                    TB_Layers.getSelectionModel().removeSelectionInterval(i, i);
                    TB_Layers.getSelectionModel().addSelectionInterval(i, i);
                    break;
                }
            }

        } else if (e.getActionCommand().equals("more")) {
            PU_More.show(BT_More, 10, 10);

        } else if (e.getActionCommand().equals("screenIdentifier")) {
            App.showScreenIdentifiers();

        } else if (e.getActionCommand().equals("fullscreen")) {
            if (MN_Fullscreen.isSelected()) {
                //--- Try to find the selected graphics device
                Enumeration<AbstractButton> buttons = btgscreens.getElements();
                while (buttons.hasMoreElements()) {
                    JRadioButtonMenuItem jitem = (JRadioButtonMenuItem) buttons.nextElement();
                    if (jitem.isSelected()) {
                        remove(SP_Main);
                        revalidate();
                        repaint();

                        GraphicsDevice gd = (GraphicsDevice) jitem.getClientProperty("gd");
                        if (gd == null) {
                            //--- No graphic device defined, try to do a full window on all screen
                            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
                            jframe = new JFrame("");
                            jframe.add(SP_Main);
                            jframe.setSize(dim);
                            jframe.setUndecorated(true);
                            jframe.setVisible(true);
                            // GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                            // gd = ge.getDefaultScreenDevice();
                            // gd.setFullScreenWindow(jframe);

                        } else {
                            DisplayMode mode = gd.getDisplayMode();
                            jframe = new JFrame("", gd.getDefaultConfiguration());
                            jframe.add(SP_Main);
                            jframe.setSize(mode.getWidth(), mode.getHeight());
                            jframe.setUndecorated(true);
                            jframe.setVisible(true);
                            gd.setFullScreenWindow(jframe);
                        }
                        break;
                    }
                }

            } else {
                jframe.setVisible(false);
                jframe.remove(SP_Main);
                jframe.dispose();
                jframe = null;

                add(SP_Main, BorderLayout.CENTER);
                revalidate();
                repaint();
            }

        } else if (e.getActionCommand().equals("scrollRight") || e.getActionCommand().equals("scrollLeft")) {
            Rectangle rect = PN_LayersButtons.getVisibleRect();
            rect.x = rect.x + (e.getActionCommand().equals("scrollRight") ? 22 : -22);
            rect.y = rect.y;
            PN_LayersButtons.scrollRectToVisible(rect);

        } else if (e.getActionCommand().equals("addLayer")) {
            MN_NewLayers.setEnabled(true);
            PU_Layers.show(BT_AddLayer, BT_AddLayer.getX(), BT_AddLayer.getY());

        } else if (e.getActionCommand().equals("upLayer")) {
            int index = TB_Layers.getSelectedRow();
            if (index == -1) return;

            LayerList ll = m.getLayers();
            if (index > 0) {
                Layer l = (Layer) TB_Layers.getValueAt(index, 1);
                ll.moveLower(l);
                layers.fireTableDataChanged();
                TB_Layers.getSelectionModel().setSelectionInterval(index - 1, index - 1);
                TB_Layers.scrollRectToVisible(new Rectangle(0, index - 1 * TB_Layers.getRowHeight(), TB_Layers.getWidth(), TB_Layers.getRowHeight()));
            }

        } else if (e.getActionCommand().equals("downLayer")) {
            //--- Move down
            int index = TB_Layers.getSelectedRow();
            if (index == -1) return;

            LayerList ll = m.getLayers();
            if (index < TB_Layers.getRowCount() - 1) {
                Layer l = (Layer) TB_Layers.getValueAt(index, 1);
                ll.moveHigher(l);
                layers.fireTableDataChanged();
                TB_Layers.getSelectionModel().setSelectionInterval(index + 1, index + 1);
                TB_Layers.scrollRectToVisible(new Rectangle(0, index + 1 * TB_Layers.getRowHeight(), TB_Layers.getWidth(), TB_Layers.getRowHeight()));
            }

        } else if (e.getActionCommand().equals("cameras")) {
            PN_Cameras.setVisible(BT_Cameras.isSelected());

        } else if (e.getActionCommand().equals("newCamera")) {
            String title = JOptionPane.showInputDialog(PN_Cameras, "Camera title");
            int index = LI_Cameras.getSelectedIndex();
            if (title != null) {
                Camera c = new Camera(title, wwd);
                cameras.add(index == -1 ? 0 : index, c);
            }

        } else if (e.getActionCommand().equals("removeCamera")) {
            int index = LI_Cameras.getSelectedIndex();
            if (index > -1) cameras.remove(index);

        } else if (e.getActionCommand().equals("updateCamera")) {
            int index = LI_Cameras.getSelectedIndex();
            if (index > -1) {
                Camera c = LI_Cameras.getSelectedValue();
                String title = JOptionPane.showInputDialog(PN_Cameras, "Update title and position", c.getTitle());
                if (title != null) c.set(title, wwd);
            }

        } else if (e.getActionCommand().equals("cameraUp")) {
            int index = LI_Cameras.getSelectedIndex();
            if (index > 0) {
                Camera source = LI_Cameras.getSelectedValue();
                Camera det = cameras.get(index - 1);
                cameras.set(index - 1, source);
                cameras.set(index, det);
                LI_Cameras.setSelectedIndex(index - 1);
                LI_Cameras.ensureIndexIsVisible(index - 1);
                LI_Cameras.repaint();
            }

        } else if (e.getActionCommand().equals("cameraDown")) {
            int index = LI_Cameras.getSelectedIndex();
            if ((index > 0) && (index < cameras.size() - 1)) {
                Camera source = LI_Cameras.getSelectedValue();
                Camera det = cameras.get(index + 1);
                cameras.set(index + 1, source);
                cameras.set(index, det);
                LI_Cameras.setSelectedIndex(index + 1);
                LI_Cameras.ensureIndexIsVisible(index + 1);
                LI_Cameras.repaint();
            }

        } else if (e.getActionCommand().equals("smallCamera")) {
            LI_Cameras.setCellRenderer(BT_SmallCamera.isSelected() ? cameraSmallCellRenderer : cameraCellRenderer);
            LI_Cameras.repaint();

        } else if (e.getActionCommand().equals("configure")) {
            JComponent jcomp = (JComponent) e.getSource();
            TinyPlugin p = (TinyPlugin) jcomp.getClientProperty("plugin");
            if (p != null) {
                TinyFactory f = p.getPluginFactory();
                app.fireActionPerformed(new AppActionEvent(this, AppActionEvent.ACTION_ID_TINYFACTORY_SETTINGS, null, f));

            }

        } else if (e.getActionCommand().equals("wireframe")) {
            wwd.getSceneController().getModel().setShowWireframeExterior(CB_Wireframe.isSelected());

        } else if (e.getActionCommand().equals("fly")) {
            wsad = 0;
            fptimer.restart();
            PN_Cross.setVisible(true);
            LB_FPKeys.setVisible(true);

            //--- Switch to fly view
            Position eye = wwd.getView().getEyePosition();
            wwd.setView(fly);
            fly.setEyePosition(eye);
            fly.setHeading(Angle.ZERO);
            fly.setPitch(Angle.POS90);
            wwd.redraw();

            wwd.requestFocusInWindow();

        } else if (e.getActionCommand().equals("walk")) {
            wsad = 0;
            fptimer.restart();
            PN_Cross.setVisible(true);
            LB_FPKeys.setVisible(true);

            //--- Switch to walk view
            Position eye = wwd.getView().getCurrentEyePosition();
            wwd.setView(walk);
            walk.setEyePosition(eye);
            walk.setHeading(Angle.ZERO);
            walk.setPitch(Angle.POS90);
            wwd.redrawNow();

            wwd.requestFocusInWindow();

        } else if (e.getActionCommand().equals("orbit")) {
            fptimer.stop();
            PN_Cross.setVisible(false);
            LB_FPKeys.setVisible(false);

            Position eye = wwd.getView().getEyePosition();
            wwd.setView(orbit);
            orbit.setEyePosition(eye.add(Position.fromDegrees(0, 0, 1000)));
            orbit.setPitch(Angle.fromDMdS(45, 0));
            wwd.redraw();

        } else if (e.getActionCommand().equals("clearAttached")) {
            for (JPlanet jp : attachedPlanets) jp.setParentPlanet(null);
            attachedPlanets.clear();

        } else if (e.getActionCommand().equals("goto")) {
            String s = JOptionPane.showInputDialog("Latitude");
            try {
                Double lat = Double.parseDouble(s);
                s = JOptionPane.showInputDialog("Longitude");
                Double lon = Double.parseDouble(s);

                Position c = Position.fromDegrees(lat, lon);
                View v = wwd.getView();
                if (v instanceof BasicOrbitView) {
                    BasicOrbitView view = (BasicOrbitView) v;
                    view.addPanToAnimator(c, view.getHeading(), view.getPitch(), view.getZoom());

                    for (JPlanet jp : attachedPlanets) {
                        View tv = jp.getWorldWindowGLJPanel().getView();
                        if (tv instanceof BasicOrbitView) {
                            BasicOrbitView tview = (BasicOrbitView) tv;
                            tview.addPanToAnimator(c, view.getHeading(), view.getPitch(), view.getZoom());

                        } else {
                            tv.goTo(c, 1000);
                        }

                    }

                } else {
                    v.goTo(c, 1000);
                }

            } catch (Exception ex) {

            }

        }

    }

    //**************************************************************************
    //*** KeyListener
    //**************************************************************************
    @Override
    public void keyTyped(KeyEvent e) {
        //---
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // System.out.println("e:" + e.getKeyCode());
        //---
        if (e.getKeyCode() == KeyEvent.VK_F) {
            boolean selected = MN_Fullscreen.isSelected();
            MN_Fullscreen.setSelected(!selected);
            ActionEvent ae = new ActionEvent(MN_Fullscreen, ActionEvent.ACTION_PERFORMED, MN_Fullscreen.getActionCommand());
            actionPerformed(ae);
            e.consume();

        } else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            boolean sticky = MN_HideLayers.isSelected();
            MN_HideLayers.setSelected(!sticky);

            actionPerformed(new ActionEvent(MN_HideLayers, ActionEvent.ACTION_PERFORMED, MN_HideLayers.getActionCommand()));
            e.consume();

        }

        //--- Store the first person key states
        shift = ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0);
        if (e.getKeyCode() == KeyEvent.VK_W) {
            // System.out.println("FORWARD");
            wsad = wsad | KEY_W;

        } else if (e.getKeyCode() == KeyEvent.VK_S) {
            wsad = wsad | KEY_S;

        } else if (e.getKeyCode() == KeyEvent.VK_A) {
            wsad = wsad | KEY_A;

        } else if (e.getKeyCode() == KeyEvent.VK_D) {
            wsad = wsad | KEY_D;

        } else if (e.getKeyCode() == KeyEvent.VK_Q) {
            View v = wwd.getView();
            v.setRoll(Angle.fromDegrees(v.getRoll().getDegrees() - 1));
            e.consume();
            wwd.redraw();

        } else if (e.getKeyCode() == KeyEvent.VK_E) {
            View v = wwd.getView();
            v.setRoll(Angle.fromDegrees(v.getRoll().getDegrees() + 1));
            e.consume();
            wwd.redraw();

        }

        // System.out.println("WSAD:" + wsad);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        shift = ((e.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) != 0);
        if (e.getKeyCode() == KeyEvent.VK_W) {
            // System.out.println("FORWARD");
            wsad = wsad & ~KEY_W;

        } else if (e.getKeyCode() == KeyEvent.VK_S) {
            wsad = wsad & ~KEY_S;

        } else if (e.getKeyCode() == KeyEvent.VK_A) {
            wsad = wsad & ~KEY_A;

        } else if (e.getKeyCode() == KeyEvent.VK_D) {
            wsad = wsad & ~KEY_D;

        }
        // Syst.out.println("WSAD:" + wsad);
    }

    //**************************************************************************
    //*** ComponentListener
    //**************************************************************************
    @Override
    public void componentResized(ComponentEvent e) {
        //--- Resize the world wind panel
        wwd.setBounds(0, 0, DP_Main.getWidth(), DP_Main.getHeight());

        //--- Resize the main visual component (at DEFAULT_LAYER)
        Component comps[] = DP_Main.getComponentsInLayer(JLayeredPane.DEFAULT_LAYER);
        for (int i = 0; i < comps.length; i++) comps[i].setBounds(0, 0, DP_Main.getWidth(), DP_Main.getHeight());

        // LB_Licence.setBounds(0,DP_Main.getHeight()-LB_Licence.getHeight(), DP_Main.getWidth(), LB_Licence.getHeight());
        //--- Replace the cameras panel
        PN_Cameras.setBounds(DP_Main.getWidth() - 320, 0, 320, DP_Main.getHeight());

        //--- Replace cross
        PN_Cross.setBounds((DP_Main.getWidth() / 2) - 50, (DP_Main.getHeight() / 2) - 50, 100, 100);
        LB_FPKeys.setBounds((DP_Main.getWidth() / 2) - 150, 10, 300, 26);

        //--- Forward to plugin the layer resize
        Iterator<WWEPlugin> it = plugins.values().iterator();
        while (it.hasNext()) it.next().doAction(WWEPlugin.DO_ACTION_NEWSIZE, DP_Main.getSize(), null);
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        //---
    }

    @Override
    public void componentShown(ComponentEvent e) {
        //---
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        //---
    }

    //**************************************************************************
    //*** ListSelectionListener
    //*************************************************************************
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == TB_Layers.getSelectionModel()) {
            if (e.getValueIsAdjusting() == true) return;

            //--- A new layer was selected
            int index = TB_Layers.getSelectedRow();
            if (index != -1) {
                CardLayout layout = (CardLayout) PN_LayersData.getLayout();
                Layer l = (Layer) TB_Layers.getValueAt(index, 1);
                LB_Layer.setText(l.getName());

                //--- Display config
                layout.show(PN_LayersData, "empty");

                //--- Choose button if present
                btgblayers.clearSelection();
                WWEPlugin p = (WWEPlugin) l.getValue(WWEPlugin.AVKEY_WORLDWIND_LAYER_PLUGIN);
                if (p != null) {
                    //--- Disactive the old one
                    if (selected != null) selected.doAction(WWEPlugin.DO_ACTION_LAYER_UNSELECTED, null, null);

                    BT_Configure.setVisible(p.getPluginFactory().getFactoryConfigComponent() != null);
                    BT_Configure.putClientProperty("plugin", p);

                    LB_LayerIcon.setIcon(p.getPluginFactory().getFactoryIcon(TinyFactory.ICON_SIZE_NORMAL));

                    //--- Select the layer button if present
                    if (p.hasLayerButton()) {
                        for (int i = 0; i < PN_LayersButtons.getComponentCount(); i++) {
                            JToggleButton jb = (JToggleButton) PN_LayersButtons.getComponent(i);
                            TinyPlugin tmp = (TinyPlugin) jb.getClientProperty("plugin");
                            if (tmp == p) {
                                jb.setSelected(true);
                                break;
                            }
                        }
                    }

                    //--- Find licence
                    String licence = (String) p.getPluginFactory().getProperty(TinyFactory.PROPERTY_COPYRIGHT_TEXT);
                    if (licence != null) {
                        LB_Licence.setText(licence);
                        LB_Licence.setIcon(p.getPluginFactory().getFactoryIcon(TinyFactory.ICON_SIZE_NORMAL));
                    }

                    layout.show(PN_LayersData, "" + p.hashCode());

                    //--- Select the current plugin
                    selected = p;
                    selected.doAction(WWEPlugin.DO_ACTION_LAYER_SELECTED, null, null);

                } else {
                    //--- Worldwind layer is not managed by WWE
                    BT_Configure.setVisible(false);
                    LB_LayerIcon.setIcon(null);
                }

            }

        }
    }

    //**************************************************************************
    //*** ChangeListener
    //**************************************************************************
    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == SP_VerticalExageration) {
            int val = (Integer) SP_VerticalExageration.getValue();
            wwd.getSceneController().setVerticalExaggeration((double) val / 10d);
            wwd.redraw();
        }
    }

    //**************************************************************************
    //*** MouseListener
    //**************************************************************************
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getSource() == LI_Cameras) {
            if (e.getClickCount() >= 2) {
                Camera c = LI_Cameras.getSelectedValue();
                if (c != null) {
                    View v = wwd.getView();
                    if (v instanceof BasicOrbitView) {
                        BasicOrbitView view = (BasicOrbitView) v;
                        view.addPanToAnimator(c.getCenterPosition(), Angle.fromDegrees(c.getHeading()), Angle.fromDegrees(c.getPitch()), c.getZoom());

                        for (JPlanet jp : attachedPlanets) {
                            View tv = jp.getWorldWindowGLJPanel().getView();
                            if (tv instanceof BasicOrbitView) {
                                BasicOrbitView tview = (BasicOrbitView) tv;
                                tview.addPanToAnimator(c.getCenterPosition(), Angle.fromDegrees(c.getHeading()), Angle.fromDegrees(c.getPitch()), c.getZoom());

                            } else {
                                tv.goTo(c.getCenterPosition(), 1000);
                            }

                        }

                    } else {
                        v.goTo(c.getCenterPosition(), 1000);
                    }
                }

            }

        } else if (e.getSource() == BT_Attach) {
            if (attachedPlanets.size() > 0) {
                PU_Attached.show(BT_Attach, 10, 10);
            }

        } else if (e.getSource() == wwd) {
            //--- Forward to selected layed
            if (selected != null) selected.layerMouseClicked(e, wwd.getCurrentPosition());
        }

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getSource() == wwd) {
            if (e.getButton() == MouseEvent.BUTTON2) {
                PU_More.show(DP_Main, e.getX(), e.getY());
            }

        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getSource() == wwd) {
            vtimer.restart();

            if (e.getButton() == MouseEvent.BUTTON2) {
                PU_More.show(DP_Main, e.getX(), e.getY());
            }

        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        DP_Main.requestFocusInWindow();

    }

    @Override
    public void mouseExited(MouseEvent e) {
        //---
    }

    //**************************************************************************
    //*** MouseWheel
    //**************************************************************************
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        View v = wwd.getView();
        for (JPlanet jp : attachedPlanets) {
            try {
                View tmp = jp.getWorldWindowGLJPanel().getView();
                // tmp.setEyePosition(v.getEyePosition());
                tmp.setHeading(v.getHeading());
                tmp.setPitch(v.getPitch());
                tmp.setRoll(v.getRoll());
                tmp.setOrientation(v.getEyePosition(), v.computePositionFromScreenPoint(wwd.getWidth() / 2, wwd.getHeight() / 2));

                jp.getWorldWindowGLJPanel().redrawNow();

            } catch (IllegalArgumentException ex) {

            }
        }
        vtimer.restart();
    }

    //**************************************************************************
    //*** MouseMotionListener
    //**************************************************************************
    @Override
    public void mouseDragged(MouseEvent e) {
        if (e.getSource() == BT_Attach) {
            cth.exportAsDrag((JButton) e.getSource(), e, TransferHandler.LINK);
        }
        View v = wwd.getView();
        for (JPlanet jp : attachedPlanets) {

            try {
                View tmp = jp.getWorldWindowGLJPanel().getView();
                // tmp.setEyePosition(v.getEyePosition());
                tmp.setHeading(v.getHeading());
                tmp.setPitch(v.getPitch());
                tmp.setRoll(v.getRoll());
                tmp.setOrientation(v.getEyePosition(), v.computePositionFromScreenPoint(wwd.getWidth() / 2, wwd.getHeight() / 2));
                jp.getWorldWindowGLJPanel().redrawNow();

            } catch (IllegalArgumentException ex) {

            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        //---
    }

    //**************************************************************************
    //*** TableModelListener
    //**************************************************************************
    @Override
    public void tableChanged(TableModelEvent e) {
        if (e.getSource() == layers) {
            if (e.getType() == TableModelEvent.UPDATE) {
                if (e.getColumn() == 0) {
                    //--- Enable, disable
                    int index = e.getFirstRow();
                    Layer l = (Layer) layers.getValueAt(index, 1);
                    WWEPlugin p = (WWEPlugin) l.getValue(WWEPlugin.AVKEY_WORLDWIND_LAYER_PLUGIN);
                    if (p != null) p.doAction(l.isEnabled() ? WWEPlugin.DO_ACTION_LAYER_ENABLED : WWEPlugin.DO_ACTION_LAYER_DISABLED, null, null);

                }
            }

        }
    }

    //*************************************************************************
    //*** Renderable
    //*************************************************************************
    //--- NOT YET USED
    public void render(DrawContext dc) {
        //---

    }

    //*************************************************************************
    //*** RenderingListener
    //*************************************************************************
    @Override
    public void stageChanged(RenderingEvent event) {
        //---
        // System.out.println("STAGE:"+event.getStage());

    }

    //**************************************************************************
    //*** Transferable
    //**************************************************************************
    @Override
    public DataFlavor[] getTransferDataFlavors() {
        DataFlavor df[] = new DataFlavor[]{DATAFLAVOR_JPLANET};
        return df;
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        if (flavor.equals(DATAFLAVOR_JPLANET)) return true;
        return false;
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (flavor.equals(DATAFLAVOR_JPLANET)) {
            return this;
        }
        return null;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        btgblayers = new javax.swing.ButtonGroup();
        PU_Layers = new javax.swing.JPopupMenu();
        MN_NewLayers = new javax.swing.JMenu();
        jSeparator7 = new javax.swing.JSeparator();
        MN_Rename = new javax.swing.JMenuItem();
        MN_RemoveLayer = new javax.swing.JMenuItem();
        PU_More = new javax.swing.JPopupMenu();
        jSeparator10 = new javax.swing.JPopupMenu.Separator();
        MN_HideTopBar = new javax.swing.JCheckBoxMenuItem();
        MN_HideStatusBar = new javax.swing.JCheckBoxMenuItem();
        MN_HideLayers = new javax.swing.JCheckBoxMenuItem();
        MN_SwitchSide = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JPopupMenu.Separator();
        MN_Screens = new javax.swing.JMenu();
        MN_Fullscreen = new javax.swing.JCheckBoxMenuItem();
        MN_ScreenIdentifier = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        jSeparator12 = new javax.swing.JPopupMenu.Separator();
        MN_GoTo = new javax.swing.JMenuItem();
        btgscreens = new javax.swing.ButtonGroup();
        btgmodel = new javax.swing.ButtonGroup();
        btgviews = new javax.swing.ButtonGroup();
        PU_Attached = new javax.swing.JPopupMenu();
        MN_ClearAttached = new javax.swing.JMenuItem();
        SP_Main = new javax.swing.JSplitPane();
        PN_Left = new javax.swing.JPanel();
        SP_Layers = new javax.swing.JSplitPane();
        PN_LayersFrame = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        TB_Tools = new javax.swing.JToolBar();
        BT_AddLayer = new javax.swing.JButton();
        BT_LayerUp = new javax.swing.JButton();
        BT_LayerDown = new javax.swing.JButton();
        BT_RenameLayer = new javax.swing.JButton();
        jSeparator9 = new javax.swing.JToolBar.Separator();
        BT_RemoveLayer = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        TB_Layers = new javax.swing.JTable();
        PN_LayersCenter = new javax.swing.JPanel();
        PN_LayersData = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        LB_LayerIcon = new javax.swing.JLabel();
        LB_Layer = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        BT_Configure = new javax.swing.JButton();
        PN_Right = new javax.swing.JPanel();
        DP_Main = new javax.swing.JDesktopPane();
        PN_Cameras = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        LI_Cameras = new javax.swing.JList<>();
        TB_CameraTools = new javax.swing.JToolBar();
        BT_NewCamera = new javax.swing.JButton();
        BT_CameraUp = new javax.swing.JButton();
        BT_CameraDown = new javax.swing.JButton();
        BT_UpdateCamera = new javax.swing.JButton();
        BT_SmallCamera = new javax.swing.JToggleButton();
        jSeparator13 = new javax.swing.JToolBar.Separator();
        BT_RemoveCamera = new javax.swing.JButton();
        LB_FPKeys = new javax.swing.JLabel();
        PN_TopBar = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        SP_LayersButtons = new javax.swing.JScrollPane();
        PN_LayersButtons = new javax.swing.JPanel();
        BT_ScrollLeft = new javax.swing.JButton();
        BT_ScrollRight = new javax.swing.JButton();
        PN_Tray = new javax.swing.JPanel();
        BT_Orbit = new javax.swing.JToggleButton();
        BT_Fly = new javax.swing.JToggleButton();
        BT_Walk = new javax.swing.JToggleButton();
        jSeparator2 = new javax.swing.JSeparator();
        BT_Cameras = new javax.swing.JToggleButton();
        jPanel7 = new javax.swing.JPanel();
        BT_More = new javax.swing.JButton();
        BT_Attach = new javax.swing.JButton();
        PN_StatusBar = new javax.swing.JPanel();
        PN_Status = new javax.swing.JPanel();
        PB_Downloading = new javax.swing.JProgressBar();
        jlabel1 = new javax.swing.JLabel();
        TF_Altitude = new javax.swing.JTextField();
        jSeparator1 = new javax.swing.JSeparator();
        jlabel2 = new javax.swing.JLabel();
        TF_Latitude = new javax.swing.JTextField();
        TF_Longitude = new javax.swing.JTextField();
        LB_Licence = new javax.swing.JLabel();
        jPanel5 = new javax.swing.JPanel();
        SP_VerticalExageration = new javax.swing.JSpinner();
        CB_Wireframe = new javax.swing.JCheckBox();

        MN_NewLayers.setText("New layer");
        PU_Layers.add(MN_NewLayers);
        PU_Layers.add(jSeparator7);

        MN_Rename.setText("Rename");
        MN_Rename.setActionCommand("renameLayer");
        PU_Layers.add(MN_Rename);

        MN_RemoveLayer.setText("Remove");
        MN_RemoveLayer.setActionCommand("removeLayer");
        PU_Layers.add(MN_RemoveLayer);

        PU_More.add(jSeparator10);

        MN_HideTopBar.setText("Hide top bar");
        MN_HideTopBar.setActionCommand("hideTopBar");
        PU_More.add(MN_HideTopBar);

        MN_HideStatusBar.setText("Hide status bar");
        MN_HideStatusBar.setActionCommand("hideStatusBar");
        PU_More.add(MN_HideStatusBar);

        MN_HideLayers.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE, 0));
        MN_HideLayers.setText("Hide layers panel");
        MN_HideLayers.setActionCommand("hideLayers");
        PU_More.add(MN_HideLayers);

        MN_SwitchSide.setText("Switch layer and world side");
        MN_SwitchSide.setActionCommand("switch");
        PU_More.add(MN_SwitchSide);
        PU_More.add(jSeparator11);

        MN_Screens.setText("Screens");

        MN_Fullscreen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, 0));
        MN_Fullscreen.setText("Fullscreen");
        MN_Fullscreen.setActionCommand("fullscreen");
        MN_Screens.add(MN_Fullscreen);

        MN_ScreenIdentifier.setText("Screen identifier");
        MN_ScreenIdentifier.setActionCommand("screenIdentifier");
        MN_Screens.add(MN_ScreenIdentifier);
        MN_Screens.add(jSeparator6);

        PU_More.add(MN_Screens);
        PU_More.add(jSeparator12);

        MN_GoTo.setText("Go to coordinates");
        MN_GoTo.setActionCommand("goto");
        PU_More.add(MN_GoTo);

        MN_ClearAttached.setText("Clear attached views");
        MN_ClearAttached.setActionCommand("clearAttached");
        PU_Attached.add(MN_ClearAttached);

        setLayout(new java.awt.BorderLayout());

        SP_Main.setDividerLocation(300);

        PN_Left.setLayout(new java.awt.BorderLayout());

        SP_Layers.setDividerLocation(200);
        SP_Layers.setDividerSize(5);
        SP_Layers.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        SP_Layers.setOneTouchExpandable(true);

        PN_LayersFrame.setMinimumSize(new java.awt.Dimension(135, 0));
        PN_LayersFrame.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new java.awt.BorderLayout());

        TB_Tools.setBorder(null);
        TB_Tools.setFloatable(false);

        BT_AddLayer.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_AddLayer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/add.png"))); // NOI18N
        BT_AddLayer.setToolTipText("new layer");
        BT_AddLayer.setActionCommand("addLayer");
        BT_AddLayer.setPreferredSize(new java.awt.Dimension(32, 32));
        TB_Tools.add(BT_AddLayer);

        BT_LayerUp.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_LayerUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/up.png"))); // NOI18N
        BT_LayerUp.setToolTipText("move layer up");
        BT_LayerUp.setActionCommand("upLayer");
        BT_LayerUp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_LayerUp.setPreferredSize(new java.awt.Dimension(32, 32));
        TB_Tools.add(BT_LayerUp);

        BT_LayerDown.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_LayerDown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/down.png"))); // NOI18N
        BT_LayerDown.setToolTipText("move layer down");
        BT_LayerDown.setActionCommand("downLayer");
        BT_LayerDown.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_LayerDown.setPreferredSize(new java.awt.Dimension(32, 32));
        TB_Tools.add(BT_LayerDown);

        BT_RenameLayer.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_RenameLayer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/edit.png"))); // NOI18N
        BT_RenameLayer.setToolTipText("rename layer");
        BT_RenameLayer.setActionCommand("renameLayer");
        BT_RenameLayer.setPreferredSize(new java.awt.Dimension(32, 32));
        TB_Tools.add(BT_RenameLayer);
        TB_Tools.add(jSeparator9);

        BT_RemoveLayer.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_RemoveLayer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/remove.png"))); // NOI18N
        BT_RemoveLayer.setToolTipText("delete layer");
        BT_RemoveLayer.setActionCommand("removeLayer");
        BT_RemoveLayer.setPreferredSize(new java.awt.Dimension(32, 32));
        TB_Tools.add(BT_RemoveLayer);

        jPanel4.add(TB_Tools, java.awt.BorderLayout.PAGE_START);

        PN_LayersFrame.add(jPanel4, java.awt.BorderLayout.NORTH);

        TB_Layers.setRowHeight(26);
        jScrollPane2.setViewportView(TB_Layers);

        PN_LayersFrame.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        SP_Layers.setLeftComponent(PN_LayersFrame);

        PN_LayersCenter.setMinimumSize(new java.awt.Dimension(19, 0));
        PN_LayersCenter.setLayout(new java.awt.BorderLayout());

        PN_LayersData.setMinimumSize(new java.awt.Dimension(10, 0));
        PN_LayersData.setLayout(new java.awt.CardLayout());
        PN_LayersData.add(jPanel3, "empty");

        PN_LayersCenter.add(PN_LayersData, java.awt.BorderLayout.CENTER);

        jPanel8.setLayout(new java.awt.BorderLayout());

        jPanel11.setMaximumSize(new java.awt.Dimension(32767, 24));
        jPanel11.setOpaque(false);
        jPanel11.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        LB_LayerIcon.setFont(new java.awt.Font("Arial", 1, 11)); // NOI18N
        LB_LayerIcon.setForeground(new java.awt.Color(52, 73, 93));
        LB_LayerIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LB_LayerIcon.setPreferredSize(new java.awt.Dimension(32, 32));
        jPanel11.add(LB_LayerIcon);

        LB_Layer.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LB_Layer.setText("...");
        jPanel11.add(LB_Layer);

        jPanel8.add(jPanel11, java.awt.BorderLayout.CENTER);

        jPanel9.setOpaque(false);

        BT_Configure.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Configure.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/configure.png"))); // NOI18N
        BT_Configure.setToolTipText("Configure");
        BT_Configure.setActionCommand("configure");
        BT_Configure.setPreferredSize(new java.awt.Dimension(32, 32));
        jPanel9.add(BT_Configure);

        jPanel8.add(jPanel9, java.awt.BorderLayout.EAST);

        PN_LayersCenter.add(jPanel8, java.awt.BorderLayout.NORTH);

        SP_Layers.setRightComponent(PN_LayersCenter);

        PN_Left.add(SP_Layers, java.awt.BorderLayout.CENTER);

        SP_Main.setLeftComponent(PN_Left);

        PN_Right.setLayout(new java.awt.BorderLayout());

        PN_Cameras.setLayout(new java.awt.BorderLayout());

        LI_Cameras.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(LI_Cameras);

        PN_Cameras.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        TB_CameraTools.setBorder(null);
        TB_CameraTools.setFloatable(false);

        BT_NewCamera.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_NewCamera.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/add.png"))); // NOI18N
        BT_NewCamera.setToolTipText("New camera");
        BT_NewCamera.setActionCommand("newCamera");
        BT_NewCamera.setBorderPainted(false);
        BT_NewCamera.setFocusPainted(false);
        BT_NewCamera.setFocusable(false);
        BT_NewCamera.setPreferredSize(new java.awt.Dimension(32, 32));
        TB_CameraTools.add(BT_NewCamera);

        BT_CameraUp.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_CameraUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/up.png"))); // NOI18N
        BT_CameraUp.setToolTipText("move layer up");
        BT_CameraUp.setActionCommand("cameraUp");
        BT_CameraUp.setBorderPainted(false);
        BT_CameraUp.setFocusPainted(false);
        BT_CameraUp.setFocusable(false);
        BT_CameraUp.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_CameraUp.setPreferredSize(new java.awt.Dimension(32, 32));
        TB_CameraTools.add(BT_CameraUp);

        BT_CameraDown.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_CameraDown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/down.png"))); // NOI18N
        BT_CameraDown.setToolTipText("move layer down");
        BT_CameraDown.setActionCommand("cameraDown");
        BT_CameraDown.setBorderPainted(false);
        BT_CameraDown.setFocusPainted(false);
        BT_CameraDown.setFocusable(false);
        BT_CameraDown.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_CameraDown.setPreferredSize(new java.awt.Dimension(32, 32));
        TB_CameraTools.add(BT_CameraDown);

        BT_UpdateCamera.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_UpdateCamera.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/edit.png"))); // NOI18N
        BT_UpdateCamera.setToolTipText("Update camera title and position");
        BT_UpdateCamera.setActionCommand("updateCamera");
        BT_UpdateCamera.setBorderPainted(false);
        BT_UpdateCamera.setFocusPainted(false);
        BT_UpdateCamera.setFocusable(false);
        BT_UpdateCamera.setPreferredSize(new java.awt.Dimension(32, 32));
        TB_CameraTools.add(BT_UpdateCamera);

        BT_SmallCamera.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/collapse.png"))); // NOI18N
        BT_SmallCamera.setActionCommand("smallCamera");
        BT_SmallCamera.setFocusable(false);
        BT_SmallCamera.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        BT_SmallCamera.setPreferredSize(new java.awt.Dimension(32, 32));
        BT_SmallCamera.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        TB_CameraTools.add(BT_SmallCamera);

        jSeparator13.setSeparatorSize(new java.awt.Dimension(10, 20));
        TB_CameraTools.add(jSeparator13);

        BT_RemoveCamera.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_RemoveCamera.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/remove.png"))); // NOI18N
        BT_RemoveCamera.setToolTipText("Remove camera");
        BT_RemoveCamera.setActionCommand("removeCamera");
        BT_RemoveCamera.setBorderPainted(false);
        BT_RemoveCamera.setFocusPainted(false);
        BT_RemoveCamera.setFocusable(false);
        BT_RemoveCamera.setPreferredSize(new java.awt.Dimension(32, 32));
        TB_CameraTools.add(BT_RemoveCamera);

        PN_Cameras.add(TB_CameraTools, java.awt.BorderLayout.PAGE_START);

        DP_Main.setLayer(PN_Cameras, javax.swing.JLayeredPane.POPUP_LAYER);
        DP_Main.add(PN_Cameras);
        PN_Cameras.setBounds(420, 0, 200, 480);

        LB_FPKeys.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        LB_FPKeys.setText("Use W,S,A,D (SHIFT) Key to move");
        LB_FPKeys.setOpaque(true);
        DP_Main.add(LB_FPKeys);
        LB_FPKeys.setBounds(150, 10, 220, 15);

        PN_Right.add(DP_Main, java.awt.BorderLayout.CENTER);

        PN_TopBar.setLayout(new java.awt.BorderLayout());

        jPanel1.setOpaque(false);
        jPanel1.setLayout(new java.awt.BorderLayout());

        SP_LayersButtons.setBorder(null);
        SP_LayersButtons.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        SP_LayersButtons.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        SP_LayersButtons.setAutoscrolls(true);
        SP_LayersButtons.setHorizontalScrollBar(null);

        PN_LayersButtons.setAutoscrolls(true);
        PN_LayersButtons.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
        SP_LayersButtons.setViewportView(PN_LayersButtons);

        jPanel1.add(SP_LayersButtons, java.awt.BorderLayout.CENTER);

        BT_ScrollLeft.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/previous.png"))); // NOI18N
        BT_ScrollLeft.setActionCommand("scrollLeft");
        BT_ScrollLeft.setBorderPainted(false);
        BT_ScrollLeft.setContentAreaFilled(false);
        BT_ScrollLeft.setMargin(new java.awt.Insets(2, 2, 2, 2));
        BT_ScrollLeft.setMaximumSize(new java.awt.Dimension(22, 22));
        BT_ScrollLeft.setMinimumSize(new java.awt.Dimension(22, 22));
        BT_ScrollLeft.setPreferredSize(new java.awt.Dimension(22, 22));
        jPanel1.add(BT_ScrollLeft, java.awt.BorderLayout.WEST);

        BT_ScrollRight.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/next.png"))); // NOI18N
        BT_ScrollRight.setToolTipText("");
        BT_ScrollRight.setActionCommand("scrollRight");
        BT_ScrollRight.setBorderPainted(false);
        BT_ScrollRight.setContentAreaFilled(false);
        BT_ScrollRight.setMargin(new java.awt.Insets(2, 2, 2, 2));
        BT_ScrollRight.setMaximumSize(new java.awt.Dimension(22, 22));
        BT_ScrollRight.setMinimumSize(new java.awt.Dimension(22, 22));
        BT_ScrollRight.setPreferredSize(new java.awt.Dimension(22, 22));
        jPanel1.add(BT_ScrollRight, java.awt.BorderLayout.EAST);

        PN_TopBar.add(jPanel1, java.awt.BorderLayout.CENTER);

        PN_Tray.setOpaque(false);
        PN_Tray.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btgviews.add(BT_Orbit);
        BT_Orbit.setSelected(true);
        BT_Orbit.setText("Orbit");
        BT_Orbit.setActionCommand("orbit");
        PN_Tray.add(BT_Orbit);

        btgviews.add(BT_Fly);
        BT_Fly.setText("Fly");
        BT_Fly.setActionCommand("fly");
        PN_Tray.add(BT_Fly);

        btgviews.add(BT_Walk);
        BT_Walk.setText("Walk");
        BT_Walk.setActionCommand("walk");
        PN_Tray.add(BT_Walk);

        jSeparator2.setPreferredSize(new java.awt.Dimension(2, 32));
        PN_Tray.add(jSeparator2);

        BT_Cameras.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/components/Resources/Icons/22x22/camera.png"))); // NOI18N
        BT_Cameras.setActionCommand("cameras");
        BT_Cameras.setPreferredSize(new java.awt.Dimension(32, 32));
        PN_Tray.add(BT_Cameras);

        PN_TopBar.add(PN_Tray, java.awt.BorderLayout.EAST);

        BT_More.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_More.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/Resources/Icons/down.png"))); // NOI18N
        BT_More.setActionCommand("more");
        BT_More.setPreferredSize(new java.awt.Dimension(32, 32));
        jPanel7.add(BT_More);

        BT_Attach.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        BT_Attach.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/worldwindearth/components/Resources/Icons/22x22/attached.png"))); // NOI18N
        BT_Attach.setToolTipText("Drag and drop the button to another view to link the camera");
        BT_Attach.setActionCommand("link");
        BT_Attach.setPreferredSize(new java.awt.Dimension(32, 32));
        jPanel7.add(BT_Attach);

        PN_TopBar.add(jPanel7, java.awt.BorderLayout.WEST);

        PN_Right.add(PN_TopBar, java.awt.BorderLayout.PAGE_START);

        PN_StatusBar.setLayout(new java.awt.BorderLayout());

        PN_Status.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        PB_Downloading.setBackground(java.awt.Color.lightGray);
        PB_Downloading.setForeground(java.awt.Color.red);
        PB_Downloading.setToolTipText("Downloading");
        PB_Downloading.setIndeterminate(true);
        PB_Downloading.setPreferredSize(new java.awt.Dimension(50, 24));
        PN_Status.add(PB_Downloading);

        jlabel1.setText("Alt [m]");
        jlabel1.setToolTipText("Altitude");
        PN_Status.add(jlabel1);

        TF_Altitude.setEditable(false);
        TF_Altitude.setPreferredSize(new java.awt.Dimension(100, 26));
        PN_Status.add(TF_Altitude);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setPreferredSize(new java.awt.Dimension(18, 18));
        PN_Status.add(jSeparator1);

        jlabel2.setText("Lat / Long [°]");
        jlabel2.setToolTipText("Latitude / Longitude");
        PN_Status.add(jlabel2);

        TF_Latitude.setEditable(false);
        TF_Latitude.setPreferredSize(new java.awt.Dimension(150, 26));
        PN_Status.add(TF_Latitude);

        TF_Longitude.setEditable(false);
        TF_Longitude.setPreferredSize(new java.awt.Dimension(150, 26));
        PN_Status.add(TF_Longitude);

        LB_Licence.setText("...");
        PN_Status.add(LB_Licence);

        PN_StatusBar.add(PN_Status, java.awt.BorderLayout.CENTER);

        SP_VerticalExageration.setModel(new javax.swing.SpinnerNumberModel(10, 0, 100, 1));
        SP_VerticalExageration.setToolTipText("Elevation exageration (10 = normal, 20 = 2x normal)");
        jPanel5.add(SP_VerticalExageration);

        CB_Wireframe.setText("Wireframe");
        CB_Wireframe.setActionCommand("wireframe");
        jPanel5.add(CB_Wireframe);

        PN_StatusBar.add(jPanel5, java.awt.BorderLayout.EAST);

        PN_Right.add(PN_StatusBar, java.awt.BorderLayout.SOUTH);

        SP_Main.setRightComponent(PN_Right);

        add(SP_Main, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BT_AddLayer;
    private javax.swing.JButton BT_Attach;
    private javax.swing.JButton BT_CameraDown;
    private javax.swing.JButton BT_CameraUp;
    private javax.swing.JToggleButton BT_Cameras;
    private javax.swing.JButton BT_Configure;
    private javax.swing.JToggleButton BT_Fly;
    private javax.swing.JButton BT_LayerDown;
    private javax.swing.JButton BT_LayerUp;
    private javax.swing.JButton BT_More;
    private javax.swing.JButton BT_NewCamera;
    private javax.swing.JToggleButton BT_Orbit;
    private javax.swing.JButton BT_RemoveCamera;
    private javax.swing.JButton BT_RemoveLayer;
    private javax.swing.JButton BT_RenameLayer;
    private javax.swing.JButton BT_ScrollLeft;
    private javax.swing.JButton BT_ScrollRight;
    private javax.swing.JToggleButton BT_SmallCamera;
    private javax.swing.JButton BT_UpdateCamera;
    private javax.swing.JToggleButton BT_Walk;
    private javax.swing.JCheckBox CB_Wireframe;
    private javax.swing.JDesktopPane DP_Main;
    private javax.swing.JLabel LB_FPKeys;
    private javax.swing.JLabel LB_Layer;
    private javax.swing.JLabel LB_LayerIcon;
    private javax.swing.JLabel LB_Licence;
    private javax.swing.JList<Camera> LI_Cameras;
    private javax.swing.JMenuItem MN_ClearAttached;
    private javax.swing.JCheckBoxMenuItem MN_Fullscreen;
    private javax.swing.JMenuItem MN_GoTo;
    private javax.swing.JCheckBoxMenuItem MN_HideLayers;
    private javax.swing.JCheckBoxMenuItem MN_HideStatusBar;
    private javax.swing.JCheckBoxMenuItem MN_HideTopBar;
    private javax.swing.JMenu MN_NewLayers;
    private javax.swing.JMenuItem MN_RemoveLayer;
    private javax.swing.JMenuItem MN_Rename;
    private javax.swing.JMenuItem MN_ScreenIdentifier;
    private javax.swing.JMenu MN_Screens;
    private javax.swing.JMenuItem MN_SwitchSide;
    private javax.swing.JProgressBar PB_Downloading;
    private javax.swing.JPanel PN_Cameras;
    private javax.swing.JPanel PN_LayersButtons;
    private javax.swing.JPanel PN_LayersCenter;
    private javax.swing.JPanel PN_LayersData;
    private javax.swing.JPanel PN_LayersFrame;
    private javax.swing.JPanel PN_Left;
    private javax.swing.JPanel PN_Right;
    private javax.swing.JPanel PN_Status;
    private javax.swing.JPanel PN_StatusBar;
    private javax.swing.JPanel PN_TopBar;
    private javax.swing.JPanel PN_Tray;
    private javax.swing.JPopupMenu PU_Attached;
    private javax.swing.JPopupMenu PU_Layers;
    private javax.swing.JPopupMenu PU_More;
    private javax.swing.JSplitPane SP_Layers;
    private javax.swing.JScrollPane SP_LayersButtons;
    private javax.swing.JSplitPane SP_Main;
    private javax.swing.JSpinner SP_VerticalExageration;
    private javax.swing.JToolBar TB_CameraTools;
    private javax.swing.JTable TB_Layers;
    private javax.swing.JToolBar TB_Tools;
    private javax.swing.JTextField TF_Altitude;
    private javax.swing.JTextField TF_Latitude;
    private javax.swing.JTextField TF_Longitude;
    private javax.swing.ButtonGroup btgblayers;
    private javax.swing.ButtonGroup btgmodel;
    private javax.swing.ButtonGroup btgscreens;
    private javax.swing.ButtonGroup btgviews;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator10;
    private javax.swing.JPopupMenu.Separator jSeparator11;
    private javax.swing.JPopupMenu.Separator jSeparator12;
    private javax.swing.JToolBar.Separator jSeparator13;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JToolBar.Separator jSeparator9;
    private javax.swing.JLabel jlabel1;
    private javax.swing.JLabel jlabel2;
    // End of variables declaration//GEN-END:variables

    //**************************************************************************
    //*** Private
    //**************************************************************************
    /**
     * Add the layer to ww panels, also create the button<p>
     *
     * @param p
     * @param index
     */
    private void installLayer(WWEPlugin p, int index) {
        //--- Add layer
        LayerList ll = m.getLayers();
        Layer l = p.getLayer();
        l.setValue(AVKEY_WORLDWIND_LAYER_PLUGIN, p);
        if (index == -1) {
            ll.add(l);

        } else {
            ll.add(index, l);
        }
        // System.out.println("INSTALL LAYER:" + l + " active:" + l.isEnabled() + " class:" + l.getClass().getName());

        //--- Store the hash code of the plugin to show the layer config panel
        JComponent jcomp = p.getConfigComponent();
        if (jcomp != null) PN_LayersData.add(jcomp, "" + p.hashCode());

        //--- Check if the layer exposed a visual panel on the map, if so the
        //--- panel will be addedto the main JDesktop pane with the default
        //--- priority
        jcomp = p.getVisualComponent();
        if (jcomp != null) {
            DP_Main.add(jcomp, JLayeredPane.DEFAULT_LAYER);
            jcomp.setBounds(0, 0, DP_Main.getWidth(), DP_Main.getHeight());
        }

        //--- Get the layer exposed button and register the action
        if (p.hasLayerButton()) {
            JToggleButton jbutton = new JToggleButton();
            jbutton.setActionCommand("activeLayer");
            jbutton.putClientProperty("plugin", p);
            jbutton.addActionListener(this);
            jbutton.setToolTipText(l.getName());
            jbutton.setIcon(p.getPluginFactory().getFactoryIcon(22));
            // jbutton.setText("");
            jbutton.setPreferredSize(new Dimension(32, 32));
            btgblayers.add(jbutton);
            PN_LayersButtons.add(jbutton);

        }

    }

    /*
    private class ImmovableDesktopManager extends DefaultDesktopManager {

        @Override
        public void dragFrame(JComponent f, int x, int y) {
            super.dragFrame(f, x, y);

        }
    }
     */
    private class JCross extends JPanel {

        final BasicStroke STROKE2 = new BasicStroke(2f);

        private JCross() {
            super();
            setOpaque(false);
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;

            g2.setColor(Color.BLACK);
            g2.setStroke(STROKE2);
            g2.drawLine(getWidth() / 2, 0, getWidth() / 2, getHeight());
            g2.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
        }
    }
    //**************************************************************************
    //*** For debug
    //**************************************************************************
}
