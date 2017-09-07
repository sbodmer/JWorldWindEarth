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
public class GazetteerFetcher extends Thread {

    WWEGazetteerPlugin plugin = null;
    String string = "";
    
    GazetteerFetcherListener listener = null;

    public GazetteerFetcher(GazetteerFetcherListener listener, String string, WWEGazetteerPlugin p) {
        this.plugin = p;
        this.string = string;
        this.listener = listener;
    }

    @Override
    public void run() {
        ArrayList<Result> list = plugin.findPlaces(string);
        if (listener != null) listener.gazetteerFetched(list);
    }

    public interface GazetteerFetcherListener {

        public void gazetteerFetched(ArrayList<Result> result);
    }
}
