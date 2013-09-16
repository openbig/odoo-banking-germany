/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/deploy/WebappsDeployer.java,v $
 * $Revision: 1.2 $
 * $Date: 2009/09/24 12:28:29 $
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
import org.mortbay.resource.Resource;

import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.webadmin.Plugin;
import de.willuhn.logging.Logger;

/**
 * Durchsucht das Verzeichnis "jameica.webadmin/webapps" im Workverzeichnis
 * des Users "~/.jameica" nach Unterverzeichnissen und registriert diese als
 * Webapps. Auf diese Weise kann man "jameica.webadmin" auch nutzen, um
 * externe Web-Apps zu hosten - indem man sie einfach da hinein verlinkt.
 */
public class WebappsDeployer implements Deployer
{

  /**
   * @see de.willuhn.jameica.webadmin.deploy.Deployer#deploy()
   */
  public Handler[] deploy()
  {
    String work = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getWorkPath();
    File dir = new File(work,"webapps");
    if (!dir.exists())
    {
      // Wenns noch nicht existiert, erstellen wir es wenigstens, damit
      // das der User nicht manuell machen muss. In dem Fall brauchen
      // wir aber nicht reinschauen ;)
      dir.mkdirs();
      return null;
    }
    
    List<Handler> handlers = new ArrayList<Handler>();
  
    Logger.info("scanning " + dir.getAbsolutePath());
    File[] dirs = dir.listFiles();
    for (File f:dirs)
    {
      if (!f.isDirectory() || !f.canRead())
      {
        Logger.info("  skipping " + f.getAbsolutePath() + " - no directory or not readable");
        continue;
      }

      final String path    = f.getAbsolutePath();
      final String context = "/" + f.getName();

      try
      {
        Logger.info("deploying " + context + " (" + path + ")");
        final WebAppContext ctx = new WebAppContext(path,context);
        ctx.setBaseResource(Resource.newResource(f.getAbsolutePath()));
        
        handlers.add(ctx);
      }
      catch (Exception e)
      {
        Logger.error("unable to deploy " + context, e);
      }
    }
    return handlers.toArray(new Handler[handlers.size()]);
  }

}


/*********************************************************************
 * $Log: WebappsDeployer.java,v $
 * Revision 1.2  2009/09/24 12:28:29  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2009/09/24 12:04:06  willuhn
 * @N Deployer, um auch externe Web-Anwendungen deployen zu koennen
 *
 **********************************************************************/