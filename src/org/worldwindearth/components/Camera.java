/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.components;

/**
 *
 * @author sbodmer
 */
public class Camera {
    String title = "N/A";
    
    public Camera(String title) {
        this.title = title;
    }
    
    public String toString() {
        return title;
    }
    
    public String getTitle() {
        return title;
    }
}
