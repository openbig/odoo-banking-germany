/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/devices/DateSerializer.java,v $
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

import java.text.DateFormat;
import java.text.ParseException;

import de.willuhn.jameica.system.Application;

/**
 * Serializer fuer ein Datums-Objekt.
 */
public class DateSerializer implements Serializer
{
  private static DateFormat DF = DateFormat.getDateTimeInstance(DateFormat.FULL,DateFormat.LONG,Application.getConfig().getLocale());
  
  /**
   * @see de.willuhn.jameica.sensors.devices.Serializer#format(java.lang.Object)
   */
  public String format(Object value)
  {
    return value == null ? "-" : DF.format(value);
  }

  /**
   * @see de.willuhn.jameica.sensors.devices.Serializer#serialize(java.lang.Object)
   */
  public String serialize(Object o)
  {
    return o == null ? null : DF.format(o);
  }

  /**
   * @see de.willuhn.jameica.sensors.devices.Serializer#unserialize(java.lang.String)
   */
  public Object unserialize(String s) throws IllegalArgumentException
  {
    try
    {
      return DF.parse(s);
    }
    catch (ParseException e)
    {
      throw new IllegalArgumentException(e);
    }
  }

}


/**********************************************************************
 * $Log: DateSerializer.java,v $
 * Revision 1.1  2009/08/21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 **********************************************************************/
