/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.osmbuildings;

import gov.nasa.worldwind.formats.geojson.GeoJSONDoc;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Renderable;

/**
 * Container for a manual added GeoJson building
 * 
 * @author sbodmer
 */
public class GeoJSONEntry {
    Renderable renderable = null;
    String title = "";
    
    /**
     * The doc must already be parsed when this method is called
     * @param title
     * @param doc 
     */
    public GeoJSONEntry(String title, GeoJSONDoc doc, OSMBuildingsTileListener listener) {
        this.title = title;
        
        //--- Create the renderable
        BasicShapeAttributes defaultAttrs = new BasicShapeAttributes();
        // Material m = new Material(Color.LIGHT_GRAY.brighter(), Color.LIGHT_GRAY, Color.LIGHT_GRAY.darker(), Color.LIGHT_GRAY.darker(), 0);
        // defaultAttrs.setInteriorMaterial(m);
        defaultAttrs.setInteriorMaterial(Material.LIGHT_GRAY);
        // defaultAttrs.setInteriorOpacity(getOpacity());
        defaultAttrs.setOutlineMaterial(Material.GRAY);
        // sa.setOutlineOpacity(opacity);
        defaultAttrs.setDrawInterior(true);
        // defaultAttrs.setDrawOutline(drawOutline);

        defaultAttrs.setEnableLighting(true);
        defaultAttrs.setEnableAntialiasing(true);
        renderable = new OSMBuildingsRenderable(doc, 10, defaultAttrs, title, listener);
        
    }
    
    @Override
    public String toString() {
        return title;
    }
    
    public Renderable getRenderable() {
        return renderable;
    }
}
