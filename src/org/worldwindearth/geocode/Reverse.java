/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.geocode;

import gov.nasa.worldwind.geom.LatLon;

/**
 * The reverse geocode result
 * 
 * @author sbodmer
 */
public class Reverse {
    public WWEGeocodePlugin producer = null;
    
    public double latitude = 0.0;
    public double longitude =  0.0;
    public String house ="";
    public String street = "";
    public String city = "";
    public String state  = "";
    public String country = "";
    public String zip = "";
    public String summary = "";
    
    public Reverse(WWEGeocodePlugin producer) {
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
}
