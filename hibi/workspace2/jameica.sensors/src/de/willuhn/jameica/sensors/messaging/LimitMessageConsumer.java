/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/messaging/LimitMessageConsumer.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/02/17 23:47:56 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.messaging;

import java.util.HashMap;
import java.util.Map;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.logging.Logger;

/**
 * Wird ueber die Limit-Ueberschreitungen von Sensoren benachrichtigt.
 */
public class LimitMessageConsumer implements MessageConsumer
{
  private final static Map<String,String> map = new HashMap<String,String>();
  
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
    return new Class[]{LimitMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    LimitMessage msg = (LimitMessage) message;
    String uuid = msg.getSensor().getUuid();
    if (uuid == null)
    {
      Logger.warn("sensor has no uuid");
      return;
    }
    
    if (msg.isOutside())
      map.put(uuid,uuid);
    else
      map.remove(uuid);
  }
  
  /**
   * Prueft, ob der Sensor ausserhalb des Limits ist.
   * @param uuid die zu pruefende Sensor-UUID.
   * @return true, wenn er ausserhalb des Limits ist.
   */
  public static boolean outsideLimit(String uuid)
  {
    return map.get(uuid) != null;
  }

}



/**********************************************************************
 * $Log: LimitMessageConsumer.java,v $
 * Revision 1.3  2011/02/17 23:47:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2011-02-17 22:38:51  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2011-02-14 16:04:51  willuhn
 * @N Messwerte hervorheben, die ausserhalb des Limits liegen
 *
 **********************************************************************/