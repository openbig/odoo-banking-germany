/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.xmlrpc/src/de/willuhn/jameica/xmlrpc/server/EchoServiceImpl.java,v $
 * $Revision: 1.3 $
 * $Date: 2007/05/15 15:40:43 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.xmlrpc.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import de.willuhn.jameica.xmlrpc.rmi.EchoService;
import de.willuhn.logging.Logger;

/**
 * Implementierung des Beispiel-Echo-Services.
 */
public class EchoServiceImpl extends UnicastRemoteObject implements EchoService
{
  
  private boolean started = false;

  /**
   * ct.
   * @throws RemoteException
   */
  public EchoServiceImpl() throws RemoteException
  {
    super();
  }

  /**
   * @see de.willuhn.jameica.xmlrpc.rmi.EchoService#echo(java.lang.String)
   */
  public String echo(String text) throws RemoteException
  {
    Logger.info("Echo-Request: " + text);
    return "Echo: " + text;
  }

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "Echo-Service";
  }

  /**
   * @see de.willuhn.datasource.Service#isStartable()
   */
  public boolean isStartable() throws RemoteException
  {
    return !isStarted();
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
    if (!isStartable())
    {
      Logger.warn("not allowed to start service or service allready started, skipping request");
      return;
    }
    this.started = true;
  }

  /**
   * @see de.willuhn.datasource.Service#stop(boolean)
   */
  public void stop(boolean arg0) throws RemoteException
  {
    if (!isStarted())
    {
      Logger.warn("service not running, skipping request");
      return;
    }
    this.started = false;
  }

}


/*********************************************************************
 * $Log: EchoServiceImpl.java,v $
 * Revision 1.3  2007/05/15 15:40:43  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2006/10/28 01:05:37  willuhn
 * @N add bindings on demand
 *
 * Revision 1.1  2006/10/26 23:54:15  willuhn
 * @N added needed jars
 * @N first working version
 *
 **********************************************************************/