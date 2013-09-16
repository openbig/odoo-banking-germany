/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/service/impl/SchedulerImpl.java,v $
 * $Revision: 1.5 $
 * $Date: 2009/08/25 11:47:44 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.service.impl;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.devices.Device;
import de.willuhn.jameica.sensors.devices.DeviceRegistry;
import de.willuhn.jameica.sensors.devices.Measurement;
import de.willuhn.jameica.sensors.messaging.MeasureMessage;
import de.willuhn.jameica.sensors.service.Scheduler;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;

/**
 * Implementierung des Scheduler-Services.
 */
public class SchedulerImpl implements Scheduler
{
  private Timer timer = null;
  private Worker worker = null;

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "scheduler service";
  }

  /**
   * @see de.willuhn.datasource.Service#isStartable()
   */
  public boolean isStartable() throws RemoteException
  {
    return !this.isStarted();
  }

  /**
   * @see de.willuhn.datasource.Service#isStarted()
   */
  public boolean isStarted() throws RemoteException
  {
    return this.timer != null && this.worker != null;
  }

  /**
   * @see de.willuhn.datasource.Service#start()
   */
  public void start() throws RemoteException
  {
    if (this.isStarted())
    {
      Logger.warn("service allready started, skipping request");
      return;
    }

    Settings settings = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();
    int minutes = settings.getInt("scheduler.interval.minutes",5);
    Logger.info("scheduler interval: " + minutes + " minutes");

    this.timer = new Timer(getName(),true);
    this.worker = new Worker();

    Logger.info("starting scheduler worker thread");
    // Wir fangen erst nach 10 Sekunden mit dem ersten Durchlauf an. Dann
    // hat das System genug Zeit zu Ende zu starten.
    this.timer.schedule(this.worker,10 * 1000L, minutes * 60 * 1000L);
  }

  /**
   * @see de.willuhn.datasource.Service#stop(boolean)
   */
  public void stop(boolean arg0) throws RemoteException
  {
    if (!this.isStarted())
    {
      Logger.warn("service not started, skipping request");
      return;
    }

    if (this.worker != null)
    {
      try
      {
        Logger.info("stopping worker thread");
        this.worker.cancel();
      }
      catch (Exception e) {
        Logger.error("error while stopping worker thread",e);
      }
      finally
      {
        this.worker = null;
      }
    }

    if (this.timer != null)
    {
      try
      {
        Logger.info("stopping timer task");
        this.timer.cancel();
      }
      catch (Exception e) {
        Logger.error("error while stopping timer task",e);
      }
      finally
      {
        this.timer = null;
      }
    }
  }

  /**
   * Unser Worker.
   */
  private class Worker extends TimerTask
  {
    /**
     * @see java.util.TimerTask#run()
     */
    public void run()
    {
      try
      {
        List<Device> devices = DeviceRegistry.getDevices();
        for (Device d:devices)
        {
          String name = d.getName();

          if (!d.isEnabled())
          {
            Logger.debug("skipping device " + name + " - not configured or disabled");
            continue;
          }
          
          try
          {
            Measurement m = d.collect();
            if (m == null)
            {
              Logger.debug("skipping device " + name + " - returned no data");
              continue;
            }
            
            if (m.getDate() == null) m.setDate(new Date());
            
            Logger.info("collected data from device: " + name);
            Application.getMessagingFactory().sendMessage(new MeasureMessage(d,m));
          }
          catch (IOException e)
          {
            Logger.error("error while collecting data from device " + name,e);
          }
        }
      }
      catch (Exception e)
      {
        Logger.error("error while collecting device data, stopping scheduler",e);
        try
        {
          stop(true);
        }
        catch (Exception e2)
        {
          Logger.error("error while stopping scheduler",e2);
        }
      }
    }
  }
}


/**********************************************************************
 * $Log: SchedulerImpl.java,v $
 * Revision 1.5  2009/08/25 11:47:44  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2009/08/21 14:26:00  willuhn
 * @N null als Rueckgabewert tolerieren
 *
 * Revision 1.3  2009/08/21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 * Revision 1.2  2009/08/19 23:46:29  willuhn
 * @N Erster Code fuer die JPA-Persistierung
 *
 * Revision 1.1  2009/08/19 10:34:43  willuhn
 * @N initial import
 *
 * Revision 1.2  2009/08/18 23:00:25  willuhn
 * @N Erste Version mit Web-Frontend
 *
 * Revision 1.1  2009/08/18 16:29:19  willuhn
 * @N DIE SCHEISSE GEHT! ;)
 *
 **********************************************************************/
