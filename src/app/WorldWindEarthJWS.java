/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import java.util.ArrayList;
import org.tinyrcp.AppLauncher;
import org.tinyrcp.JAppLauncher;

/**
 * The Java web start version which will not load dynamically the plugins, but
 * will have a fixed list of factories to create<p>
 *
 * @author sbodmer
 */
public class WorldWindEarthJWS {

    static final String ADITIONAL_ARGUMENTS[] = {
        "-factory", "org.worldwindearth.earth.JEarthFactory",
        "-factory", "org.worldwindearth.flatearth.JFlatEarthFactory",
        "-factory", "gov.nasa.skygradient.JSkyGradientWWEFactory",
        "-factory", "gov.nasa.compass.JCompassWWEFactory",
        "-factory", "gov.nasa.bmng.JBMNGWWEFactory",
        "-factory", "gov.nasa.stars.JStarsWWEFactory",
        "-main", "org.worldwindearth.JWorldWindEarth"
    };

    /**
     * @param args the command line arguments
     */
    public static void main(final String args[]) {

        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(JAppLauncher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(JAppLauncher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(JAppLauncher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(JAppLauncher.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                //--- Prepare default boot splash frame
                JAppLauncher japp = new JAppLauncher(args);
                japp.setVisible(true);

                //--- Start manual boot process, no dynamic loading
                String targs[] = new String[args.length+ADITIONAL_ARGUMENTS.length];
                for (int i = 0; i < args.length; i++) targs[i] = args[i];
                for (int i = 0; i < ADITIONAL_ARGUMENTS.length; i++) targs[args.length+i] = ADITIONAL_ARGUMENTS[i];
                
                AppLauncher.boot(japp, new String[0], targs, "WorldWindEarth");
            }
        });

    }

}
