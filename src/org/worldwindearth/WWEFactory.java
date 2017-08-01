/*
 * AbstractJobFactory.java
 *
 * Created on March 1, 2007, 9:00 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.worldwindearth;

import org.tinyrcp.TinyFactory;


/**
 * Worldwind layer factory
 * @author sbodmer
 */
public interface WWEFactory extends TinyFactory {
    public static final String PLUGIN_CATEGORY_WORLDWIND_LAYER              = "wwe,layer";
    
    public static final String PLUGIN_FAMILY_WORLDWIND_LAYER_NASA           = "wwe,layer,nasa";
    public static final String PLUGIN_FAMILY_WORLDWIND_LAYER_WORLDWIND      = "wwe,layer,worldwind";
    public static final String PLUGIN_FAMILY_WORLDWIND_LAYER_MAPTILES       = "wwe,layer,maptiles";
    public static final String PLUGIN_FAMILY_WORLDWIND_LAYER_WMS            = "wwe,layer,wms";
    public static final String PLUGIN_FAMILY_WORLDWIND_LAYER_BUILDINGS      = "wwe,layer,buildings";
    public static final String PLUGIN_FAMILY_WORLDWIND_LAYER_GRATICULE      = "wwe,layer,graticule";
    public static final String PLUGIN_FAMILY_WORLDWIND_LAYER_GEOCODING      = "wwe,layer,geocoding";
    
    
}
