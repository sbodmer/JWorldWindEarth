/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.components;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolygon;
import java.util.Arrays;

/**
 *
 * @author sbodmer
 */
public class DragSurfacePolygon extends SurfacePolygon {

    public static final int ARROWS = 0;

    DragSurfacePolygonListener listener = null;
    String signature = "";

    /**
     * Create a pre defined shape
     *
     * @param geo
     * @param fac The scale factor (relative to degrees)
     * @param dlat
     * @param dlon
     */
    public DragSurfacePolygon(int geo, double fac, double dlat, double dlon) {
        super();
        if (geo == ARROWS) {
            Iterable<LatLon> locations = Arrays.asList(
                    LatLon.fromDegrees(dlat + 0, dlon + 0),
                    LatLon.fromDegrees(dlat + (6 * fac), dlon + 0),
                    LatLon.fromDegrees(dlat + (6 * fac), dlon - (1 * fac)),
                    LatLon.fromDegrees(dlat + (8 * fac), dlon + (1 * fac)),
                    LatLon.fromDegrees(dlat + (6 * fac), dlon + (3 * fac)),
                    LatLon.fromDegrees(dlat + (6 * fac), dlon + (2 * fac)),
                    
                    LatLon.fromDegrees(dlat + (2 * fac), dlon + (2 * fac)),
                    LatLon.fromDegrees(dlat + (2 * fac), dlon + (6 * fac)),
                    LatLon.fromDegrees(dlat + (3 * fac), dlon + (6 * fac)),
                    LatLon.fromDegrees(dlat + (1 * fac), dlon + (8 * fac)),
                    LatLon.fromDegrees(dlat + (-1 * fac), dlon + (6 * fac)),
                    LatLon.fromDegrees(dlat + 0, dlon + (6 * fac)));
                    
            setLocations(locations);
        }
    }

    public DragSurfacePolygon() {
        super();
    }

    public DragSurfacePolygon(SurfacePolygon source) {
        super(source);

    }

    public DragSurfacePolygon(ShapeAttributes normalAttrs) {
        super(normalAttrs);
    }

    public DragSurfacePolygon(Iterable<? extends LatLon> iterable) {
        super(iterable);
    }

    public DragSurfacePolygon(ShapeAttributes normalAttrs, Iterable<? extends LatLon> iterable) {
        super(normalAttrs);

    }

    //**************************************************************************
    //*** API
    //**************************************************************************
    public void setDragSurfacePolygonListener(DragSurfacePolygonListener listener) {
        this.listener = listener;
    }

    public DragSurfacePolygonListener getDragSurfacePolygonListener() {
        return listener;
    }

    public void setSignature(String s) {
        this.signature = s;
    }

    public String getSignature() {
        return signature;
    }

    
    //**************************************************************************
    //*** SurfacePolygon
    //**************************************************************************
    /**
     * No dragging move
     * @param oldReferencePosition
     * @param newReferencePosition 
     */
    @Override
    protected void doMoveTo(Position oldReferencePosition, Position newReferencePosition) {
        super.doMoveTo(oldReferencePosition, newReferencePosition);
        
        
    }

    /**
     * Dragging move
     * 
     * @param globe
     * @param oldReferencePosition
     * @param newReferencePosition 
     */
    @Override
    protected void doMoveTo(Globe globe, Position oldReferencePosition, Position newReferencePosition) {
        super.doMoveTo(globe, oldReferencePosition, newReferencePosition);
        if (listener != null) listener.surfacePolygonDragged(this, newReferencePosition);
    }

    static public interface DragSurfacePolygonListener {

        public void surfacePolygonDragged(DragSurfacePolygon poly, Position newPosition);

    }
}
