/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.components.layers.buildings;

/**
 *
 * @author sbodmer
 */
public class BuildingsProvider {

    String url = "";
    String title = "";
    int minLevel = 15;
    int maxLevel = 15;

    public BuildingsProvider() {
        //---
    }
    public BuildingsProvider(String title, String url, int minLevel, int maxLevel) {
        this.title = title;
        this.url = url;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    public int getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }
    
    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }
    
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public String toString() {
        return title;
    }
}
