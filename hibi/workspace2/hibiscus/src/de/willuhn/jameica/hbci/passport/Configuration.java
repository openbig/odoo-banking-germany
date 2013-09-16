/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passport/Configuration.java,v $
 * $Revision: 1.2 $
 * $Date: 2011/06/17 08:49:18 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.hbci.passport;

import java.rmi.RemoteException;

import de.willuhn.util.ApplicationException;




/**
 * Interface fuer eine einzelne Passport-Konfiguration.
 */
public interface Configuration
{
  /**
   * Liefert einen Beschreibungstext fuer die Konfiguration.
   * @return Beschreibungstext.
   */
  public String getDescription();

  /**
   * Liefert die Klasse des Konfigurationsdialogs.
   * Oeffnet der Anwender den Konfigurations-Dialog durch
   * Doppelklick auf die Liste der vorhandenen Konfigurationen,
   * dann wird eine Instanz dieser Klasse erzeugt und
   * in der GUI angezeigt. Als Referenz kann hierzu
   * <i>de.willuhn.jameica.passports.ddv.View</i> dienen.
   * @return Die Klasse des Konfig-Dialogs.
   * Muss von <code>AbstractView</code> abgeleitet sein.
   * @throws RemoteException
   */
  public Class getConfigDialog() throws RemoteException;
  
  /**
   * Loescht die Konfiguration.
   * @throws ApplicationException
   */
  public void delete() throws ApplicationException;

}



/**********************************************************************
 * $Log: Configuration.java,v $
 * Revision 1.2  2011/06/17 08:49:18  willuhn
 * @N Contextmenu im Tree mit den Bank-Zugaengen
 * @N Loeschen von Bank-Zugaengen direkt im Tree
 *
 * Revision 1.1  2011-04-29 09:17:34  willuhn
 * @N Neues Standard-Interface "Configuration" fuer eine gemeinsame API ueber alle Arten von HBCI-Konfigurationen
 * @R Passports sind keine UnicastRemote-Objekte mehr
 *
 **********************************************************************/