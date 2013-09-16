/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/rest/Application.java,v $
 * $Revision: 1.11 $
 * $Date: 2011/06/21 10:03:29 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.webadmin.rest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Config;
import de.willuhn.jameica.webadmin.annotation.Doc;
import de.willuhn.jameica.webadmin.annotation.Path;
import de.willuhn.jameica.webadmin.messaging.StatusBarMessageConsumer;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * REST-Bean zum Abfragen von System-Infos.
 */
@Doc("System: Liefert Statusinformationen und die Systemkonfiguration von Jameica")
public class Application implements AutoRestBean
{
  /**
   * Liefert die Uptime und Startzeit des Servers.
   * @return die Uptime.
   * @throws IOException
   */
  @Doc(value="Liefert die Uptime und Startzeit des Jameica-Servers",
       example="system/uptime")
  @Path("/system/uptime$")
  public JSONObject getUptime() throws IOException
  {
    I18N i18n = de.willuhn.jameica.system.Application.getPluginLoader().getPlugin(de.willuhn.jameica.webadmin.Plugin.class).getResources().getI18N();
    Date started = de.willuhn.jameica.system.Application.getStartDate();

    ////////////////////////////////////////////////////////////////////////////
    // Uptime ausrechnen
    long minutes = (System.currentTimeMillis() - started.getTime()) / 1000L / 60L;
    long hours   = minutes / 60;

    minutes %= 60; // Restminuten abzueglich Stunden

    // ggf. ne "0" vorn dran schreiben
    String mins = (minutes < 10 ? ("0" + minutes) : "" + minutes);

    String uptime = null;
    if (hours < 24) // weniger als 1 Tag?
    {
      uptime = hours + ":" + mins + " h";
    }
    else
    {
      long days = hours / 24;
      uptime = i18n.tr("{0} Tag(e), {1}:{2} h",Long.toString(days),Long.toString(hours % 24),mins);
    }
    ////////////////////////////////////////////////////////////////////////////
    
    Map o = new HashMap();
    o.put("started",new SimpleDateFormat("dd.MM.yyyy HH:mm:ss").format(started));
    o.put("uptime",uptime);

    return new JSONObject(o);
  }

  /**
   * Liefert die System-Konfiguration.
   * @return die System-Konfiguration.
   * @throws IOException
   */
  @Doc(value="Liefert die System-Konfiguration des Jameica-Servers",
       example="system/config")
  @Path("/system/config$")
  public JSONObject getConfig() throws IOException
  {
    try
    {
      Map all = new HashMap();
      
      Config config = de.willuhn.jameica.system.Application.getConfig();
      
      all.put("locale",config.getLocale().toString());
      
      {
        Map map = new HashMap();
        map.put("port",      config.getRmiPort());
        map.put("ssl",       config.getRmiSSL());
        map.put("clientauth",config.getRmiUseClientAuth());
        all.put("rmi",map);
      }

      {
        Map map = new HashMap();
        map.put("dir",    config.getBackupDir());
        map.put("enabled",config.getUseBackup());
        map.put("count",  config.getBackupCount());
        all.put("backup",map);
      }

      {
        Map map = new HashMap();
        map.put("config",config.getConfigDir());
        map.put("work",config.getWorkDir());
        Map plugins = new HashMap();
        plugins.put("system",config.getSystemPluginDir().getAbsolutePath());
        plugins.put("user",  config.getUserPluginDir().getAbsolutePath());
        plugins.put("config",config.getPluginDirs());
        map.put("plugins",plugins);
        all.put("dir",map);
      }
      
      {
        Map map = new HashMap();
        map.put("file",config.getLogFile());
        map.put("level",config.getLogLevel());
        all.put("log",map);
      }
      
      {
        Map map = new HashMap();
        map.put("host",config.getProxyHost());
        map.put("port",config.getProxyPort() == -1 ? "" : String.valueOf(config.getProxyPort()));
        all.put("proxy",map);
      }

      {
        Map map = new HashMap();
        map.put("multicastlookup",config.getMulticastLookup());
        map.put("shareservices",config.getShareServices());
        all.put("service",map);
      }

      return new JSONObject(all);
    }
    catch (ApplicationException ae)
    {
      throw new IOException(ae.getMessage());
    }
  }

  /**
   * Liefert Versions-Informationen zu Jameica.
   * @return Versions-Informationen zu Jameica.
   * @throws IOException
   */
  @Doc(value="Liefert die Versions-Informationen des Jameica-Servers",
       example="system/version")
  @Path("/system/version$")
  public JSONObject getVersion() throws IOException
  {
    Manifest mf = de.willuhn.jameica.system.Application.getManifest();

    Map map = new HashMap();
    map.put("builddate",mf.getBuildDate());
    map.put("buildnumber",mf.getBuildnumber());
    map.put("version",mf.getVersion());
    return new JSONObject(map);
  }

  /**
   * Liefert Host-Informationen zu Jameica.
   * @return Host-Informationen zu Jameica.
   * @throws IOException
   */
  @Doc(value="Liefert die Host-Informationen des Jameica-Servers",
       example="system/host")
  @Path("/system/host$")
  public JSONObject getHost() throws IOException
  {
    try
    {
      Map map = new HashMap();
      map.put("name",de.willuhn.jameica.system.Application.getCallback().getHostname());
      return new JSONObject(map);
    }
    catch (Exception e)
    {
      Logger.error("unable to get host config",e);
      throw new IOException("unable to get host config");
    }
  }

  /**
   * Liefert die Liste der beim Systemstart aufgelaufenen Nachrichten.
   * @return die Systemnachrichten.
   * @throws IOException
   */
  @Doc(value="Liefert eine Liste der beim Systemstart aufgelaufenen Nachrichten des Jameica-Servers",
      example="system/welcome")
  @Path("/system/welcome$")
  public JSONArray getWelcome() throws IOException
  {
    return new JSONArray(Arrays.asList(de.willuhn.jameica.system.Application.getWelcomeMessages()));
  }

  /**
   * Liefert die letzte Statusbar-Message.
   * @return die letzte Statusbar-Message.
   * @throws IOException
   */
  @Doc(value="Liefert die letzte Status-Meldung des Systems",
       example="system/status")
  @Path("/system/status$")
  public JSONObject getStatus() throws IOException
  {
    StatusBarMessage m = StatusBarMessageConsumer.getLastMessage();
    Map map = new HashMap();
    map.put("title",m != null ? m.getTitle() : "");
    map.put("text",m != null ? m.getText() : "");
    map.put("type",m != null ? m.getType() : "");

    return new JSONObject(map);
  }
}


/*********************************************************************
 * $Log: Application.java,v $
 * Revision 1.11  2011/06/21 10:03:29  willuhn
 * @B Beim Klick auf "Zertifikats-Details" wurde u.U. eine NPE angezeigt
 * @N Download von Zertifikaten
 *
 * Revision 1.10  2010-11-02 00:56:31  willuhn
 * @N Umstellung des Webfrontends auf Velocity/Webtools
 *
 * Revision 1.9  2010/05/12 10:59:20  willuhn
 * @N Automatische Dokumentations-Seite fuer die REST-Beans basierend auf der Annotation "Doc"
 *
 * Revision 1.8  2010/05/11 14:59:48  willuhn
 * @N Automatisches Deployment von REST-Beans
 *
 * Revision 1.7  2010/03/18 09:29:35  willuhn
 * @N Wenn REST-Beans Rueckgabe-Werte liefern, werrden sie automatisch als toString() in den Response-Writer geschrieben
 **********************************************************************/