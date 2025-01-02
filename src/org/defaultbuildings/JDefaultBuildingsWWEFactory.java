/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.defaultbuildings;

import gov.nasa.worldwind.WorldWindow;
import java.util.ResourceBundle;
import javax.swing.table.DefaultTableModel;
import org.tinyrcp.TinyPlugin;
import org.worldwindearth.components.layers.buildings.BuildingsProvider;
import org.worldwindearth.components.layers.buildings.JAbstractBuildingsWWEFactory;

/**
 *
 * @author sbodmer
 */
public class JDefaultBuildingsWWEFactory extends JAbstractBuildingsWWEFactory {

    public JDefaultBuildingsWWEFactory() {
        super();
        ResourceBundle bundle = ResourceBundle.getBundle("org/defaultbuildings/DefaultBuildings");

        LB_Name.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/defaultbuildings/Resources/Icons/22x22/Buildings.png"))); // NOI18N
        LB_Name.setText(bundle.getString("factory_name"));

        BuildingsProvider pr = new BuildingsProvider();
        pr.setTitle("www.les-studios-inexistants.ch");
        pr.setUrl("file:///usr/share/worldwindearth/buildings/tile/${Z}/${X}/${Y}/buildings.xml");
        pr.setMaxLevel(15);
        pr.setMinLevel(15);
        list.addElement(pr);

        DefaultTableModel model = (DefaultTableModel) TB_Providers.getModel();
        Object obj[] = {pr.getTitle(), pr.getUrl(), pr.getMinLevel(), pr.getMaxLevel()};
        model.addRow(obj);

    }

    @Override
    protected String getCopyrightText() {
        return "© right owners";
    }

    @Override
    public TinyPlugin newPlugin(Object o) {
        return new JDefaultBuildingsWWEPlugin(this, (WorldWindow) o, list);
    }

}
