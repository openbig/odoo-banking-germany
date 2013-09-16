/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/messaging/LiveMeasurement.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/08/21 13:34:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.messaging;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.sensors.devices.Device;
import de.willuhn.jameica.sensors.devices.Measurement;

/**
 * Message-Consumer, der die aktuellen Live-Messwerte bereithaelt.
 */
public class LiveMeasurement implements MessageConsumer
{
  private final static Map<Device,Measurement> liveValues = new HashMap<Device,Measurement>();

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{MeasureMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    MeasureMessage msg = (MeasureMessage) message;
    liveValues.put(msg.getDevice(),msg.getMeasurement());
  }

  /**
   * Liefert eine Map mit den aktuellen Messwerten aller Geraete.
   * @return aktuelle Messwerte aller Geraete.
   */
  public static Map<Device,Measurement> getValues()
  {
    return Collections.unmodifiableMap(liveValues);
  }
  

}


/**********************************************************************
 * $Log: LiveMeasurement.java,v $
 * Revision 1.2  2009/08/21 13:34:17  willuhn
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
