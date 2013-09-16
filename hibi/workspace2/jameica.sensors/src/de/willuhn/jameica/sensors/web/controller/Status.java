/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/web/controller/Status.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/09/13 09:08:34 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.web.controller;

import java.util.HashMap;
import java.util.Map;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.sensors.devices.Device;
import de.willuhn.jameica.sensors.devices.Measurement;
import de.willuhn.jameica.sensors.devices.Sensor;
import de.willuhn.jameica.sensors.devices.Serializer;
import de.willuhn.jameica.sensors.devices.StringSerializer;
import de.willuhn.jameica.sensors.messaging.LimitMessageConsumer;
import de.willuhn.jameica.sensors.messaging.LiveMeasurement;
import de.willuhn.logging.Logger;

/**
 * Controller-Bean fuer einen Sensor-Status.
 */
@Lifecycle(Type.REQUEST)
public class Status
{
  private Map<Class<? extends Serializer>,Serializer> cache = new HashMap<Class<? extends Serializer>,Serializer>();
  
  /**
   * Liefert die Live-Messwerte.
   * @return die Live-Messwerte.
   */
  public Map<Device,Measurement> getMeasurements()
  {
    return LiveMeasurement.getValues();
  }
  
  /**
   * Formatiert den Messwert des Sensors.
   * @param s Sensor.
   * @return Format des Sensors.
   */
  public String format(Sensor s)
  {
    Object value = s.getValue();
    try
    {
      Class c = s.getSerializer();
      // Mal schauen, ob wir den Serializer schon instanziiert haben
      Serializer si = cache.get(c);
      if (si == null)
      {
        si = (Serializer) c.newInstance();
        cache.put(c,si);
      }
      return si.format(value);
    }
    catch (Exception e)
    {
      Logger.error("unable to format value " + value + " for sensor " + s.getName() + " [" + s.getUuid() + "]",e);
    }
    return new StringSerializer().format(value);
  }
  
  /**
   * Prueft, ob der Sensor ausserhalb des Limits ist.
   * @param s der zu pruefende Sensor.
   * @return true, wenn der Sensor ausserhalb des Limits ist.
   */
  public boolean outsideLimit(Sensor s)
  {
    if (s == null)
      return false;
    return LimitMessageConsumer.outsideLimit(s.getUuid());
  }
}


/**********************************************************************
 * $Log: Status.java,v $
 * Revision 1.5  2011/09/13 09:08:34  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.4  2011-06-28 09:56:36  willuhn
 * @N Lifecycle-Annotation aus jameica.webadmin in util verschoben
 *
 * Revision 1.3  2011-02-14 16:04:51  willuhn
 * @N Messwerte hervorheben, die ausserhalb des Limits liegen
 *
 * Revision 1.2  2009-08-21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 * Revision 1.1  2009/08/19 10:34:43  willuhn
 * @N initial import
 *
 * Revision 1.1  2009/08/18 23:00:25  willuhn
 * @N Erste Version mit Web-Frontend
 *
 **********************************************************************/
