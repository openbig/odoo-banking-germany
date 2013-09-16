/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.sensors/src/de/willuhn/jameica/sensors/web/Deployer.java,v $
 * $Revision: 1.4 $
 * $Date: 2009/09/16 11:26:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.sensors.web;

import java.io.File;

import org.mortbay.jetty.security.UserRealm;

import de.willuhn.jameica.plugin.Manifest;
import de.willuhn.jameica.sensors.Plugin;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.webadmin.deploy.AbstractWebAppDeployer;
import de.willuhn.jameica.webadmin.server.JameicaUserRealm;

/**
 * Deployer fuer das Hibiscus-Webfrontend.
 */
public class Deployer extends AbstractWebAppDeployer
{
  /**
   * @see de.willuhn.jameica.webadmin.deploy.AbstractWebAppDeployer#getContext()
   */
  protected String getContext()
  {
    return "/sensors";
  }

  /**
   * @see de.willuhn.jameica.webadmin.deploy.AbstractWebAppDeployer#getPath()
   */
  protected String getPath()
  {
    Manifest mf = Application.getPluginLoader().getManifest(Plugin.class);
    return mf.getPluginDir() + File.separator + "webapps" + getContext();
  }
  
  /**
   * @see de.willuhn.jameica.webadmin.deploy.AbstractWebAppDeployer#getSecurityRoles()
   */
  protected String[] getSecurityRoles()
  {
    return new String[]{"admin"};
  }

  /**
   * @see de.willuhn.jameica.webadmin.deploy.AbstractWebAppDeployer#getUserRealm()
   */
  protected UserRealm getUserRealm()
  {
    return de.willuhn.jameica.webadmin.Settings.getUseAuth() ? new JameicaUserRealm() : null;
  }

}


/*********************************************************************
 * $Log: Deployer.java,v $
 * Revision 1.4  2009/09/16 11:26:14  willuhn
 * @C Auth fuer /sensors von /webadmin wiederverwenden
 *
 * Revision 1.3  2009/08/21 14:10:35  willuhn
 * @N Authentifizierung optional
 *
 * Revision 1.2  2009/08/19 23:46:29  willuhn
 * @N Erster Code fuer die JPA-Persistierung
 *
 * Revision 1.1  2009/08/19 10:34:43  willuhn
 * @N initial import
 *
 * Revision 1.3  2009/08/18 23:33:57  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2009/08/18 23:27:33  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2009/08/18 23:00:25  willuhn
 * @N Erste Version mit Web-Frontend
 *
 **********************************************************************/