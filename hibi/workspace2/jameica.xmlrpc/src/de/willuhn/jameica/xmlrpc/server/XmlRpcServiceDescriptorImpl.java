/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.xmlrpc/src/de/willuhn/jameica/xmlrpc/server/XmlRpcServiceDescriptorImpl.java,v $
 * $Revision: 1.8 $
 * $Date: 2012/03/29 20:52:19 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.xmlrpc.server;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import de.willuhn.datasource.GenericObject;
import de.willuhn.datasource.Service;
import de.willuhn.jameica.messaging.LookupService;
import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.plugin.PluginLoader;
import de.willuhn.jameica.plugin.ServiceDescriptor;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.xmlrpc.Settings;
import de.willuhn.jameica.xmlrpc.rmi.XmlRpcServiceDescriptor;
import de.willuhn.logging.Logger;

/**
 * Container fuer die Eigenschaften eines XML-RPC-Services.
 */
public class XmlRpcServiceDescriptorImpl extends UnicastRemoteObject implements XmlRpcServiceDescriptor
{
  private Manifest manifest         = null;
  private ServiceDescriptor service = null;
  
  /**
   * ct.
   * @param manifest
   * @param service
   * @throws RemoteException
   */
  public XmlRpcServiceDescriptorImpl(Manifest manifest, ServiceDescriptor service) throws RemoteException
  {
    super();
    this.manifest = manifest;
    this.service  = service;
  }
  
  /**
   * @see de.willuhn.jameica.xmlrpc.rmi.XmlRpcServiceDescriptor#isShared()
   */
  public boolean isShared() throws RemoteException
  {
    return Settings.SETTINGS.getBoolean(getID() + ".shared",false);
  }
  
  /**
   * @see de.willuhn.jameica.xmlrpc.rmi.XmlRpcServiceDescriptor#setShared(boolean)
   */
  public void setShared(boolean shared) throws RemoteException
  {
    boolean current = isShared();

    // Keine Aenderung am Zustand
    if (current == shared)
      return;
    
    Settings.SETTINGS.setAttribute(getID() + ".shared",shared);

    if (shared) // Service war vorher nicht oeffentlich, jetzt ist er es
    {
      Logger.info("xmlrpc service enabled: " + this.getURL());
      LookupService.register("xmlrpc:" + this.getID(),this.getURL());
    }
    else // Service war vorher oeffentlich, jetzt ist er es nicht mehr
    {
      Logger.info("xmlrpc service disabled: " + this.getURL());
      LookupService.unRegister("xmlrpc:" + this.getID());
    }
  }
  
  /**
   * @see de.willuhn.jameica.xmlrpc.rmi.XmlRpcServiceDescriptor#getServiceName()
   */
  public String getServiceName() throws RemoteException
  {
    return this.service.getName();
  }
  
  /**
   * @see de.willuhn.jameica.xmlrpc.rmi.XmlRpcServiceDescriptor#getPluginName()
   */
  public String getPluginName() throws RemoteException
  {
    return this.manifest.getName();
  }
  
  /**
   * @see de.willuhn.jameica.xmlrpc.rmi.XmlRpcServiceDescriptor#getService()
   */
  public Service getService() throws RemoteException
  {
    PluginLoader loader = Application.getPluginLoader();
    try
    {
      return Application.getServiceFactory().lookup(loader.getPlugin(this.manifest.getPluginClass()).getClass(),this.service.getName());
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException("unable to load service",e);
    }
  }

  /**
   * @see de.willuhn.datasource.GenericObject#equals(de.willuhn.datasource.GenericObject)
   */
  public boolean equals(GenericObject arg0) throws RemoteException
  {
    if (arg0 == null || !(arg0 instanceof XmlRpcServiceDescriptor))
      return false;
    return this.getID().equals(arg0.getID());
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttribute(java.lang.String)
   */
  public Object getAttribute(String arg0) throws RemoteException
  {
    if ("servicename".equals(arg0))
      return getServiceName();
    if ("pluginname".equals(arg0))
      return getPluginName();
    if ("shared".equals(arg0))
      return new Boolean(isShared());
    return null;
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getAttributeNames()
   */
  public String[] getAttributeNames() throws RemoteException
  {
    return new String[]{"servicename","pluginname","shared"};
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getID()
   */
  public String getID() throws RemoteException
  {
    if (Settings.getUseInterfaceNames())
      return service.getClassname();
    return manifest.getName() + "." + service.getName();
  }

  /**
   * @see de.willuhn.datasource.GenericObject#getPrimaryAttribute()
   */
  public String getPrimaryAttribute() throws RemoteException
  {
    return "servicename";
  }

  /**
   * @see de.willuhn.jameica.xmlrpc.rmi.XmlRpcServiceDescriptor#getURL()
   */
  public String getURL() throws RemoteException
  {
    try
    {
      InetAddress host = de.willuhn.jameica.webadmin.Settings.getAddress();
      String address = host == null ? Application.getCallback().getHostname() : host.getHostAddress();
      StringBuffer sb = new StringBuffer();
      sb.append("http");
      if (de.willuhn.jameica.webadmin.Settings.getUseSSL()) sb.append("s");
      sb.append("://");
      sb.append(address);
      sb.append(":");
      sb.append(de.willuhn.jameica.webadmin.Settings.getPort());
      sb.append("/xmlrpc/");
      sb.append(this.getID());
      return sb.toString();
    }
    catch (Exception e)
    {
      throw new RemoteException(e.getMessage(),e);
    }
  }

}


/*********************************************************************
 * $Log: XmlRpcServiceDescriptorImpl.java,v $
 * Revision 1.8  2012/03/29 20:52:19  willuhn
 * @C Kompatibilitaet zu Jameica 2.2 wieder hergestellt
 *
 * Revision 1.7  2012/03/28 22:28:13  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.6  2011-01-27 00:10:24  willuhn
 * @C Code-Cleanup
 * @N XML-RPC-Services koennen jetzt zur Laufzeit aktiviert/deaktiviert werden, ohne den HTTP-Listener neu starten zu muessen
 * @B es wurde nicht geprueft, ob der Service zwischenzeitlich deaktiviert wurde oder ueberhaupt gestartet war
 *
 * Revision 1.5  2008/04/04 00:17:13  willuhn
 * @N Apache XML-RPC von 3.0 auf 3.1 aktualisiert
 * @N jameica.xmlrpc ist jetzt von jameica.webadmin abhaengig
 * @N jameica.xmlrpc nutzt jetzt keinen eigenen embedded Webserver mehr sondern den Jetty von jameica.webadmin mittels Servlet. Damit kann nun XML-RPC ueber den gleichen TCP-Port (8080) gemacht werden, wo auch die restlichen Webfrontends laufen -> spart einen TCP-Port und skaliert besser wegen Multi-Threading-Support in Jetty
 *
 * Revision 1.4  2007/12/13 16:11:51  willuhn
 * @N Generischer Message-Queue-Service
 *
 * Revision 1.3  2007/10/18 22:13:14  willuhn
 * @N XML-RPC URL via Service-Descriptor abfragbar
 *
 * Revision 1.2  2007/06/13 14:50:10  willuhn
 * @N Als XML-RPC-Servicenamen koennen nun auch direkt die Interface-Namen verwendet werden. Das ermoeglicht die Verwendung von dynamischen Proxies auf Clientseite.
 *
 * Revision 1.1  2007/04/05 12:14:40  willuhn
 * @N Liste der Services im Handler statisch
 * @C XmlRpcService in XmlRpcServiceDescriptor umbenannt
 *
 * Revision 1.1  2006/10/31 17:06:26  willuhn
 * @N GUI to configure xml-rpc
 *
 **********************************************************************/