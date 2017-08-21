/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth;

import gov.nasa.worldwind.awt.AWTInputHandler;
import java.awt.event.MouseEvent;

/**
 *
 * @author sbodmer
 */
public class WWEInputHandler extends AWTInputHandler {
    public WWEInputHandler() {
        super();
    }
    
    //**************************************************************************
    //*** MouseListener
    //**************************************************************************
    /**
     * Remove the anoying simple click / double click move
     * 
     * @param mouseEvent 
     */
    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        // System.out.println("CLICKED:"+mouseEvent.getClickCount());
        // if (mouseEvent.getClickCount() == 1) return;
        // super.mouseClicked(mouseEvent);
        callMouseClickedListeners(mouseEvent);
    }
    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        // System.out.println("PRESSED:"+mouseEvent);
        super.mousePressed(mouseEvent);
    }
}
