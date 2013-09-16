/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/notify/operator/GreaterThan.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/02/18 12:29:41 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.notify.operator;

import de.willuhn.jameica.sensors.devices.Sensor;


/**
 * Implementierung eines Operators, der dann ausloest, wenn
 * der Messwert das Limit ueberschritten hat.
 */
public class GreaterThan extends AbstractOperator
{
  /**
   * @see de.willuhn.jameica.sensors.notify.operator.Operator#matches(de.willuhn.jameica.sensors.devices.Sensor, java.lang.String)
   */
  public boolean matches(Sensor sensor, String limit) throws IllegalArgumentException
  {
    check(sensor,limit); // Gatekeeper

    Comparable cValue = prepareSensor(sensor.getValue());
    Comparable cLimit = prepareLimit(sensor.getSerializer(),limit);
    
    return cValue.compareTo(cLimit) > 0;
  }

}



/**********************************************************************
 * $Log: GreaterThan.java,v $
 * Revision 1.2  2011/02/18 12:29:41  willuhn
 * @N Regel-Operatoren umgebaut. Es gibt jetzt auch einen "Outside"-Operator mit dessen Hilfe eine Unter- UND Obergrenze in EINER Regel definiert werden kann
 *
 * Revision 1.1  2010/03/01 18:12:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2010/03/01 13:16:12  willuhn
 * @N Erster Code fuer automatische Benachrichtigungen bei Limit-Ueberschreitungen von Sensoren.
 *
 **********************************************************************/