/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/notify/operator/Operator.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/09/13 09:08:34 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.notify.operator;

import de.willuhn.jameica.sensors.devices.Sensor;

/**
 * Interface, welches entscheidet, ob der Sensor-Wert eine Benachrichtigung
 * ausloesen soll oder nicht.
 * Die Implementierungen sind typischerweise einfache mathematische
 * Operatoren wie "kleiner als", "groesser als" oder "im Bereich zwischen".
 */
public interface Operator
{
  /**
   * Prueft, ob der Messwert das Testkriterium erfuellt.
   * Falls die Implementierung beispielsweise pruefen soll, ob ein
   * Maximalwert ueberschritten ist, muss sie dann "true" zurueckliefern,
   * wenn "value" groesser als "limit" ist.
   * @param sensor der Sensor samt seinem Messwert.
   * @param limit der festgelegte Grenzwert.
   * @return true, wenn die Bedingung erfuellt ist.
   * @throws IllegalArgumentException wenn die Parameter ungueltig sind.
   */
  public boolean matches(Sensor sensor, String limit) throws IllegalArgumentException;
}



/**********************************************************************
 * $Log: Operator.java,v $
 * Revision 1.3  2011/09/13 09:08:34  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.2  2011-02-18 12:29:41  willuhn
 * @N Regel-Operatoren umgebaut. Es gibt jetzt auch einen "Outside"-Operator mit dessen Hilfe eine Unter- UND Obergrenze in EINER Regel definiert werden kann
 *
 * Revision 1.1  2010/03/01 18:12:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2010/03/01 13:16:12  willuhn
 * @N Erster Code fuer automatische Benachrichtigungen bei Limit-Ueberschreitungen von Sensoren.
 *
 **********************************************************************/