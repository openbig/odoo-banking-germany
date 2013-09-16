/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.soap/src/de/willuhn/jameica/soap/services/PublishService.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/01/19 00:33:59 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.soap.services;

import java.rmi.RemoteException;

import javax.jws.WebService;

import de.willuhn.datasource.Service;

/**
 * Service, der das Publishing/Unpublishing der Webservices uebernimmt.
 */
public interface PublishService extends Service
{
  /**
   * Deployed einen Webservice manuell.
   * @param name Name des Webservices.
   * @param service Webservice.
   * @throws RemoteException
   */
  public void publish(String name, Object service) throws RemoteException;
  
  /**
   * Deployed einen Webservice manuell.
   * Als Name wird der Wert von {@link WebService#name()} der Annotation verwendet.
   * @param service Webservice.
   * @throws RemoteException
   */
  public void publish(Object service) throws RemoteException;

  /**
   * Un-Deployed einen Webservice.
   * @param name Name des Webservices.
   * @throws RemoteException
   */
  public void unpublish(String name) throws RemoteException;
}



/**********************************************************************
 * $Log: PublishService.java,v $
 * Revision 1.1  2010/01/19 00:33:59  willuhn
 * @C Publishing der Webservices aus MessageConsumer in dedizierten PublishService verschoben
 * @N Auto-Deployment von Services via AutoService
 *
 **********************************************************************/