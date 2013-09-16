/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/devices/Sensorgroup.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/09/13 09:08:34 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.devices;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface fuer eine Sensor-Gruppe.
 */
public class Sensorgroup implements UniqueItem
{
  /**
   * Default-Konsolidierungsfunktion.
   */
  public final static Consolidation CONSOLIDATION_DEFAULT = Consolidation.AVERAGE;

  private String name = null;
  private String uuid = null;
  private Consolidation consolidation = CONSOLIDATION_DEFAULT;
  
  private List<Sensor> sensors = null;
  
  /**
   * Die zu verwendende Konsolidierungsfunktion.
   */
  public enum Consolidation
  {
    /**
     * Durchschnittswerte bilden.
     */
    AVERAGE,
    
    /**
     * Minimum-Werte.
     */
    MIN, 
    
    /**
     * Maximum-Werte.
     */
    MAX,
    
    /**
     * Letzter Wert.
     */
    LAST, 
    
    /**
     * Erster Wert.
     */
    FIRST,
    
    /**
     * Summe.
     */
    TOTAL
  }
  
  /**
   * Liefert einen sprechenden Namen fuer die Sensor-Gruppe.
   * @return sprechender Name der Sensor-Gruppe.
   */
  public String getName()
  {
    return this.name;
  }
  
  /**
   * Speichert einen sprechenden Namen fuer die Sensor-Gruppe.
   * @param name sprechender Name fuer die Sensor-Gruppe.
   */
  public void setName(String name)
  {
    this.name = name; 
  }
  
  /**
   * Liefert die Liste der Sensoren dieser Gruppe.
   * @return die Liste der Sensoren dieser Gruppe.
   */
  public List<Sensor> getSensors()
  {
    if (this.sensors == null)
      this.sensors = new ArrayList<Sensor>();
    return this.sensors;
  }

  /**
   * @see de.willuhn.jameica.sensors.devices.UniqueItem#getUuid()
   */
  public String getUuid()
  {
    return this.uuid;
  }
  
  /**
   * Speichert die eindeutige ID fuer das Objekt.
   * Diese ID sollte sich niemals aendern, da sich sonst bereits
   * archivierte Messwerte nicht mehr diesem Objekt zuordnen lassen.
   * @param uuid die eindeutige ID fuer das Objekt.
   */
  public void setUuid(String uuid)
  {
    this.uuid = uuid;
  }

  /**
   * Liefert die Konsolidierungsfunktion.
   * @return die Konsolidierungsfunktion.
   */
  public Consolidation getConsolidation()
  {
    return this.consolidation;
  }

  /**
   * Speicher die Konsolidierungsfunktion.
   * @param consolidation die Konsolidierungsfunktion.
   */
  public void setConsolidation(Consolidation consolidation)
  {
    this.consolidation = consolidation;
  }
}


/**********************************************************************
 * $Log: Sensorgroup.java,v $
 * Revision 1.5  2011/09/13 09:08:34  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.4  2009/10/13 15:51:04  willuhn
 * @N Sensor-API um Konsolidierungsfunktion erweitert
 *
 * Revision 1.3  2009/09/08 10:38:00  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2009/08/21 17:27:37  willuhn
 * @N RRD-Service
 *
 * Revision 1.1  2009/08/21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 **********************************************************************/
