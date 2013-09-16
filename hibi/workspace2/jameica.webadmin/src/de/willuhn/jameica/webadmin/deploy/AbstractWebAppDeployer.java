/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/deploy/AbstractWebAppDeployer.java,v $
 * $Revision: 1.7 $
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
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.jetty.webapp.WebAppContext;

import de.willuhn.logging.Logger;

/**
 * Abstrakte Basis-Implementierung eines Deployers, der Web-Anwendungen deployen kann.
 * Zum Etablieren einer Webanwendung muss lediglich von dieser Klasse abgeleitet werden.
 */
public abstract class AbstractWebAppDeployer implements Deployer
{

  /**
   * @see de.willuhn.jameica.webadmin.deploy.Deployer#deploy()
   */
  public final Handler[] deploy()
  {
    String path    = getPath();
    String context = getContext();

    Logger.info("deploying " + context + " (" + path + ")");
    WebAppContext app = new WebAppContext(path,context);

    // Classloader explizit angeben. Sonst verwendet Jetty den System-Classloader, der nichts kennt
    app.setClassLoader(this.getClass().getClassLoader());

    UserRealm realm = getUserRealm();
    if (realm != null)
    {
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

      // Wir nehmen uns den Security-Handler der Webapp und passen
      // ihne fuer uns an.
      // NIE WIEDER AENDERN! Sonst liefert request.getRemoteUser() null!
      SecurityHandler sh = app.getSecurityHandler();
      sh.setUserRealm(realm);
      sh.setConstraintMappings(new ConstraintMapping[]{cm});
//    app.setSecurityHandler(sh);
    }
    
    return new Handler[]{app};
  }
  
  /**
   * Liefert den Pfad im Dateisystem zu der Web-Anwendung.
   * Also das Verzeichnis, in dem sich die index.jsp befindet.
   * @return Pfad im Dateisystem zur Webanwendung.
   */
  protected abstract String getPath();
  
  /**
   * Liefert den Namen des Contextes.
   * Soll die Webanwendung also unter "http://server/test" erreichbar
   * sein, muss die Funktion "/test" zurueckliefern.
   * @return der Name des Context.
   */
  protected abstract String getContext();
  
  /**
   * Liefert die Benutzer-Rollen, die in der Web-Applikation zur Verfuegung stehen.
   * Dummy-Implementierung, die keine Rollen zurueckliefert.
   * Kann jedoch ueberschrieben werden.
   * @return Liste der Rollen der Webanwendung. 
   */
  protected String[] getSecurityRoles()
  {
    return null;
  }
  
  /**
   * Liefert das Login-Handle, welches fuer die Web-Applikation verwendet werden soll.
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
 * $Log: AbstractWebAppDeployer.java,v $
 * Revision 1.7  2008/04/10 13:02:29  willuhn
 * @N Zweischritt-Deployment. Der Server wird zwar sofort initialisiert, wenn der Jameica-Service startet, gestartet wird er aber erst, wenn die ersten Handler resgistriert werden
 * @N damit koennen auch nachtraeglich zur Laufzeit weitere Handler hinzu registriert werden
 * @R separater Worker in HttpServiceImpl entfernt. Der Classloader wird nun direkt von den Deployern gesetzt. Das ist wichtig, da Jetty fuer die Webanwendungen sonst den System-Classloader nutzt, welcher die Plugins nicht kennt
 *
 * Revision 1.6  2007/12/04 19:09:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2007/12/04 18:43:27  willuhn
 * @N Update auf Jetty 6.1.6
 * @N request.getRemoteUser() geht!!
 *
 * Revision 1.4  2007/12/04 12:13:48  willuhn
 * @N Login pro Webanwendung konfigurierbar
 *
 * Revision 1.3  2007/12/03 23:43:49  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2007/12/03 19:00:19  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2007/05/15 13:42:36  willuhn
 * @N Deployment von Webapps, WARs fertig und konfigurierbar
 *
 **********************************************************************/