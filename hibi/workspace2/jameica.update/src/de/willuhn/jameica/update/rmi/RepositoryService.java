/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/rmi/RepositoryService.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/06/01 11:02:39 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.update.rmi;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.List;

import de.willuhn.datasource.Service;

/**
 * Interface fuer den Repository-Service.
 */
public interface RepositoryService extends Service
{
  /**
   * Oeffnet ein Repository.
   * @param url URL zum Repository.
   * @return das Repository.
   * @throws RemoteException
   */
  public Repository open(URL url) throws RemoteException;

  /**
   * Liefert die Liste der Repositories.
   * @return Liste der Repositories.
   * @throws RemoteException
   */
  public List<URL> getRepositories() throws RemoteException;

  /**
   * Fuegt ein neues Online-Repository hinzu.
   * @param url URL des Online-Repositories.
   * @throws RemoteException
   */
  public void addRepository(URL url) throws RemoteException;

  /**
   * Entfernt ein Online-Repository.
   * @param url URL des Online-Repositories.
   * @throws RemoteException
   */
  public void removeRepository(URL url) throws RemoteException;
}


/**********************************************************************
 * $Log: RepositoryService.java,v $
 * Revision 1.4  2011/06/01 11:02:39  willuhn
 * @N Update auf 1.1 - benoetigt jetzt Jameica 1.11 oder hoeher
 *
 * Revision 1.3  2009/01/18 00:18:56  willuhn
 * @C "setRepositories(List)" ersetzt gegen "addRepository(URL)" und "removeRepository(URL)"
 *
 * Revision 1.2  2008/12/16 14:15:13  willuhn
 * @C Command-Pattern entfernt. Brachte keinen wirklichen Mehrwert und erschwerte die Benutzung zusammen mit ProgressMonitor
 *
 * Revision 1.1  2008/12/12 01:13:17  willuhn
 * @N Transport-API
 *
 * Revision 1.1  2008/12/10 00:33:19  willuhn
 * @N initial checkin des Update-Plugins
 *
 **********************************************************************/
