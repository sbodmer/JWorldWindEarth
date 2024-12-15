/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.AWTInputHandler;
import gov.nasa.worldwind.event.DragSelectEvent;
import gov.nasa.worldwind.event.SelectEvent;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.pick.PickedObjectList;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

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
     * Remove the annoying simple click / double click move
     *
     * @param mouseEvent
     */
    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        // System.out.println("CLICKED:"+mouseEvent.getClickCount());
        // if (mouseEvent.getClickCount() == 1) return;
        // super.mouseClicked(mouseEvent);
        // callMouseClickedListeners(mouseEvent);

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
        super.mousePressed(mouseEvent);
    }

    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
        Position pos = wwd.getCurrentPosition();
        callMouseMovedListeners(mouseWheelEvent);

        Position eye = wwd.getView().getCurrentEyePosition();
        // System.out.println("POS:"+pos+" EYE:"+eye);
        // super.mouseWheelMoved(mouseWheelEvent);
        /*
        BasicOrbitView view = (BasicOrbitView) wwd.getView();
        Vec4 fw = view.getCurrentForwardVector();
        System.out.println("fw:"+fw);
         */
        // view.addPanToAnimator(pos, Angle.fromDegrees(pos.getHeading()), Angle.fromDegrees(c.getPitch()), c.getZoom());
        // this.wwd.getView().getViewInputHandler().mouseWheelMoved(mouseWheelEvent);
        super.mouseWheelMoved(mouseWheelEvent);

        /*
        if (this.wwd == null)
        {
            return;
        }

        if (mouseWheelEvent == null)
        {
            return;
        }

        this.callMouseWheelMovedListeners(mouseWheelEvent);

        if (!mouseWheelEvent.isConsumed())
            this.wwd.getView().getViewInputHandler().mouseWheelMoved(mouseWheelEvent);
         */
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        super.mouseDragged(mouseEvent);
    }
}
