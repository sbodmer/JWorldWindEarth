/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.skygradient;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.terrain.RectangularTessellator;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import org.tinyrcp.App;
import org.w3c.dom.Element;
import org.worldwindearth.WWEFactory;
import org.worldwindearth.WWEFactory;
import org.worldwindearth.WWEPlugin;

/**
 * Stars dome
 *
 * @author sbodmer
 */
public class JSkyGradientWWEPlugin extends JPanel implements WWEPlugin, ActionListener, PositionListener {

    App app = null;
    WWEFactory factory = null;
    WorldWindow ww = null;
    Vec4 eyePoint = null;

    // SunPositionProvider sunPositionProvider = new BasicSunPositionProvider();

    SkyGradientLayer layer = new SkyGradientLayer();

    //--- For the sun shading, use this one instead of SkyGradientLayer
    AtmosphereLayer atmosphereLayer = new AtmosphereLayer();
    
    /**
     * Creates new form JTerminalsLayer
     */
    public JSkyGradientWWEPlugin(WWEFactory factory, WorldWindow ww) {
        super();
        this.factory = factory;
        this.ww = ww;

        initComponents();

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
        return null;
        // return PN_Config;
    }

    @Override
    public WWEFactory getPluginFactory() {
        return factory;
    }

    @Override
    public void setup(App app, Object arg) {
        this.app = app;

        layer.setName("Atmosphere");

        CB_SunShading.addActionListener(this);

        BT_Light.addActionListener(this);
        BT_Shade.addActionListener(this);
    }

    @Override
    public void configure(Element config) {
        //---

    }

    @Override
    public void cleanup() {
        layer.dispose();

    }

    @Override
    public void saveConfig(Element config) {
        //---
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

        } else if (e.getActionCommand().equals("sunshading")) {
            if (CB_SunShading.isSelected()) {
                ww.addPositionListener(this);
                
            } else {
                ww.removePositionListener(this);
                
            }
            // Add lens flare layer

        }
    }

    //**************************************************************************
    //*** PositionListener
    //**************************************************************************
    @Override
    public void moved(PositionEvent event) {
        if (eyePoint == null || eyePoint.distanceTo3(ww.getView().getEyePoint()) > 1000) {
            eyePoint = ww.getView().getEyePoint();
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

        BTG_Effects = new javax.swing.ButtonGroup();
        PN_Config = new javax.swing.JPanel();
        CB_SunShading = new javax.swing.JCheckBox();
        BT_Light = new javax.swing.JButton();
        BT_Shade = new javax.swing.JButton();
        BT_Relative = new javax.swing.JRadioButton();
        BT_Absolute = new javax.swing.JRadioButton();
        SL_Azimuth = new javax.swing.JSlider();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        SL_Elevation = new javax.swing.JSlider();

        CB_SunShading.setText("Sun shading on Earth");
        CB_SunShading.setActionCommand("enabled");

        BT_Light.setBackground(java.awt.Color.white);
        BT_Light.setText("Light");
        BT_Light.setActionCommand("light");
        BT_Light.setBorderPainted(false);
        BT_Light.setContentAreaFilled(false);
        BT_Light.setOpaque(true);

        BT_Shade.setBackground(java.awt.Color.black);
        BT_Shade.setText("Shade");
        BT_Shade.setActionCommand("shade");
        BT_Shade.setBorderPainted(false);
        BT_Shade.setContentAreaFilled(false);

        BTG_Effects.add(BT_Relative);
        BT_Relative.setText("BT_Relative");

        BTG_Effects.add(BT_Absolute);
        BT_Absolute.setSelected(true);
        BT_Absolute.setText("BT_Absolute");

        SL_Azimuth.setFont(new java.awt.Font("Arial", 0, 9)); // NOI18N
        SL_Azimuth.setMajorTickSpacing(90);
        SL_Azimuth.setMaximum(360);
        SL_Azimuth.setPaintLabels(true);
        SL_Azimuth.setPaintTicks(true);
        SL_Azimuth.setValue(125);

        jLabel1.setText("Elevation");

        jLabel2.setText("Azimuth");

        SL_Elevation.setFont(new java.awt.Font("Arial", 0, 9)); // NOI18N
        SL_Elevation.setMajorTickSpacing(10);
        SL_Elevation.setMaximum(90);
        SL_Elevation.setMinimum(-10);
        SL_Elevation.setPaintLabels(true);
        SL_Elevation.setPaintTicks(true);

        javax.swing.GroupLayout PN_ConfigLayout = new javax.swing.GroupLayout(PN_Config);
        PN_Config.setLayout(PN_ConfigLayout);
        PN_ConfigLayout.setHorizontalGroup(
            PN_ConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PN_ConfigLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PN_ConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(CB_SunShading, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(PN_ConfigLayout.createSequentialGroup()
                        .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(SL_Azimuth, javax.swing.GroupLayout.DEFAULT_SIZE, 354, Short.MAX_VALUE))
                    .addGroup(PN_ConfigLayout.createSequentialGroup()
                        .addGroup(PN_ConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PN_ConfigLayout.createSequentialGroup()
                                .addComponent(BT_Light)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(BT_Shade))
                            .addGroup(PN_ConfigLayout.createSequentialGroup()
                                .addComponent(BT_Relative)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(BT_Absolute)))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(PN_ConfigLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 69, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(SL_Elevation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        PN_ConfigLayout.setVerticalGroup(
            PN_ConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PN_ConfigLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(CB_SunShading)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PN_ConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BT_Light)
                    .addComponent(BT_Shade))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PN_ConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BT_Relative)
                    .addComponent(BT_Absolute))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PN_ConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(SL_Azimuth, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PN_ConfigLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SL_Elevation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(238, Short.MAX_VALUE))
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
    private javax.swing.JCheckBox CB_SunShading;
    private javax.swing.JPanel PN_Config;
    private javax.swing.JSlider SL_Azimuth;
    private javax.swing.JSlider SL_Elevation;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables

    
    
    
    private void update() {
        if (CB_SunShading.isSelected()) {
		
            /*
			// Configuration.setValue(AVKey.TESSELLATOR_CLASS_NAME, RectangularNormalTessellator.class.getName());
			// Get tessellator
			Tesselator tesselator = new RectangularNormalTessellator();
			ww.getModel().getGlobe().setTessellator(tesselator);

			// Update colors
			tesselator.setLightColor(BT_Light.getBackground());
			tesselator.setAmbientColor(BT_Shade.getBackground());
			// Compute Sun direction
			Vec4 sun, light;
			if (BT_Relative.isSelected()) {
				// Enable UI controls
				SL_Azimuth.setEnabled(true);
				SL_Elevation.setEnabled(true);
				// Compute Sun position relative to the eye position
				Angle elevation = Angle.fromDegrees(SL_Elevation.getValue());
				Angle azimuth = Angle.fromDegrees(SL_Azimuth.getValue());
				Position eyePos = ww.getView().getEyePosition();
				sun = Vec4.UNIT_Y;
				sun = sun.transformBy3(Matrix.fromRotationX(elevation));
				sun = sun.transformBy3(Matrix.fromRotationZ(azimuth.multiply(-1)));

				// JYC : Not sure at all of this !!!
				// JYC sun = sun.transformBy3( wwd.getModel().getGlobe().computeTransformToPosition( eyePos.getLatitude(), eyePos.getLongitude(), 0));
				sun = sun.transformBy3(wwd.getModel().getGlobe().computeSurfaceOrientationAtPosition(eyePos.getLatitude(), eyePos.getLongitude(), 0));
				// Position pos = new Position( new LatLon( eyePos.getLatitude(), eyePos.getLongitude() ),0 );

				// sun = sun.transformBy3( wwd.getModel().getGlobe().computePointFromPosition(pos) );

				// sun = wwd.getModel().getGlobe().computePointFromPosition(pos).normalize3();
			} else {
				// Disable UI controls
				SL_Azimuth.setEnabled(false);
				SL_Elevation.setEnabled(false);
				
				// Compute Sun position according to current date and time
				LatLon sunPos = sunPositionProvider.getPosition();
				sun = ww.getModel().getGlobe().computePointFromPosition(new Position(sunPos, 0)).normalize3();
			}

			light = sun.getNegative3();
			tesselator.setLightDirection(light);

			if (lensFlareLayer != null)
				lensFlareLayer.setSunDirection(sun);

			if (atmosphereLayer != null && atmos)
				atmosphereLayer.setSunDirection(sun);
            */
            
		} else {
			// Turn off lighting
            /*
			if (tesselator != null)
				tesselator.setLightDirection(null);

			// Configuration.setValue(AVKey.TESSELLATOR_CLASS_NAME, RectangularTessellator.class.getName());
			// tesselator = new RectangularTessellator();
			ww.getModel().getGlobe().setTessellator(new RectangularTessellator());
			// Get tessellator
			tesselator = null;
            */
            
            /*
			if (lensFlareLayer != null) {
				lensFlareLayer.setSunDirection(null);
			}

            if (atmosphereLayer != null) {
                atmosphereLayer.setSunDirection(null);

                
                ww.getModel().getLayers().set(i, skyGradientLayer);
            }   
            */
			
		}
        
		// Redraw
		ww.redraw();
	}
}
