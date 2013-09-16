/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/messaging/DeployMessageConsumer.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/09/24 11:01:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.webadmin.messaging;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.HandlerCollection;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.SystemMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.webadmin.Plugin;
import de.willuhn.jameica.webadmin.deploy.Deployer;
import de.willuhn.jameica.webadmin.rmi.HttpService;
import de.willuhn.logging.Logger;

/**
 * Das Deployen der Web-Anwendungen koennen wir erst machen, nachdem alle
 * Plugins geladen sind. Daher via Message-Consumer.
 */
public class DeployMessageConsumer implements MessageConsumer
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
    if (((SystemMessage)message).getStatusCode() != SystemMessage.SYSTEM_STARTED)
      return;
    
    
    ContextHandlerCollection collection = new ContextHandlerCollection();

    try
    {
      Class[] cl = Application.getClassLoader().getClassFinder().findImplementors(Deployer.class);
      for (int i=0;i<cl.length;++i)
      {
        try
        {
          Logger.info("init deployer " + cl[i].getName());
          Deployer d = (Deployer) cl[i].newInstance();
          Handler[] handlers = d.deploy();
          if (handlers == null || handlers.length == 0)
          {
            Logger.info("skipping deployer " + d.getClass() + " - contains no handlers");
            continue;
          }
          for (int k=0;k<handlers.length;++k)
          {
            collection.addHandler(handlers[k]);
          }
          
        }
        catch (Exception e)
        {
          Logger.error("error while loading deployer " + cl[i].getName() + ", skipping",e);
        }
      }
    }
    catch (ClassNotFoundException e)
    {
      Logger.warn("no deployers found, skipping http-server");
      return;
    }
    
    // Wir erzeugen eine Handler-Collection mit Default-Handler.
    HandlerCollection handlers = new HandlerCollection();
    handlers.addHandler(collection);
    
    // Liefert eine Liste der verfuegbaren Contexte auf der Startseite (Information-Leak)
    // handlers.addHandler(new DefaultHandler());

    HttpService server = (HttpService) Application.getServiceFactory().lookup(Plugin.class,"listener.http");
    server.addHandler(handlers);
  }

}


/*********************************************************************
 * $Log: DeployMessageConsumer.java,v $
 * Revision 1.2  2009/09/24 11:01:47  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2008/04/10 13:02:29  willuhn
 * @N Zweischritt-Deployment. Der Server wird zwar sofort initialisiert, wenn der Jameica-Service startet, gestartet wird er aber erst, wenn die ersten Handler resgistriert werden
 * @N damit koennen auch nachtraeglich zur Laufzeit weitere Handler hinzu registriert werden
 * @R separater Worker in HttpServiceImpl entfernt. Der Classloader wird nun direkt von den Deployern gesetzt. Das ist wichtig, da Jetty fuer die Webanwendungen sonst den System-Classloader nutzt, welcher die Plugins nicht kennt
 *
 **********************************************************************/