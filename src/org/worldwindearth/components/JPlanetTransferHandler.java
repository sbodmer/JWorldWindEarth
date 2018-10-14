/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.components;

import java.awt.datatransfer.Transferable;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.TransferHandler;

/**
 *
 * @author sbodmer
 */
public class JPlanetTransferHandler extends TransferHandler {

    JPlanet dest = null;

    /**
     * The passed object is the planet where the drop should be linked (destination
     * object)
     * 
     * @param dest 
     */
    public JPlanetTransferHandler(JPlanet dest) {
        super();

        this.dest = dest;
    }

    //**************************************************************************
    //*** TransferHandler
    //**************************************************************************
    @Override
    public int getSourceActions(JComponent jcomp) {
        return LINK;
    }

    @Override
    public Transferable createTransferable(JComponent jcomp) {
        if (jcomp instanceof JButton) {
            JButton jb = (JButton) jcomp;
            JPlanet jplanet = (JPlanet) jb.getClientProperty("jplanet");
            return jplanet;
        }
        return null;
    }

    @Override
    public void exportDone(JComponent c, Transferable t, int action) {
        if (action == LINK) {

        }
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        if (support.isDataFlavorSupported(JPlanet.DATAFLAVOR_JPLANET)) {
            try {
                Transferable th = support.getTransferable();
                JPlanet jplanet = (JPlanet) th.getTransferData(JPlanet.DATAFLAVOR_JPLANET);
                //--- Do not link same source and dest
                if (jplanet == dest) return false;
                
                return true;
            } catch (Exception ex) {

            }
        }
        return false;
    }

    /**
     * The passed support transferable is the source JPlanet which started
     * the drag and drop
     * 
     * @param support
     * @return 
     */
    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        try {
            Transferable th = support.getTransferable();
            //--- The source of the drag and drop
            JPlanet jplanet = (JPlanet) th.getTransferData(JPlanet.DATAFLAVOR_JPLANET);
            jplanet.addAttachedPlanet(dest);
            dest.getWorldWindowGLJPanel().redraw();
            dest.setParentPlanet(jplanet);
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

}
