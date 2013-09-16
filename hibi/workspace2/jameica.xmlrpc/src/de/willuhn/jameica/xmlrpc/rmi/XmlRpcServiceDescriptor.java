/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.xmlrpc/src/de/willuhn/jameica/xmlrpc/rmi/XmlRpcServiceDescriptor.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/10/18 22:13:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.xmlrpc.rmi;

import java.rmi.RemoteException;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.Service;

/**
 * Container fuer die Eigenschaften eines XML-RPC-Services.
 */
public interface XmlRpcServiceDescriptor extends GenericObject
{
  /**
   * Prueft, ob der Service via XML-RPC verfuegbar sein soll.
   * @return true, wenn er verfuegbar sein soll.
   * @throws RemoteException
   */
  public boolean isShared() throws RemoteException;
  
  /**
   * Legt fest, ob der Service via XML-RPC verfuegbar sein soll.
   * @param shared true, wenn er verfuegbar sein soll.
   * @throws RemoteException
   */
  public void setShared(boolean shared) throws RemoteException;
  
  /**
   * Liefert den Namen des Services.
   * @return Name des Services.
   * @throws RemoteException
   */
  public String getServiceName() throws RemoteException;

  /**
   * Liefert den Namen des Plugins.
   * @return Name des Plugins.
   * @throws RemoteException
   */
  public String getPluginName() throws RemoteException;
  
  /**
   * Liefert den zugehoerigen Service.
   * @return der zugehoerige Service.
   * @throws RemoteException
   */
  public Service getService() throws RemoteException;
  
  /**
   * Liefert die XML-RPC URL des Services.
   * @return die XML-RPC URL.
   * @throws RemoteException
   */
  public String getURL() throws RemoteException;
}


/*********************************************************************
 * $Log: XmlRpcServiceDescriptor.java,v $
 * Revision 1.2  2007/10/18 22:13:14  willuhn
 * @N XML-RPC URL via Service-Descriptor abfragbar
 *
 * Revision 1.1  2007/04/05 12:14:40  willuhn
 * @N Liste der Services im Handler statisch
 * @C XmlRpcService in XmlRpcServiceDescriptor umbenannt
 *
 * Revision 1.3  2006/10/31 17:06:26  willuhn
 * @N GUI to configure xml-rpc
 *
 **********************************************************************/