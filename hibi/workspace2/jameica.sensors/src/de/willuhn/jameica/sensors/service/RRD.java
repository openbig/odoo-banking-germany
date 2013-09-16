/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/service/RRD.java,v $
 * $Revision: 1.4 $
 * $Date: 2009/10/13 16:46:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.service;

import java.rmi.RemoteException;
import java.util.Date;

import de.willuhn.datasource.Service;
import de.willuhn.jameica.sensors.devices.Device;
import de.willuhn.jameica.sensors.devices.Sensor;
import de.willuhn.jameica.sensors.devices.Sensorgroup;

/**
 * Service, der die RRD-Charts erzeugt.
 */
public interface RRD extends Service
{
  /**
   * Erzeugt eine Chartgrafik fuer die Sensoren.
   * @param device Devices.
   * @param group Sensor-Gruppe.
   * @param sensor der zu zeichnende Sensor.
   * Wird keiner angegeben, werden Graphen fuer alle Sensoren aus der Gruppe gezeichnet.
   * Andernfalls nur fuer diesen einen.
   * @param start Start-Datum.
   * @param end End-Datum.
   * @return die erzeugte Grafik im PNG-Format.
   * @throws RemoteException wenn es zu einem Fehler kam oder keine Daten vorliegen.
   */
  public byte[] render(Device device, Sensorgroup group, Sensor sensor, Date start, Date end) throws RemoteException;
}


/**********************************************************************
 * $Log: RRD.java,v $
 * Revision 1.4  2009/10/13 16:46:14  willuhn
 * @N Graph pro Sensor zeichnen
 *
 * Revision 1.3  2009/08/22 00:03:42  willuhn
 * @N Das Zeichnen der Charts funktioniert! ;)
 *
 * Revision 1.2  2009/08/21 18:07:55  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2009/08/21 17:27:37  willuhn
 * @N RRD-Service
 *
 **********************************************************************/
