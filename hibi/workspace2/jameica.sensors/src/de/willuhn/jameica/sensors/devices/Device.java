/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/devices/Device.java,v $
 * $Revision: 1.4 $
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

import java.io.IOException;

/**
 * Basis-Interface, welches von einem Messgeraet implementiert werden muss.
 * Implementierungen muessen der Bean-Konvention entsprechen und einen
 * parameterlosen Konstruktor mit public-Modifier besitzen.
 */
public interface Device extends UniqueItem
{
  /**
   * Fuehrt eine Messung auf dem Geraet durch und liefert die Messwerte.
   * @return die Messwerte.
   * @throws IOException
   */
  public Measurement collect() throws IOException;
  
  /**
   * Liefert true, wenn die Messwerte des Geraetes regelmaessig abgefragt werden sollen.
   * @return true, wenn die Messwerte des Geraetes regelmaessig abgefragt werden sollen.
   */
  public boolean isEnabled();
  
  /**
   * Liefert einen sprechenden Namen fuer das Device.
   * @return sprechender Namen fuer das Device.
   */
  public String getName();
}


/**********************************************************************
 * $Log: Device.java,v $
 * Revision 1.4  2009/08/21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 * Revision 1.3  2009/08/20 22:08:42  willuhn
 * @N Erste komplett funktionierende Version der Persistierung
 *
 * Revision 1.2  2009/08/19 23:46:29  willuhn
 * @N Erster Code fuer die JPA-Persistierung
 *
 * Revision 1.1  2009/08/19 10:34:43  willuhn
 * @N initial import
 *
 * Revision 1.2  2009/08/18 23:00:25  willuhn
 * @N Erste Version mit Web-Frontend
 *
 * Revision 1.1  2009/08/18 16:29:19  willuhn
 * @N DIE SCHEISSE GEHT! ;)
 *
 **********************************************************************/
