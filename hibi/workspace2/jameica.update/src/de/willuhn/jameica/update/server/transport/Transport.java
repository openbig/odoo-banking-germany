/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/server/transport/Transport.java,v $
 * $Revision: 1.6 $
 * $Date: 2011/01/21 17:53:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.update.server.transport;

import java.io.OutputStream;
import java.util.List;

import de.willuhn.util.ProgressMonitor;

/**
 * Kapselt die Datenuebertragung zwischen Jameica und dem Repository.
 */
public interface Transport
{
  /**
   * Ruft die Daten von der angegebenen URL herunter und schreibt sie in den Stream.
   * @param os OutputStream, in den die Daten geschrieben werden.
   * Der OutputStream wird vom Transport bereits geschlossen.
   * @param monitor optionaler Progress-Monitor.
   * @throws Exception
   */
  public void get(OutputStream os, ProgressMonitor monitor) throws Exception;
  
  /**
   * Prueft, ob die angegebene URL existiert.
   * @return true, wenn sie existiert, sonst false.
   */
  public boolean exists();
  
  /**
   * Liefert die Dateigroesse der URL in Bytes.
   * @return Dateigroesse der URL in Bytes oder -1, wenn sie nicht ermittelbar ist.
   */
  public long getSize();
  
  /**
   * Liefert eine Liste der vom Transport untertuetzten Protokolle.
   * @return Liste der Protokolle.
   * Z.Bsp. "http".
   */
  public List<String> getProtocols();
}


/**********************************************************************
 * $Log: Transport.java,v $
 * Revision 1.6  2011/01/21 17:53:14  willuhn
 * @N Download-Groessen der Plugins mit anzeigen und im Hintergrund laden (dann wird die View schneller angezeigt)
 * @R transport#exists() im Konstruktor von PluginDataImpl entfernt. Spart 50% TCP-Verbindungen beim Aufbau des Trees. Und wenn das Plugin nicht existiert, kann man auch einfach die Exception fangen.
 *
 * Revision 1.5  2009/10/29 18:06:06  willuhn
 * @N Manuelle Suche nach Updates
 * @R Test auf last-modified entfernt - war nicht wirklich deterministisch loesbar ;)
 *
 * Revision 1.4  2009/10/28 01:20:48  willuhn
 * @N Erster Code fuer automatische Update-Checks
 * @C Code-Cleanup - sauberere Fehlermeldung, wenn Plugins auf dem Server nicht (mehr) gefunden werden
 *
 * Revision 1.3  2009/01/18 01:42:46  willuhn
 * @N Abrufen und Pruefen der Plugin-Signaturen
 *
 * Revision 1.2  2008/12/16 14:15:13  willuhn
 * @C Command-Pattern entfernt. Brachte keinen wirklichen Mehrwert und erschwerte die Benutzung zusammen mit ProgressMonitor
 *
 * Revision 1.1  2008/12/12 01:13:17  willuhn
 * @N Transport-API
 *
 **********************************************************************/
