/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/devices/Measurement.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/08/21 13:34:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.devices;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Basis-Klasse einer einzelnen Messung.
 */
public class Measurement
{
  private List<Sensorgroup> groups = null;
  private Date date = null;
  
  /**
   * Liefert die Liste der Sensor-Gruppen.
   * @return Liste der Sensor-Gruppen.
   */
  public List<Sensorgroup> getSensorgroups()
  {
    if (this.groups == null)
      this.groups = new ArrayList<Sensorgroup>();
    return this.groups;
  }
  
  /**
   * Liefert den Zeitpunkt der Messung.
   * @return Zeitpunkt der Messung.
   */
  public Date getDate()
  {
    return this.date;
  }
  
  /**
   * Speichert das Datum der Messung.
   * @param d Datum der Messung.
   */
  public void setDate(Date d)
  {
    this.date = d;
  }
}


/**********************************************************************
 * $Log: Measurement.java,v $
 * Revision 1.1  2009/08/21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 **********************************************************************/
