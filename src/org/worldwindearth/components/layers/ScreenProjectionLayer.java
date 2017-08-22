/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.components.layers;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import org.worldwindearth.components.MarkerPoint;

/**
 * This layer will also forward the drawing calls to a listener (for 2d
 * rendering for example)
 *
 * @author sbodmer
 */
public class ScreenProjectionLayer extends RenderableLayer {

    ScreenProjectionListener screenProjectionlistener = null;

    /**
     * The list of projectable
     */
    ArrayList<ScreenProjectable> projectables = new ArrayList<>();

    public ScreenProjectionLayer() {
        super();
        

    }

    public void setScreenProjectionListener(ScreenProjectionListener spl) {
        this.screenProjectionlistener = spl;
    }
    
    public void addProjectable(ScreenProjectable proj) {
        if (!projectables.contains(proj)) projectables.add(proj);
    }

    public boolean removeProjectable(ScreenProjectable proj) {
        return projectables.remove(proj);

    }

    
    /**
     * The parent doRender will be called first (so renderable are displayed),
     * then the projectable are processed.
     *
     * If a renderable implements the ScreenProjectable it will also be
     * processed
     *
     * @param dc
     */
    @Override
    protected void doRender(DrawContext dc) {
        super.doRender(dc);
        if (screenProjectionlistener == null) return;

        //--- Fist the custom one
        for (ScreenProjectable proj : projectables) {
            //--- Forward the calls for each passed object
            Vec4 loc = null;
            Position world = proj.getProjectablePosition();
            loc = dc.getSurfaceGeometry().getSurfacePoint(world);
            if (loc == null) loc = dc.getGlobe().computePointFromPosition(world);
            Vec4 screenPoint = dc.getView().project(loc);
            Point screen = new Point((int) screenPoint.x, (int) screenPoint.y);
            
            screenProjectionlistener.screenProjectedPoint(proj, screen);
        }

        //--- Then the renderable ones
        for (Renderable r : renderables) {
            if (r instanceof ScreenProjectable) {
                ScreenProjectable proj = (ScreenProjectable) r;
                
                //--- Forward the calls for each passed object
                Vec4 loc = null;
                Position world = proj.getProjectablePosition();
                loc = dc.getSurfaceGeometry().getSurfacePoint(world);
                if (loc == null) loc = dc.getGlobe().computePointFromPosition(world);
                Vec4 screenPoint = dc.getView().project(loc);
                Point screen = new Point((int) screenPoint.x, (int) screenPoint.y);
                screenProjectionlistener.screenProjectedPoint(proj, screen);
            }
        }
        
    }
    
    /**
     * The projection event
     */
    public interface ScreenProjectionListener {

        public void screenProjectedPoint(ScreenProjectable obj, Point screen);
    }

    /**
     * The interface the object must implement to be projected
     */
    public interface ScreenProjectable {

        /**
         * The world position
         *
         * @return
         */
        public Position getProjectablePosition();
        
        /**
         * Return the name of the proojectable (can be html)
         * @return 
         */
        public String getProjectableName();
    }

}
