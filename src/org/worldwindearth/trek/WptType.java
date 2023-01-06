/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.worldwindearth.trek;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Cone;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import java.util.Date;

/**
 *
 * @author sbodmer
 */
public class WptType {

    public double lat = 0d;
    public double lon = 0d;
    public double ele = 0d;
    public Date time = new Date();
    public String name = "";

    /**
     * WW to render for the waypoints
     */
    public Cone cone = null;
    public Cylinder cylinder = null;
    
    public WptType(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;

    }

    public String toString() {
        if (name.equals("")) return time.toInstant().toString();
        return name;
    }

    public Cone generateCone() {
        if (this.cone != null) return cone;
        
        //--- Prepare renderable for the waypoints
        Position p = Position.fromDegrees(lat, lon, 0);
        cone = new Cone(p, 20, 5);
        ShapeAttributes a1 = new BasicShapeAttributes();
        a1.setInteriorMaterial(Material.BLUE);
        a1.setOutlineMaterial(Material.BLUE);
        a1.setInteriorOpacity(0.5);
        a1.setEnableLighting(false);
        // a1.setOutlineWidth(2d);
        a1.setDrawInterior(true);
        a1.setDrawOutline(false);
        cone.setAttributes(a1);
        cone.setVisible(true);
        cone.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        cone.setDragEnabled(true);
        return cone;
    }

    public Cylinder generateCylinder() {
        if (this.cylinder != null) return cylinder;
        
        //--- Prepare renderable for the waypoints
        Position p = Position.fromDegrees(lat, lon, 0);
        cylinder = new Cylinder(p, 10, 2);
        ShapeAttributes a1 = new BasicShapeAttributes();
        a1.setInteriorMaterial(Material.RED);
        a1.setOutlineMaterial(Material.RED);
        a1.setInteriorOpacity(0.5);
        a1.setEnableLighting(false);
        // a1.setOutlineWidth(2d);
        a1.setDrawInterior(true);
        a1.setDrawOutline(false);
        cylinder.setAttributes(a1);
        cylinder.setVisible(true);
        cylinder.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
        cylinder.setDragEnabled(true);
        return cylinder;
    }
}
