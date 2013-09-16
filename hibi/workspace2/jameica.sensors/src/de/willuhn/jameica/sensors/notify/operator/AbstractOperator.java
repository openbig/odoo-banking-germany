/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/notify/operator/AbstractOperator.java,v $
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
import de.willuhn.jameica.sensors.devices.Serializer;
import de.willuhn.jameica.sensors.devices.StringSerializer;

/**
 * Abstrakte Basis-Klasse der Operatoren.
 */
public abstract class AbstractOperator implements Operator
{
  /**
   * Bereitet das Limit so vor, dass es verglichen werden kann.
   * @param serializer der Serializer des Sensors.
   * @param limit das Limit laut Regel.
   * @return das deserialisierte Limit.
   * @throws IllegalArgumentException
   */
  Comparable prepareLimit(Class<? extends Serializer> serializer, String limit) throws IllegalArgumentException
  {
    if (limit == null || limit.trim().length() == 0)
      throw new IllegalArgumentException("no limit given");

    if (serializer == null)
      serializer = StringSerializer.class;
    
    try
    {
      Serializer s = serializer.newInstance();
      return prepareSensor(s.unserialize(limit.trim()));
    }
    catch (Exception e)
    {
      throw new IllegalArgumentException("unable to load serializer",e);
    }
  }
  
  /**
   * Bereitet den Messwert so vor, dass er verglichen werden kann.
   * @param value der Messwert.
   * @return der vergleichbare Messwert.
   */
  Comparable prepareSensor(Object value)
  {
    if (value == null)
      return null;
    
    if (value instanceof Number)
      return new Double(((Number)value).doubleValue());
    
    if (value instanceof Comparable)
      return (Comparable) value;
    
    return value.toString();
  }
  
  /**
   * Prueft die Parameter auf Gueltigkeit.
   * @param sensor der Sensor.
   * @param limit das Limit.
   * @throws IllegalArgumentException
   */
  void check(Sensor sensor, String limit) throws IllegalArgumentException
  {
    if (sensor == null)
      throw new IllegalArgumentException("no sensor given");

    if (limit == null || limit.trim().length() == 0)
      throw new IllegalArgumentException("no limit given");

    Object value = sensor.getValue();
    if (value == null)
      throw new IllegalArgumentException("sensor has no value");
  }
}



/**********************************************************************
 * $Log: AbstractOperator.java,v $
 * Revision 1.1  2011/02/18 12:29:41  willuhn
 * @N Regel-Operatoren umgebaut. Es gibt jetzt auch einen "Outside"-Operator mit dessen Hilfe eine Unter- UND Obergrenze in EINER Regel definiert werden kann
 *
 **********************************************************************/