/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/deploy/Deployer.java,v $
 * $Revision: 1.2 $
 * $Date: 2007/12/04 18:43:27 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.webadmin.deploy;

import org.mortbay.jetty.Handler;

/**
 * Basis-Interface fuer Applikations-Deployer.
 */
public interface Deployer
{
  /**
   * Deployed ein oder mehrere Webanwendungen in den Server.
   * @return die Handler.
   */
  public Handler[] deploy();
}


/*********************************************************************
 * $Log: Deployer.java,v $
 * Revision 1.2  2007/12/04 18:43:27  willuhn
 * @N Update auf Jetty 6.1.6
 * @N request.getRemoteUser() geht!!
 *
 * Revision 1.1  2007/05/15 13:42:36  willuhn
 * @N Deployment von Webapps, WARs fertig und konfigurierbar
 *
 **********************************************************************/