package org.worldwindearth.groundcursor;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfaceCircle;
import gov.nasa.worldwind.view.firstperson.BasicFlyView;

/**
 *
 */
public class GroundCursorLayer extends RenderableLayer {
    SurfaceCircle cursor = null;
    
    /**
     * The last viewport center postion in world coordinates
     */
    Position center = null;
    
    /**
     * Sets fog range/density according to view altitude
     */
    public GroundCursorLayer() {
        
        //--- Prepare the cursor
        ShapeAttributes a1 = new BasicShapeAttributes();
        a1.setInteriorMaterial(Material.GREEN);
        a1.setInteriorOpacity(0.5);
        a1.setEnableLighting(false);
        a1.setOutlineMaterial(Material.BLACK);
        a1.setOutlineWidth(2d);
        a1.setDrawInterior(true);
        a1.setDrawOutline(true);
        cursor = new SurfaceCircle(a1, LatLon.ZERO, 20d);
        cursor.setVisible(true);
        addRenderable(cursor);
    }


    @Override
    public void doRender(DrawContext dc) {
        center = dc.getViewportCenterPosition();
        if (dc.getView() instanceof BasicFlyView) center = Position.ZERO;
        
        //--- Move cursor to center of viewport
        if (center != null) cursor.moveTo(new Position(center, 0));
        super.doRender(dc);
    }

    @Override
    public String toString() {
        return this.getName();
    }
    
   
}
