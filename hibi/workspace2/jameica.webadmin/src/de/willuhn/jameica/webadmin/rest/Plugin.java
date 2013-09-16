/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/rest/Plugin.java,v $
 * $Revision: 1.16 $
 * $Date: 2011/09/13 09:08:31 $
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.plugin.Dependency;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.webadmin.annotation.Doc;
import de.willuhn.jameica.webadmin.annotation.Path;
import de.willuhn.jameica.webadmin.annotation.Request;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;


/**
 * REST-Kommando fuer den Zugriff auf Plugins.
 */
@Doc("System: Liefert Informationen über die installierten Plugins")
public class Plugin implements AutoRestBean
{
  private final static I18N i18n = de.willuhn.jameica.system.Application.getPluginLoader().getPlugin(de.willuhn.jameica.webadmin.Plugin.class).getResources().getI18N();

  @Request
  private HttpServletRequest request = null;
  
  /**
   * Action-Methode zum Starten des angegebenen Services.
   * @throws IOException
   */
  public void start() throws IOException
  {
    String plugin = request.getParameter("plugin");
    String service = request.getParameter("service");
    try
    {
      if (plugin == null || plugin.length() == 0)
        throw new ApplicationException(i18n.tr("Kein Plugin angegeben"));

      if (service == null || service.length() == 0)
        throw new ApplicationException(i18n.tr("Kein Service angegeben"));
      
      de.willuhn.datasource.Service s = new Service().find(plugin,service);
      s.start();
      
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Service gestartet"),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
    catch (Exception e)
    {
      Logger.error("error while starting service",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Starten des Services: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * Action-Methode zum Stoppen des angegebenen Services.
   * @throws IOException
   */
  public void stop() throws IOException
  {
    String plugin = request.getParameter("plugin");
    String service = request.getParameter("service");
    try
    {
      if (plugin == null || plugin.length() == 0)
        throw new ApplicationException(i18n.tr("Kein Plugin angegeben"));

      if (service == null || service.length() == 0)
        throw new ApplicationException(i18n.tr("Kein Service angegeben"));
      
      de.willuhn.datasource.Service s = new Service().find(plugin,service);
      s.stop(true);
      
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Service gestoppt"),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (ApplicationException ae)
    {
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(ae.getMessage(),StatusBarMessage.TYPE_ERROR));
    }
    catch (Exception e)
    {
      Logger.error("error while stopping service",e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Stoppen des Services: {0}",e.getMessage()),StatusBarMessage.TYPE_ERROR));
    }
  }

  /**
   * Liefert die Details des angegebenen Plugins.
   * @param plugin der Name des Plugins.
   * @return die Details des angegebenen Plugins.
   * @throws IOException
   */
  @Doc(value="Liefert die Details des angegebenen Plugins",
       example="plugins/get/jameica.webadmin")
  @Path("/plugins/get/(.*)$")
  public JSONObject getDetails(String plugin) throws IOException
  {
    if (plugin == null || plugin.length() == 0)
      throw new IOException("no plugin given");
    
    Manifest mf = Application.getPluginLoader().getManifestByName(plugin);
    if (mf == null)
      throw new IOException("plugin " + plugin + " not found");

    Map data = new HashMap();
    data.put("name",        StringUtils.trimToEmpty(mf.getName()));
    data.put("builddate",   StringUtils.trimToEmpty(mf.getBuildDate()));
    data.put("buildnumber", StringUtils.trimToEmpty(mf.getBuildnumber()));
    data.put("description", StringUtils.trimToEmpty(mf.getDescription()));
    data.put("homepage",    StringUtils.trimToEmpty(mf.getHomepage()));
    data.put("license",     StringUtils.trimToEmpty(mf.getLicense()));
    data.put("class",       StringUtils.trimToEmpty(mf.getPluginClass()));
    data.put("plugindir",   StringUtils.trimToEmpty(mf.getPluginDir()));
    data.put("url",         StringUtils.trimToEmpty(mf.getURL()));
    data.put("version",     mf.getVersion());

    ArrayList deps = new ArrayList();
    Dependency[] d = mf.getDependencies();
    if (d != null && d.length > 0)
    {
      for (int k=0;k<d.length;++k)
      {
        Map dep = new HashMap();
        dep.put("name",d[k].getName());
        dep.put("version",d[k].getVersion());
        deps.add(dep);
      }
    }
    data.put("dependencies",deps);

    return new JSONObject(data);
  }
  
  /**
   * Liefert die installierten Plugins.
   * @return die installierten Plugins.
   * @throws IOException
   */
  @Doc(value="Liefert eine Liste der installierten Plugins",
       example="plugins/list")
  @Path("/plugins/list$")
  public JSONArray getList() throws IOException
  {
    List<JSONObject> list = new ArrayList<JSONObject>();
    List plugins = Application.getPluginLoader().getInstalledManifests();
    for (int i=0;i<plugins.size();++i)
    {
      Manifest mf = (Manifest) plugins.get(i);
      list.add(getDetails(mf.getName()));
    }
    return new JSONArray(list);
  }
}


/**********************************************************************
 * $Log: Plugin.java,v $
 * Revision 1.16  2011/09/13 09:08:31  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.15  2010-11-02 00:56:30  willuhn
 * @N Umstellung des Webfrontends auf Velocity/Webtools
 *
 * Revision 1.14  2010/05/12 10:59:20  willuhn
 * @N Automatische Dokumentations-Seite fuer die REST-Beans basierend auf der Annotation "Doc"
 *
 * Revision 1.13  2010/05/11 14:59:48  willuhn
 * @N Automatisches Deployment von REST-Beans
 *
 * Revision 1.12  2010/03/18 09:29:35  willuhn
 * @N Wenn REST-Beans Rueckgabe-Werte liefern, werrden sie automatisch als toString() in den Response-Writer geschrieben
 **********************************************************************/