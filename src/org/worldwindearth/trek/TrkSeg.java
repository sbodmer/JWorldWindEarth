/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.trek;

import gov.nasa.worldwind.render.Path;
import java.util.ArrayList;
import org.w3c.dom.Element;

/**
 *
 * @author fbrun
 */
public class TrkSeg {

    public int segment = 0;
    public ArrayList<WptType> trkpt = new ArrayList<>();
    
    /**
     * WW path to render
     */
    public Path path = null;
    
    public TrkSeg(int segment) {
        this.segment = segment;
    }
    
    public String toString() {
        return ""+segment;
    }
}
