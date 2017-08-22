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
import java.util.HashMap;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JLayeredPane;
import javax.swing.ListSelectionModel;
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
import org.worldwindearth.components.layers.ScreenProjectionLayer;

/**
 * Geocode main layer
 *
 * @author sbodmer
 */
public class JGeocodeWWEPlugin extends JLayeredPane implements WWEPlugin, ActionListener, ChangeListener, ScreenProjectionLayer.ScreenProjectionListener, ReverseFetcher.ReverseFetcherListener, ListSelectionListener, ScreenProjectionLayer.ScreenProjectable, MarkerPoint.MarkerPointScreenListener {

    public static final int MARKER_LAYER = new Integer(10);

    static final Stroke STROKE1 = new BasicStroke(1);
    static final Stroke STROKE3 = new BasicStroke(3);

    App app = null;
    TinyFactory factory = null;
    WorldWindow ww = null;
    JDesktopPane jdesktop = null;

    ScreenProjectionLayer layer = new ScreenProjectionLayer();

    Position cursor = Position.ZERO;

    /**
     * The cursor balloon
     */
    JBalloon jballoon = null;
    MarkerPoint m = null;
    Point screen = new Point(0, 0);
    /**
     * The list of geocoder
     */
    ArrayList<WWEGeocodePlugin> geocoders = new ArrayList<>();

    /**
     * The list of results
     */
    DefaultListModel<JBalloon> reverse = new DefaultListModel<>();

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
        LI_Top.setModel(reverse);
        
        PN_Top.setVisible(false);
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

        jdesktop = (JDesktopPane) arg;

        SL_Opacity.addChangeListener(this);

        LI_Reverse.setCellRenderer(new JBalloonCellRenderer());
        LI_Reverse.getSelectionModel().addListSelectionListener(this);

        LI_Top.setCellRenderer(new JBalloonCellRenderer());
        LI_Top.getSelectionModel().addListSelectionListener(this);
        
        jdesktop.add(PN_Top, JLayeredPane.POPUP_LAYER);
        
        jballoon = new JBalloon(this, getClass().getResource("/org/worldwindearth/geocode/Resources/Icons/32x32/Balloon.png"));
        jdesktop.add(jballoon, JLayeredPane.PALETTE_LAYER);
        jballoon.setSize(jballoon.getPreferredSize());
        jballoon.setVisible(false);

        //--- For debug
        // m = new MarkerPoint(Position.fromDegrees(46.1935, 6.1291));
        // layer.addRenderable(m);

        layer.setScreenProjectionListener(this);
        layer.setName("Geocode");
        layer.addProjectable(jballoon);

        // jdesktop.add(IF_Names, JDesktopPane.PALETTE_LAYER);
        // IF_Names.setBounds(100,100, 320, 200);
        IF_Names.setVisible(false);

        //--- Default to be hidden
        // setVisible(false);
        //--- Create the geocode plugin
        ArrayList<TinyFactory> facs = app.getFactories(WWEFactory.PLUGIN_CATEGORY_WORLDWIND_GEOCODER);
        for (TinyFactory f : facs) {
            WWEGeocodePlugin p = (WWEGeocodePlugin) f.newPlugin(null);
            p.setup(app, ww);
            geocoders.add(p);

        }

    }

    @Override
    public void cleanup() {
        jdesktop.remove(PN_Top);
        layer.removeProjectable(this);
        
        jdesktop.remove(jballoon);
        layer.removeProjectable(jballoon);
        
        for (int i=0;i<reverse.getSize();i++) {
            JBalloon jb = reverse.get(i);
            jdesktop.remove(jb);
            layer.removeProjectable(jb);
        }
        reverse.clear();
        
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
            PN_Top.setBounds(300, 0, dim.width-600, 96);
            
        } else if (action.equals(WWEPlugin.DO_ACTION_LAYER_SELECTED)) {
            IF_Names.setVisible(true);
            PN_Top.setVisible(true);
            setVisible(true);

        } else if (action.equals(WWEPlugin.DO_ACTION_LAYER_UNSELECTED)) {
            IF_Names.setVisible(false);
            PN_Top.setVisible(false);
            setVisible(false);
            
        } else if (action.equals(WWEPlugin.DO_ACTION_LAYER_ENABLED)) {
            // PN_Top.setVisible(true);
            
        } else if (action.equals(WWEPlugin.DO_ACTION_LAYER_DISABLED)) {
            PN_Top.setVisible(false);
            jballoon.setVisible(false);
            for (int i=0;i<reverse.getSize();i++) reverse.get(i).setVisible(false);
            
        }

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
            //--- Set the elevation to zero so the projection is correct
            cursor = new Position(pos, 0);
            
            // jballoon.setLocation(e.getX()-32, e.getY()-jballoon.getHeight());
            jballoon.setVisible(true);

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
            JBalloon jb = new JBalloon(r, getClass().getResource("/org/worldwindearth/geocode/Resources/Icons/32x32/BalloonBlue.png"));
            jb.setSize(jb.getPreferredSize());
            layer.addProjectable(jb);
            reverse.addElement(jb);
            jdesktop.add(jb, JLayeredPane.PALETTE_LAYER);

        }
    }

    //**************************************************************************
    //*** ScreenProjectionListener
    //**************************************************************************
    @Override
    public void screenProjectedPoint(ScreenProjectionLayer.ScreenProjectable obj, Point screen) {
        //--- If it's a ballon, replace it
        if (obj == jballoon) {
            jballoon.setLocation(screen.x - 16, jdesktop.getHeight() - screen.y - jballoon.getHeight());
            
        } else if (obj instanceof JBalloon) {
            JBalloon jb = (JBalloon) obj;
            jb.setLocation(screen.x - 16, jdesktop.getHeight() - screen.y - jb.getHeight());
            jb.setVisible(true);

        } else if (obj == m) {
            /*
            jballoon.setLocation(screen.x, jdesktop.getHeight() - screen.y);
            jballoon.setVisible(true);
             */
        }
        // jballoon.setLocation(screen.x-32, jdesktop.getHeight() - screen.y - jballoon.getHeight());
        // jballoon.setVisible(true);

    }

    @Override
    public void projectedScreenMarkerPoint(MarkerPoint m, Position world, Point screen) {
        this.screen = screen;

        /*
        jballoon.setLocation(screen.x-32, getHeight()-screen.y-64);
        jballoon.setVisible(true);
         */
    }

    //**************************************************************************
    //*** ListSelectionListener
    //**************************************************************************
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource() instanceof ListSelectionModel) {
            if (e.getValueIsAdjusting() == true) return;

            JBalloon r = (e.getSource()==LI_Reverse.getSelectionModel()?LI_Reverse.getSelectedValue():LI_Top.getSelectedValue());
            if (r != null) {
                Position pos = r.getProjectablePosition();
                ww.getView().goTo(pos, 300);
                // BasicOrbitView view = (BasicOrbitView) wwd.getView();
                // view.addPanToAnimator(c.getCenterPosition(), Angle.fromDegrees(c.getHeading()), Angle.fromDegrees(c.getPitch()), c.getZoom());

            }
        }
    }

    @Override
    public Position getProjectablePosition() {
        return cursor;
    }

    @Override
    public String getProjectableName() {
        return "";
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
        PN_Top = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        LI_Top = new javax.swing.JList<>();
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

        PN_Top.setLayout(new java.awt.BorderLayout());

        LI_Top.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane3.setViewportView(LI_Top);

        PN_Top.add(jScrollPane3, java.awt.BorderLayout.CENTER);

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
    private javax.swing.JList<JBalloon> LI_Reverse;
    private javax.swing.JList<JBalloon> LI_Top;
    private javax.swing.JProgressBar PB_Waiting;
    private javax.swing.JPanel PN_Config;
    private javax.swing.JPanel PN_Top;
    private javax.swing.JSlider SL_Opacity;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
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
        for (int i = 0; i < reverse.size(); i++) {
            JBalloon jb = reverse.get(i);
            layer.removeProjectable(jb);
            jdesktop.remove(jb);
        }
        reverse.clear();

        //--- Start the fetchin in parallel
        for (WWEGeocodePlugin p : geocoders) {
            ReverseFetcher fetcher = new ReverseFetcher(this, pos, p);
            fetcher.start();
        }

    }

    

}
