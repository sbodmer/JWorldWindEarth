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
public interface BuildingsTileListener {
    public void buildingLoaded(BuildingsTile btile);
    /**
     * If current and total is -1 then the loading process just started
     * @param btile
     * @param current
     * @param total 
     */
    public void buildingLoading(BuildingsTile btile, long current, long total);
    public void buildingLoadingFailed(BuildingsTile btile, String reason);
    
    /**
     * WHen the layer needs to clear some tile
     * @param btile 
     */
    public void buildingRemoved(BuildingsTile btile);
    
}
