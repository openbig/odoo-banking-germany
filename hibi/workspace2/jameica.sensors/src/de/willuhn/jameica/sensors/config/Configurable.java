/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/config/Configurable.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/09/15 17:00:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.config;

import java.util.List;

/**
 * Interface fuer ein konfigurierbares Modul.
 * Wenn ein Modul ueber die Web-Oberflaeche konfigurierbar sein soll,
 * muss es dieses Interface hier implementieren.
 */
public interface Configurable
{
  /**
   * Liefert einen sprechenden Namen fuer das Modul.
   * @return sprechender Name fuer das Modul.
   */
  public String getName();
  
  /**
   * Liefert eine Liste der konfigurierbaren Parameter.
   * @return Liste der konfigurierbaren Parameter.
   */
  public List<Parameter> getParameters();
  
  /**
   * Speichert die Parameter.
   * @param parameters die zu speichernden Parameter.
   */
  public void setParameters(List<Parameter> parameters);

}


/**********************************************************************
 * $Log: Configurable.java,v $
 * Revision 1.1  2009/09/15 17:00:17  willuhn
 * @N Konfigurierbarkeit aller Module ueber das Webfrontend
 *
 **********************************************************************/
