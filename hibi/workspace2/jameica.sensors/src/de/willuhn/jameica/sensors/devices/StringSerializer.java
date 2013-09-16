/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/devices/StringSerializer.java,v $
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


/**
 * Default-Serializer, der davon ausgeht, dass es sich bei den Werten um Strings handelt.
 */
public class StringSerializer implements Serializer
{

  /**
   * @see de.willuhn.jameica.sensors.devices.Serializer#serialize(java.lang.Object)
   */
  public String serialize(Object o)
  {
    return o == null ? null : o.toString();
  }

  /**
   * @see de.willuhn.jameica.sensors.devices.Serializer#unserialize(java.lang.String)
   */
  public Object unserialize(String s)
  {
    return s;
  }

  /**
   * @see de.willuhn.jameica.sensors.devices.Serializer#format(java.lang.Object)
   */
  public String format(Object o)
  {
    return o == null ? "" : o.toString();
  }

}


/**********************************************************************
 * $Log: StringSerializer.java,v $
 * Revision 1.1  2009/08/21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 **********************************************************************/
