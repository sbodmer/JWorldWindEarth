/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.components;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sphere;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.ogc.kml.KMLPlacemark;
import gov.nasa.worldwind.ogc.kml.impl.KMLGlobeBalloonImpl;
import gov.nasa.worldwind.render.BasicBalloonAttributes;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.GlobeAnnotationBalloon;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ScreenAnnotationBalloon;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.Size;
import gov.nasa.worldwind.render.markers.BasicMarker;
import gov.nasa.worldwind.render.markers.BasicMarkerAttributes;
import gov.nasa.worldwind.render.markers.Marker;
import gov.nasa.worldwind.render.markers.MarkerAttributes;
import gov.nasa.worldwind.render.markers.MarkerShape;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

/**
 *
 * @author sbodmer
 */
public class MarkerPoint implements Renderable {

    Position world = null;
    Point screen = null;
    ScreenPointListener listener = null;

    Cylinder cursor = null;
    BasicMarker marker = null;
    GlobeAnnotationBalloon balloon = null;
    PointPlacemark point = null;
    ScreenAnnotationBalloon sballoon = null;

    public MarkerPoint(Position world) {
        this(world, null, "", null);
    }

    public MarkerPoint(Position world, ScreenPointListener listener) {
        this(world, listener, "", null);
    }
    
    public MarkerPoint(Position world, ScreenPointListener listener, String label) {
        this(world, listener, label, null);
    }

    public MarkerPoint(Position world, ScreenPointListener listener, String label, URL icon) {
        this.world = world;
        this.listener = listener;

        /*
        ShapeAttributes attrs = new BasicShapeAttributes();
        attrs.setInteriorMaterial(Material.YELLOW);
        attrs.setInteriorOpacity(0.7);
        attrs.setEnableLighting(true);
        attrs.setOutlineMaterial(Material.RED);
        attrs.setOutlineWidth(2d);
        attrs.setDrawInterior(true);
        attrs.setDrawOutline(false);

        cursor = new Cylinder(world, 100, 5);
        cursor.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        cursor.setAttributes(attrs);
        cursor.setVisible(true);
        cursor.setValue(AVKey.DISPLAY_NAME, "Cursor");
         */
 /*
        BasicBalloonAttributes ba = new BasicBalloonAttributes();
        ba.setOutlineMaterial(Material.WHITE);
        // ba.setInteriorMaterial(Material.WHITE);
        // ba.setImageSource(getClass().getResource("/org/nominatim/Resources/Icons/64x64/Balloon.png"));
        // ba.setSize(Size.fromPixels(64, 64));
        ba.setBalloonShape(AVKey.SHAPE_RECTANGLE);
        // ba.setCornerRadius(0);
        ba.setImageOpacity(1);
        // ba.setInteriorOpacity(0);
        // ba.setOutlineOpacity(0);
        // ba.setLeaderShape(AVKey.SHAPE_NONE);
        // ba.setLeaderWidth(0);
        // ba.setCornerRadius(0);
        ba.setDrawInterior(true);
        
        // ba.setDrawOutline(false);
        // ba.setMaximumSize(Size.fromPixels(64, 64));

        // ba.setSize(Size.fromPixels(100,100));
        // marker.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        balloon = new GlobeAnnotationBalloon("???", world);
        balloon.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        balloon.setAttributes(ba);
         */
        PointPlacemarkAttributes pa = new PointPlacemarkAttributes();
        if (icon != null) {
            try {
                // getClass().getResource("/org/nominatim/Resources/Icons/64x64/Balloon.png")
                pa.setImage(ImageIO.read(icon));
                pa.setDrawImage(true);
                pa.setImageOffset(new Offset(32d, 0d, AVKey.PIXELS, AVKey.PIXELS));
                // pa.setImageAddress(getClass().getResource("/org/nominatim/Resources/Icons/64x64/Balloon.png").toString());
            } catch (IOException ex) {
                //---
            }
        }
        pa.setLineMaterial(Material.YELLOW);
        pa.setLineWidth(2d);
        pa.setLabelMaterial(Material.WHITE);
        pa.setDrawLabel(true);
        pa.setUsePointAsDefaultImage(true);
        // pa.setLabelOffset(new Offset(100d,100d,AVKey.PIXELS,AVKey.PIXELS));

        point = new PointPlacemark(world);
        point.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        point.setLabelText(label);
        point.setAttributes(pa);
        point.setLineEnabled(true);

        // sballoon = new ScreenAnnotationBalloon("???", new Point(0,0));
        // sballoon.setAttributes(ba);
    }

    //**************************************************************************
    //*** API
    //**************************************************************************
    public void setVisible(boolean visible) {
        point.setVisible(visible);
    }

    public void setPosition(Position pos) {
        point.setPosition(pos);
    }

    public Point getScreenPosition() {
        return screen;
    }

    //**************************************************************************
    //*** Renderable
    //**************************************************************************
    @Override
    public void render(DrawContext dc) {
        Vec4 loc = null;
        if (world.getElevation() < dc.getGlobe().getMaxElevation()) loc = dc.getSurfaceGeometry().getSurfacePoint(world);
        if (loc == null) loc = dc.getGlobe().computePointFromPosition(world);
        Vec4 screenPoint = dc.getView().project(loc);
        screen = new Point((int) screenPoint.x, (int) screenPoint.y);

        Rectangle vp = dc.getView().getViewport();
        // cursor.render(dc);
        // marker.render(dc, loc, 10);
        // balloon.render(dc);
        point.render(dc);
        // sballoon.setScreenLocation(new Point(screen.x, vp.height-screen.y));
        // sballoon.render(dc);

        if (listener != null) listener.projectedScreenPoint(world, screen);
    }

    public interface ScreenPointListener {

        public void projectedScreenPoint(Position world, Point screen);
    }
}
