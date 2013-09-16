/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.xmlrpc/src/de/willuhn/jameica/xmlrpc/Settings.java,v $
 * $Revision: 1.11 $
 * $Date: 2008/04/04 00:17:13 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.xmlrpc;

import java.util.ArrayList;
import java.util.List;

import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.PluginLoader;
import de.willuhn.jameica.plugin.ServiceDescriptor;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.xmlrpc.rmi.XmlRpcServiceDescriptor;
import de.willuhn.jameica.xmlrpc.server.XmlRpcServiceDescriptorImpl;
import de.willuhn.logging.Logger;

/**
 * Container fuer die Einstellungen.
 */
public class Settings
{
  /**
   * Die Einstellungen des Plugins.
   */
  public final static de.willuhn.jameica.system.Settings SETTINGS = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();
  
  private static XmlRpcServiceDescriptor[] services = null;
  
  /**
   * Liefert true, die Interface-Namen der Services als XML-RPC-Namen verwendet werden sollen.
   * @return true, wenn die Interface-Namen der Services als XML-RPC-Namen verwendet werden sollen.
   */
  public static boolean getUseInterfaceNames()
  {
    return SETTINGS.getBoolean("xmlrpc.useinterfacenames",false);
  }
  
  /**
   * Legt fest, ob die Interface-Namen der Services als XML-RPC-Namen verwendet werden sollen.
   * @param names true wenn die Interface-Namen der Services als XML-RPC-Namen verwendet werden sollen.
   */
  public static void setUseInterfaceNames(boolean names)
  {
    SETTINGS.setAttribute("xmlrpc.useinterfacenames",names);
  }

  /**
   * Liefert die Liste aller potentiellen Services. Auch jene, welche nicht aktiv sind.
   * @return Liste aller XML-RPC-tauglichen Services.
   */
  public static XmlRpcServiceDescriptor[] getServices()
  {
    if (services != null)
      return services;

    Logger.info("checking XML-RPC services");

    PluginLoader loader = Application.getPluginLoader();
    List manifests  = loader.getInstalledManifests();

    Manifest self = Application.getPluginLoader().getManifest(Plugin.class);
    
    ArrayList l = new ArrayList();
    for (int i=0;i<manifests.size();++i)
    {
      Manifest mf = (Manifest) manifests.get(i);
      
      ServiceDescriptor[] services = mf.getServices();
      
      Logger.info("  checking plugin " + mf.getName());
      if (services == null || services.length == 0)
        continue;
      for (int k=0;k<services.length;++k)
      {
        try
        {
          if ("listener.http".equals(services[k].getName()) && self.getPluginClass().equals(mf.getPluginClass()))
            continue; // Das sind wir selbst

          Logger.info("    checking service " + mf.getName() + "." + services[k].getName());

          l.add(new XmlRpcServiceDescriptorImpl(mf,services[k]));
        }
        catch (Exception e)
        {
          Logger.error("unable to load service",e);
        }
      }
    }
    services = (XmlRpcServiceDescriptor[]) l.toArray(new XmlRpcServiceDescriptor[l.size()]);
    return services;
  }
}


/*********************************************************************
 * $Log: Settings.java,v $
 * Revision 1.11  2008/04/04 00:17:13  willuhn
 * @N Apache XML-RPC von 3.0 auf 3.1 aktualisiert
 * @N jameica.xmlrpc ist jetzt von jameica.webadmin abhaengig
 * @N jameica.xmlrpc nutzt jetzt keinen eigenen embedded Webserver mehr sondern den Jetty von jameica.webadmin mittels Servlet. Damit kann nun XML-RPC ueber den gleichen TCP-Port (8080) gemacht werden, wo auch die restlichen Webfrontends laufen -> spart einen TCP-Port und skaliert besser wegen Multi-Threading-Support in Jetty
 *
 * Revision 1.10  2007/06/13 14:50:10  willuhn
 * @N Als XML-RPC-Servicenamen koennen nun auch direkt die Interface-Namen verwendet werden. Das ermoeglicht die Verwendung von dynamischen Proxies auf Clientseite.
 *
 * Revision 1.9  2007/04/16 12:36:41  willuhn
 * @C getInstalledPlugins und getInstalledManifests liefern nun eine Liste vom Typ "List" statt "Iterator"
 *
 * Revision 1.8  2007/04/05 12:14:40  willuhn
 * @N Liste der Services im Handler statisch
 * @C XmlRpcService in XmlRpcServiceDescriptor umbenannt
 *
 * Revision 1.7  2007/04/05 10:42:33  willuhn
 * @N Registrieren der XML/RPC-Handler erst nachdem alle Services geladen wurden (mittels SystemMessage). Somit koennen bereits beim Initialisieren die XMLRPC-URLs im Log ausgegeben werden und nicht erst beim ersten Request.
 *
 * Revision 1.6  2007/01/24 15:52:24  willuhn
 * @N Client access restrictions
 *
 * Revision 1.5  2006/12/22 13:49:58  willuhn
 * @N server kann an interface gebunden werden
 *
 * Revision 1.4  2006/12/22 09:31:38  willuhn
 * @N bind address
 *
 * Revision 1.3  2006/10/31 17:44:20  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2006/10/31 17:06:26  willuhn
 * @N GUI to configure xml-rpc
 *
 * Revision 1.1  2006/10/31 01:43:08  willuhn
 * *** empty log message ***
 *
 **********************************************************************/