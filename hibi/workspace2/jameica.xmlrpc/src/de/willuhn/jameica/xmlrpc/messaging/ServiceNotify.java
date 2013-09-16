/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.xmlrpc/src/de/willuhn/jameica/xmlrpc/messaging/ServiceNotify.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/04/04 00:17:13 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.xmlrpc.messaging;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.SystemMessage;
import de.willuhn.jameica.xmlrpc.Settings;
import de.willuhn.jameica.xmlrpc.rmi.XmlRpcServiceDescriptor;
import de.willuhn.logging.Logger;

/**
 * Listet alle freigegebenen XML-RPC-Services beim Server-Start auf.
 * Dient nur informativen Zwecken, damit Entwickler sofort sehen,
 * unter welchen URLs die Services verfuegbar sind.
 */
public class ServiceNotify implements MessageConsumer 
{
  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{SystemMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    if (message == null)
      return;
    
    if (((SystemMessage)message).getStatusCode() != SystemMessage.SYSTEM_STARTED)
    return;
    
    XmlRpcServiceDescriptor[] all = Settings.getServices();
    if (all == null || all.length == 0)
      return;
    
    Logger.info("XML-RPC-Services");
    for (int i=0;i<all.length;++i)
    {
      try
      {
        XmlRpcServiceDescriptor service = all[i];
        if (!service.isShared())
          continue;

        Logger.info("   * " + service.getURL());
      }
      catch (Exception e)
      {
        Logger.error("unable to get service url, skipping",e);
      }
    }
  }
}


/*********************************************************************
 * $Log: ServiceNotify.java,v $
 * Revision 1.1  2008/04/04 00:17:13  willuhn
 * @N Apache XML-RPC von 3.0 auf 3.1 aktualisiert
 * @N jameica.xmlrpc ist jetzt von jameica.webadmin abhaengig
 * @N jameica.xmlrpc nutzt jetzt keinen eigenen embedded Webserver mehr sondern den Jetty von jameica.webadmin mittels Servlet. Damit kann nun XML-RPC ueber den gleichen TCP-Port (8080) gemacht werden, wo auch die restlichen Webfrontends laufen -> spart einen TCP-Port und skaliert besser wegen Multi-Threading-Support in Jetty
 *
 *********************************************************************/