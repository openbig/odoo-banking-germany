/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/notify/RuleProcessor.java,v $
 * $Revision: 1.19 $
 * $Date: 2011/02/18 12:29:41 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.notify;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import net.n3.nanoxml.IXMLElement;
import net.n3.nanoxml.IXMLParser;
import net.n3.nanoxml.StdXMLReader;
import net.n3.nanoxml.XMLParserFactory;
import de.willuhn.io.FileFinder;
import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.devices.Measurement;
import de.willuhn.jameica.sensors.devices.Sensor;
import de.willuhn.jameica.sensors.devices.Sensorgroup;
import de.willuhn.jameica.sensors.devices.Serializer;
import de.willuhn.jameica.sensors.devices.StringSerializer;
import de.willuhn.jameica.sensors.messaging.LimitMessage;
import de.willuhn.jameica.sensors.notify.notifier.Notifier;
import de.willuhn.jameica.sensors.notify.operator.Operator;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.util.XPathEmu;
import de.willuhn.logging.Logger;

/**
 * Durchsucht das Workverzeichnis nach Notify-Regeln in Form von XML-Dateien
 * und verarbeitet diese.
 */
public class RuleProcessor
{
  private Hashtable<String,Date> log = new Hashtable<String,Date>();
  
  /**
   * Fuehrt die Regelverarbeitung fuer die uebergebene Messung durch.
   * @param m die Messung.
   */
  public void process(Measurement m)
  {
    List<Rule> rules = this.findRules();
    for (Rule r:rules)
    {
      try
      {
        handleRule(m,r);
      }
      catch (Exception e)
      {
        Logger.error(e.getMessage(),e);
      }
    }
    
    
    // Wir machen noch ein Cleanup im Log. Alles, was aelter als
    // 24h ist, werfen wir raus. Dann kommt eine erneute Benachrichtigung,
    // wenn der Sensor nach einem Tag immer noch ungueltige Werte liefert.
    Enumeration<String> e = this.log.keys();
    long now = System.currentTimeMillis();
    while (e.hasMoreElements())
    {
      String key = e.nextElement();
      Date last = this.log.get(key);
      if (now - last.getTime() > (24 * 60 * 60 * 1000L))
      {
        Logger.info("removing key " + key + " from log, sensor out of limit since " + last);
        this.log.remove(key);
      }
    }
  }
  
  /**
   * Bearbeitet die Benachrichtigungsregel.
   * @param m die Messung.
   * @param r die Regel.
   * throws Exception
   */
  private void handleRule(Measurement m, Rule r) throws Exception
  {
    ////////////////////////////////////////////////////////////////////////
    // NULL-Checks
    if (m == null)
      throw new Exception("no measurement given");
    
    if (r == null)
      throw new Exception("no rule given");

    Sensor s = findSensor(m,r.getSensor());
    if (s == null)
      return; // darf passieren, wenn der Sensor in einer anderen Messung steht.

    Notifier n    = r.getNotifier();
    Operator o    = r.getOperator();
    String limit  = getLimit(m,r);
    Sensorgroup g = findGroup(m,r.getSensor());
    
    if (o == null)
      throw new Exception("rule for sensor " + r.getSensor() + " has no operator");

    if (limit == null || limit.length() == 0)
      throw new Exception("rule for sensor " + r.getSensor() + " has no limit");
    
    ////////////////////////////////////////////////////////////////////////
    
    ////////////////////////////////////////////////////////////////////////
    // Serializer
    Class<? extends Serializer> c = s.getSerializer();
    if (c == null)
      c = StringSerializer.class;
    Serializer serializer = c.newInstance();
    //
    ////////////////////////////////////////////////////////////////////////
    
    String name = s.getName();
    if (g != null)
      name = g.getName() + " - " + name;
    
    // Benutzerdefinierte Betreff-Zeile
    
    String subject = "[" + Application.getPluginLoader().getManifest(Plugin.class).getName() + "][" + name + "] ";
    String body = "Sensor name  : " + name + "\n" +
                  "Sensor uuid  : " + s.getUuid() + "\n\n" +
                  "Current Value: " + serializer.format(s.getValue()) + "\n" +
                  "Limit        : " + limit;

    String id = r.getID();
    Date last = log.get(id);
    
    boolean outside = o.matches(s,limit);

    // Wir geben noch via Messaging Bescheid, ob der Sensor ausserhalb oder innerhalb des Limits ist.
    Application.getMessagingFactory().sendMessage(new LimitMessage(s,outside));

    if (outside)
    {
      if (last != null) // war vorher schon ausgefallen
        subject += "STILL ";
      else
        log.put(id,new Date()); // Wir tragen den Vorfall ins Log ein

      subject += "OUTSIDE limit. current value: " + serializer.format(s.getValue()) + ", limit: " + limit;
      Logger.info(subject);
  
      // Versenden, wenn Notifier vorhanden
      if (n != null)
      {
        String subjectOutside = r.getParams().get("mail.subject.outside");
        n.outsideLimit(subjectOutside != null && subjectOutside.length() > 0 ? subjectOutside : subject,body,r.getParams(),last);
      }
    }
    else if (last != null) // Sensor ist soeben wieder in den Normbereich zurueckgekehrt
    {
      log.remove(id); // wir entfernen ihn aus dem Log

      subject += "INSIDE limit. current value: " + serializer.format(s.getValue()) + ", limit: " + limit;
      Logger.info(subject);
      
      // Versenden, wenn Notifier vorhanden
      if (n != null)
      {
        String subjectInside  = r.getParams().get("mail.subject.inside");
        n.insideLimit(subjectInside != null && subjectInside.length() > 0 ? subjectInside : subject,body,r.getParams());
      }
    }
  }
  
  /**
   * Durchsucht die Messung nach dem angegebenen Sensor.
   * @param m die Messung.
   * @param uuid der Sensor.
   * @return der Sensor oder NULL, wenn er nicht gefunden wurde.
   */
  private Sensor findSensor(Measurement m, String uuid)
  {
    if (uuid == null || uuid.length() == 0)
    {
      Logger.warn("no sensor uuid given, skipping");
      return null;
    }
    
    List<Sensorgroup> groups = m.getSensorgroups();
    for (Sensorgroup g:groups)
    {
      List<Sensor> sensors = g.getSensors();
      for (Sensor s:sensors)
      {
        if (uuid.equals(s.getUuid()))
          return s;
      }
    }
    
    Logger.debug("sensor uuid " + uuid + " not found in measurement");
    return null;
  }
  
  /**
   * Sucht die Sensor-Gruppe des Sensors.
   * @param m die Messung.
   * @param sensorUuid die UUID des Sensors.
   * @return die Sensor-Gruppe oder NULL, wenn sie nicht gefunden wurde.
   */
  private Sensorgroup findGroup(Measurement m, String sensorUuid)
  {
    if (sensorUuid == null || sensorUuid.length() == 0)
    {
      Logger.warn("no sensor uuid given, skipping");
      return null;
    }
    
    List<Sensorgroup> groups = m.getSensorgroups();
    for (Sensorgroup g:groups)
    {
      List<Sensor> sensors = g.getSensors();
      for (Sensor s:sensors)
      {
        if (sensorUuid.equals(s.getUuid()))
          return g;
      }
    }
    
    Logger.debug("group for sensor uuid " + sensorUuid + " not found in measurement");
    return null;
  }
  
  /**
   * Liefert den Limit-Wert der Regel.
   * Die Funktion evaluiert evtl. vorhandene Ausdruecke im Limit-Wert.
   * @param m die Messung.
   * @param r die Regel.
   * @return der Limit-Wert.
   * @throws Exception
   */
  private String getLimit(Measurement m, Rule r) throws Exception
  {
    String limit = r.getLimit();
    if (limit != null && limit.matches("\\{.*\\}"))
    {
      // Limit verweist auf den Wert eines anderen Sensors.
      String uuid = limit.substring(0,limit.length()-1).substring(1);
      Sensor s = findSensor(m,uuid);
      if (s == null)
        return null;
      Object o = s.getValue();
      Class<? extends Serializer> c = s.getSerializer();
      if (c == null)
        c = StringSerializer.class;
      limit = o != null ? c.newInstance().format(o) : null;
    }
    return limit;
  }
  
  /**
   * Liefert die gefundenen Regeln.
   * @return Liste der gefundenen Regeln.
   */
  private List<Rule> findRules()
  {
    List<Rule> rules = new ArrayList<Rule>();

    // Wir suchen im Pluginverzeichnis und im Work-Verzeichnis. Jeweils
    // im Unterverzeichnis "rules"
    
    File sys = new File(Application.getPluginLoader().getManifest(Plugin.class).getPluginDir(),"rules");
    rules.addAll(findRules(sys));
    
    File user = new File(Application.getPluginLoader().getPlugin(Plugin.class).getResources().getWorkPath(),"rules");
    if (!user.exists())
    {
      Logger.info("creating " + user);
      user.mkdirs();
    }
    rules.addAll(findRules(user));
    
    return rules;
  }
  
  /**
   * Liefert die Regeln im angegebenen Verzeichnis.
   * @param dir das Verzeichnis, welches nach Regeln durchsucht werden soll.
   * @return Liste der gefundenen Regeln.
   */
  private List<Rule> findRules(File dir)
  {
    List<Rule> rules = new ArrayList<Rule>();
    
    try
    {
      FileFinder finder = new FileFinder(dir);
      finder.extension("xml");
      File[] files = finder.findRecursive();
      
      // Wir sortieren die gefundenen Dateien jetzt noch alphabetisch
      Arrays.sort(files);
      
      for (File f:files)
      {
        InputStream is = null;
        try
        {
          is = new BufferedInputStream(new FileInputStream(f));
          IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
          parser.setReader(new StdXMLReader(is));
          XPathEmu xpath = new XPathEmu((IXMLElement) parser.parse());
          IXMLElement[] list = xpath.getElements("rule");
          for (IXMLElement i:list)
          {
            Rule r = new Rule(i);
            if (r.isEnabled())
              rules.add(r);
          }
        }
        catch (Exception e)
        {
          Logger.error("error while reading file " + f,e);
        }
        finally
        {
          if (is != null)
          {
            try
            {
              is.close();
            }
            catch (Exception e)
            {
              Logger.error("error while closing file " + f,e);
            }
          }
        }
      }
    }
    catch (Exception e)
    {
      Logger.error("error while searching for notify rules in " + dir,e);
    }
    return rules;
  }
}



/**********************************************************************
 * $Log: RuleProcessor.java,v $
 * Revision 1.19  2011/02/18 12:29:41  willuhn
 * @N Regel-Operatoren umgebaut. Es gibt jetzt auch einen "Outside"-Operator mit dessen Hilfe eine Unter- UND Obergrenze in EINER Regel definiert werden kann
 *
 * Revision 1.18  2011-02-17 23:47:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2011-02-17 22:43:14  willuhn
 * *** empty log message ***
 *
 * Revision 1.16  2011-02-17 22:38:51  willuhn
 * *** empty log message ***
 *
 * Revision 1.15  2011-02-17 22:12:39  willuhn
 * *** empty log message ***
 *
 * Revision 1.14  2011-02-17 18:08:45  willuhn
 * *** empty log message ***
 *
 * Revision 1.13  2011-02-14 16:04:51  willuhn
 * @N Messwerte hervorheben, die ausserhalb des Limits liegen
 *
 * Revision 1.12  2010-03-24 12:09:35  willuhn
 * @N Benutzerdefinierte Betreff-Zeile
 *
 * Revision 1.11  2010/03/23 18:35:45  willuhn
 * @N Rule serialisierbar
 *
 * Revision 1.10  2010/03/02 14:07:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.9  2010/03/02 14:03:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.8  2010/03/02 13:55:51  willuhn
 * @N Encoding
 * @N Sensor-Gruppe mit anzeigen
 *
 * Revision 1.7  2010/03/02 13:00:36  willuhn
 * @N Cleanup verwaister Sensoren im Log
 *
 * Revision 1.6  2010/03/02 12:43:52  willuhn
 * @C Ausfall-Log nicht mehr persistieren
 *
 * Revision 1.5  2010/03/02 00:43:54  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2010/03/02 00:28:41  willuhn
 * @B bugfixing
 *
 * Revision 1.3  2010/03/01 23:51:07  willuhn
 * @N Benachrichtigung, wenn Sensor zurueck im normalen Bereich ist
 * @N Merken des letzten Notify-Status, sodass nur beim ersten mal eine Mail gesendet wird
 *
 * Revision 1.2  2010/03/01 18:12:23  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2010/03/01 17:08:18  willuhn
 * @N Mail-Benachrichtigung via javax.mail
 *
 * Revision 1.1  2010/03/01 13:16:12  willuhn
 * @N Erster Code fuer automatische Benachrichtigungen bei Limit-Ueberschreitungen von Sensoren.
 *
 **********************************************************************/