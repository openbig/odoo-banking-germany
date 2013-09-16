/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.scripting/src/de/willuhn/jameica/scripting/Settings.java,v $
 * $Revision: 1.3 $
 * $Date: 2012/04/05 23:22:21 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.scripting;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.scripting.services.ScriptingService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Haelt die Einstellungen des Plugins vor.
 */
public class Settings
{
  /**
   * Die Queue, die nach dem Hinzufuegen eines Scripts benachrichtigt wird.
   */
  public final static String QUEUE_ADDED   = "jameica.scripting.added";
  
  /**
   * Die Queue, die nach dem Entfernen eines Scripts benachrichtigt wird.
   */
  public final static String QUEUE_REMOVED = "jameica.scripting.removed";
  
  private final static de.willuhn.jameica.system.Settings settings = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();
  
  private static ScriptEngine engine = null;
  
  /**
   * Liefert die zu verwendende Script-Engine oder NULL, wenn keine existiert.
   * @return die zu verwendende Script-Engine oder NULL, wenn keine existiert.
   */
  public static ScriptEngine getScriptEngine()
  {
    if (engine == null)
    {
      ScriptEngineManager factory = new ScriptEngineManager();
      engine = factory.getEngineByName("JavaScript");
    }
    return engine;
  }
  
  /**
   * Liefert die Liste der vom User registrierten Scripts.
   * @return Liste der vom User registrierten Scripts.
   * Niemals NULL sondern hoechstens eine leere Liste.
   */
  public static List<File> getScripts()
  {
    String[] list = settings.getList("scripts",new String[0]);
    List<File> files = new ArrayList<File>();
    for (String s:list)
    {
      if (s == null || s.length() == 0)
        continue;
      files.add(new File(s));
    }
    
    Collections.sort(files); // Sortiert sieht schoener aus
    
    return files;
  }
  
  /**
   * Prueft, ob das angegebene Script bereits hinzugefuegt wurde.
   * @param file das zu pruefende Script.
   * @return true, wenn es bereits vorhanden ist.
   * @throws IOException
   */
  public static boolean contains(File file) throws IOException
  {
    List<File> existing = getScripts();
    for (File f:existing)
    {
      if (f.getCanonicalPath().equals(file.getCanonicalPath()))
        return true;
    }
    
    return false;
  }
  
  /**
   * Fuegt ein neues Script hinzu.
   * @param file das hinzuzufuegende Script.
   * @throws ApplicationException wenn die Script-Datei bereits vorhanden ist.
   */
  public static void addScript(File file) throws ApplicationException
  {
    if (file == null)
      return;
    
    I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();

    // Checken, ob wir das Script schon haben
    try
    {
      if (contains(file))
        throw new ApplicationException(i18n.tr("Script-Datei {0} ist bereits registriert",file.getAbsolutePath()));
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to convert path " + file + " to canonical form",e);
      throw new ApplicationException(i18n.tr("Fehler beim Import der Script-Datei {0}: {1}",new String[]{file.getAbsolutePath(),e.getMessage()}));
    }
    
    List<File> existing = getScripts();
    Logger.info("adding script " + file);
    existing.add(file); // add to data
    
    List<String> list = new ArrayList<String>();
    for (File f:existing)
    {
      list.add(f.getAbsolutePath()); // store
    }
    settings.setAttribute("scripts",list.toArray(new String[list.size()]));
    
    reload();

    // Via Messaging Bescheid geben, dass wir das Script hinzugefuegt haben.
    Application.getMessagingFactory().getMessagingQueue(QUEUE_ADDED).sendMessage(new QueryMessage(file));
  }
  
  /**
   * Startet den Scripting-Service neu, damit die Scripts neu geladen werden.
   */
  public static void reload()
  {
    try
    {
      ScriptingService service = (ScriptingService) Application.getServiceFactory().lookup(Plugin.class,"scripting");
      if (service.isStarted())
      {
        Logger.info("restarting scripting service");
        service.stop(true);
        service.start();
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to restart scripting service",e);
    }
  }
  
  /**
   * Entfernt das Script.
   * @param file das zu entfernende Script.
   */
  public static void removeScript(File file)
  {
    if (file == null)
      return;
    
    List<File> existing = getScripts();
    List<String> newList  = new ArrayList<String>();

    for (File f:existing)
    {
      try
      {
        if (f.getCanonicalPath().equals(file.getCanonicalPath()))
          continue; // Das ist das zu entfernende
        newList.add(f.getAbsolutePath());
      }
      catch (Exception e)
      {
        Logger.error("unable to convert path " + file + " to canonical form",e);
      }
    }

    if (existing.size() == newList.size()) // keine Aenderungen
      return;
    
    Logger.info("removing script " + file);
    settings.setAttribute("scripts",newList.toArray(new String[newList.size()]));

    reload();

    // Via Messaging Bescheid geben, dass wir das Script entfernt haben.
    Application.getMessagingFactory().getMessagingQueue(QUEUE_REMOVED).sendMessage(new QueryMessage(file));
  }
}



/**********************************************************************
 * $Log: Settings.java,v $
 * Revision 1.3  2012/04/05 23:22:21  willuhn
 * @C ApplicationException weiterwerfen
 *
 * Revision 1.2  2012/03/20 23:14:13  willuhn
 * @N Support zum Registrieren von Scripts via Messaging (erster Teil von BUGZILLA 1208)
 *
 * Revision 1.1  2010-07-23 14:41:03  willuhn
 * @N Hinzufuegen und Entfernen von Scripts via GUI
 *
 **********************************************************************/