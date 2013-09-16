/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/notify/notifier/Notifier.java,v $
 * $Revision: 1.4 $
 * $Date: 2010/08/31 11:01:38 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.notify.notifier;

import java.util.Date;
import java.util.Map;

/**
 * Interface fuer die verschiedenen Benachrichtigungsarten (Mail, Log, etc.).
 */
public interface Notifier
{
  /**
   * Wird aufgerufen, wenn der Sensor einen Wert ausserhalb des Limits geliefert hat.
   * Die Implementierung muss dann hier die Benachrichtigung versenden.
   * @param subject Betreff-Text.
   * @param description Beschreibungstext.
   * @param params optionale Zustell-Parameter gemaess "params" der XML-Regeldatei.
   * @param since Datum, seit wann der Sensor bereits Werte ausserhalb des Limits liefert.
   * Ist dies die erste Messung, in der der Sensor unnormale Werte liefert, wird NULL
   * uebergeben. Sobald der Sensor wieder in den Normal-Bereich zurueckkehrt, wird
   * das Datum wieder auf NULL gesetzt.
   * Die Implementierung kann so selbst entscheiden, ob und wie oft sie wiederholte
   * Benachrichtigungen sendet. Soll sie beispielsweise nur beim ersten Auftreten des
   * Ausfalls gesendet werden, genuegt eine Pruefung auf "since == null".
   * @throws Exception
   */
  public void outsideLimit(String subject, String description, Map<String,String> params, Date since) throws Exception;
  
  /**
   * Wird einmalig aufgerufen, wenn der Sensor vorher ausserhalb des Limit
   * war und jetzt zurueckgekehrt ist.
   * @param subject Betreff-Text.
   * @param description Beschreibungstext.
   * @param params optionale Zustell-Parameter gemaess "params" der XML-Regeldatei.
   * @throws Exception
   */
  public void insideLimit(String subject, String description, Map<String,String> params) throws Exception;
}



/**********************************************************************
 * $Log: Notifier.java,v $
 * Revision 1.4  2010/08/31 11:01:38  willuhn
 * @D javadoc
 *
 * Revision 1.3  2010/03/02 12:43:52  willuhn
 * @C Ausfall-Log nicht mehr persistieren
 *
 * Revision 1.2  2010/03/01 23:51:07  willuhn
 * @N Benachrichtigung, wenn Sensor zurueck im normalen Bereich ist
 * @N Merken des letzten Notify-Status, sodass nur beim ersten mal eine Mail gesendet wird
 *
 * Revision 1.1  2010/03/01 18:12:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2010/03/01 17:08:18  willuhn
 * @N Mail-Benachrichtigung via javax.mail
 *
 * Revision 1.1  2010/03/01 13:16:12  willuhn
 * @N Erster Code fuer automatische Benachrichtigungen bei Limit-Ueberschreitungen von Sensoren.
 *
 **********************************************************************/