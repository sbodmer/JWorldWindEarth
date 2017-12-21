/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.AWTInputHandler;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.pick.PickedObjectList;
import java.awt.event.KeyEvent;
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

        //--- Check if an object was picked
        PickedObjectList pickedObjects = this.wwd.getObjectsAtCurrentPosition();

        // this.callMouseClickedListeners(mouseEvent);
        if ((pickedObjects != null)
                && (pickedObjects.getTopPickedObject() != null)
                && (!pickedObjects.getTopPickedObject().isTerrain())) {
            // Something is under the cursor, so it's deemed "selected".
            if (MouseEvent.BUTTON1 == mouseEvent.getButton()) {
                if (mouseEvent.getClickCount() <= 1) {
                    callSelectListeners(new SelectEvent(this.wwd, SelectEvent.LEFT_CLICK, mouseEvent, pickedObjects));

                } else {
                    callSelectListeners(new SelectEvent(this.wwd, SelectEvent.LEFT_DOUBLE_CLICK, mouseEvent, pickedObjects));
                }

            } else if (MouseEvent.BUTTON3 == mouseEvent.getButton()) {
                callSelectListeners(new SelectEvent(this.wwd, SelectEvent.RIGHT_CLICK, mouseEvent, pickedObjects));
            }

            wwd.getView().firePropertyChange(AVKey.VIEW, null, wwd.getView());

        } else {
            //--- Not an object click, forward to listener
            //--- Do not call parent to avoid the double click move
            callMouseClickedListeners(mouseEvent);
        }
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        // System.out.println("PRESSED:"+mouseEvent);
        super.mousePressed(mouseEvent);
    }

}
