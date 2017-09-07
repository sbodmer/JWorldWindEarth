/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.geocode;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.poi.BasicPointOfInterest;
import gov.nasa.worldwind.poi.PointOfInterest;
import org.tinyrcp.TinyPlugin;
import org.worldwindearth.components.layers.ScreenProjectionLayer;

/**
 * The reverse geocode result
 * 
 * @author sbodmer
 */
public class Result implements ScreenProjectionLayer.ScreenProjectable {
    public TinyPlugin producer = null;
    
    public double latitude = 0.0;
    public double longitude =  0.0;
    public double elevation = 0.0;
    public String house ="";
    public String street = "";
    public String city = "";
    public String state  = "";
    public String country = "";
    public String zip = "";
    public String summary = "";
    
    public Result(TinyPlugin producer) {
       this.producer = producer; 
    }
    
    @Override
    public String toString() {
        // return house+" "+street+", "+zip+" "+city+", "+state+", "+country;
        return summary;
    }
    
    public void setLocation(double lat, double lon) {
        this.latitude = lat;
        this.longitude = lon;
    }
    
    public void setLatitude(double lat) {
        this.latitude = lat;
        
    }
    
    public void setLongitude(double lon) {
        this.longitude = lon;
        
    }
    
    public void setElevation(double elevation) {
        this.elevation = elevation;
    }
    
    public void setHouse(String house) {
        this.house = house;
    }
    
    public void setStreet(String street) {
        this.street = street;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public void setState(String state) {
        this.state = state;
    }
    
    public void setCountry(String country) {
        this.country = country;
    }
    
    public void setZip(String zip) {
        this.zip = zip;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * Return a instant of PointOfInterest with a fixed location
     * 
     * @return 
     */
    public ReversePointOfInterest getPointOfInterest() {
        LatLon latlon = LatLon.fromDegrees(latitude, longitude);
        return new ReversePointOfInterest(this, latlon);
    }
    
    //**************************************************************************
    //*** ScreenProjectable
    //**************************************************************************
    @Override
    public Position getProjectablePosition() {
        return Position.fromDegrees(latitude, longitude, elevation);
    }
    
    public String getProjectableName() {
        return summary;
    }
}
