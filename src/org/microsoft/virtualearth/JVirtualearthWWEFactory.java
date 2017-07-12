package org.microsoft.virtualearth;

import gov.nasa.worldwind.WorldWindow;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.tinyrcp.App;
import org.tinyrcp.TinyPlugin;
import org.w3c.dom.Element;
import org.worldwindearth.WWEFactory;

/**
 *
 * @author sbodmer
 */
public class JVirtualearthWWEFactory extends JPanel implements WWEFactory {
    public static final String LICENCE_TEXT = "© Microsoft";
    
    ResourceBundle bundle = null;
    App app = null;

    /**
     * 
     */
    public JVirtualearthWWEFactory() {
        bundle = ResourceBundle.getBundle("org.microsoft.virtualearth.Virtualearth");

        initComponents();

        // LB_Name.setText(bundle.getString("text_name"));
        // LB_Description.setText(bundle.getString("text_description"));
    }
    //***************************************************************************
    //*** TinyFactory
    //***************************************************************************
    @Override
    public String getFactoryCategory() {
        return PLUGIN_CATEGORY_WORLDWIND_LAYER;
    }

    @Override
    public JComponent getFactoryConfigComponent() {
        return null;
    }
    
    @Override
    public void initialize(App app) {
        this.app = app;

    }

    @Override
    public String getFactoryDescription() {
        return LB_Description.getText();
    }

    @Override
    public void configure(Element config) {
        //---
    }

    @Override
    public Icon getFactoryIcon(int size) {
        return LB_Name.getIcon();
    }

    @Override
    public String getFactoryName() {
        return LB_Name.getText();
    }

    /**
     * The pass argument is the WorldWindow
     * @param arg
     * @return 
     */
    @Override
    public TinyPlugin newPlugin(Object arg) {
        return new JVirtualearthWWEPlugin(this, (WorldWindow) arg);
        
    }

    @Override
    public Object getProperty(String property) {
        if (property.equals(PROPERTY_LICENCE_TEXT)) return LICENCE_TEXT;
        return null;
    }

    @Override
    public String getFactoryFamily() {
        return PLUGIN_FAMILY_WORLDWIND_LAYER_MAPTILES;
    }

    @Override
    public void store(Element config) {
        //---
    }

    @Override
    public void destroy() {
        //---
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        LB_Name = new javax.swing.JLabel();
        LB_Description = new javax.swing.JLabel();

        LB_Name.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/microsoft/virtualearth/Resources/Icons/22x22/virtualearth.png"))); // NOI18N
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/microsoft/virtualearth/Virtualearth"); // NOI18N
        LB_Name.setText(bundle.getString("factory_name")); // NOI18N

        LB_Description.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        LB_Description.setText("<html><body>\nMicrosoft Virtualearth\n</body></html>");
        LB_Description.setVerticalAlignment(javax.swing.SwingConstants.TOP);

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
    public javax.swing.JLabel LB_Description;
    public javax.swing.JLabel LB_Name;
    // End of variables declaration//GEN-END:variables

    
}
