/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/beans/Device.java,v $
 * $Revision: 1.4 $
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
 * Bean fuer die Persistierung eines Devices (bzw. der Messwerte.
 */
@Entity
@Table(name="device", uniqueConstraints = {
    @UniqueConstraint(columnNames="uuid")
})
public class Device
{
  @Id
  @GeneratedValue
  private Long id = null;

  private String uuid = null;

  @OneToMany(cascade=CascadeType.ALL)
  @JoinColumn(name="device_id")
  private List<Sensor> sensors = null;

  /**
   * Liefert die Sensoren des Devices.
   * @return die Sensoren des Devices.
   */
  public List<Sensor> getSensors()
  {
    if (this.sensors == null)
      this.sensors = new ArrayList<Sensor>();
    return this.sensors;
  }

  /**
   * Liefert die ID des Devices.
   * @return ID des Devices.
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
}


/**********************************************************************
 * $Log: Device.java,v $
 * Revision 1.4  2009/08/21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 * Revision 1.3  2009/08/20 22:08:42  willuhn
 * @N Erste komplett funktionierende Version der Persistierung
 *
 * Revision 1.2  2009/08/20 18:07:43  willuhn
 * @N Persistierung funktioniert rudimentaer
 *
 * Revision 1.1  2009/08/19 23:46:28  willuhn
 * @N Erster Code fuer die JPA-Persistierung
 *
 **********************************************************************/
