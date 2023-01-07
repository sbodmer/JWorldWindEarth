/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.firstperson.BasicFlyView;

/**
 *
 * @author sbodmer
 */
public class BasicWalkFlyView extends BasicFlyView {
    public BasicWalkFlyView() {
        super();
        // this.viewLimits.setEyeElevationLimits(5, 5);
        
    }
    @Override
    public Position getCurrentEyePosition() {
        //--- Limit the altitude to ground level
        Position eye = super.getCurrentEyePosition();
        double ele = getGlobe().getElevation(eye.latitude, eye.longitude);
        this.viewLimits.setEyeElevationLimits(ele+5, ele+5);
        return super.getCurrentEyePosition();
    }
}
