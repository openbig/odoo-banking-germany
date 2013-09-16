/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/rmi/HttpService.java,v $
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

package de.willuhn.jameica.webadmin.rmi;

import java.rmi.RemoteException;

import org.mortbay.jetty.Handler;

import de.willuhn.datasource.Service;


/**
 * Service, der den HTTP-Dienst startet und beendet.
 */
public interface HttpService extends Service
{
  /**
   * Fuegt dem Server einen Handler hinzu. 
   * @param handler der neue Handler.
   * @throws RemoteException
   */
  public void addHandler(Handler handler) throws RemoteException;
}


/**********************************************************************
 * $Log: HttpService.java,v $
 * Revision 1.2  2008/04/10 13:02:29  willuhn
 * @N Zweischritt-Deployment. Der Server wird zwar sofort initialisiert, wenn der Jameica-Service startet, gestartet wird er aber erst, wenn die ersten Handler resgistriert werden
 * @N damit koennen auch nachtraeglich zur Laufzeit weitere Handler hinzu registriert werden
 * @R separater Worker in HttpServiceImpl entfernt. Der Classloader wird nun direkt von den Deployern gesetzt. Das ist wichtig, da Jetty fuer die Webanwendungen sonst den System-Classloader nutzt, welcher die Plugins nicht kennt
 *
 * Revision 1.1  2007/04/09 17:12:01  willuhn
 * *** empty log message ***
 *
 **********************************************************************/
