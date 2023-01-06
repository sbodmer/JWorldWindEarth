/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.worldwindearth.trek;

import gov.nasa.worldwind.render.Path;
import java.util.ArrayList;

/**
 *
 * @author sbodmer
 */
public class RteType {
    public ArrayList<WptType> rtept = new ArrayList<>();
    public String name = "";
    
    public Path path = null;
    
    public RteType() {
        
    }
    
    public String toString() {
        return name;
    }
}
