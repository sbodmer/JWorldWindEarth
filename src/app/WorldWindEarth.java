/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import java.io.File;
import org.tinyrcp.AppLauncher;
import org.tinyrcp.JAppLauncher;

/**
 * The layers are resolved in
 * <PRE>
 * {CWD}/lib/ext
 * {HOME}/.WorldWindEarth/plugins
 * </PRE>
 *
 * @author sbodmer
 */
public class WorldWindEarth {

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
                //--- Prepare the loader
                String libs[] = {
                    "lib"+File.separator+"ext",
                    System.getProperty("user.home")+File.separator+".WorldWindEarth"
                        
                };
                
                //--- Prepare default boot splash frame
                JAppLauncher japp = new JAppLauncher(args);
                japp.setAppCustomPanel(new JWWESplash());
                japp.setAppTitle("JWorldWindEarth");
                japp.setVisible(true);
                
                //--- Start boot process
                //--- When the boot has finished, the splash screen should close itself
                AppLauncher.boot(japp, libs, args, "WorldWindEarth");
            }
        });
        
    }

}
