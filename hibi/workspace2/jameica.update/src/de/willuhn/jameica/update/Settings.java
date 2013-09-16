/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/Settings.java,v $
 * $Revision: 1.5 $
 * $Date: 2009/10/29 18:06:06 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.update;

import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.rmi.UpdateService;
import de.willuhn.logging.Logger;

/**
 * Container fuer die Einstellungen.
 */
public class Settings
{
  /**
   * Die URL des System-Repository.
   */
  public final static String SYSTEM_REPOSITORY = "https://www.willuhn.de/products/jameica/updates";

  /**
   * Die Einstellungen des Plugins.
   */
  private final static de.willuhn.jameica.system.Settings SETTINGS = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();

  /**
   * Liefert eine Liste mit URLs zu Online-Repositories mit Plugins.
   * @return Liste mit URLs zu Online-Repositories mit Plugins.
   */
  public final static List<URL> getRepositories()
  {
    String[] urls = SETTINGS.getList("repository.url",new String[0]);
    List<URL> list = new ArrayList<URL>();
    
    try
    {
      list.add(new URL(SYSTEM_REPOSITORY));
    }
    catch (Exception e)
    {
      Logger.error("SUSPEKT! unable to add system repository " + SYSTEM_REPOSITORY,e);
    }

    for (String url:urls)
    {
      if (url == null || url.length() == 0 || url.equalsIgnoreCase(SYSTEM_REPOSITORY))
        continue;
      try
      {
        list.add(new URL(url));
      }
      catch (Exception e)
      {
        Logger.error("invalid url: " + url + ", skipping",e);
      }
    }
    return list;
  }
  
  /**
   * Speichert die Liste der URLs zu Online-Repositories mit Plugins.
   * @param list Liste der URLs zu Online-Repositories mit Plugins.
   */
  public final static void setRepositories(List<URL> list)
  {
    Map<URL,URL> duplicates = new Hashtable<URL,URL>();
    
    List<String> urls = new ArrayList<String>();
    if (list != null && list.size() > 0)
    {
      for (URL u:list)
      {
        if (duplicates.get(u) != null)
          continue;
        duplicates.put(u,u);
        String s = u.toString();
        if (s == null || s.length() == 0 || s.equalsIgnoreCase(SYSTEM_REPOSITORY))
          continue;
        urls.add(s);
      }
    }
    SETTINGS.setAttribute("repository.url",urls.size() > 0 ? urls.toArray(new String[urls.size()]) : null);
  }
  
  /**
   * Liefert das Intervall (in Tagen), in denen nach Updates gesucht werden soll.
   * @return Intervall in Tagen.
   */
  public final static int getUpdateInterval()
  {
    return SETTINGS.getInt("update.check.days",7);
  }
  
  /**
   * Speichert das Intervall (in Tagen), in denen nach Updates gesucht werden soll.
   * @param days Intervall in Tagen.
   */
  public final static void setUpdateInterval(int days)
  {
    if (days <= 0)
    {
      Logger.warn("invalid update interval");
      return;
    }
    SETTINGS.setAttribute("update.check.days",days);
  }
  
  /**
   * Prueft, ob ueberhaupt regelmaessig nach Updates gesucht werden soll.
   * @return true, wenn regelmaessig nach Updates gesucht werden soll.
   */
  public final static boolean getUpdateCheck()
  {
    return SETTINGS.getBoolean("update.check",false);
  }
  
  /**
   * Legt fest, ob ueberhaupt regelmaessig nach Updates gesucht werden soll.
   * @param b true, wenn regelmaessig nach Updates gesucht werden soll.
   */
  public final static void setUpdateCheck(boolean b)
  {
    SETTINGS.setAttribute("update.check",b);
    try
    {
      UpdateService service = (UpdateService) Application.getServiceFactory().lookup(Plugin.class,"update");
      if (b && !service.isStarted())
        service.start();
      else if (!b && service.isStarted())
        service.stop(true);
    }
    catch (Exception e)
    {
      Logger.error("unable to start update service",e);
    }
  }
  
  /**
   * Prueft, ob Updates automatisch installiert werden sollen oder nur eine
   * Benachrichtigung erfolgen soll.
   * @return true, wenn automatisch installiert werden soll.
   */
  public final static boolean getUpdateInstall()
  {
    return SETTINGS.getBoolean("update.install",false);
  }
  
  /**
   * Legt fest, ob Updates automatisch installiert werden sollen oder nur eine
   * Benachrichtigung erfolgen soll.
   * @param b true, wenn automatisch installiert werden soll.
   */
  public final static void setUpdateInstall(boolean b)
  {
    SETTINGS.setAttribute("update.install",b);
  }
}


/*********************************************************************
 * $Log: Settings.java,v $
 * Revision 1.5  2009/10/29 18:06:06  willuhn
 * @N Manuelle Suche nach Updates
 * @R Test auf last-modified entfernt - war nicht wirklich deterministisch loesbar ;)
 *
 * Revision 1.4  2009/10/28 17:00:58  willuhn
 * @N Automatischer Check nach Updates mit der Wahlmoeglichkeit, nur zu benachrichtigen oder gleich zu installieren
 *
 * Revision 1.3  2009/05/05 23:48:22  willuhn
 * @C geaenderte URL
 *
 * Revision 1.2  2008/12/16 10:46:08  willuhn
 * @N Funktionalitaet zum Hinzufuegen und Loeschen von Repository-URLs
 *
 * Revision 1.1  2008/12/10 00:33:19  willuhn
 * @N initial checkin des Update-Plugins
 *
 **********************************************************************/