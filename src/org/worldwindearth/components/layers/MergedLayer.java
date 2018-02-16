/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.components.layers;

import gov.nasa.worldwind.layers.AbstractLayer;
import gov.nasa.worldwind.render.DrawContext;
import java.util.ArrayList;

/**
 * Call the render() on all added layers (so multiple layer can be seen as one)
 * 
 * @author sbodmer
 */
public class MergedLayer extends AbstractLayer {

    ArrayList<AbstractLayer> layers = new ArrayList<>();
    
    public MergedLayer() {
        super();
    }
    
    //**************************************************************************
    //*** API
    //**************************************************************************
    public void addLayer(AbstractLayer l) {
        if (!layers.contains(l)) layers.add(l);
    }
    
    public boolean removeLayer(AbstractLayer l) {
        return layers.remove(l);
    }
    
    //**************************************************************************
    //*** AbstractLayer
    //**************************************************************************
    @Override
    public void doRender(DrawContext dc) {
        //--- Nothing here
        
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        for (int i=0;i<layers.size();i++) layers.get(i).setEnabled(enabled);
    }
    
    @Override
    public void dispose() {
        super.dispose();
        for (int i=0;i<layers.size();i++) layers.get(i).dispose();
    }
    
    @Override
    public void render(DrawContext dc) {
        System.out.println("RENDER");
        for (int i=0;i<layers.size();i++) layers.get(i).render(dc);
        
    }
}
