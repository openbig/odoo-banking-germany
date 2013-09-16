/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/notify/operator/Outside.java,v $
 * $Revision: 1.1 $
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
 * der Messwert ausserhalb des definierten Bereiches ist.
 * Der Bereich muss im Format "von:bis" angegeben sein.
 * Also z.Bsp. "-20:40".
 */
public class Outside extends AbstractOperator
{
  /**
   * @see de.willuhn.jameica.sensors.notify.operator.Operator#matches(de.willuhn.jameica.sensors.devices.Sensor, java.lang.String)
   */
  public boolean matches(Sensor sensor, String limit) throws IllegalArgumentException
  {
    check(sensor,limit); // Gatekeeper
    
    Comparable cValue = prepareSensor(sensor.getValue());
    
    String s = limit.toString();
    String[] limits = s.split(":");
    if (limits == null || limits.length != 2)
      throw new IllegalArgumentException("invalid limit definition: " + s + ", needed format: \"$from:$to\" (example: -20:50)");

    Comparable cLimitFrom = prepareLimit(sensor.getSerializer(),limits[0]);
    Comparable cLimitTo   = prepareLimit(sensor.getSerializer(),limits[1]);
    
    // Wir sind "outside", wenn
    // a) cValue < cLimitFrom ODER
    // b) cValue > cLimitTo
    return cValue.compareTo(cLimitFrom) < 0 || cValue.compareTo(cLimitTo) > 0;
  }
}



/**********************************************************************
 * $Log: Outside.java,v $
 * Revision 1.1  2011/02/18 12:29:41  willuhn
 * @N Regel-Operatoren umgebaut. Es gibt jetzt auch einen "Outside"-Operator mit dessen Hilfe eine Unter- UND Obergrenze in EINER Regel definiert werden kann
 *
 **********************************************************************/