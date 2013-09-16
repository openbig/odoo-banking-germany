/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/web/controller/Charts.java,v $
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

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;

/**
 * Controller-Bean fuer die Charts.
 */
@Lifecycle(Type.REQUEST)
public class Charts
{
  /**
   * Liefert den UNIX-Timestamp von vor 1 Stunde.
   * @return den UNIX-Timestamp von vor 1 Stunde.
   */
  public String getHour()
  {
    long now = System.currentTimeMillis() / 1000L;
    now -= (60 * 60);
    return String.valueOf(now);
  }
  
  /**
   * Liefert den UNIX-Timestamp von vor 1 Tag.
   * @return den UNIX-Timestamp von vor 1 Tag.
   */
  public String getDay()
  {
    long now = System.currentTimeMillis() / 1000L;
    now -= (60 * 60 * 24);
    return String.valueOf(now);
  }
  
  /**
   * Liefert den UNIX-Timestamp von vor 1 Woche.
   * @return den UNIX-Timestamp von vor 1 Woche.
   */
  public String getWeek()
  {
    long now = System.currentTimeMillis() / 1000L;
    now -= (60 * 60 * 24 * 7);
    return String.valueOf(now);
  }
  
  /**
   * Liefert den UNIX-Timestamp von vor 1 Monat.
   * @return den UNIX-Timestamp von vor 1 Monat.
   */
  public String getMonth()
  {
    long now = System.currentTimeMillis() / 1000L;
    now -= (60 * 60 * 24 * 30);
    return String.valueOf(now);
    
  }
  
  /**
   * Liefert den UNIX-Timestamp von vor 6 Monaten.
   * @return den UNIX-Timestamp von vor 6 Monaten.
   */
  public String getHalfYear()
  {
    long now = System.currentTimeMillis() / 1000L;
    now -= (60 * 60 * 24 * 30 * 6);
    return String.valueOf(now);
  }

  /**
   * Liefert den UNIX-Timestamp von vor 1 Jahr.
   * @return den UNIX-Timestamp von vor 1 Jahr.
   */
  public String getYear()
  {
    long now = System.currentTimeMillis() / 1000L;
    now -= (60 * 60 * 24 * 365);
    return String.valueOf(now);
  }
}


/**********************************************************************
 * $Log: Charts.java,v $
 * Revision 1.5  2011/09/13 09:08:34  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.4  2011-06-28 09:56:36  willuhn
 * @N Lifecycle-Annotation aus jameica.webadmin in util verschoben
 *
 * Revision 1.3  2009/10/13 17:21:50  willuhn
 * @N Graph pro Sensor zeichnen
 *
 * Revision 1.2  2009/08/22 00:15:18  willuhn
 * @B daemlich ;)
 *
 * Revision 1.1  2009/08/22 00:03:42  willuhn
 * @N Das Zeichnen der Charts funktioniert! ;)
 *
 **********************************************************************/
