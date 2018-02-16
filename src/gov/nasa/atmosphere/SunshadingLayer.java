/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.atmosphere;

import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.render.DrawContext;

/**
 * Container class with the lens flare rendering included
 *
 * @author sbodmer
 */
public class SunshadingLayer extends AtmosphereLayer {

    LensFlareLayer lLayer = LensFlareLayer.getPresetInstance(LensFlareLayer.PRESET_BOLD);

    /**
     * Show lens flare or note
     */
    boolean lf = false;

    public SunshadingLayer() {
        //---
        lLayer.setEnabled(true);
    }

    //**************************************************************************
    //*** API
    //**************************************************************************
    public void setLensFlare(boolean lf) {
        this.lf = lf;

    }

    //**************************************************************************
    //*** AtmoshpereLayer
    //**************************************************************************
    @Override
    public void doRender(DrawContext dc) {
        super.doRender(dc);
        // if (lf) lLayer.render(dc);
    }

    public void render(DrawContext dc) {
        super.render(dc);
        if (lf) lLayer.render(dc);
    }
    
    
    @Override
    public void setSunDirection(Vec4 direction) {
        super.setSunDirection(direction);
        lLayer.setSunDirection(direction);
    }

    public void dispose() {
        super.dispose();
        lLayer.dispose();
    }

}
