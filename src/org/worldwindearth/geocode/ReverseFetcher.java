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
public class ReverseFetcher extends Thread {

    WWEGeocodePlugin plugin = null;
    Position pos = null;
    ReverseFetcherListener listener = null;

    public ReverseFetcher(ReverseFetcherListener listener, Position pos, WWEGeocodePlugin p) {
        this.plugin = p;
        this.pos = pos;
        this.listener = listener;
    }

    @Override
    public void run() {
        ArrayList<Result> list = plugin.reverse(pos);
        if (listener != null) listener.reverseFetched(list);
    }

    public interface ReverseFetcherListener {

        public void reverseFetched(ArrayList<Result> result);
    }
}
