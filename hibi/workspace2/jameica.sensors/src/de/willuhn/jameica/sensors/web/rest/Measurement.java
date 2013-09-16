/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/web/rest/Measurement.java,v $
 * $Revision: 1.4 $
 * $Date: 2011/09/13 09:08:34 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.web.rest;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import de.willuhn.jameica.sensors.devices.Device;
import de.willuhn.jameica.sensors.devices.Sensor;
import de.willuhn.jameica.sensors.devices.Sensorgroup;
import de.willuhn.jameica.sensors.devices.Serializer;
import de.willuhn.jameica.sensors.messaging.LimitMessageConsumer;
import de.willuhn.jameica.sensors.messaging.LiveMeasurement;
import de.willuhn.jameica.webadmin.annotation.Doc;
import de.willuhn.jameica.webadmin.annotation.Path;
import de.willuhn.jameica.webadmin.rest.AutoRestBean;
import de.willuhn.logging.Logger;

/**
 * REST-Bean fuer den Zugriff auf eine ganze Messung.
 */
@Doc("jameica.sensors: Bietet Zugriff auf alle Messwerte einer Messung")
public class Measurement implements AutoRestBean
{
  private static DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  /**
   * Liefert alle Messwerte aller Devices.
   * @return alle Messwerte.
   * @throws Exception
   */
  @Doc(value="Liefert alle Messwerte aller Devices",
       example="measurement")
  @Path("/measurement")
  public JSONArray measurement() throws Exception
  {
    try
    {
      Map<Device,de.willuhn.jameica.sensors.devices.Measurement> values = LiveMeasurement.getValues();

      // Liste der Devices
      List<JSONObject> list = new ArrayList<JSONObject>();
      
      Iterator<Device> devices = values.keySet().iterator();
      while (devices.hasNext())
      {
        Device device = devices.next();
        de.willuhn.jameica.sensors.devices.Measurement m = values.get(device);

        // Device
        Map map = new HashMap();
        map.put("name",device.getName());
        map.put("uuid",device.getUuid());
        map.put("date",dateformat.format(m.getDate()));

        // Sensor-Gruppen
        List<JSONObject> list2 = new ArrayList<JSONObject>();
        List<Sensorgroup> groups = m.getSensorgroups();
        for (Sensorgroup group:groups)
        {
          Map map2 = new HashMap();
          map2.put("name",group.getName());
          map2.put("uuid",group.getUuid());
          
          List<JSONObject> list3 = new ArrayList<JSONObject>();
          List<Sensor> sensors = group.getSensors();
          for (Sensor sensor:sensors)
          {
            Map map3 = new HashMap();
            map3.put("name",sensor.getName());
            map3.put("uuid",sensor.getUuid());
            map3.put("type",sensor.getType().toString());
            
            Object value = sensor.getValue();
            String text = "";
            if (value != null)
            {
              try
              {
                // Haben wir einen Serializer?
                Class<Serializer> serializer = sensor.getSerializer();
                if (serializer != null)
                {
                  Serializer sl = serializer.newInstance();
                  text = sl.format(value);
                }
                else
                {
                  text = value.toString();
                }
              }
              catch (Exception e)
              {
                Logger.error("unable to unserialize value " + value,e);
              }
            }
            map3.put("value",text);
            
            // Noch vermerken, ob der Sensor ausserhalb der Norm ist
            map3.put("outsidelimit",LimitMessageConsumer.outsideLimit(sensor.getUuid()));
            list3.add(new JSONObject(map3));
          }
          map2.put("sensors",list3); // Liste der Sensoren in die Gruppe tun
          list2.add(new JSONObject(map2)); // Sensorgruppe zur Liste tun
        }
        map.put("groups",list2); // Liste der Sensorgruppen ins Device tun
        list.add(new JSONObject(map)); // Device in die Gesamtliste tun
      }

      return new JSONArray(list);
    }
    catch (Exception e)
    {
      Logger.error("unable to load measurements",e);
      throw new IOException("unable to load measurements: " + e.getMessage());
    }
  }
}



/**********************************************************************
 * $Log: Measurement.java,v $
 * Revision 1.4  2011/09/13 09:08:34  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.3  2011-02-15 16:56:10  willuhn
 * @N Vermerken, ob Sensor ausserhalb Limit
 *
 * Revision 1.2  2011-02-15 16:35:28  willuhn
 * @B json.jar fehlte noch im Build-Script
 * @N Datum in Ausgabe
 *
 **********************************************************************/