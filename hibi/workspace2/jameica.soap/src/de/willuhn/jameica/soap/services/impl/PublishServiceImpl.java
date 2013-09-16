/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.soap/src/de/willuhn/jameica/soap/services/impl/PublishServiceImpl.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/01/21 08:43:12 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.soap.services.impl;

import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.jws.WebService;
import javax.xml.ws.Endpoint;

import de.willuhn.jameica.messaging.LookupService;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.soap.AutoService;
import de.willuhn.jameica.soap.services.PublishService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;

/**
 * Implementierung des Publish-Services.
 */
public class PublishServiceImpl implements PublishService
{
  private Map<String,Endpoint> services = null;

  /**
   * @see de.willuhn.jameica.soap.services.PublishService#publish(java.lang.Object)
   */
  public void publish(Object service) throws RemoteException
  {
    publish(null,service);
  }

  /**
   * @see de.willuhn.jameica.soap.services.PublishService#publish(java.lang.String, java.lang.Object)
   */
  public void publish(String name, Object service) throws RemoteException
  {
    if (!this.isStarted())
      throw new RemoteException("public service not started");

    if (service == null)
      throw new RemoteException("no service instance given");

    if (name == null) name = createName(service);
    if (name == null || name.length() == 0) throw new RemoteException("no service name given");
    
    if (name.startsWith("/"))
      name = name.substring(1);

    Logger.info("publishing webservice " + service.getClass().getName() + " at /" + name);
    Endpoint ep = Endpoint.publish("/" + name,service);
    
    services.put(name,ep);
    LookupService.register("soap:" + createId(service),createUrl(name));
    Application.getMessagingFactory().getMessagingQueue("jameica.soap.publish.done").sendMessage(new QueryMessage(name,service));

  }

  /**
   * @see de.willuhn.jameica.soap.services.PublishService#unpublish(java.lang.String)
   */
  public void unpublish(String name) throws RemoteException
  {
    if (!this.isStarted())
      throw new RemoteException("public service not started");

    if (name == null)
      throw new RemoteException("no service name given");
    
    if (name.startsWith("/"))
      name = name.substring(1);

    Endpoint ep = this.services.remove(name);
    if (ep == null)
      throw new RemoteException("endpoint not found for name: " + name);
    
    Object service = ep.getImplementor();

    Logger.info("unpublishing webservice " + name);
    ep.stop();

    LookupService.unRegister("soap:" + createId(service));
    Application.getMessagingFactory().getMessagingQueue("jameica.soap.unpublish").sendMessage(new QueryMessage(name,service));
    
  }

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "publish-service";
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
    return this.services != null;
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
    
    this.services = new HashMap<String,Endpoint>();
    
    Logger.info("searching for auto-services");
    try
    {
      ClassFinder finder = Application.getClassLoader().getClassFinder();
      Class[] classes = finder.findImplementors(AutoService.class);
      for (Class c:classes)
      {
        try
        {
          // Wir publishen nicht direkt sondern indirekt uebers Messaging.
          // Dann werden die Services erst dann veroeffentlicht, wenn CXF online ist.
          AutoService as = (AutoService) c.newInstance();
          Application.getMessagingFactory().getMessagingQueue("jameica.soap.publish").sendMessage(new QueryMessage(as));
        }
        catch (Exception e)
        {
          Logger.error("error while publishing service " + c.getName() + ", skipping",e);
        }
      }
    }
    catch (ClassNotFoundException cnfe)
    {
      Logger.info("no auto-services found");
    }
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
    
    try
    {
      // Wir iterieren nicht direkt ueber das keySet sondern ueber die Array-Kopie.
      // In unpublish wird ein this.services.remove() gemacht, was sonst eine
      // ConcurrentModificationException werfen wuerde
      String[] keys = this.services.keySet().toArray(new String[this.services.size()]);
      for (String s:keys)
      {
        try
        {
          unpublish(s);
        }
        catch (Exception e)
        {
          Logger.error("error while unpublishing service " + s + ", skipping",e);
        }
      }
    }
    finally
    {
      this.services = null;
    }
  }

  /**
   * Ermittelt eine Service-Lookup-ID aus der Instanz.
   * @param service Service-Instanz.
   * @return Service-Lookup-ID.
   * @throws RemoteException
   */
  private String createId(Object service) throws RemoteException
  {
    WebService s = service.getClass().getAnnotation(WebService.class);
    if (s == null)
      return service.getClass().getName(); // Annotation fehlt. Dann nehmen wir den Klassennamen der Implementierung

    return s.endpointInterface();
  }

  /**
   * Ermittelt den Service-Namen aus der Instanz.
   * @param service Service-Instanz.
   * @return Service-Name.
   * @throws RemoteException
   */
  private String createName(Object service) throws RemoteException
  {
    WebService s = service.getClass().getAnnotation(WebService.class);
    if (s == null)
      return null;

    return s.name();
  }

  /**
   * Erzeugt die SOAP-URL fuer einen Service-Namen.
   * @param name Service-Name.
   * @return URL.
   * @throws RemoteException
   */
  private String createUrl(String name) throws RemoteException
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
      sb.append("/soap/");
      sb.append(name);
      return sb.toString();
    }
    catch (RemoteException re)
    {
      throw re;
    }
    catch (Exception e)
    {
      throw new RemoteException(e.getMessage(),e);
    }
  }

}



/**********************************************************************
 * $Log: PublishServiceImpl.java,v $
 * Revision 1.2  2010/01/21 08:43:12  willuhn
 * @B concurrentModificationException beim Shutdown des Publish-Services
 * @B falsches Multicast-Unregister beim Shutdown
 *
 * Revision 1.1  2010/01/19 00:33:59  willuhn
 * @C Publishing der Webservices aus MessageConsumer in dedizierten PublishService verschoben
 * @N Auto-Deployment von Services via AutoService
 *
 **********************************************************************/