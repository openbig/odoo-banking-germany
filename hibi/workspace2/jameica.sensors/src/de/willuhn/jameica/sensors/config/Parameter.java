/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/config/Parameter.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/09/15 17:00:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.config;

import java.io.Serializable;

/**
 * Bean fuer einen einzeln Parameter.
 */
public class Parameter implements Serializable
{
  /**
   * Name des Parameters.
   */
  private String name = null;
  
  /**
   * Beschreibung des Parameters.
   */
  private String description = null;
  
  /**
   * Wert.
   */
  private String value = null;
  
  /**
   * Identifier.
   */
  private String uuid = null;

  /**
   * Liefert den Namen des Parameters.
   * @return Name des Parameters.
   */
  public String getName()
  {
    return this.name;
  }

  /**
   * ct.
   * Bean-Konstruktor.
   */
  public Parameter()
  {
  }
  
  /**
   * ct.
   * @param name Name des Parameters.
   * @param desc Beschreibung des Parameters.
   * @param value Wert des Parameters.
   * @param uuid UUID des Parameters.
   */
  public Parameter(String name, String desc, String value, String uuid)
  {
    this.name        = name;
    this.description = desc;
    this.value       = value;
    this.uuid        = uuid;
  }
  
  /**
   * Speichert den Namen des Parameters.
   * @param name Name des Parameters.
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Liefert die Beschreibung des Parameters.
   * @return die Beschreibung des Parameters.
   */
  public String getDescription()
  {
    return this.description;
  }

  /**
   * Speichert die Beschreibung des Parameters.
   * @param description die Beschreibung des Parameters.
   */
  public void setDescription(String description)
  {
    this.description = description;
  }

  /**
   * Liefert den Wert des Parameters.
   * @return Wert des Parameters.
   */
  public String getValue()
  {
    return this.value;
  }

  /**
   * Speichert den Wert des Parameters.
   * @param value Wert des Parameters.
   */
  public void setValue(String value)
  {
    this.value = value;
  }

  /**
   * Liefert eine eindeutige ID fuer den Parameter.
   * @return ID fuer den Parameter.
   */
  public String getUuid()
  {
    return this.uuid;
  }

  /**
   * Speichert eine eindeutige ID fuer den Parameter.
   * @param uuid ID fuer den Parameter.
   */
  public void setUuid(String uuid)
  {
    this.uuid = uuid;
  }
}


/**********************************************************************
 * $Log: Parameter.java,v $
 * Revision 1.1  2009/09/15 17:00:17  willuhn
 * @N Konfigurierbarkeit aller Module ueber das Webfrontend
 *
 **********************************************************************/
