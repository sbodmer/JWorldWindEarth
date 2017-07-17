/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.worldwindearth.components;

import com.jogamp.opengl.util.awt.AWTGLReadBufferUtil;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.event.RenderingListener;
import gov.nasa.worldwind.geom.Position;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.media.opengl.GLAutoDrawable;

/**
 *
 * @author sbodmer
 */
public class Camera implements RenderingListener {

    String title = "N/A";
    WorldWindow wwd = null;
    Image thumbnail = null;
    Position eye = null;
    
    /**
     * A screenshot will be generated for the next GL rendering pass
     * 
     * @param title
     * @param wwd 
     */
    public Camera(String title, WorldWindow wwd) {
        this.title = title;
        this.wwd = wwd;
        
        eye = wwd.getView().getCurrentEyePosition();
        
        //--- For the screenshot
        wwd.addRenderingListener(this);
    }

    public Camera(String title, Position eye, Image thumbnail) {
        this.title = title;
        this.eye = eye;
        this.thumbnail = thumbnail;
    }
    
    //**************************************************************************
    //*** API
    //**************************************************************************
    /**
     * Set the title, the position and a new screenshot will be generated
     * 
     * @param title 
     */
    public void set(String title, WorldWindow wwd) {
        this.title = title;
        this.wwd = wwd;
        eye = wwd.getView().getCurrentEyePosition();
        
        //--- For the screenshot
        wwd.addRenderingListener(this);
    }
    
    @Override
    public String toString() {
        return title;
    }

    public String getTitle() {
        return title;
    }

    public Image getThumbnail() {
        return thumbnail;
    }
    
    public Position getEyePosition() {
        return eye;
    }
    
    //**************************************************************************
    //*** Rendering Listener
    //**************************************************************************
    @Override
    public void stageChanged(RenderingEvent event) {
        if (event.getStage().equals(RenderingEvent.AFTER_BUFFER_SWAP)) {
            try {
                GLAutoDrawable glad = (GLAutoDrawable) event.getSource();
                AWTGLReadBufferUtil glReadBufferUtil = new AWTGLReadBufferUtil(glad.getGLProfile(), false);
                BufferedImage image = glReadBufferUtil.readPixelsToBufferedImage(glad.getGL(), true);
                thumbnail = image.getScaledInstance(128, 84, Image.SCALE_SMOOTH);
                
                /*
                String suffix = WWIO.getSuffix(this.snapFile.getPath());
                ImageIO.write(image, suffix, this.snapFile);
                System.out.printf("Image saved to file %s\n", this.snapFile.getPath());
                */
                    
            } finally {
                this.wwd.removeRenderingListener(this);
            }
        }
    }
}
