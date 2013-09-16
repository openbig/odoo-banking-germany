/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/rest/Server.java,v $
 * $Revision: 1.6 $
 * $Date: 2010/11/02 00:56:31 $
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
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;

import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.Settings;
import de.willuhn.jameica.webadmin.JSONClient;
import de.willuhn.jameica.webadmin.Plugin;
import de.willuhn.jameica.webadmin.annotation.Doc;
import de.willuhn.jameica.webadmin.annotation.Path;
import de.willuhn.jameica.webadmin.annotation.Request;
import de.willuhn.jameica.webadmin.annotation.Response;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * REST-Kommandos zum Hinzufuegen und Entfernen von Servern.
 */
@Doc("System: Ermöglicht das Hinzufügen weiterer Jameica-Server zur Management-Console")
public class Server implements AutoRestBean
{
  private final static I18N i18n = de.willuhn.jameica.system.Application.getPluginLoader().getPlugin(de.willuhn.jameica.webadmin.Plugin.class).getResources().getI18N();
  private final static Settings SETTINGS = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();

  @Request
  private HttpServletRequest request = null;
  
  @Response
  private HttpServletResponse response = null;

  /**
   * Fuegt einen Server hinzu.
   * @throws IOException
   */
  @Doc(value="Fügt einen weiteren Jameica-Server zur Management-Console hinzu." +
  		       "Die Funktion erwartet folgende 4 Parameter via GET oder POST.<br/>" +
  		       "<ul>" +
  		       "  <li><b>host</b>: Hostname des Jameica-Servers</li>" +
             "  <li><b>ssl</b>: &quot;true&quot; wenn für den Zugriff HTTPS verwendet werden soll</li>" +
             "  <li><b>port</b>: TCP-Port der Management-Console des entfernten Jameica-Servers (meist 8080)</li>" +
             "  <li><b>password</b>: Das Master-Passwort des entfernten Jameica-Servers</li>" +
  		       "</ul>",
  		 example="server/add")
  @Path("/server/add$")
  public void add() throws IOException
  {
    try
    {
      String host = request.getParameter("host");
      if (host != null && host.length() > 0)
      {
        boolean ssl = (request.getParameter("ssl") != null && "true".equals(request.getParameter("ssl")));
        int port = 8080;
        try
        {
          port = Integer.parseInt(request.getParameter("port"));
        } catch (Exception e) {/*ignore*/}
        if (port <= 0)
          port = 8080;
        
        String url = "http" + (ssl ? "s" : "") + "://" + host + ":" + port + "/webadmin";
        SETTINGS.setAttribute("jameica.server." + host,url);
        de.willuhn.jameica.webadmin.Settings.setServerPassword(url,request.getParameter("password"));
        
        de.willuhn.jameica.system.Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Server {0} hinzugefügt",host),StatusBarMessage.TYPE_SUCCESS));
        response.sendRedirect("/webadmin/"); // Damit beim Reload nicht erneut abgesendet wird
      }
    }
    catch (Exception e)
    {
      Logger.error("unable to add server",e);
      throw new IOException("unable to add server");
    }
  }

  
  /**
   * Liefert eine Liste aller weiteren registrierten Server.
   * @return Liste der registrierten Server.
   */
  @Doc(value="Liefert eine Liste der weiteren registrierten Jameica-Server",
       example="server/list")
  @Path("/server/list$")
  public JSONArray getList()
  {
    String[] names = SETTINGS.getAttributes();
    List<Map> list = new ArrayList<Map>();
    if (names == null || names.length == 0)
      return new JSONArray(list);
      
    for (String name:names)
    {
      if (!name.startsWith("jameica.server."))
        continue;
      String alias = name.replaceFirst("jameica\\.server\\.","");
      Map map = new HashMap();
      map.put("name",alias);
      map.put("url",SETTINGS.getString(name,null));
      list.add(map);
    }
    return new JSONArray(list);
  }
  
  /**
   * Fuehrt ein REST-Kommando auf dem angegebenen Server aus.
   * @param server Alias-Name des Servers.
   * @param restCommand das REST-Kommando.
   * @return das JSON-Response von dem Server.
   * @throws IOException
   */
  public JSONArray execute(String server,String restCommand) throws IOException
  {
    // Server-URL ermitteln
    String url = SETTINGS.getString("jameica.server." + server,null);
    if (url == null)
      return null;
    try
    {
      return (JSONArray) JSONClient.execute(url,restCommand);
    }
    catch (Exception e)
    {
      Logger.error("unable to execute json command " + restCommand + " on server " + server,e);
      throw new IOException("unable to execute json command " + restCommand + " on server " + server);
    }
  }

  /**
   * Entfernt den Server aus der Liste.
   * @param name Aliasname des Servers.
   */
  public void removeServer(String name)
  {
    if (name == null || name.length() == 0)
      return;
    SETTINGS.setAttribute("jameica.server." + name,(String) null);
  }
}


/*********************************************************************
 * $Log: Server.java,v $
 * Revision 1.6  2010/11/02 00:56:31  willuhn
 * @N Umstellung des Webfrontends auf Velocity/Webtools
 *
 * Revision 1.5  2010/05/12 10:59:20  willuhn
 * @N Automatische Dokumentations-Seite fuer die REST-Beans basierend auf der Annotation "Doc"
 *
 * Revision 1.4  2010/05/11 14:59:48  willuhn
 * @N Automatisches Deployment von REST-Beans
 **********************************************************************/