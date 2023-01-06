package org.worldwindearth.trek;

import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Path;
import java.util.ArrayList;

/**
 *
 */
public class TraceLayer extends RenderableLayer {
    /**
     * The lines
     */
    ArrayList<Path> tracks = new ArrayList<>();
    ArrayList<Path> routes = new ArrayList<>();
    
    /**
     * Sets fog range/density according to view altitude
     */
    public TraceLayer() {
        //---
        // setPickEnabled(true);
    }

    //**************************************************************************
    //*** API
    //************************************************************************** 
    public void clearTracks() {
        for (int i=0;i<tracks.size();i++) removeRenderable(tracks.get(i));
        tracks.clear();
    }
    
    public void addTrack(Path path) {
        if (tracks.contains(path)) return;
        tracks.add(path);
        addRenderable(path);
        
    }

    public void removeTrack(Path path) {
        if (!tracks.contains(path)) return;
        tracks.remove(path);
        removeRenderable(path);
    }
    
    public void clearRoutes() {
        for (int i=0;i<routes.size();i++) removeRenderable(routes.get(i));
        routes.clear();
    }
    
    public void addRoute(Path path) {
        if (routes.contains(path)) return;
        routes.add(path);
        addRenderable(path);
        
    }

    public void removeRoute(Path path) {
        if (!routes.contains(path)) return;
        routes.remove(path);
        removeRenderable(path);
    }
    
    //**************************************************************************
    //*** Layer
    //**************************************************************************
    @Override
    public void doRender(DrawContext dc) {
        super.doRender(dc);
    }

    @Override
    public String toString() {
        return this.getName();
    }

    //**************************************************************************
    //*** Private
    //**************************************************************************
    
}
