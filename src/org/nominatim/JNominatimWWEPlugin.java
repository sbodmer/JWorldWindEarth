/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nominatim;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.view.orbit.BasicOrbitView;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.tinyrcp.App;
import org.tinyrcp.TinyFactory;
import org.tinyrcp.TinyPlugin;
import org.w3c.dom.Element;
import org.worldwindearth.WWEPlugin;

/**
 * Nominatim
 *
 * @author sbodmer
 */
public class JNominatimWWEPlugin extends JLayeredPane implements WWEPlugin, ActionListener, ChangeListener {

    static final Stroke STROKE1 = new BasicStroke(1);
    static final Stroke STROKE2 = new BasicStroke(2);

    App app = null;
    TinyFactory factory = null;
    WorldWindow ww = null;

    NominatimLayer layer = new NominatimLayer();

    /**
     * Creates new form JTerminalsLayer
     *
     * 
     * @param factory
     */
    public JNominatimWWEPlugin(TinyFactory factory, WorldWindow ww) {
        super();
        this.factory = factory;
        this.ww = ww;

        initComponents();

        //--- Default to be hidden
        setVisible(false);
    }

    //**************************************************************************
    //*** Swing
    //**************************************************************************
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

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

        g2.setColor(Color.RED);
        g2.setStroke(STROKE2);
        g2.drawLine(getWidth() / 2, getHeight() / 2, (int) proj.getX(), getHeight() - (int) proj.getY());

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

        SL_Opacity.addChangeListener(this);

        layer.setName("Nominatim");
        layer.setValue(AVKEY_WORLDWIND_LAYER_PLUGIN, this);

    }

    @Override
    public void cleanup() {
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
            setVisible(true);

        } else if (action.equals(WWEPlugin.DO_ACTION_LAYER_UNSELECTED)) {
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
        jInternalFrame1 = new javax.swing.JInternalFrame();
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList<>();

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
                .addContainerGap(195, Short.MAX_VALUE))
        );

        PN_Config.add(jPanel2, java.awt.BorderLayout.NORTH);

        jInternalFrame1.setClosable(true);
        jInternalFrame1.setDefaultCloseOperation(javax.swing.WindowConstants.HIDE_ON_CLOSE);
        jInternalFrame1.setIconifiable(true);
        jInternalFrame1.setResizable(true);
        jInternalFrame1.setVisible(true);

        jScrollPane1.setViewportView(jList1);

        jInternalFrame1.getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        add(jInternalFrame1);
        jInternalFrame1.setBounds(0, 0, 340, 230);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel PN_Config;
    private javax.swing.JSlider SL_Opacity;
    private javax.swing.JInternalFrame jInternalFrame1;
    private javax.swing.JList<String> jList1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    public Point getPoint(DrawContext dc, Position pos) {
        Vec4 loc = null;
        if (pos.getElevation() < dc.getGlobe().getMaxElevation()) loc = dc.getSurfaceGeometry().getSurfacePoint(pos);
        if (loc == null) loc = dc.getGlobe().computePointFromPosition(pos);
        Vec4 screenPoint = dc.getView().project(loc);
        return new Point((int) screenPoint.x, (int) screenPoint.y);
        
    }

}
