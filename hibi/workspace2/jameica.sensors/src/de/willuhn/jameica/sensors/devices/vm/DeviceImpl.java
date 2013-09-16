/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/devices/vm/DeviceImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/03/02 00:28:41 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.devices.vm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.config.Configurable;
import de.willuhn.jameica.sensors.config.Parameter;
import de.willuhn.jameica.sensors.devices.DecimalSerializer;
import de.willuhn.jameica.sensors.devices.Device;
import de.willuhn.jameica.sensors.devices.Measurement;
import de.willuhn.jameica.sensors.devices.Sensor;
import de.willuhn.jameica.sensors.devices.Sensorgroup;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Das Device gibt Status-Informationen ueber die JVM aus.
 */
public class DeviceImpl implements Device, Configurable
{
  private final static I18N i18n         = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
  private final static Settings settings = new Settings(DeviceImpl.class);

  /**
   * @see de.willuhn.jameica.sensors.devices.Device#collect()
   */
  public Measurement collect() throws IOException
  {
    Measurement m = new Measurement();
    Runtime rt = Runtime.getRuntime();

    {
      Sensorgroup group = new Sensorgroup();
      group.setUuid(this.getUuid() + ".mem");
      group.setName("memory usage");
      
      {
        Sensor<Long> s = new Sensor<Long>();
        s.setSerializer(DecimalSerializer.class);
        s.setUuid(group.getUuid() + ".total");
        s.setName("total memory (MB)");
        s.setValue(rt.totalMemory() / 1024 / 1024);
        group.getSensors().add(s);
      }
      {
        Sensor<Long> s = new Sensor<Long>();
        s.setSerializer(DecimalSerializer.class);
        s.setUuid(group.getUuid() + ".max");
        s.setName("maximum memory (MB)");
        s.setValue(rt.maxMemory() / 1024 / 1024);
        group.getSensors().add(s);
      }
      {
        Sensor<Long> s = new Sensor<Long>();
        s.setSerializer(DecimalSerializer.class);
        s.setUuid(group.getUuid() + ".free");
        s.setName("free memory (MB)");
        s.setValue(rt.freeMemory() / 1024 / 1024);
        group.getSensors().add(s);
      }

      m.getSensorgroups().add(group);
    }

    return m;
  }

  /**
   * @see de.willuhn.jameica.sensors.devices.UniqueItem#getUuid()
   */
  public String getUuid()
  {
    return "jameica.sensors.vm.device";
  }

  /**
   * @see de.willuhn.jameica.sensors.devices.Device#getName()
   */
  public String getName()
  {
    return "JVM Statistics";
  }

  /**
   * @see de.willuhn.jameica.sensors.devices.Device#isEnabled()
   */
  public boolean isEnabled()
  {
    return settings.getBoolean("enabled",true);
  }

  /**
   * @see de.willuhn.jameica.sensors.config.Configurable#getParameters()
   */
  public List<Parameter> getParameters()
  {
    List<Parameter> params = new ArrayList<Parameter>();
    params.add(new Parameter(i18n.tr("JVM-Statistiken aktiviert"),i18n.tr("Liefert Messwerte der JVM, u.a. die Speicher-Auslastung. Mögliche Werte: true/false"),settings.getString("enabled","true"),this.getUuid() + ".enabled"));
    return params;
  }

  /**
   * @see de.willuhn.jameica.sensors.config.Configurable#setParameters(java.util.List)
   */
  public void setParameters(List<Parameter> parameters)
  {
    for (Parameter p:parameters)
    {
      String id = p.getUuid();
      
      // Wir schneiden unsere Device-UUID wieder ab
      id = id.substring(this.getUuid().length()+1); // das "+1" ist fuer den "." als Trennzeichen

      String oldValue = settings.getString(id,null);
      String newValue = p.getValue();
      
      String s1 = oldValue == null ? "" : oldValue;
      String s2 = newValue == null ? "" : newValue;
      if (!s1.equals(s2))
      {
        Logger.info("parameter \"" + p.getName() + "\" [" + id + "] changed. old value: " + oldValue + ", new value: " + newValue);
        settings.setAttribute(id,newValue);
      }
    }
  }
  
  

}


/**********************************************************************
 * $Log: DeviceImpl.java,v $
 * Revision 1.2  2010/03/02 00:28:41  willuhn
 * @B bugfixing
 *
 * Revision 1.1  2009/11/24 14:44:10  willuhn
 * @N Neues Device fuer VM-Stats
 *
 **********************************************************************/
