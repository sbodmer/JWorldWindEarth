/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.worldwindearth.trek;

import java.util.Date;

/**
 *
 * @author sbodmer
 */
public class WptType {

    public double lat = 0d;
    public double lon = 0d;
    public double ele = 0d;
    public Date time = new Date();
    public String name = "";
    
    public WptType(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
        
    }
    
}
