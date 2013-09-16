/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/server/RepositoryServiceImpl.java,v $
 * $Revision: 1.4 $
 * $Date: 2009/01/18 00:18:56 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.update.server;

import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

import de.willuhn.jameica.update.Settings;
import de.willuhn.jameica.update.rmi.Repository;
import de.willuhn.jameica.update.rmi.RepositoryService;
import de.willuhn.logging.Logger;

/**
 * Implementierung des Repository-Services.
 */
public class RepositoryServiceImpl extends UnicastRemoteObject implements RepositoryService
{
  /**
   * ct.
   * @throws RemoteException
   */
  public RepositoryServiceImpl() throws RemoteException
  {
    super();
  }

  private boolean started = false;
  
  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "Repository-Service";
  }

  /**
   * @see de.willuhn.datasource.Service#isStartable()
   */
  public boolean isStartable() throws RemoteException
  {
    return !this.isStarted();
  }

  /**
   * @see de.willuhn.datasource.Service#isStarted()
   */
  public boolean isStarted() throws RemoteException
  {
    return this.started;
  }

  /**
   * @see de.willuhn.datasource.Service#start()
   */
  public void start() throws RemoteException
  {
    if (this.isStarted())
    {
      Logger.warn("service allready started, skipping request");
      return;
    }
    this.started = true;
  }

  /**
   * @see de.willuhn.datasource.Service#stop(boolean)
   */
  public void stop(boolean restartable) throws RemoteException
  {
    if (!this.isStarted())
    {
      Logger.warn("service not started, skipping request");
      return;
    }
    this.started = false;
  }

  /**
   * @see de.willuhn.jameica.update.rmi.RepositoryService#open(java.net.URL)
   */
  public Repository open(URL url) throws RemoteException
  {
    return new RepositoryImpl(url);
  }

  /**
   * @see de.willuhn.jameica.update.rmi.RepositoryService#getRepositories()
   */
  public List<URL> getRepositories() throws RemoteException
  {
    return Settings.getRepositories();
  }

  /**
   * @see de.willuhn.jameica.update.rmi.RepositoryService#addRepository(java.net.URL)
   */
  public void addRepository(URL url) throws RemoteException
  {
    if (url == null)
      throw new RemoteException("no repository url given");

    List<URL> list = getRepositories();
    if (list.contains(url))
    {
      Logger.warn("repository " + url + " allready exists");
      return;
    }
    
    list.add(url);
    Settings.setRepositories(list);
    Logger.info("repository " + url + " added");
  }

  /**
   * @see de.willuhn.jameica.update.rmi.RepositoryService#removeRepository(java.net.URL)
   */
  public void removeRepository(URL url) throws RemoteException
  {
    if (url == null)
      throw new RemoteException("no repository url given");

    List<URL> list = getRepositories();
    if (!list.contains(url))
    {
      Logger.warn("repository " + url + " does not exist");
      return;
    }
    
    list.remove(url);
    Settings.setRepositories(list);
    Logger.info("repository " + url + " removed");
  }
}


/**********************************************************************
 * $Log: RepositoryServiceImpl.java,v $
 * Revision 1.4  2009/01/18 00:18:56  willuhn
 * @C "setRepositories(List)" ersetzt gegen "addRepository(URL)" und "removeRepository(URL)"
 *
 * Revision 1.3  2008/12/16 23:25:38  willuhn
 * @D javadoc
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
