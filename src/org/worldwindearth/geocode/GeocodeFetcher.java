/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.geocode;

import gov.nasa.worldwind.geom.Position;
import java.util.ArrayList;

/**
 *
 * @author sbodmer
 */
public class GeocodeFetcher extends Thread {

    WWEGeocodePlugin plugin = null;
    String house = "";
    String street = "";
    String city= "";
    String zip = "";
    String country = "";
    
    GeocodeFetcherListener listener = null;

    public GeocodeFetcher(GeocodeFetcherListener listener, String house, String street, String zip, String city, String country, WWEGeocodePlugin p) {
        this.plugin = p;
        this.house = house;
        this.street = street;
        this.city = city;
        this.zip = zip;
        this.country = country;
        this.listener = listener;
    }

    @Override
    public void run() {
        ArrayList<Result> list = plugin.geocode(house, street, zip, city, country);
        if (listener != null) listener.geocodeFetched(list);
    }

    public interface GeocodeFetcherListener {

        public void geocodeFetched(ArrayList<Result> result);
    }
}
