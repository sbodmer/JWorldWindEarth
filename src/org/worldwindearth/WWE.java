/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth;

/**
 *
 * @author sbodmer
 */
public class WWE {
    
    /**
     * The sun direction (Vec4) in the tessellator
     */
    public static String TESSELATOR_KEY_SUN_DIRECTION = "wwe.sun.direction";
    
    /**
     * The sun color (Color) in the tessellator
     */
    public static String TESSELATOR_KEY_SUN_COLOR = "wwe.sun.color";
    
    /**
     * The sun ambient color (Color) in the tessellator
     */
    public static String TESSELATOR_KEY_SUN_AMBIENT_COLOR = "wwe.sun.ambient.color";
    
    /**
     * Returns the distance in meters between two location
     * 
     * @param lat1
     * @param lon1
     * @param lat2
     * @param lon2
     * @return 
     */
    public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
        lat1 = lat1 * Math.PI / 180;
        lat2 = lat2 * Math.PI / 180;
        lon1 = lon1 * Math.PI / 180;
        lon2 = lon2 * Math.PI / 180;

        double distance = 2 * Math.asin(Math.sqrt((Math.sin((lat1 - lat2) / 2)) * (Math.sin((lat1 - lat2) / 2))
                + Math.cos(lat1) * Math.cos(lat2) * (Math.sin((lon1 - lon2) / 2)) * (Math.sin((lon1 - lon2) / 2))));

        return (distance * 6372.795*1000);

    }
}
