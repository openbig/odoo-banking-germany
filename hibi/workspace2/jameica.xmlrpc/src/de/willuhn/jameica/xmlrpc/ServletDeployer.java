/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.xmlrpc/src/de/willuhn/jameica/xmlrpc/ServletDeployer.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/04/04 00:17:13 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.xmlrpc;

import org.mortbay.jetty.security.UserRealm;

import de.willuhn.jameica.webadmin.deploy.AbstractServletDeployer;
import de.willuhn.jameica.webadmin.server.JameicaUserRealm;
import de.willuhn.jameica.xmlrpc.server.XmlRpcServlet;

/**
 * Deployer fuer das XML-RPC-Servlet.
 */
public class ServletDeployer extends AbstractServletDeployer
{
  /**
   * @see de.willuhn.jameica.webadmin.deploy.AbstractServletDeployer#getContext()
   */
  protected String getContext()
  {
    return "/xmlrpc";
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
    return new JameicaUserRealm()
    {

      /**
       * @see de.willuhn.jameica.webadmin.server.JameicaUserRealm#getName()
       */
      public String getName()
      {
        // Name des Realms koennte man mal noch konfigurierbar machen
        // Aber aus Gruenden der Abwaertskompatibilitaet (zur alten
        // jameica.xmlrpc-Version) lass ich das mal stehen.
        return "XML-RPC";
      }
      
    };
  }

  /**
   * @see de.willuhn.jameica.webadmin.deploy.AbstractServletDeployer#getServletClass()
   */
  protected Class getServletClass()
  {
    return XmlRpcServlet.class;
  }

}


/*********************************************************************
 * $Log: ServletDeployer.java,v $
 * Revision 1.1  2008/04/04 00:17:13  willuhn
 * @N Apache XML-RPC von 3.0 auf 3.1 aktualisiert
 * @N jameica.xmlrpc ist jetzt von jameica.webadmin abhaengig
 * @N jameica.xmlrpc nutzt jetzt keinen eigenen embedded Webserver mehr sondern den Jetty von jameica.webadmin mittels Servlet. Damit kann nun XML-RPC ueber den gleichen TCP-Port (8080) gemacht werden, wo auch die restlichen Webfrontends laufen -> spart einen TCP-Port und skaliert besser wegen Multi-Threading-Support in Jetty
 *
 **********************************************************************/