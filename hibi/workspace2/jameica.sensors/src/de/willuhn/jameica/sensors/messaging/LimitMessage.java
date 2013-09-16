/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/messaging/LimitMessage.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/02/14 16:04:51 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.messaging;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.sensors.devices.Sensor;

/**
 * Wird verschickt, wenn ein Sensor das Limit verlaesst oder ins Limit zurueckgekehrt ist.
 */
public class LimitMessage implements Message
{
  private Sensor sensor   = null;
  private boolean outside = false;
  
  /**
   * ct.
   * @param sensor
   * @param outside true, wenn der Sensor ausserhalb des Limits ist.
   */
  public LimitMessage(Sensor sensor, boolean outside)
  {
    this.sensor  = sensor;
    this.outside = outside;
  }

  /**
   * Liefert den Sensor.
   * @return sensor
   */
  public Sensor getSensor()
  {
    return sensor;
  }
  
  /**
   * Liefert true, wenn des Sensor ausserhalb des Limits ist.
   * @return true, wenn des Sensor ausserhalb des Limits ist.
   */
  public boolean isOutside()
  {
    return this.outside;
  }

}



/**********************************************************************
 * $Log: LimitMessage.java,v $
 * Revision 1.1  2011/02/14 16:04:51  willuhn
 * @N Messwerte hervorheben, die ausserhalb des Limits liegen
 *
 **********************************************************************/