/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/deploy/AbstractServletDeployer.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/04/10 13:02:29 $
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
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.jetty.servlet.ServletHandler;

import de.willuhn.jameica.webadmin.Settings;
import de.willuhn.jameica.webadmin.server.JameicaUserRealm;
import de.willuhn.logging.Logger;

/**
 * Abstrakte Basis-Implementierung eines Deployers, der Servlets deployen kann.
 */
public abstract class AbstractServletDeployer implements Deployer
{

  /**
   * @see de.willuhn.jameica.webadmin.deploy.Deployer#deploy()
   */
  public final Handler[] deploy()
  {
    String context = getContext();
    Class servlet  = getServletClass();

    Logger.info("deploying " + context + " (" + servlet.getName() + ")");

    ServletHandler sv = new ServletHandler();
    sv.addServletWithMapping(servlet,"/");

    ContextHandler handler = new ContextHandler(context);
    handler.setHandler(sv);
    
    // Classloader explizit angeben. Sonst verwendet Jetty den System-Classloader, der nichts kennt
    handler.setClassLoader(this.getClass().getClassLoader());

    if (! Settings.getUseAuth())
      return new Handler[]{handler};

    UserRealm realm = getUserRealm();
    if (realm == null)
    {
      Logger.error("  no user realm defined but authentication activated. fallback to login via master password");
      realm = new JameicaUserRealm();
    }
    Logger.info("  activating authentication via " + realm.getName());
    Constraint constraint = new Constraint();
    constraint.setName(Constraint.__BASIC_AUTH);
    constraint.setAuthenticate(true);
    String[] roles = getSecurityRoles();
    if (roles != null)
    {
      constraint.setRoles(roles);
    }

    ConstraintMapping cm = new ConstraintMapping();
    cm.setConstraint(constraint);
    cm.setPathSpec("/*");

    SecurityHandler sh = new SecurityHandler();
    sh.setUserRealm(realm);
    sh.setConstraintMappings(new ConstraintMapping[]{cm});
    sh.setHandler(handler);

    return new Handler[]{sh};
  }
  
  /**
   * Liefert den Namen des Contextes.
   * Soll die Webanwendung also unter "http://server/test" erreichbar
   * sein, muss die Funktion "/test" zurueckliefern.
   * @return der Name des Context.
   */
  protected abstract String getContext();
  
  /**
   * Liefert das zu deployende Servlet.
   * @return das Servlet.
   */
  protected abstract Class getServletClass();
  
  /**
   * Liefert die Benutzer-Rollen, die im Servlet zur Verfuegung stehen.
   * Dummy-Implementierung, die keine Rollen zurueckliefert.
   * Kann jedoch ueberschrieben werden.
   * @return Liste der Rollen des Servlets.
   */
  protected String[] getSecurityRoles()
  {
    return null;
  }
  
  /**
   * Liefert das Login-Handle, welches fuer das Servlet verwendet werden soll.
   * Dummy-Implementierung, die kein Login-Handle zurueckliefert.
   * Kann jedoch ueberschrieben werden.
   * @return das Login-Handle.
   */
  protected UserRealm getUserRealm()
  {
    return null;
  }
}


/*********************************************************************
 * $Log: AbstractServletDeployer.java,v $
 * Revision 1.2  2008/04/10 13:02:29  willuhn
 * @N Zweischritt-Deployment. Der Server wird zwar sofort initialisiert, wenn der Jameica-Service startet, gestartet wird er aber erst, wenn die ersten Handler resgistriert werden
 * @N damit koennen auch nachtraeglich zur Laufzeit weitere Handler hinzu registriert werden
 * @R separater Worker in HttpServiceImpl entfernt. Der Classloader wird nun direkt von den Deployern gesetzt. Das ist wichtig, da Jetty fuer die Webanwendungen sonst den System-Classloader nutzt, welcher die Plugins nicht kennt
 *
 * Revision 1.1  2008/04/04 00:16:58  willuhn
 * @N Apache XML-RPC von 3.0 auf 3.1 aktualisiert
 * @N jameica.xmlrpc ist jetzt von jameica.webadmin abhaengig
 * @N jameica.xmlrpc nutzt jetzt keinen eigenen embedded Webserver mehr sondern den Jetty von jameica.webadmin mittels Servlet. Damit kann nun XML-RPC ueber den gleichen TCP-Port (8080) gemacht werden, wo auch die restlichen Webfrontends laufen -> spart einen TCP-Port und skaliert besser wegen Multi-Threading-Support in Jetty
 *
 **********************************************************************/