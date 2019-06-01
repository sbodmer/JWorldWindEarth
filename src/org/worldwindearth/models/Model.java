/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.models;

import gov.nasa.worldwind.render.Renderable;
import java.io.File;

/**
 *
 * @author sbodmer
 */
public class Model {

    private double lat = 0d;
    private double lon = 0d;
    private double alt = 0d;
    private double azimuth = 0.0d;
    private double elevation = 0.0d;
    private double roll = 0.0d;
    private double scale = 1.0d;
    
    private boolean visible = true;

    private File file = null;
    private Renderable renderable = null;
    private String title = "";
    
    public Model(File f) {
        this.file = f;
        
    }
    
    public Model(double lat, double lon, double alt, File f) {
        this.lat = lat;
        this.lon = lon;
        this.alt = alt;
        this.file = f;
        title = f.getName();
    }

    @Override
    public String toString() {
        return title + " (" + lat + "," + lon + "," + alt + ")";
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isVisible() {
        return visible;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setLatitude(double lat) {
        this.lat = lat;
    }
    public double getLatitude() {
        return lat;
    }
    
    public void setLongitude(double lon) {
        this.lon = lon;
    }
    
    public double getLongitude() {
        return lon;
    }
    
    public void setAltitude(double height) {
        this.alt = height;
    }
    
    public double getAltitude() {
        return alt;
    }
    
    public void setAzimuth(double h) {
        this.azimuth = h;
    }
    public double getAzimuth() {
        return azimuth;
    }
    
    public void setElevation(double p) {
        this.elevation = p;
    }
    
    public double getElevation() {
        return elevation;
    }
    
    public void setRoll(double r){
        this.roll = r;
    }
    
    public double getRoll()  {
        return roll;
    }
    
    public void setScale(double scale) {
        this.scale = scale;
    }
    
    public double getScale() {
        return scale;
    }
    public File getFile() { 
        return file;
    }
    
    public Renderable getRenderable() {
        return renderable;
    }
    
    public void setRenderable(Renderable renderable) {
        this.renderable = renderable;
    }
}
