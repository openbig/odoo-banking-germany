/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/deploy/WarDeployer.java,v $
 * $Revision: 1.11 $
 * $Date: 2012/03/29 21:11:30 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.webadmin.deploy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.webapp.WebAppContext;

import de.willuhn.io.FileFinder;
import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.webadmin.Plugin;
import de.willuhn.logging.Logger;

/**
 * Durchsucht alle Plugins nach WAR-Dateien und deployed sie.
 */
public class WarDeployer implements Deployer
{

  /**
   * @see de.willuhn.jameica.webadmin.deploy.Deployer#deploy()
   */
  public Handler[] deploy()
  {
    List<Config> wars = new ArrayList<Config>();

    ////////////////////////////////////////////////////////////////////////////
    // 1. Wir suchen nach War-Datein in ~/.jameica/jameica.webadmin/webapps
    {
      String work = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getWorkPath();
      File dir = new File(work,"webapps");

      FileFinder finder = new FileFinder(dir);
      finder.extension("war");
      File[] files = finder.findRecursive();
      for (File f:files)
      {
        wars.add(new Config(f,null));
      }
    }
    ////////////////////////////////////////////////////////////////////////////
    

    
    ////////////////////////////////////////////////////////////////////////////
    // 2. Wir suchen in den Plugin-Verzeichnissen

    List list = Application.getPluginLoader().getInstalledPlugins();

    for (int i=0;i<list.size();++i)
    {
      AbstractPlugin plugin = (AbstractPlugin) list.get(i);
      File dir = new File(plugin.getManifest().getPluginDir());

      FileFinder finder = new FileFinder(dir);
      finder.extension("war");
      File[] files = finder.findRecursive();
      for (File f:files)
      {
        wars.add(new Config(f,plugin));
      }
    }
    ////////////////////////////////////////////////////////////////////////////

    if (wars.size() == 0)
    {
      Logger.info("no war files found");
      return null;
    }

    List<Handler> handlers = new ArrayList<Handler>();

    for (Config c:wars)
    {
      final String path    = c.file.getAbsolutePath();
      final String context = "/" + c.file.getName().replaceFirst("\\.war$",""); // ".war" am Ende noch abschneiden 

      Logger.info("deploying " + context + " (" + path + ")");

      try
      {
        final WebAppContext ctx = new WebAppContext(path,context);

        // Classloader explizit angeben. Sonst verwendet Jetty den System-Classloader, der nichts kennt
        if (c.plugin != null)
          ctx.setClassLoader(c.plugin.getManifest().getClassLoader());

        handlers.add(ctx);
      }
      catch (Exception e)
      {
        Logger.error("unable to deploy " + context, e);
      }
    }
    return (Handler[]) handlers.toArray(new Handler[handlers.size()]);
  }
  
  /**
   * Hilfsklasse, um WAR-Datei und Plugin zusammenzuhalten.
   */
  private class Config
  {
    private File file = null;
    private AbstractPlugin plugin = null;
    
    /**
     * ct.
     * @param file
     * @param plugin
     */
    private Config(File file, AbstractPlugin plugin)
    {
      this.file = file;
      this.plugin = plugin;
    }
  }

}


/*********************************************************************
 * $Log: WarDeployer.java,v $
 * Revision 1.11  2012/03/29 21:11:30  willuhn
 * @C Kompatibilitaet zu Jameica 2.2 leider doch nicht moeglich
 *
 * Revision 1.10  2012/03/29 20:54:40  willuhn
 * @C Kompatibilitaet zu Jameica 2.2 wieder hergestellt
 *
 * Revision 1.9  2012/03/28 22:28:21  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.8  2009/09/24 12:04:06  willuhn
 * @N Deployer, um auch externe Web-Anwendungen deployen zu koennen
 *
 * Revision 1.7  2009/04/23 09:05:51  willuhn
 * @C deprecated api
 *
 * Revision 1.6  2008/04/10 13:02:29  willuhn
 * @N Zweischritt-Deployment. Der Server wird zwar sofort initialisiert, wenn der Jameica-Service startet, gestartet wird er aber erst, wenn die ersten Handler resgistriert werden
 * @N damit koennen auch nachtraeglich zur Laufzeit weitere Handler hinzu registriert werden
 * @R separater Worker in HttpServiceImpl entfernt. Der Classloader wird nun direkt von den Deployern gesetzt. Das ist wichtig, da Jetty fuer die Webanwendungen sonst den System-Classloader nutzt, welcher die Plugins nicht kennt
 *
 * Revision 1.5  2007/12/04 18:43:27  willuhn
 * @N Update auf Jetty 6.1.6
 * @N request.getRemoteUser() geht!!
 *
 * Revision 1.4  2007/12/03 23:43:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2007/12/03 19:00:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2007/06/01 09:36:23  willuhn
 * @B war deployer bricht mit der Suche zu frueh ab
 *
 * Revision 1.1  2007/05/15 13:42:36  willuhn
 * @N Deployment von Webapps, WARs fertig und konfigurierbar
 *
 **********************************************************************/