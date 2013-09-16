/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.xmlrpc/src/de/willuhn/jameica/xmlrpc/server/HandlerMappingImpl.java,v $
 * $Revision: 1.20 $
 * $Date: 2011/01/27 00:10:24 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.xmlrpc.server;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcHandler;
import org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcNoSuchHandlerException;

import de.willuhn.jameica.messaging.LookupService;
import de.willuhn.jameica.xmlrpc.Settings;
import de.willuhn.jameica.xmlrpc.rmi.XmlRpcServiceDescriptor;
import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;

/**
 * Implementiert das Mapping von XML-RPC-Namen auf Klassen.
 * @author willuhn
 */
public class HandlerMappingImpl extends AbstractReflectiveHandlerMapping implements XmlRpcHandlerMapping
{
  private static boolean loaded = false;

  /**
   * Initialisiert die Handler.
   * @throws XmlRpcException
   */
  private synchronized void init() throws XmlRpcException
  {
    if (loaded)
      return;

    loaded = true;

    try
    {
      // Wir registrieren unseren eigenen Request-Prozessor.
      // Andernfalls wuerde das Ding fuer jeden Request eine neue Instanz
      // des Services erzeugen. In unserer Impl halten wir die Instanzen
      // und pruefen auch, ob sie noch freigegeben sind.
      this.setRequestProcessorFactoryFactory(new MyRequestProcessorFactoryFactory());

      XmlRpcServiceDescriptor[] all = Settings.getServices();
      for (XmlRpcServiceDescriptor service:all)
      {
        try
        {
          // Wir registrieren erstmal alle. Erst beim Request - also live - pruefen
          // wir, ob er noch gestartet ist und das Sharing noch freigegeben ist
          // Damit weiss der Haendler erstmal, was grundsaetzlich moeglich ist
          registerPublicMethods(service.getID(),service.getService().getClass());
          
          // Das Veroeffentlichen im Lookup-Service machen wir aber nur, wenn
          // der Dienst zu diesem Zeitpunkt wirklich freigegeben war. Wird er
          // zur Laufzeit freigegeben, erfolgt das Lookup-Register dann direkt in
          // XmlRpcServiceDescriptorImpl#setShared
          if (service.isShared())
            LookupService.register("xmlrpc:" + service.getID(),service.getURL());
        }
        catch (IllegalStateException ie)
        {
          // Das darf durchaus vorkommen. Naemlich genau dann, wenn der Service
          // Methoden enthaelt, deren Parameter oder Rueckgabe-Werte nicht RPC-tauglich
          // sind. Wir loggen das daher nur in Debug-Level
          Logger.write(Level.DEBUG,"service contains unsupported methods, skipping",ie);
        }
        catch (Exception e)
        {
          Logger.error("unable to register service, skipping",e);
        }
      }
    }
    catch (Exception e)
    {
      throw new XmlRpcException("unable to register services",e);
    }
  }


  /**
   * @see org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping#getHandler(java.lang.String)
   */
  public XmlRpcHandler getHandler(String pHandlerName) throws XmlRpcNoSuchHandlerException, XmlRpcException
  {
    init();
    return super.getHandler(pHandlerName);
  }
}


/*********************************************************************
 * $Log: HandlerMappingImpl.java,v $
 * Revision 1.20  2011/01/27 00:10:24  willuhn
 * @C Code-Cleanup
 * @N XML-RPC-Services koennen jetzt zur Laufzeit aktiviert/deaktiviert werden, ohne den HTTP-Listener neu starten zu muessen
 * @B es wurde nicht geprueft, ob der Service zwischenzeitlich deaktiviert wurde oder ueberhaupt gestartet war
 *
 * Revision 1.19  2008-04-04 00:17:13  willuhn
 * @N Apache XML-RPC von 3.0 auf 3.1 aktualisiert
 * @N jameica.xmlrpc ist jetzt von jameica.webadmin abhaengig
 * @N jameica.xmlrpc nutzt jetzt keinen eigenen embedded Webserver mehr sondern den Jetty von jameica.webadmin mittels Servlet. Damit kann nun XML-RPC ueber den gleichen TCP-Port (8080) gemacht werden, wo auch die restlichen Webfrontends laufen -> spart einen TCP-Port und skaliert besser wegen Multi-Threading-Support in Jetty
 *
 * Revision 1.18  2007/12/14 13:41:01  willuhn
 * *** empty log message ***
 *
 * Revision 1.17  2007/12/14 13:28:40  willuhn
 * @C Lookup-Service nach Jameica verschoben
 *
 * Revision 1.16  2007/11/16 18:34:06  willuhn
 * @D javadoc fixed
 * @R removed unused methods/deprecated methods
 *
 * Revision 1.15  2007/10/18 22:13:14  willuhn
 * @N XML-RPC URL via Service-Descriptor abfragbar
 *
 * Revision 1.14  2007/04/05 12:14:40  willuhn
 * @N Liste der Services im Handler statisch
 * @C XmlRpcService in XmlRpcServiceDescriptor umbenannt
 *
 * Revision 1.13  2007/04/05 11:12:56  willuhn
 * *** empty log message ***
 *
 * Revision 1.12  2007/04/05 10:42:32  willuhn
 * @N Registrieren der XML/RPC-Handler erst nachdem alle Services geladen wurden (mittels SystemMessage). Somit koennen bereits beim Initialisieren die XMLRPC-URLs im Log ausgegeben werden und nicht erst beim ersten Request.
 *
 * Revision 1.11  2007/03/07 17:05:09  willuhn
 * @B Bei Fehler eines Services nicht gleich die komplette Registrierung abbrechen
 *
 * Revision 1.10  2007/02/15 11:04:25  willuhn
 * @D
 *
 * Revision 1.9  2007/02/15 11:03:58  willuhn
 * @B Services wurden bei jedem Request neu instanziiert
 *
 * Revision 1.8  2007/02/13 16:00:15  willuhn
 * *** empty log message ***
 *
 * Revision 1.7  2006/12/22 16:14:07  willuhn
 * @N Ausgabe der IP, an die der Service gebunden wurde
 *
 * Revision 1.6  2006/10/31 17:06:26  willuhn
 * @N GUI to configure xml-rpc
 *
 * Revision 1.5  2006/10/31 01:43:07  willuhn
 * *** empty log message ***
 *
 * Revision 1.4  2006/10/28 01:05:37  willuhn
 * @N add bindings on demand
 *
 * Revision 1.3  2006/10/26 23:54:15  willuhn
 * @N added needed jars
 * @N first working version
 *
 * Revision 1.2  2006/10/23 23:07:28  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2006/10/19 16:08:30  willuhn
 * *** empty log message ***
 *
 *********************************************************************/