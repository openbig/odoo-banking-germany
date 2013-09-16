/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/rest/Service.java,v $
 * $Revision: 1.15 $
 * $Date: 2012/03/29 20:54:40 $
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

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.ServiceDescriptor;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.webadmin.annotation.Doc;
import de.willuhn.jameica.webadmin.annotation.Path;
import de.willuhn.logging.Logger;

/**
 * REST-Kommandos zum Starten und Stoppen von Services.
 */
@Doc("System: Bietet Zugriff auf die Services der Plugins.")
public class Service implements AutoRestBean
{
  /**
   * Startet den Service.
   * @param plugin Name des Plugins.
   * @param service Name des Services.
   * @return der Service-Status.
   * @throws IOException
   */
  @Doc(value="Startet den angegebenen Service (meinservicename) des angegebenen Plugins (meinplugin)",
       example="plugins/meinplugin/services/meinservicename/start")
  @Path("/plugins/(.*?)/services/(.*?)/start$")
  public JSONObject start(String plugin, String service) throws IOException
  {
    try
    {
      de.willuhn.datasource.Service s = find(plugin,service);
      s.start();
      return new JSONObject().put("started",Boolean.toString(s.isStarted()));
    }
    catch (IOException e)
    {
      throw e;
    }
    catch (Exception e2)
    {
      Logger.error("unable to start service",e2);
      throw new IOException("unable to start service");
    }
  }
  
  /**
   * Stoppt den Service.
   * @param plugin Name des Plugins.
   * @param service Name des Services.
   * @return der Service-Status.
   * @throws IOException
   */
  @Doc(value="Stoppt den angegebenen Service (meinservicename) des angegebenen Plugins (meinplugin)",
       example="plugins/meinplugin/services/meinservicename/stop")
  @Path("/plugins/(.*?)/services/(.*?)/stop$")
  public JSONObject stop(String plugin, String service) throws IOException
  {
    try
    {
      de.willuhn.datasource.Service s = find(plugin,service);
      s.stop(false);
      return new JSONObject().put("started",Boolean.toString(s.isStarted()));
    }
    catch (IOException e)
    {
      throw e;
    }
    catch (Exception e2)
    {
      Logger.error("unable to stop service",e2);
      throw new IOException("unable to stop service");
    }
  }

  /**
   * Liefert den Service-Status.
   * @param plugin Name des Plugins.
   * @param service Name des Services.
   * @return der Service-Status.
   * @throws IOException
   */
  @Doc(value="Liefert den Status den angegebenen Service (meinservicename) des angegebenen Plugins (meinplugin)",
       example="plugins/meinplugin/services/meinservicename/status")
  @Path("/plugins/(.*?)/services/(.*?)/status$")
  public JSONObject getStatus(String plugin, String service) throws IOException
  {
    try
    {
      de.willuhn.datasource.Service s = find(plugin,service);
      return new JSONObject().put("started",Boolean.toString(s.isStarted()));
    }
    catch (IOException e)
    {
      throw e;
    }
    catch (Exception e2)
    {
      Logger.error("unable to get service status",e2);
      throw new IOException("unable to get service status");
    }
  }

  /**
   * Listet die Services eines Plugins auf.
   * @param plugin Name des Plugins.
   * @return Liste der Services.
   * @throws IOException
   */
  @Doc(value="Liefert eine Liste der Services des angegebenen Plugins (meinplugin)",
       example="plugins/meinplugin/services/list")
  @Path("/plugins/(.*?)/services/list$")
  public JSONArray getList(String plugin) throws IOException
  {
    if (plugin == null || plugin.length() == 0)
      throw new IOException("no plugin given");

    Manifest mf = Application.getPluginLoader().getManifestByName(plugin);
    if (mf == null)
      throw new IOException("plugin " + plugin + " not found");
    
    List<Map> list = new ArrayList<Map>();
    ServiceDescriptor[] services = mf.getServices();
    for (ServiceDescriptor d:services)
    {
      try
      {
        de.willuhn.datasource.Service s = find(plugin,d.getName());

        Map data = new HashMap();
        data.put("name",        StringUtils.trimToEmpty(d.getName()));
        data.put("description", StringUtils.trimToEmpty(s.getName()));
        data.put("class",       StringUtils.trimToEmpty(d.getClassname()));
        data.put("depends",     d.depends());
        data.put("autostart",   d.autostart());
        data.put("shared",      d.share());
        data.put("started",     s.isStarted());
        list.add(data);
      }
      catch (Exception e)
      {
        Logger.error("unable to load service " + d.getName(),e);
      }
    }
    return new JSONArray(list);
  }

  /**
   * Sucht den angegebenen Service im Plugin.
   * @param plugin das Plugin.
   * @param service der Service-Name.
   * @return Instanz des Services.
   * @throws Exception
   */
  de.willuhn.datasource.Service find(String plugin, String service) throws Exception
  {
    Manifest mf = Application.getPluginLoader().getManifestByName(plugin);
    if (mf == null)
      throw new IOException("plugin " + plugin + " not found");

    de.willuhn.datasource.Service s = Application.getServiceFactory().lookup(Application.getPluginLoader().getPlugin(mf.getPluginClass()).getClass(),service);
    if (s == null)
      throw new IOException("service " + service + " not found in plugin " + plugin);

    return s;
  }
}


/*********************************************************************
 * $Log: Service.java,v $
 * Revision 1.15  2012/03/29 20:54:40  willuhn
 * @C Kompatibilitaet zu Jameica 2.2 wieder hergestellt
 *
 * Revision 1.14  2012/03/28 22:28:21  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.13  2010-11-02 00:56:31  willuhn
 * @N Umstellung des Webfrontends auf Velocity/Webtools
 *
 * Revision 1.12  2010/05/12 10:59:20  willuhn
 * @N Automatische Dokumentations-Seite fuer die REST-Beans basierend auf der Annotation "Doc"
 *
 * Revision 1.11  2010/05/11 14:59:48  willuhn
 * @N Automatisches Deployment von REST-Beans
 *
 * Revision 1.10  2010/03/18 09:29:35  willuhn
 * @N Wenn REST-Beans Rueckgabe-Werte liefern, werrden sie automatisch als toString() in den Response-Writer geschrieben
 **********************************************************************/