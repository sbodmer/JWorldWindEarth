/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.geocode;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.poi.BasicPointOfInterest;

/**
 * Container for implementing PointOfInterest
 * 
 * @author sbodmer
 */
public class ReversePointOfInterest extends BasicPointOfInterest {
    Result reverse = null;
    
    public ReversePointOfInterest(Result reverse, LatLon latlon) {
        super(latlon);
        this.reverse = reverse;
    }
    
    /**
     *
     * @return
     */
    public Result getReverse() {
        return reverse;
    }
}
