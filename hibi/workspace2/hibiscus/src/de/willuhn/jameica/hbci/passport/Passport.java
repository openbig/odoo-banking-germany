/**********************************************************************
 * $Source: /cvsroot/hibiscus/hibiscus/src/de/willuhn/jameica/hbci/passport/Passport.java,v $
 * $Revision: 1.7 $
 * $Date: 2011/04/29 09:17:35 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.hbci.passport;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import de.willuhn.jameica.hbci.rmi.Konto;


/**
 * Basis-Interface aller Passports.
 * Insofern ein Passport (Sicherheitsmedium) beim Start der Anwendung
 * gefunden werden soll, muss er dieses Interface implementieren.<br>
 * Als Referenz kann hierbei der bereits existierende Passport fuer
 * die Unterstuetzung von DDV-Chipkarten vorhandene
 * <i>de.willuhn.jameica.passports.ddv.server.PassportImpl</i> dienen.
 */
public interface Passport extends Remote {


	/**
	 * Diese Funktion wird von Hibiscus aufgerufen, wenn der Passport
	 * initialisiert wird.
	 * Sie kann vom Passport implementiert werden - muss jedoch nicht.
	 * Falls der Passport jedoch wissen muss, fuer welches Konto er
	 * gerade zustaendig ist, kann er es ueber diese Funktion erfahren.
	 * Die Funktion wird unmittelbar vor der Ausfuehrung eines HBCI-Jobs
	 * im Konto ausgefuehrt.
   * @param konto das Konto, fuer welches der Passport gerade verwendet
   * werden soll.
   * @throws RemoteException
   * BUGZILLA #7 http://www.willuhn.de/bugzilla/show_bug.cgi?id=7
   */
  public void init(Konto konto) throws RemoteException;

	/**
	 * Liefert den sprechenden Namen des Passports.
	 * Dieser Name wird dem Benutzer zum Beispiel in Combo-Boxen angezeigt.
   * @return Name des Passports.
   * @throws RemoteException
   */
  public String getName() throws RemoteException;
  
  /**
   * Liefert einen Info-Text, den die Passport-Klasse frei implementieren kann.
   * Sie kann hier z.Bsp. die Anzahl vorhandener PIN/TAN-Konfigurationen oder
   * Schluesseldisketten zuruecklistern.
   * BUGZILLA 471
   * @return Info-Text zu dem Sicherheitsmedium.
   * @throws RemoteException
   */
  public String getInfo() throws RemoteException;
	
	/**
	 * Liefert das Passport-Handle.
	 * Das ist im Prinzip die direkte Schnittstelle zu HBCI4Java.
   * @return Handle.
   * @throws RemoteException
   */
  public PassportHandle getHandle() throws RemoteException;

	/**
	 * Liefert die Klasse des Konfigurationsdialogs.
	 * Oeffnet der Anwender den Konfigurations-Dialog durch
	 * Doppelklick auf die Liste der vorhandenen Passports,
	 * dann wird eine Instanz dieser Klasse erzeugt und
	 * in der GUI angezeigt. Als Referenz kann hierzu
	 * <i>de.willuhn.jameica.passports.ddv.View</i> dienen.
   * @return Die Klasse des Konfig-Dialogs.
   * Muss von <code>AbstractView</code> abgeleitet sein.
   * @throws RemoteException
   */
  public Class getConfigDialog() throws RemoteException;
  
  /**
   * Liefert die Liste der Konfigurationen.
   * @return die Liste der Konfigurationen.
   * @throws RemoteException
   */
  public List<? extends Configuration> getConfigurations() throws RemoteException;
}


/**********************************************************************
 * $Log: Passport.java,v $
 * Revision 1.7  2011/04/29 09:17:35  willuhn
 * @N Neues Standard-Interface "Configuration" fuer eine gemeinsame API ueber alle Arten von HBCI-Konfigurationen
 * @R Passports sind keine UnicastRemote-Objekte mehr
 *
 * Revision 1.6  2010/04/22 12:42:03  willuhn
 * @N Erste Version des Supports fuer Offline-Konten
 *
 * Revision 1.5  2010/04/21 23:39:24  willuhn
 * @N Support fuer Offline-Konten
 *
 * Revision 1.4  2010/04/14 16:51:11  willuhn
 * @N BUGZILLA 471
 **********************************************************************/