/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/rmi/PluginData.java,v $
 * $Revision: 1.8 $
 * $Date: 2011/01/21 17:53:15 $
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

import de.willuhn.datasource.GenericObject;
import de.willuhn.jameica.plugin.Dependency;
import de.willuhn.jameica.plugin.Version;

/**
 * Container fuer die Meta-Daten eines Plugins.
 */
public interface PluginData extends GenericObject
{
  /**
   * Liefert die zugehoerige Plugin-Gruppe.
   * @return die zugehoerige Plugin-Gruppe.
   * @throws RemoteException
   */
  public PluginGroup getPluginGroup() throws RemoteException;
  
  /**
   * Liefert den Namen des Plugins.
   * @return Name des Plugins.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;
  
  /**
   * Liefert die Dateigroesse des Downloads in Bytes.
   * @return Dateigroesse des Downloads in Bytes oder -1, wenn sie nicht ermittelbar ist.
   * @throws RemoteException
   */
  public long getSize() throws RemoteException;
  
  /**
   * Liefert einen Beschreibungstext.
   * @return Beschreibungstext.
   * @throws RemoteException
   */
  public String getDescription() throws RemoteException;
  
  /**
   * Liefert die Download-URL des Plugins.
   * @return Download-URL.
   * @throws RemoteException
   */
  public URL getDownloadUrl() throws RemoteException;
  
  /**
   * Liefert die URL mit der Signatur des Plugins.
   * @return URL der Signatur.
   * @throws RemoteException
   */
  public URL getSignatureUrl() throws RemoteException;
  
  /**
   * Liefert die Versionsnummer der installierten Version.
   * @return Versionsnummer der installierten Version oder NULL wenn das Plugin
   * noch nicht installiert ist.
   * @throws RemoteException
   */
  public Version getInstalledVersion() throws RemoteException;
  
  /**
   * Prueft, ob die installierte Version identisch zur verfuegbaren ist.
   * @return true, wenn die Versionsnummern uebereinstimmen.
   * @throws RemoteException
   */
  public boolean isInstalledVersion() throws RemoteException;

  /**
   * Liefert die Versionsnummer der verfuegbaren Version.
   * @return Versionsnummer der verfuegbaren Version.
   * @throws RemoteException
   */
  public Version getAvailableVersion() throws RemoteException;
  
  /**
   * Prueft, ob das Plugin installiert werden kann.
   * @return true, wenn das Plugin installiert werden kann.
   * @throws RemoteException
   */
  public boolean isInstallable() throws RemoteException;
  
  /**
   * Liefert eine Liste der Abhaengigkeiten.
   * @return Liste der Abhaengigkeiten.
   * @throws RemoteException
   */
  public Dependency[] getDependencies() throws RemoteException;
}


/**********************************************************************
 * $
 **********************************************************************/
