/*
 * Copyright 2006-2009, 2017, 2020 United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 * 
 * The NASA World Wind Java (WWJ) platform is licensed under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 * 
 * NASA World Wind Java (WWJ) also contains the following 3rd party Open Source
 * software:
 * 
 *     Jackson Parser – Licensed under Apache 2.0
 *     GDAL – Licensed under MIT
 *     JOGL – Licensed under  Berkeley Software Distribution (BSD)
 *     Gluegen – Licensed under Berkeley Software Distribution (BSD)
 * 
 * A complete listing of 3rd Party software notices and licenses included in
 * NASA World Wind Java (WWJ)  can be found in the WorldWindJava-v2.2 3rd-party
 * notices and licenses PDF found in code directory.
 */

package gov.nasa.worldwindx.applications.worldwindow.features;

import gov.nasa.worldwindx.applications.worldwindow.core.*;
import gov.nasa.worldwindx.applications.worldwindow.util.Util;

import java.awt.event.*;

/**
 * @author tag
 * @version $Id: Measurement.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class Measurement extends AbstractFeature
{
    private WWODialog dialog;

    public Measurement()
    {
        this(null);
    }

    public Measurement(Registry registry)
    {
        super("Measurement", Constants.FEATURE_MEASUREMENT,
            "gov/nasa/worldwindx/applications/worldwindow/images/globe-sextant-64x64.png", registry);
        setEnabled(true);
    }

    @Override
    public void initialize(Controller controller)
    {
        super.initialize(controller);

        this.addToToolBar();
    }

    @Override
    public boolean isTwoState()
    {
        return true;
    }

    public boolean isOn()
    {
        return this.dialog != null && this.dialog.getJDialog().isVisible();
    }

    @Override
    public void turnOn(boolean tf)
    {
        if (this.dialog != null)
            this.dialog.setVisible(tf);
    }

    @Override
    protected void doActionPerformed(ActionEvent actionEvent)
    {
        if (this.dialog == null)
            this.dialog = (WWODialog) this.controller.getRegisteredObject(Constants.FEATURE_MEASUREMENT_DIALOG);
        if (this.dialog == null)
        {
            Util.getLogger().severe("Measurement dialog not registered");
            return;
        }

        this.turnOn(!this.isOn());
    }
}
