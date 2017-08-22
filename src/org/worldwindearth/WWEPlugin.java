/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth;

import gov.nasa.worldwind.layers.Layer;
import java.awt.event.MouseEvent;
import javax.swing.JToggleButton;
import org.tinyrcp.TinyPlugin;

/**
 * The prepare will be called with the main component which will host this plugin
 * as last argument (JComponent)
 * 
 * @author sbodmer
 */
public interface WWEPlugin extends TinyPlugin {
    /**
     * The key to store the plugin instance which produced a layer
     */
    public static final String AVKEY_WORLDWIND_LAYER_PLUGIN = "org.worldwindearth.WWEPlugin";
    
    /**
     * When the viewport should be fetched again if needed<p>
     *
     * No argument
     */
    public static final String DO_ACTION_VIEWPORT_NEEDS_REFRESH = "org.worldwindearth.viewportNeedsRefresh";
    
    /**
     * When the old selected layer is not more active, this message is broadcasted
     * by the framework to the previous selected layer<p>
     * 
     */
    public static final String DO_ACTION_LAYER_UNSELECTED = "org.worldwindearth.layerUnselected";
    
    /**
     * The new selected layer will received this message
     */
    public static final String DO_ACTION_LAYER_SELECTED = "org.worldwindearth.layerSelected";
    
    /**
     * When the layer was enabled
     */
    public static final String DO_ACTION_LAYER_ENABLED = "org.worldwindearth.layerEnabled";
    
    /**
     * When the layer was disabled
     */
    public static final String DO_ACTION_LAYER_DISABLED = "org.worldwindearth.layerDisabled";
    
    /**
     * Return the ww layer
     * 
     * @return 
     */
    public Layer getLayer();
    
    /**
     * Returns if this layer exposed a button to select the layer<p>
     * 
     * @return
     */
    public boolean hasLayerButton();
    
    /**
     * Called when the plugin is selected and a mouse click occured on the World wind window
     * @param evt
     * @return 
     */
    public void layerMouseClicked(MouseEvent evt, gov.nasa.worldwind.geom.Position pos);
    
}
