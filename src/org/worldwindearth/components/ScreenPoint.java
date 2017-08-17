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
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import java.awt.Point;

/**
 *
 * @author sbodmer
 */
public class ScreenPoint implements Renderable {

    Position world = null;
    Point screen = null;
    ScreenPointListener listener = null;
    Cylinder cursor = null;

    public ScreenPoint(Position world, ScreenPointListener listener) {
        this.world = world;
        this.listener = listener;

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

    }

    @Override
    public void render(DrawContext dc) {
        Vec4 loc = null;
        if (world.getElevation() < dc.getGlobe().getMaxElevation()) loc = dc.getSurfaceGeometry().getSurfacePoint(world);
        if (loc == null) loc = dc.getGlobe().computePointFromPosition(world);
        Vec4 screenPoint = dc.getView().project(loc);
        screen = new Point((int) screenPoint.x, (int) screenPoint.y);

        cursor.render(dc);
        
        if (listener != null) listener.projectedScreenPoint(world, screen);
    }

    public interface ScreenPointListener {

        public void projectedScreenPoint(Position world, Point screen);
    }
}
