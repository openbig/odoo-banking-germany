/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/web/controller/Config.java,v $
 * $Revision: 1.4 $
 * $Date: 2012/03/28 22:28:18 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.web.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import de.willuhn.annotation.Lifecycle;
import de.willuhn.annotation.Lifecycle.Type;
import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.sensors.config.Configurable;
import de.willuhn.jameica.sensors.config.Parameter;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.webadmin.annotation.Request;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;

/**
 * Controller-Bean fuer die Konfiguration.
 */
@Lifecycle(Type.REQUEST)
public class Config
{
  @Request
  private HttpServletRequest request = null;
  
  private List<Configurable> configs = null;
  
  private String status = null;
  
  /**
   * Liefert eine Liste der Configurables.
   * @return Liste der Configurables.
   */
  public synchronized List<Configurable> getConfigs()
  {
    if (this.configs != null)
      return this.configs;
    
    this.configs = new ArrayList<Configurable>();
    try
    {
      ClassFinder finder = Application.getPluginLoader().getManifest(Plugin.class).getClassLoader().getClassFinder();
      Class<Configurable>[] found = finder.findImplementors(Configurable.class);
      for (Class<Configurable> c:found)
      {
        try
        {
          this.configs.add(c.newInstance());
        }
        catch (Exception e)
        {
          Logger.error("unable to load configurable " + c + " - skipping",e);
        }
      }
      
    }
    catch (ClassNotFoundException e)
    {
      Logger.error("no configurables found");
    }
    return this.configs;
  }
  
  /**
   * Speichert die Konfigurationen.
   * @throws Exception
   */
  public void save() throws Exception
  {
    List<Configurable> configs = this.getConfigs();
    for (Configurable c:configs)
    {
      List<Parameter> parameters = c.getParameters();
      for (Parameter p:parameters)
        p.setValue(request.getParameter(p.getUuid()));
      c.setParameters(parameters);
    }
    this.status = "settings saved";
  }
  
  /**
   * Liefert den aktuellen Status-Text.
   * @return der Status-Text.
   */
  public String getStatus()
  {
    return this.status;
  }
}


/**********************************************************************
 * $Log: Config.java,v $
 * Revision 1.4  2012/03/28 22:28:18  willuhn
 * @N Einfuehrung eines neuen Interfaces "Plugin", welches von "AbstractPlugin" implementiert wird. Es dient dazu, kuenftig auch Jameica-Plugins zu unterstuetzen, die selbst gar keinen eigenen Java-Code mitbringen sondern nur ein Manifest ("plugin.xml") und z.Bsp. Jars oder JS-Dateien. Plugin-Autoren muessen lediglich darauf achten, dass die Jameica-Funktionen, die bisher ein Object vom Typ "AbstractPlugin" zuruecklieferten, jetzt eines vom Typ "Plugin" liefern.
 * @C "getClassloader()" verschoben von "plugin.getRessources().getClassloader()" zu "manifest.getClassloader()" - der Zugriffsweg ist kuerzer. Die alte Variante existiert weiterhin, ist jedoch als deprecated markiert.
 *
 * Revision 1.3  2011-09-13 09:08:34  willuhn
 * @C Code-Cleanup
 *
 * Revision 1.2  2011-06-28 09:56:36  willuhn
 * @N Lifecycle-Annotation aus jameica.webadmin in util verschoben
 *
 * Revision 1.1  2009/09/15 17:00:17  willuhn
 * @N Konfigurierbarkeit aller Module ueber das Webfrontend
 *
 **********************************************************************/
