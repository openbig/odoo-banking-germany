/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/server/transport/TransportRegistry.java,v $
 * $Revision: 1.7 $
 * $Date: 2012/03/29 21:07:55 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.update.server.transport;

import java.net.URL;
import java.rmi.RemoteException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import de.willuhn.annotation.Inject;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.update.Plugin;
import de.willuhn.jameica.update.server.transport.annotation.TransportUrl;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;
import de.willuhn.util.I18N;

/**
 * Verwaltet die Transport-Protokolle.
 */
public class TransportRegistry
{
  private static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
  private static Map<String,Class<? extends Transport>> map = null;
  
  /**
   * Liefert die Transport-Implementierung fuer die URL.
   * @param url URL.
   * @return Transport-Implementierung.
   * @throws RemoteException
   */
  public static Transport getTransport(URL url) throws RemoteException
  {
    init();


    if (url == null)
      throw new RemoteException(i18n.tr("Keine URL angegeben"));
    
    String p = url.getProtocol().toLowerCase();
    Class<? extends Transport> c = map.get(p);
    if (c == null)
      throw new RemoteException(i18n.tr("Protokoll {0} wird vom Update-System nicht unterstützt",p));
    
    try
    {
      Transport t = c.newInstance();
      Inject.inject(t,TransportUrl.class,url);
      return t;
    }
    catch (Exception e)
    {
      Logger.error("unable to load class " + c,e);
      throw new RemoteException(i18n.tr("Protokoll-Implementierung {0} konnte nicht geladen werden: {1}", new String[]{p,e.getMessage()}));
    }
  }
  
  /**
   * Initialisiert die Transport-Registry.
   */
  private static synchronized void init()
  {
    if (map != null)
      return;
    
    Logger.info("init transport registry");

    map = new Hashtable<String,Class<? extends Transport>>();
    
    try
    {
      ClassFinder finder = Application.getPluginLoader().getManifest(Plugin.class).getClassLoader().getClassFinder();
      Class[] classes = finder.findImplementors(Transport.class);
      for (Class c:classes)
      {
        try
        {
          Transport t = (Transport) c.newInstance();
          Logger.info("  " + c.getName());
          List<String> protocols = t.getProtocols();
          if (protocols == null || protocols.size() == 0)
          {
            Logger.warn("  supports no protocols, skipping");
            continue;
          }
          for (String p:protocols)
          {
            if (p == null)
              continue;
            p = p.toLowerCase();
            Logger.info("    " + p);
            
            // Wir registrieren nicht die Instanz sondern
            // nur die Klasse. Wir erzeugen anschliessend
            // fuer jedes Kommando eine neue Instanz.
            map.put(p,t.getClass());
          }
        }
        catch (Throwable t)
        {
          Logger.error("unable to load " + c + ", skipping",t);
        }
      }
    }
    catch (ClassNotFoundException e)
    {
      Logger.error("no transport implementations found",e);
    }
  }
}


/**********************************************************************
 * $Log: TransportRegistry.java,v $
 * Revision 1.7  2012/03/29 21:07:55  willuhn
 * @C Kompatibilitaet zu Jameica 2.2 leider doch nicht moeglich
 *
 * Revision 1.6  2012/03/29 20:50:47  willuhn
 * @C Kompatibilitaet zu Jameica 2.2 wieder hergestellt
 *
 * Revision 1.5  2012/03/28 22:28:03  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.4  2011-06-01 11:02:39  willuhn
 * @N Update auf 1.1 - benoetigt jetzt Jameica 1.11 oder hoeher
 *
 * Revision 1.3  2011-03-30 12:14:03  willuhn
 * @N Neuer Injector fuer DI
 *
 * Revision 1.2  2009/10/28 01:20:48  willuhn
 * @N Erster Code fuer automatische Update-Checks
 * @C Code-Cleanup - sauberere Fehlermeldung, wenn Plugins auf dem Server nicht (mehr) gefunden werden
 *
 * Revision 1.1  2008/12/12 01:13:17  willuhn
 * @N Transport-API
 *
 **********************************************************************/
