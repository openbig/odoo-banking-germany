/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/rmi/UpdateService.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/10/29 18:06:06 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.update.rmi;

import java.util.List;

import de.willuhn.datasource.Service;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Service, der regelmaessig prueft, ob Updates fuer installierte Plugins
 * in den Repositories liegen.
 */
public interface UpdateService extends Service
{
  /**
   * Sucht nach Updates fuer die installierten Plugins und liefert sie zurueck.
   * @param monitor optionale Angabe eines Progress-Monitor, in dem der Pruef-Fortschritt angezeigt wird.
   * @return die gefundenen Updates.
   * @throws ApplicationException
   */
  public List<PluginData> findUpdates(ProgressMonitor monitor) throws ApplicationException;

}


/**********************************************************************
 * $
 **********************************************************************/
