package gov.nasa.skygradient;

import gov.nasa.worldwind.geom.LatLon;

/**
 * @author Michael de Hoog
 * @version $Id$
 */
public interface SunPositionProvider
{
	public LatLon getPosition();
}
