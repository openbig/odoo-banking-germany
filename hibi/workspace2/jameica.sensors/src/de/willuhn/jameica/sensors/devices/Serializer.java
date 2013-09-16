/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/devices/Serializer.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/08/21 13:34:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.devices;


/**
 * Interface fuer einen Wert-Serializer.
 * Die Messwerte von Sensoren koennen von unterschiedlichsten
 * Datentypen sein. Da das Framework diese nicht kennt, soll
 * das Device selbst entscheiden koennen, wie die Werte (z.Bsp.
 * fuer die Speicherung in einer Datenbank) serialisiert werden
 * koennen.
 * Ist an einem Sensor kein Serializer angegeben, wird davon
 * ausgegangen, dass der Messwert als String vorliegt und
 * nicht serialisiert werden muss.
 * Implementierungen des Serializer-Interfaces muessen der
 * Bean-Spezifikation entsprechen - also einen parameterlosen
 * Konstruktor besitzen.
 */
public interface Serializer
{
  /**
   * Serialisiert den Messwert.
   * @param o der zu serialisierende Messwert.
   * @return der serialisierte Messwert.
   */
  public String serialize(Object o);
  
  /**
   * De-serialisiert den Messwert.
   * @param s der zu de-serialisierende Messwert.
   * @return der de-serialisierte Messwert.
   * @throws IllegalArgumentException wenn der Messwert nicht de-serialisiert werden kann.
   */
  public Object unserialize(String s) throws IllegalArgumentException;

  /**
   * Liefert eine formatierte und anzeigbare Version des Messwertes.
   * Die Implementierung kann hier z.Bsp. die Einheit (cm, °C, etc.) anhaengen.
   * @param value der zu formatierende Wert.
   * @return der formatierte Wert.
   */
  public String format(Object value);
}


/**********************************************************************
 * $Log: Serializer.java,v $
 * Revision 1.1  2009/08/21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 **********************************************************************/
