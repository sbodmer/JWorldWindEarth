/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nominatim;

import gov.nasa.worldwind.exception.NoItemException;
import gov.nasa.worldwind.exception.ServiceException;
import gov.nasa.worldwind.poi.Gazetteer;
import gov.nasa.worldwind.poi.PointOfInterest;
import java.util.List;

/**
 *
 * @author sbodmer
 */
public class NominatimGazetteer implements Gazetteer {

    public NominatimGazetteer() {
        //---
    }
    
    @Override
    public List<PointOfInterest> findPlaces(String placeInfo) throws NoItemException, ServiceException {
        return null;
    }
    
}
