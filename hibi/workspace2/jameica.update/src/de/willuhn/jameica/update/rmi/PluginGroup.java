/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/rmi/PluginGroup.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/01/18 13:51:36 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.update.rmi;

import java.rmi.RemoteException;
import java.security.cert.X509Certificate;
import java.util.List;

import de.willuhn.datasource.GenericObjectNode;

/**
 * Eine Gruppe von Plugins.
 */
public interface PluginGroup extends GenericObjectNode
{
  /**
   * Liefert das zugehoerige Repository.
   * @return das zugehoerige Repository.
   * @throws RemoteException
   */
  public Repository getRepository() throws RemoteException;
  
  /**
   * Liefert den Namen der Plugin-Gruppe.
   * @return Name der Plugin-Gruppe.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;
  
  /**
   * Liefert die in der Gruppe enthaltenen Plugins.
   * @return die in der Gruppe enthaltenen Plugins.
   * @throws RemoteException
   */
  public List<PluginData> getPlugins() throws RemoteException;
  
  /**
   * Liefert das Zertifikat der Plugin-Gruppe.
   * @return das Zertifikat oder NULL, wenn keines angegeben ist.
   * @throws RemoteException
   */
  public X509Certificate getCertificate() throws RemoteException;

}


/**********************************************************************
 * $Log: PluginGroup.java,v $
 * Revision 1.3  2009/01/18 13:51:36  willuhn
 * @N Zertifikate pro Plugin-Gruppe konfigurierbar
 *
 * Revision 1.2  2008/12/31 01:07:21  willuhn
 * @N Plugins remote (via RMI) auf einem Jameica-Server downloadbar
 *
 * Revision 1.1  2008/12/31 00:40:30  willuhn
 * @N BUGZILLA 675 Gruppierung von Plugins
 *
 **********************************************************************/
