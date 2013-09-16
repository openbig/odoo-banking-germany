/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/deploy/WebAdminDeployer.java,v $
 * $Revision: 1.6 $
 * $Date: 2009/04/23 09:05:51 $
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

import org.mortbay.jetty.security.UserRealm;

import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.webadmin.Plugin;
import de.willuhn.jameica.webadmin.Settings;
import de.willuhn.jameica.webadmin.server.JameicaUserRealm;

/**
 * Deployed die Admin-Console.
 */
public class WebAdminDeployer extends AbstractWebAppDeployer
{
  /**
   * @see de.willuhn.jameica.webadmin.deploy.AbstractWebAppDeployer#getContext()
   */
  protected String getContext()
  {
    return "/webadmin";
  }

  /**
   * @see de.willuhn.jameica.webadmin.deploy.AbstractWebAppDeployer#getPath()
   */
  protected String getPath()
  {
    return Application.getPluginLoader().getPlugin(Plugin.class).getManifest().getPluginDir() + File.separator + "webapps" + File.separator + "webadmin";
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
    return Settings.getUseAuth() ? new JameicaUserRealm() : null;
  }
  
  
}


/*********************************************************************
 * $Log: WebAdminDeployer.java,v $
 * Revision 1.6  2009/04/23 09:05:51  willuhn
 * @C deprecated api
 *
 * Revision 1.5  2007/12/04 12:13:48  willuhn
 * @N Login pro Webanwendung konfigurierbar
 *
 * Revision 1.4  2007/12/03 23:43:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2007/12/03 19:00:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2007/05/15 15:33:17  willuhn
 * @N helloworld.war
 * @C Webadmin komplett auf JSP umgestellt
 * @C build-Script angepasst
 *
 * Revision 1.1  2007/05/15 13:42:36  willuhn
 * @N Deployment von Webapps, WARs fertig und konfigurierbar
 *
 **********************************************************************/