/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.models;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.event.*;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.*;
import gov.nasa.worldwind.view.firstperson.BasicFlyView;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import osm.map.worldwind.gl.GLRenderable;
import osm.map.worldwind.gl.obj.ObjLoaderProgressListener;
import osm.map.worldwind.gl.obj.ObjRenderable;

/**
 * Always assume a zoom level of 15
 *
 * @author sbodmer
 */
public class ModelsLayer extends RenderableLayer implements ActionListener, ObjLoaderProgressListener {

    /**
     * The last viewport center postion in world coordinates
     */
    protected Position center = null;

    /**
     * The worldwindow for this layer
     */
    protected WorldWindow ww = null;

    /**
     * Manually added entries to load
     */
    protected ArrayList<Model> entries = new ArrayList<>();

    /**
     * The loaded and renderable models
     */
    protected ArrayList<Model> loaded = new ArrayList<>();

    protected ModelLoaderListener listener = null;

    // Cylinder c = null;
    public ModelsLayer() {
        super();

        //--- Prepare the screen credits
        // ScreenCredit sc = new ScreenCreditImage("OSM Buildings", getClass().getResource("/org/osmbuildings/Resources/Icons/32x32/osmbuildings.png"));
        // sc.setLink("http://www.osmbuildings.org");
        // sc.setOpacity(1);
        // setScreenCredit(sc);
    }

    //**************************************************************************
    //*** API
    //*************************************************************************
    public void clearModels() {
        entries.clear();
        loaded.clear();
        removeAllRenderables();

    }

    public void setModelLoadingListener(ModelLoaderListener listener) {
        this.listener = listener;

    }

    /**
     * If the passed mode has a null renderable, then it will be loaded by this
     * layer (if format is supported)
     *
     * @param f
     */
    public void addModel(Model m) {
        entries.add(m);

    }

    public boolean removeModel(Model m) {
        entries.remove(m);
        boolean b = loaded.remove(m);
        if (b) removeRenderable(m.getRenderable());
        if (ww != null) ww.redraw();
        return b;
    }

    //**************************************************************************
    //*** AbstractLayer
    //**************************************************************************
    @Override
    public void setOpacity(double opacity) {
        super.setOpacity(opacity);

        /*
        for (Renderable r : renderables) {
            if (r instanceof OSMBuildingsRenderable) {
                OSMBuildingsRenderable or = (OSMBuildingsRenderable) r;
                ((OSMBuildingsRenderable) r).setOpacity(opacity);
            }
        }
         */
    }

    @Override
    public void dispose() {
        clearModels();
        super.dispose();
    }

    @Override
    public boolean isLayerInView(DrawContext dc) {
        // dc.addScreenCredit(getScreenCredit());

        return true;
    }

    /**
     * Fetch the buldings data for the center of the viewport
     *
     * @param dc
     */
    @Override
    public void doRender(DrawContext dc) {
        super.doRender(dc);

        center = dc.getViewportCenterPosition();
        //--- If Fly view, the center is the eye (no center postion available)
        if (dc.getView() instanceof BasicFlyView) {
            center = dc.getView().getCurrentEyePosition();
        }
    }

    @Override
    public void doPreRender(DrawContext dc) {
        while (!entries.isEmpty()) {
            Model f = entries.remove(0);
            // ObjLoader obj = new ObjLoader(f.getPath(),dc.getGL().getGL2(), false, false);
            // System.out.println("OBJ:" + obj);
            // Position pos = Position.fromDegrees(48.38105948, -4.47514588);
            if (f.getRenderable() == null) {
                Rectangle vp = dc.getView().getViewport();
                Position pos = dc.getView().computePositionFromScreenPoint(vp.width / 2, vp.height / 2);
                if (f.getFile().getName().endsWith(".obj")) {
                    //--- The loading of the model in the GL context can be long and
                    //--- freeze the the Event thread
                    ObjRenderable r = new ObjRenderable(pos, f.getFile().getPath(), true, false, this);
                    f.setRenderable(r);
                    loaded.add(f);

                }

            } else {
                loaded.add(f);
            }
            if (f.getRenderable() != null) {
                addRenderable(f.getRenderable());
                
            }

        }

        //--- Position models
        for (Model m : loaded) {
            Renderable r = m.getRenderable();
            Position pos = Position.fromDegrees(m.getLatitude(), m.getLongitude(), m.getAltitude());
            if (r instanceof GLRenderable) {
                GLRenderable glr = (GLRenderable) r;
                glr.setPosition(pos);
                glr.setVisible(m.isVisible());
                glr.setAzimuth(m.getAzimuth());
                glr.setElevation(m.getElevation());
                glr.setRoll(m.getRoll());
                glr.setSize(m.getScale());

            }

        }
        super.doPreRender(dc);
    }

    //**************************************************************************
    //*** MessageListener
    //**************************************************************************
    /**
     * If view stopped
     *
     * @param msg
     */
    @Override
    public void onMessage(Message msg) {
        // System.out.println("onMessage:" + msg.getName() + " when:" + msg.getWhen() + " source:" + msg.getSource());

    }

    //**************************************************************************
    //*** ActionListener
    //**************************************************************************
    @Override
    public void actionPerformed(ActionEvent e) {

    }

    //**************************************************************************
    //*** ObjLoadingProgressListener
    //**************************************************************************
    @Override
    public void objLoading(String file, String string1) {
        if (listener != null) listener.modelLoading(file, string1);
    }

    @Override
    public void objLoadingFailed(String file, String string1) {
        if (listener != null) listener.modelLoadingFailed(file, string1);
    }

    @Override
    public void objLoaded(String file, String string1) {
        if (listener != null) listener.modelLoaded(file, string1);
    }

    //**************************************************************************
    //*** Private
    //**************************************************************************
}
