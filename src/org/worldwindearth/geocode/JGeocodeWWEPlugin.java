/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.geocode;

import org.worldwindearth.components.MarkerPoint;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.Layer;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.tinyrcp.App;
import org.tinyrcp.TinyFactory;
import org.tinyrcp.TinyPlugin;
import org.w3c.dom.Element;
import org.worldwindearth.WWEFactory;
import org.worldwindearth.WWEPlugin;

/**
 * Geocode main layer
 *
 * @author sbodmer
 */
public class JGeocodeWWEPlugin extends JLayeredPane implements WWEPlugin, ActionListener, ChangeListener, MarkerPoint.ScreenPointListener, ReverseFetcher.ReverseFetcherListener, ListSelectionListener {

    static final Stroke STROKE1 = new BasicStroke(1);
    static final Stroke STROKE3 = new BasicStroke(3);

    App app = null;
    TinyFactory factory = null;
    WorldWindow ww = null;
    JDesktopPane jdesktop = null;

    GeocodeLayer layer = new GeocodeLayer();

    Point screen = null;
    MarkerPoint cursor = null;
    
    /**
     * The list of results
     */
    ArrayList<MarkerPoint> points = new ArrayList<>();
    
    /**
     * The list of geocoder
     */
    ArrayList<WWEGeocodePlugin> geocoders = new ArrayList<>();

    DefaultListModel<Reverse> reverse = new DefaultListModel<>();

    /**
     * Creates new form JTerminalsLayer
     *
     *
     * @param factory
     */
    public JGeocodeWWEPlugin(TinyFactory factory, WorldWindow ww) {
        super();
        this.factory = factory;
        this.ww = ww;

        initComponents();

        LI_Reverse.setModel(reverse);
    }

    //**************************************************************************
    //*** Swing
    //**************************************************************************
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        /*
        // ww.getView().
        BasicOrbitView view = (BasicOrbitView) ww.getView();
        Position pos = view.getCenterPosition();
        
        // Vec4 c = view.getCenterPoint();
        // System.out.println("POS:"+pos+" VEC4:"+c.getX()+" "+c.);
        // Vec4 vec4 = Vec4.fromArray2(new double[] { 6.1529, 46,1591 }, 0);
        // Vec4 proj = view.project(c);
        // System.out.println("VEC4:"+c+" PROJ:"+proj);

        Globe globe = ww.getModel().getGlobe();
        Position lsi = new Position(LatLon.fromDegrees(46.1931, 6.129162), 360d);
        Vec4 v = globe.computePointFromLocation(lsi);
        Vec4 proj = view.project(v);
        // System.out.println("VEC4:"+v+" PROJ:"+proj);
         */
        Rectangle rec = IF_Names.getBounds();

        g2.setColor(Color.BLACK);
        g2.setStroke(STROKE3);
        g2.drawLine(rec.x + (rec.width / 2), rec.y + (rec.height / 2), (int) screen.getX(), getHeight() - (int) screen.getY());
        g2.setStroke(STROKE1);

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
        cursor = new MarkerPoint(Position.fromDegrees(46.1935, 6.1291), this, "", getClass().getResource("/org/worldwindearth/geocode/Resources/Icons/64x64/Balloon.png"));

        jdesktop = (JDesktopPane) arg;

        SL_Opacity.addChangeListener(this);

        LI_Reverse.setCellRenderer(new JReverseCellRenderer());
        LI_Reverse.getSelectionModel().addListSelectionListener(this);
        
        layer.setName("Geocode");
        layer.addRenderable(cursor);
        cursor.setVisible(false);

        // jdesktop.add(IF_Names, JDesktopPane.PALETTE_LAYER);
        // IF_Names.setBounds(100,100, 320, 200);
        IF_Names.setVisible(false);

        //--- Default to be hidden
        setVisible(false);

        //--- Create the geocode plugin
        ArrayList<TinyFactory> facs = app.getFactories(WWEFactory.PLUGIN_CATEGORY_WORLDWIND_GEOCODER);
        for (TinyFactory f : facs) {
            WWEGeocodePlugin p = (WWEGeocodePlugin) f.newPlugin(null);
            p.setup(app, null);
            geocoders.add(p);

        }

    }

    @Override
    public void cleanup() {
        // jdesktop.remove(IF_Names);
        for (WWEGeocodePlugin p : geocoders) p.cleanup();
        geocoders.clear();

        layer.dispose();

    }

    @Override
    public void saveConfig(Element config) {
        if (config == null) return;

        config.setAttribute("opacity", "" + SL_Opacity.getValue());

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
        if (action.equals(TinyPlugin.DO_ACTION_NEWSIZE)) {
            Dimension dim = (Dimension) argument;

        } else if (action.equals(WWEPlugin.DO_ACTION_LAYER_SELECTED)) {
            IF_Names.setVisible(true);
            setVisible(true);

        } else if (action.equals(WWEPlugin.DO_ACTION_LAYER_UNSELECTED)) {
            IF_Names.setVisible(false);
            setVisible(false);
        }

        return null;
    }

    @Override
    public void setPluginName(String name) {
        layer.setName(name);
    }

    @Override
    public JComponent getVisualComponent() {
        return this;
    }

    @Override
    public JComponent getConfigComponent() {
        return PN_Config;
    }

    @Override
    public void configure(Element config) {
        if (config == null) return;

        try {
            int opacity = Integer.parseInt(config.getAttribute("opacity"));
            SL_Opacity.setValue(opacity);
            layer.setOpacity(opacity / 100d);

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

    /**
     * Place the balloon on the clicked position
     *
     * @param e
     */
    @Override
    public void layerMouseClicked(MouseEvent e, Position pos) {
        if (e.getClickCount() >= 2) {
            cursor.setVisible(true);
            cursor.setPosition(pos);
            e.consume();

            //--- fetch
            fetchReverse(pos);
        }
    }

    //**************************************************************************
    //*** ActionListener
    //**************************************************************************
    @Override
    public void actionPerformed(ActionEvent e) {
        //---
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
    }

    //**************************************************************************
    //*** ReverseFetcherListener
    //**************************************************************************
    /**
     * The result can arrive in parallel
     *
     * @param result
     */
    @Override
    public void reverseFetched(final ArrayList<Reverse> result) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                reverseFetchedEL(result);
            }
        });
    }

    public void reverseFetchedEL(ArrayList<Reverse> result) {
        for (Reverse r : result) {
            reverse.addElement(r);
            
            MarkerPoint m = new MarkerPoint(Position.fromDegrees(r.latitude, r.longitude), this, r.summary, getClass().getResource("/org/worldwindearth/geocode/Resources/Icons/64x64/BalloonBlue.png"));
            m.setVisible(true);
            layer.addRenderable(m);
            
            points.add(m);
        }
    }

    //**************************************************************************
    //*** ScreenPointListener
    //**************************************************************************
    @Override
    public void projectedScreenPoint(Position world, Point screen) {
        this.screen = screen;
        // System.out.println("SCREEN:"+screen);
    }

    //**************************************************************************
    //*** ListSelectionListener
    //**************************************************************************
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() == LI_Reverse.getSelectionModel()) {
            if (e.getValueIsAdjusting() == true) return;
            
            Reverse r = LI_Reverse.getSelectedValue();
            if (r != null) {
                ww.getView().goTo(Position.fromDegrees(r.latitude, r.longitude), 200);
                // BasicOrbitView view = (BasicOrbitView) wwd.getView();
                // view.addPanToAnimator(c.getCenterPosition(), Angle.fromDegrees(c.getHeading()), Angle.fromDegrees(c.getPitch()), c.getZoom());

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

        PN_Config = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        SL_Opacity = new javax.swing.JSlider();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        LI_Reverse = new javax.swing.JList<>();
        jPanel3 = new javax.swing.JPanel();
        PB_Waiting = new javax.swing.JProgressBar();
        IF_Names = new javax.swing.JInternalFrame();
        jScrollPane1 = new javax.swing.JScrollPane();
        LI_Results = new javax.swing.JList<>();

        PN_Config.setLayout(new java.awt.BorderLayout());

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
                .addComponent(SL_Opacity, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SL_Opacity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        PN_Config.add(jPanel2, java.awt.BorderLayout.NORTH);

        jPanel1.setLayout(new java.awt.BorderLayout());

        LI_Reverse.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(LI_Reverse);

        jPanel1.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        PN_Config.add(jPanel1, java.awt.BorderLayout.CENTER);

        PB_Waiting.setPreferredSize(new java.awt.Dimension(100, 26));
        jPanel3.add(PB_Waiting);

        PN_Config.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        IF_Names.setClosable(true);
        IF_Names.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        IF_Names.setIconifiable(true);
        IF_Names.setResizable(true);
        IF_Names.setVisible(true);

        LI_Results.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane1.setViewportView(LI_Results);

        IF_Names.getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        add(IF_Names);
        IF_Names.setBounds(100, 100, 340, 120);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JInternalFrame IF_Names;
    private javax.swing.JList<String> LI_Results;
    private javax.swing.JList<Reverse> LI_Reverse;
    private javax.swing.JProgressBar PB_Waiting;
    private javax.swing.JPanel PN_Config;
    private javax.swing.JSlider SL_Opacity;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    // End of variables declaration//GEN-END:variables

    //**************************************************************************
    //*** Private
    //**************************************************************************
    /**
     * Fetch the reverse (position => name) with the ReverseFetcher thread
     *
     * @param pos
     */
    private void fetchReverse(Position pos) {
        //--- Remove old points
        reverse.clear();
        for (MarkerPoint m: points) layer.removeRenderable(m);
        points.clear();
        
        //--- Start the fetchin in parallel
        for (WWEGeocodePlugin p : geocoders) {
            ReverseFetcher fetcher = new ReverseFetcher(this, pos, p);
            fetcher.start();
        }

    }

    

}
