/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/rmi/Repository.java,v $
 * $Revision: 1.7 $
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
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import de.willuhn.util.ApplicationException;

/**
 * Interface fuer ein Repository.
 */
public interface Repository extends Remote
{
  /**
   * Liefert die URL des Repository.
   * @return URL des Repository.
   * @throws RemoteException
   */
  public URL getUrl() throws RemoteException;
  
  /**
   * Liefert eine sprechende Bezeichnung des Repository.
   * @return sprechende Bezeichnung des Repository.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;
  
  /**
   * Liefert die im Repository enthaltenen Plugingruppen.
   * @return die im Repository enthaltenen Plugingruppen.
   * @throws RemoteException
   */
  public List<PluginGroup> getPluginGroups() throws RemoteException;

  /**
   * Liefert die im Repository enthaltenen Plugins.
   * @return die im Repository enthaltenen Plugins.
   * @throws RemoteException
   */
  public List<PluginData> getPlugins() throws RemoteException;
  
  /**
   * Laedt das angegebene Plugin herunter, sodass es beim naechsten Start installiert wird.
   * @param plugin das herunterzuladende Plugin.
   * @param interactive true, wenn Rueckfragen an den User erfolgen duerfen.
   * @throws RemoteException
   * @throws ApplicationException
   */
  public void download(PluginData plugin, boolean interactive) throws RemoteException,ApplicationException;
}


/**********************************************************************
 * $Log: Repository.java,v $
 * Revision 1.7  2011/06/01 11:02:39  willuhn
 * @N Update auf 1.1 - benoetigt jetzt Jameica 1.11 oder hoeher
 *
 * Revision 1.6  2009/10/28 17:00:58  willuhn
 * @N Automatischer Check nach Updates mit der Wahlmoeglichkeit, nur zu benachrichtigen oder gleich zu installieren
 *
 * Revision 1.5  2008/12/31 01:07:21  willuhn
 * @N Plugins remote (via RMI) auf einem Jameica-Server downloadbar
 *
 * Revision 1.4  2008/12/31 00:40:30  willuhn
 * @N BUGZILLA 675 Gruppierung von Plugins
 *
 * Revision 1.3  2008/12/16 16:16:47  willuhn
 * @N Erste Version mit Download und install
 *
 * Revision 1.2  2008/12/16 11:43:38  willuhn
 * @N Detail-Dialog fuer Repositories
 *
 * Revision 1.1  2008/12/12 01:13:17  willuhn
 * @N Transport-API
 *
 **********************************************************************/
