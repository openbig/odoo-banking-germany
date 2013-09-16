/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/beans/Sensor.java,v $
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

package de.willuhn.jameica.sensors.beans;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * Bean fuer einen Sensor.
 */
@Entity
@Table(name="sensor", uniqueConstraints = {
    @UniqueConstraint(columnNames="uuid")
})
public class Sensor
{
  @Id
  @GeneratedValue
  private Long id = null;
  
  private String uuid = null;
  
  private String serializer = null;

  @OneToMany(cascade=CascadeType.ALL)
  @JoinColumn(name="sensor_id")
  private List<Value> values = null;

  /**
   * Liefert alle Messergebnisse.
   * @return alle Messergebnisse.
   */
  public List<Value> getValues()
  {
    if (this.values == null)
      this.values = new ArrayList<Value>();
    return this.values;
  }

  /**
   * Liefert die ID des Sensors.
   * @return ID des Sensors.
   */
  public Long getId()
  {
    return this.id;
  }

  /**
   * Liefert die eindeutige Kennung des Devices.
   * @return UUID des Devices.
   */
  public String getUuid()
  {
    return this.uuid;
  }

  /**
   * Speichert die eindeutige Kennung des Devices.
   * @param uuid die UUID des Devices.
   */
  public void setUuid(String uuid)
  {
    this.uuid = uuid;
  }

  /**
   * Liefert den Klassennamen des Serializers fuer die Messwerte.
   * @return Klassenname des Serializers fuer die Messwerte.
   */
  public String getSerializer()
  {
    return this.serializer;
  }

  /**
   * Speichert den Klassennamen des Serializers fuer die Messwerte.
   * @param serializer Klassenname des Serializers fuer die Messwerte.
   */
  public void setSerializer(String serializer)
  {
    this.serializer = serializer;
  }
  
  
  
}


/**********************************************************************
 * $Log: Sensor.java,v $
 * Revision 1.1  2009/08/21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 * Revision 1.4  2009/08/20 22:08:42  willuhn
 * @N Erste komplett funktionierende Version der Persistierung
 *
 * Revision 1.3  2009/08/20 18:07:43  willuhn
 * @N Persistierung funktioniert rudimentaer
 *
 * Revision 1.2  2009/08/19 23:46:28  willuhn
 * @N Erster Code fuer die JPA-Persistierung
 *
 * Revision 1.1  2009/08/19 10:34:43  willuhn
 * @N initial import
 *
 * Revision 1.2  2009/08/19 00:43:06  willuhn
 * @N hibernate fuer Persistierung
 *
 * Revision 1.1  2009/08/18 23:00:25  willuhn
 * @N Erste Version mit Web-Frontend
 *
 **********************************************************************/
