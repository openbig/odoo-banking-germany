/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/service/impl/ArchiveImpl.java,v $
 * $Revision: 1.13 $
 * $Date: 2012/03/28 22:28:18 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.service.impl;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.beans.Device;
import de.willuhn.jameica.sensors.beans.Value;
import de.willuhn.jameica.sensors.config.Configurable;
import de.willuhn.jameica.sensors.config.Parameter;
import de.willuhn.jameica.sensors.devices.Measurement;
import de.willuhn.jameica.sensors.devices.Sensor;
import de.willuhn.jameica.sensors.devices.Sensorgroup;
import de.willuhn.jameica.sensors.devices.Serializer;
import de.willuhn.jameica.sensors.messaging.MeasureMessage;
import de.willuhn.jameica.sensors.service.Archive;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung des Archiv-Services.
 */
public class ArchiveImpl implements Archive, Configurable
{
  private EntityManager entityManager = null;
  private MessageConsumer consumer    = null;

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName()
  {
    return "archive service";
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
    return this.consumer != null;
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
    if (!settings.getBoolean("hibernate.enabled",false))
    {
      Logger.info("archive service disabled");
      return;
    }

    this.consumer = new MyMessageConsumer();
    Application.getMessagingFactory().registerMessageConsumer(this.consumer);
    Logger.info("archive service started");
  }
  
  /**
   * Liefert den EntityManager.
   * @return der EntityManager.
   */
  private EntityManager getEntityManager()
  {
    if (this.entityManager == null)
    {
      Logger.info("init entity manager");
      Settings settings = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();
      Map params = new HashMap();
      params.put("hibernate.connection.driver_class",settings.getString("hibernate.connection.driver_class","com.mysql.jdbc.Driver"));
      params.put("hibernate.connection.url",settings.getString("hibernate.connection.url","jdbc:mysql://localhost:3306/jameica_sensors?useUnicode=Yes&characterEncoding=ISO8859_1"));
      params.put("hibernate.connection.username",settings.getString("hibernate.connection.username","jameica_sensors"));
      params.put("hibernate.connection.password",settings.getString("hibernate.connection.password","jameica_sensors"));
      params.put("hibernate.dialect",settings.getString("hibernate.dialect","org.hibernate.dialect.MySQLDialect"));
      params.put("hibernate.show_sql",settings.getString("hibernate.show_sql","false"));
      params.put("hibernate.hbm2ddl.auto",settings.getString("hibernate.hbm2ddl.auto","update")); // create,update,validate

      EntityManagerFactory ef = Persistence.createEntityManagerFactory("jameica_sensors",params);
      this.entityManager = ef.createEntityManager();
    }
    return this.entityManager;
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
    
    Application.getMessagingFactory().unRegisterMessageConsumer(this.consumer);
    
    try
    {
      if (this.entityManager != null)
        this.entityManager.close();
    }
    finally
    {
      this.entityManager = null;
      this.consumer = null;
    }
  }
  
  /**
   * Archiviert die Messergebnisse fuer ein Device.
   * @param uuid UUID des Devices.
   * @param m die Messung.
   */
  private void archive(String uuid, Measurement m)
  {
    ClassLoader cl = Application.getPluginLoader().getPlugin(Plugin.class).getManifest().getClassLoader();
    
    EntityTransaction tx = null;
    EntityManager em     = this.getEntityManager();
    
    try
    {
      int count = 0;
      tx = em.getTransaction();
      tx.begin();
      
      
      //////////////////////////////////////////////////////////////////////////
      // Device
      Device d = (Device) findObject("Device",uuid);
      if (d == null)
      {
        Logger.info("adding new device [uuid: " + uuid + "] to archive");
        d = new Device();
        d.setUuid(uuid);
        
        em.persist(d);
      }
      //////////////////////////////////////////////////////////////////////////
      

      //////////////////////////////////////////////////////////////////////////
      // jetzt holen wir alle Sensoren und speichern die Messergebnisse
      // Vor jedem Sensor pruefen wir noch, ob wir ihn schon in der Datenbank haben
      List<Sensorgroup> groups = m.getSensorgroups();
      for (Sensorgroup group:groups)
      {
        // Die Sensor-Gruppe selbst muss nicht archiviert werden. Sie dient
        // nur der strukturierten Ausgabe auf einer GUI
        List<Sensor> sensors = group.getSensors();

        if (sensors == null || sensors.size() == 0)
        {
          Logger.warn("sensor group " + group.getName() + " [uuid: " + group.getUuid() + "] from device [uuid: " + uuid + "] contains no sensor values, skipping");
          continue;
        }
        
        for (Sensor sensor:sensors)
        {
          de.willuhn.jameica.sensors.beans.Sensor archiveSensor = (de.willuhn.jameica.sensors.beans.Sensor) findObject("Sensor",sensor.getUuid());
          if (archiveSensor == null)
          {
            Logger.info("adding new sensor [uuid: " + sensor.getUuid() + "] to archive");
            archiveSensor = new de.willuhn.jameica.sensors.beans.Sensor();
            archiveSensor.setUuid(sensor.getUuid());

            // Serializer nicht vergessen
            Class serializer = sensor.getSerializer();
            archiveSensor.setSerializer(serializer != null ? serializer.getName() : null);
            
            // Sensor zum Device hinzufuegen
            d.getSensors().add(archiveSensor);
            em.persist(archiveSensor);
          }
          
          // Messwerte speichern
          try
          {
            Serializer s = (Serializer) cl.loadClass(archiveSensor.getSerializer()).newInstance();
            Value value = new Value();
            value.setDate(m.getDate()); // Datum aus Messung uebernehmen
            value.setValue(s.serialize(sensor.getValue()));
            archiveSensor.getValues().add(value);

            em.persist(value);
            count++;
          }
          catch (Exception e)
          {
            // Wegen einem einzelnen Messwert brechen wir nicht ab.
            Logger.error("unable to serialize sensor value",e);
          }
        }
      }

      
      // OK, alles speichern
      tx.commit();
      Logger.info("added " + count + " values to archive");
    }
    catch (PersistenceException pe)
    {
      if (tx != null && tx.isActive())
        tx.rollback();
      throw pe;
    }
  }
  
  /**
   * Sucht das angegebene Objekt im Archiv.
   * @param table Tabellenname.
   * @param uuid die UUID des Objekts.
   * @return der Sensor aus dem Archiv oder NULL, wenn er da noch nicht existiert.
   */
  private Object findObject(String table, String uuid)
  {
    try
    {
      Query q = getEntityManager().createQuery("from " + table + " where uuid = ?");
      q.setParameter(1,uuid);
      return q.getSingleResult();
    }
    catch (NoResultException e)
    {
      // ignore
    }
    return null;
  }
  
  /**
   * Mit dem Message-Consumer abonnieren wir die aktuellen Messwerte fuer die Archivierung.
   */
  private class MyMessageConsumer implements MessageConsumer
  {

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
     */
    public boolean autoRegister()
    {
      return false;
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
     */
    public Class[] getExpectedMessageTypes()
    {
      return new Class[]{MeasureMessage.class};
    }

    /**
     * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
     */
    public void handleMessage(Message message) throws Exception
    {
      MeasureMessage msg = (MeasureMessage) message;
      archive(msg.getDevice().getUuid(),msg.getMeasurement());
    }
  }

  /**
   * @see de.willuhn.jameica.sensors.config.Configurable#getParameters()
   */
  public List<Parameter> getParameters()
  {
    I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
    Settings settings = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();
    

    List<Parameter> params = new ArrayList<Parameter>();
    params.add(new Parameter(i18n.tr("Archiv-Service aktiviert"),i18n.tr("Aktiviert/Deaktiviert das Schreiben der Messwerte in die Datenbank. Mögliche Werte: true/false"),settings.getString("hibernate.enabled","false"),"hibernate.enabled"));
    params.add(new Parameter(i18n.tr("JDBC-Treiber"),i18n.tr("Für MySQL z.Bsp. com.mysql.jdbc.Driver"),settings.getString("hibernate.connection.driver_class","com.mysql.jdbc.Driver"),"hibernate.connection.driver_class"));
    params.add(new Parameter(i18n.tr("JDBC-URL"),i18n.tr("Für MySQL z.Bsp. jdbc:mysql://localhost:3306/jameica_sensors"),settings.getString("hibernate.connection.url","jdbc:mysql://localhost:3306/jameica_sensors?useUnicode=Yes&characterEncoding=ISO8859_1"),"hibernate.connection.url"));
    params.add(new Parameter(i18n.tr("JDBC-Username"),i18n.tr("Name des Datenbank-Benutzers"),settings.getString("hibernate.connection.username","jameica_sensors"),"hibernate.connection.username"));
    params.add(new Parameter(i18n.tr("JDBC-Passwort"),i18n.tr("Passwort des Datenbank-Benutzers"),settings.getString("hibernate.connection.password","jameica_sensors"),"hibernate.connection.password"));
    params.add(new Parameter(i18n.tr("Hibernate-Dialekt"),i18n.tr("Für MySQL z.Bsp. org.hibernate.dialect.MySQLDialect"),settings.getString("hibernate.dialect","org.hibernate.dialect.MySQLDialect"),"hibernate.dialect"));
    return params;
  }

  /**
   * @see de.willuhn.jameica.sensors.config.Configurable#setParameters(java.util.List)
   */
  public void setParameters(List<Parameter> parameters)
  {
    Settings settings = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();

    int count = 0;
    for (Parameter p:parameters)
    {
      String id = p.getUuid();
      
      String oldValue = settings.getString(id,null);
      String newValue = p.getValue();
      
      String s1 = oldValue == null ? "" : oldValue;
      String s2 = newValue == null ? "" : newValue;
      if (!s1.equals(s2))
      {
        Logger.info("parameter \"" + p.getName() + "\" [" + id + "] changed. old value: " + oldValue + ", new value: " + newValue);
        settings.setAttribute(id,newValue);
        count++;
      }
    }
    
    if (count > 0)
    {
      Logger.info("restarting archive service");
      try
      {
        if (this.isStarted())
          this.stop(true);
        if (!this.isStarted())
          this.start();
      }
      catch (RemoteException re)
      {
        Logger.error("unable to restart archive service",re);
      }
    }
  }
}


/**********************************************************************
 * $Log: ArchiveImpl.java,v $
 * Revision 1.13  2012/03/28 22:28:18  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.12  2010/03/01 00:19:04  willuhn
 * *** empty log message ***
 *
 * Revision 1.11  2009/09/15 17:00:17  willuhn
 * @N Konfigurierbarkeit aller Module ueber das Webfrontend
 *
 * Revision 1.10  2009/08/24 10:34:44  willuhn
 * @N Archiv-Service nur starten, wenn konfiguriert
 *
 * Revision 1.9  2009/08/21 17:27:37  willuhn
 * @N RRD-Service
 *
 * Revision 1.8  2009/08/21 13:40:44  willuhn
 * @N DB-Zugangsdaten konfigurierbar
 *
 * Revision 1.7  2009/08/21 13:34:17  willuhn
 * @N Redesign der Device-API
 * @N Cleanup in Persistierung
 * @B Bugfixing beim Initialisieren des EntityManagers
 *
 * Revision 1.6  2009/08/21 00:43:03  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2009/08/20 23:26:25  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2009/08/20 23:26:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2009/08/20 22:08:42  willuhn
 * @N Erste komplett funktionierende Version der Persistierung
 *
 * Revision 1.2  2009/08/20 18:07:43  willuhn
 * @N Persistierung funktioniert rudimentaer
 *
 * Revision 1.1  2009/08/19 23:46:29  willuhn
 * @N Erster Code fuer die JPA-Persistierung
 *
 **********************************************************************/
