/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.xmlrpc/src/de/willuhn/jameica/xmlrpc/server/MyRequestProcessorFactoryFactory.java,v $
 * $Revision: 1.3 $
 * $Date: 2011/01/27 00:10:24 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.xmlrpc.server;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.server.RequestProcessorFactoryFactory.RequestSpecificProcessorFactoryFactory;

import de.willuhn.datasource.Service;
import de.willuhn.jameica.xmlrpc.Settings;
import de.willuhn.jameica.xmlrpc.rmi.XmlRpcServiceDescriptor;
import de.willuhn.logging.Logger;

/**
 * Ueberschrieben, damit der Service nicht bei jedem Request neu instanziiert wird.
 */
public class MyRequestProcessorFactoryFactory extends RequestSpecificProcessorFactoryFactory
{
  private Map<String,XmlRpcServiceDescriptor> cache = new HashMap<String,XmlRpcServiceDescriptor>();

  /**
   * Liefert die Instanz des Services.
   * @see org.apache.xmlrpc.server.RequestProcessorFactoryFactory$RequestSpecificProcessorFactoryFactory#getRequestProcessor(java.lang.Class, org.apache.xmlrpc.XmlRpcRequest)
   */
  protected Object getRequestProcessor(Class pClass, XmlRpcRequest pRequest) throws XmlRpcException
  {
    String method = pRequest.getMethodName();
    if (method == null || method.indexOf(".") == -1)
      throw new XmlRpcException("no xmlrpc service given or name invalid: " + method);
    
    // "method ist der vollqualifizierte Name bestehend aus
    // pluginname.servicename.methodname
    // Um den Service zu finden, muessen wir den methodname
    // abschneiden
    method = method.substring(0,method.lastIndexOf("."));

    // Cache checken
    XmlRpcServiceDescriptor descriptor = cache.get(method);
    
    // Kennen wir noch nicht. Also suchen wir ihn
    if (descriptor == null)
    {
      XmlRpcServiceDescriptor[] services = Settings.getServices();
      for (XmlRpcServiceDescriptor d:services)
      {
        try
        {
          if (d.getID().equals(method))
          {
            // gefunden
            descriptor = d;
            
            // In den Cache tun
            cache.put(method,d);
            break;
          }
        }
        catch (RemoteException re)
        {
          Logger.error("error while checking xmlrpc service: " + method,re);
        }
      }
    }

    // Weder im Cache noch aktuell gefunden
    if (descriptor == null)
      throw new XmlRpcException("xmlrpc service unknown: " + method);

    try
    {
      // OK, wir haben den Service. Mal schauen, ob er freigegeben ist
      if (!descriptor.isShared())
        throw new XmlRpcException("xmlrpc service not enabled: " + method);

      // Instanz holen
      Service s = descriptor.getService();
      if (!s.isStarted()) // ist er auch gestartet?
        throw new XmlRpcException("xmlrpc service not started: " + method);

      // Ok, zulaessig
      return s;
    }
    catch (RemoteException re)
    {
      Logger.error("error while checking service " + method,re);
      throw new XmlRpcException("internal error occurred for service: " + method);
    }
  }
}


/*********************************************************************
 * $Log: MyRequestProcessorFactoryFactory.java,v $
 * Revision 1.3  2011/01/27 00:10:24  willuhn
 * @C Code-Cleanup
 * @N XML-RPC-Services koennen jetzt zur Laufzeit aktiviert/deaktiviert werden, ohne den HTTP-Listener neu starten zu muessen
 * @B es wurde nicht geprueft, ob der Service zwischenzeitlich deaktiviert wurde oder ueberhaupt gestartet war
 *
 * Revision 1.2  2007/04/05 12:14:40  willuhn
 * @N Liste der Services im Handler statisch
 * @C XmlRpcService in XmlRpcServiceDescriptor umbenannt
 *
 * Revision 1.1  2007/02/15 11:03:58  willuhn
 * @B Services wurden bei jedem Request neu instanziiert
 *
 **********************************************************************/