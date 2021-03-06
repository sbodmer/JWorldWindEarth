/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nasa.atmosphere;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Draw the trigo circle
 *
 * @author sbodmer
 */
public class JCircle extends javax.swing.JPanel {
    static final BasicStroke STROKE2 = new BasicStroke(2f);
    static final BasicStroke STROKE1 = new BasicStroke(1f);
    double angle = 0;
    
    /**
     * Creates new form JCircle
     */
    public JCircle() {
        initComponents();
    }

    //**************************************************************************
    //*** API
    //**************************************************************************
    /**
     * Set the angle 0-360;
     * 
     * @param degrees 
     */
    public void setAngle(double degrees) {
        this.angle = degrees;
        repaint();
    }
    
    //**************************************************************************
    //*** Swing
    //**************************************************************************
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 =(Graphics2D) g;
        
        g2.drawOval(5, 5, getWidth() - 10, getHeight() - 10);
        g2.setColor(Color.DARK_GRAY);
        g2.drawLine(0, getHeight() / 2, getWidth(), getHeight() / 2);
        // g.drawLine(getWidth()/2, 0, getWidth()/2, getHeight());
        double rad = angle*2*Math.PI/360;
        double y = Math.sin(rad);
        double x = Math.cos(rad);
        
        g2.setStroke(STROKE2);
        g2.setColor(Color.BLUE);
        int cx = getWidth()/2;
        int cy = getHeight()/2;
        g2.drawLine(cx, cy, (int) (cx+(x*20)), (int) (cy- (y*20)));
        g2.setColor(Color.BLACK);
        g2.setStroke(STROKE1);
        
    }

    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
