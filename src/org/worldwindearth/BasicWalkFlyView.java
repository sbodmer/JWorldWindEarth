/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth;

import gov.nasa.worldwind.view.firstperson.BasicFlyView;

/**
 *
 * @author sbodmer
 */
public class BasicWalkFlyView extends BasicFlyView {
    public BasicWalkFlyView() {
        super();
        
        this.viewLimits.setEyeElevationLimits(5, 5);

    }
}
