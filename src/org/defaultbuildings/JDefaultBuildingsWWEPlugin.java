/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.defaultbuildings;

import gov.nasa.worldwind.WorldWindow;
import javax.swing.DefaultComboBoxModel;
import org.worldwindearth.WWEFactory;
import org.worldwindearth.components.layers.buildings.JAbstractBuildingsWWEPlugin;

/**
 *
 * @author sbodmer
 */
public class JDefaultBuildingsWWEPlugin extends JAbstractBuildingsWWEPlugin {
    public JDefaultBuildingsWWEPlugin(WWEFactory factory, WorldWindow ww, DefaultComboBoxModel<org.worldwindearth.components.layers.buildings.BuildingsProvider> list) {
        super(factory, ww, list);
        
        configPath = "/org/defaultbuildings/Resources/Config/DefaultBuildings.xml";
        
    }
}
