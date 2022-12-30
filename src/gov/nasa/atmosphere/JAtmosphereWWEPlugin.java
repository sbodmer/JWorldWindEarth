/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.atmosphere;

import gov.nasa.atmosphere.AtmosphereLayer;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLJPanel;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.layers.BasicLayerFactory;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.StarsLayer;
import gov.nasa.worldwind.layers.TiledImageLayer;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.tinyrcp.App;
import org.w3c.dom.Element;
import org.worldwindearth.WWEFactory;
import org.worldwindearth.WWEPlugin;
import gov.nasa.worldwind.terrain.Tessellator;
import java.awt.BorderLayout;
import java.io.InputStream;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.worldwindearth.WWEInputHandler;
import org.worldwindearth.components.layers.MergedLayer;

/**
 * Earth sun shading and lens flare layer
 *
 * @author sbodmer
 */
public class JAtmosphereWWEPlugin extends JPanel implements WWEPlugin, ActionListener, PositionListener, ChangeListener {

    App app = null;
    WWEFactory factory = null;
    WorldWindow ww = null;
    Vec4 eyePoint = null;

    SunPositionProvider sunPositionProvider = new BasicSunPositionProvider();
    RectangularNormalTessellator tessellator = new RectangularNormalTessellator();
    
    /**
     * Layer which contains the other one
     */
    SunshadingLayer layer = new SunshadingLayer();
    JCircle azi = null;
    JCircle ele = null;
    WorldWindowGLJPanel pww = null;
    SunshadingLayer player = new SunshadingLayer(); //--- Preview
    /**
     *
     */
    public JAtmosphereWWEPlugin(WWEFactory factory, WorldWindow ww) {
        super();
        this.factory = factory;
        this.ww = ww;

        initComponents();

        azi = new JCircle();
        PN_Azimut.add(azi, BorderLayout.CENTER);
        
        ele = new JCircle();
        PN_Elevation.add(ele, BorderLayout.CENTER);
    }

    //**************************************************************************
    //*** Plugin
    //**************************************************************************
    @Override
    public String getPluginName() {
        return layer.getName();
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
    public WWEFactory getPluginFactory() {
        return factory;
    }

    @Override
    public void setup(App app, Object arg) {
        this.app = app;

        layer.setName("Atmosphere");
        
        BT_Light.addActionListener(this);
        BT_Shade.addActionListener(this);
        CB_LensFlare.addActionListener(this);

        BT_Relative.addActionListener(this);
        BT_Absolute.addActionListener(this);

        SL_Azimut.addChangeListener(this);
        SL_Elevation.addChangeListener(this);

        //--- Preview
        pww = new WorldWindowGLJPanel();
        Model m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
        m.setGlobe(new Earth());
        m.getGlobe().setTessellator(tessellator);
        pww.setModel(m);
        PN_Preview.add(pww, BorderLayout.CENTER);
        
        BasicLayerFactory bl = new BasicLayerFactory();
        LayerList ll = m.getLayers();
        ll.add(new StarsLayer());
        Layer l = (TiledImageLayer) bl.createFromConfigSource("config/Earth/BMNGWMSLayer2.xml", null);
        l.setName("Blue Marble (2004)");
        l.setEnabled(true);
        ll.add(l);
        ll.add(player);
    }

    @Override
    public void configure(Element config) {
        if (config == null) return;

        try {
            int nb = Integer.parseInt(config.getAttribute("azimuth"));
            SL_Azimut.removeChangeListener(this);
            SL_Azimut.setValue(nb);
            SL_Azimut.addChangeListener(this);

            nb = Integer.parseInt(config.getAttribute("elevation"));
            SL_Elevation.removeChangeListener(this);
            SL_Elevation.setValue(nb);
            SL_Elevation.addChangeListener(this);

            Color c = new Color(Integer.parseInt(config.getAttribute("light")));
            BT_Light.setBackground(c);
            c = new Color(Integer.parseInt(config.getAttribute("shade")));
            BT_Shade.setBackground(c);
            boolean lf = config.getAttribute("lensFlare").equals("true");
            CB_LensFlare.setSelected(lf);
            layer.setLensFlare(CB_LensFlare.isSelected());
            player.setLensFlare(CB_LensFlare.isSelected());
            
        } catch (NumberFormatException ex) {
            //---
        }

    }

    @Override
    public void cleanup() {
        pww.destroy();
        player.dispose();
        
        layer.dispose();

    }

    @Override
    public void saveConfig(Element config) {
        if (config == null) return;

        config.setAttribute("lensFlare", "" + CB_LensFlare.isSelected());
        config.setAttribute("orientation", BT_Relative.isSelected() ? "relative" : "absolute");
        config.setAttribute("azimuth", "" + SL_Azimut.getValue());
        config.setAttribute("elevation", "" + SL_Elevation.getValue());
        config.setAttribute("light", "" + BT_Light.getBackground().getRGB());
        config.setAttribute("shade", "" + BT_Shade.getBackground().getRGB());
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
        if (action.equals(DO_ACTION_LAYER_ENABLED)) {
            //--- Add position listener to update light direction relative to the eye
            ww.removePositionListener(this);    //--- Remove it first to be sure no double adding
            ww.addPositionListener(this);
            ww.getModel().getGlobe().setTessellator(tessellator);
            
            
        } else if (action.equals(DO_ACTION_LAYER_DISABLED)) {
            ww.removePositionListener(this);
            ww.getModel().getGlobe().setTessellator(null);
            
        }
        return null;
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
        return false;
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
        if (e.getActionCommand().equals("light")
                || e.getActionCommand().equals("shade")) {
            Color c = JColorChooser.showDialog(PN_Config, "Choose a color...", ((JButton) e.getSource()).getBackground());
            if (c != null) {
                ((JButton) e.getSource()).setBackground(c);

            }
            update();

        } else if (e.getActionCommand().equals("lensFlare")) {
            layer.setLensFlare(CB_LensFlare.isSelected());
            player.setLensFlare(CB_LensFlare.isSelected());
            update();

        } else if (e.getActionCommand().equals("relative")) {
            SL_Azimut.setEnabled(true);
            SL_Elevation.setEnabled(true);
            update();

        } else if (e.getActionCommand().equals("absolute")) {
            SL_Azimut.setEnabled(false);
            SL_Elevation.setEnabled(false);
            update();

        }
    }

    //**************************************************************************
    //*** PositionListener
    //**************************************************************************
    @Override
    public void moved(PositionEvent event) {
        if (eyePoint == null || eyePoint.distanceTo3(ww.getView().getEyePoint()) > 1000) {
            update();
            eyePoint = ww.getView().getEyePoint();

        }
    }

    //**************************************************************************
    //*** ChangeListener
    //**************************************************************************
    @Override
    public void stateChanged(ChangeEvent e) {
        update();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        BTG_Effects = new javax.swing.ButtonGroup();
        PN_Config = new javax.swing.JPanel();
        BT_Light = new javax.swing.JButton();
        BT_Shade = new javax.swing.JButton();
        BT_Relative = new javax.swing.JRadioButton();
        BT_Absolute = new javax.swing.JRadioButton();
        SL_Azimut = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        SL_Elevation = new javax.swing.JSlider();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        CB_LensFlare = new javax.swing.JCheckBox();
        PN_Preview = new javax.swing.JPanel();
        PN_Elevation = new javax.swing.JPanel();
        PN_Azimut = new javax.swing.JPanel();

        BT_Light.setBackground(java.awt.Color.white);
        BT_Light.setActionCommand("light");
        BT_Light.setBorderPainted(false);
        BT_Light.setPreferredSize(new java.awt.Dimension(32, 32));

        BT_Shade.setBackground(java.awt.Color.black);
        BT_Shade.setActionCommand("shade");
        BT_Shade.setBorderPainted(false);
        BT_Shade.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        BT_Shade.setPreferredSize(new java.awt.Dimension(32, 32));

        BTG_Effects.add(BT_Relative);
        BT_Relative.setSelected(true);
        BT_Relative.setText("Relative");
        BT_Relative.setActionCommand("relative");

        BTG_Effects.add(BT_Absolute);
        BT_Absolute.setText("Absolute");
        BT_Absolute.setActionCommand("absolute");

        SL_Azimut.setFont(new java.awt.Font("Arial", 0, 9)); // NOI18N
        SL_Azimut.setMajorTickSpacing(90);
        SL_Azimut.setMaximum(360);
        SL_Azimut.setPaintLabels(true);
        SL_Azimut.setPaintTicks(true);
        SL_Azimut.setValue(125);

        jLabel1.setText("Elevation");
        jLabel1.setPreferredSize(new java.awt.Dimension(100, 26));

        jLabel2.setText("Azimut");
        jLabel2.setPreferredSize(new java.awt.Dimension(100, 26));

        SL_Elevation.setFont(new java.awt.Font("Arial", 0, 9)); // NOI18N
        SL_Elevation.setMajorTickSpacing(20);
        SL_Elevation.setMaximum(90);
        SL_Elevation.setMinimum(-90);
        SL_Elevation.setPaintLabels(true);
        SL_Elevation.setPaintTicks(true);

        jLabel3.setText("Shade");
        jLabel3.setPreferredSize(new java.awt.Dimension(100, 26));

        jLabel4.setText("Light");
        jLabel4.setPreferredSize(new java.awt.Dimension(100, 26));

        CB_LensFlare.setText("Lens Flare");
        CB_LensFlare.setActionCommand("lensFlare");

        PN_Preview.setLayout(new java.awt.BorderLayout());

        PN_Elevation.setPreferredSize(new java.awt.Dimension(41, 41));
        PN_Elevation.setLayout(new java.awt.BorderLayout());

        PN_Azimut.setPreferredSize(new java.awt.Dimension(41, 41));
        PN_Azimut.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout PN_ConfigLayout = new javax.swing.GroupLayout(PN_Config);
        PN_Config.setLayout(PN_ConfigLayout);
        PN_ConfigLayout.setHorizontalGroup(
            PN_ConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PN_ConfigLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PN_ConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(PN_Preview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PN_ConfigLayout.createSequentialGroup()
                        .addGroup(PN_ConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, PN_ConfigLayout.createSequentialGroup()
                                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(SL_Elevation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, PN_ConfigLayout.createSequentialGroup()
                                .addGroup(PN_ConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(PN_ConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(PN_ConfigLayout.createSequentialGroup()
                                        .addComponent(BT_Relative)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(BT_Absolute))
                                    .addGroup(PN_ConfigLayout.createSequentialGroup()
                                        .addGroup(PN_ConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(BT_Light, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(BT_Shade, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(CB_LensFlare)))
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, PN_ConfigLayout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(SL_Azimut, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PN_ConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, PN_ConfigLayout.createSequentialGroup()
                                .addComponent(PN_Azimut, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(PN_Elevation, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        PN_ConfigLayout.setVerticalGroup(
            PN_ConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PN_ConfigLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PN_ConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BT_Light, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(CB_LensFlare, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(2, 2, 2)
                .addGroup(PN_ConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(BT_Shade, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PN_ConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BT_Relative)
                    .addComponent(BT_Absolute))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PN_ConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(PN_ConfigLayout.createSequentialGroup()
                        .addGroup(PN_ConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SL_Azimut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(PN_ConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(SL_Elevation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(PN_ConfigLayout.createSequentialGroup()
                        .addComponent(PN_Azimut, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(PN_Elevation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PN_Preview, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup BTG_Effects;
    private javax.swing.JRadioButton BT_Absolute;
    private javax.swing.JButton BT_Light;
    private javax.swing.JRadioButton BT_Relative;
    private javax.swing.JButton BT_Shade;
    private javax.swing.JCheckBox CB_LensFlare;
    private javax.swing.JPanel PN_Azimut;
    private javax.swing.JPanel PN_Config;
    private javax.swing.JPanel PN_Elevation;
    private javax.swing.JPanel PN_Preview;
    private javax.swing.JSlider SL_Azimut;
    private javax.swing.JSlider SL_Elevation;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    // End of variables declaration//GEN-END:variables

    private void update() {
        azi.setAngle(SL_Azimut.getValue());
        ele.setAngle(SL_Elevation.getValue());
        
        // Update colors
        tessellator.setLightColor(BT_Light.getBackground());
        tessellator.setAmbientColor(BT_Shade.getBackground());
        // Compute Sun direction
        Vec4 sun, light;
        if (BT_Relative.isSelected()) {
            
            // Compute Sun position relative to the eye position
            Angle elevation = Angle.fromDegrees(SL_Elevation.getValue());
            Angle azimuth = Angle.fromDegrees(SL_Azimut.getValue());
            Position eyePos = ww.getView().getEyePosition();
            sun = Vec4.UNIT_Y;
            sun = sun.transformBy3(Matrix.fromRotationX(elevation));
            sun = sun.transformBy3(Matrix.fromRotationZ(azimuth.multiply(-1)));

            // JYC : Not sure at all of this !!!
            // JYC sun = sun.transformBy3( wwd.getModel().getGlobe().computeTransformToPosition( eyePos.getLatitude(), eyePos.getLongitude(), 0));
            sun = sun.transformBy3(ww.getModel().getGlobe().computeSurfaceOrientationAtPosition(eyePos.getLatitude(), eyePos.getLongitude(), 0));

        } else {
            // Compute Sun position according to current date and time
            LatLon sunPos = sunPositionProvider.getPosition();
            sun = ww.getModel().getGlobe().computePointFromPosition(new Position(sunPos, 0)).normalize3();
        }

        light = sun.getNegative3();
        tessellator.setLightDirection(light);
        
        layer.setSunDirection(sun);
        player.setSunDirection(sun);
        
        // Redraw
        ww.redraw();
        pww.redraw();
    }

}
