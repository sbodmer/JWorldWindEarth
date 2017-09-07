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
    public static final String PLUGIN_CATEGORY_WORLDWIND_GAZETTEER          = "wwe,gazetteer";
    public static final String PLUGIN_CATEGORY_WORLDWIND_GEOCODER           = "wwe,geocoder";
    
    public static final String PLUGIN_FAMILY_WORLDWIND_LAYER_NASA           = "nasa";
    public static final String PLUGIN_FAMILY_WORLDWIND_LAYER_WORLDWIND      = "worldwind";
    public static final String PLUGIN_FAMILY_WORLDWIND_LAYER_MAPTILES       = "maptiles";
    public static final String PLUGIN_FAMILY_WORLDWIND_LAYER_WMS            = "wms";
    public static final String PLUGIN_FAMILY_WORLDWIND_LAYER_BUILDINGS      = "buildings";
    public static final String PLUGIN_FAMILY_WORLDWIND_LAYER_GRATICULE      = "graticule";
    public static final String PLUGIN_FAMILY_WORLDWIND_LAYER_SEARCH         = "search";
    
}
