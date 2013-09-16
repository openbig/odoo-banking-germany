/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/beans/Value.java,v $
 * $Revision: 1.5 $
 * $Date: 2009/08/21 13:34:17 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.beans;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Ein einzelner Messwert eines Sensors.
 */
@Entity
@Table(name="value")
public class Value
{
  @Id
  @GeneratedValue
  private Long id = null;
  
  @Temporal(TemporalType.TIMESTAMP)
  private Date date = null;
  
  private String value = null;
  
  /**
   * Liefert die ID des Messwertes.
   * @return ID des Messwertes.
   */
  public Long getId()
  {
    return this.id;
  }

  /**
   * Liefert den Zeitpunkt der Messung.
   * @return Zeitpunkt der Messung.
   */
  public Date getDate()
  {
    return this.date;
  }
  
  /**
   * Speichert den Zeitpunkt der Messung.
   * @param date Zeitpunkt der Messung.
   */
  public void setDate(Date date)
  {
    this.date = date;
  }

  /**
   * Liefert den eigentlichen Messwert.
   * @return der Messwert.
   */
  public String getValue()
  {
    return this.value;
  }

  /**
   * Speichert den Messwert.
   * @param value der Messwert.
   */
  public void setValue(String value)
  {
    this.value = value;
  }
}


/**********************************************************************
 * $Log: Value.java,v $
 * Revision 1.5  2009/08/21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 **********************************************************************/
