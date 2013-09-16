/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/rmi/RestService.java,v $
 * $Revision: 1.5 $
 * $Date: 2010/05/11 23:21:44 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.webadmin.rmi;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.willuhn.datasource.Service;
import de.willuhn.jameica.webadmin.beans.RestBeanDoc;

/**
 * Service, der eine Mini-REST-API bereitstellt.
 */
public interface RestService extends Service
{
  /**
   * Verarbeitet einen Request.
   * @param request
   * @param response
   * @throws IOException
   */
  public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException;
  
  /**
   * Registriert eine Bean.
   * @param bean die zu registrierende Bean.
   * Die Bean muss einen parameterlosen Konstruktor mit public-Modifier besitzen
   * und mindestens eine Annotation "Path("/gewuenschte/url")" besitzen, um
   * korrekt registriert zu werden.
   * @throws RemoteException
   */
  public void register(Object bean) throws RemoteException;

  /**
   * Deregistriert die Bean.
   * @param bean die Bean.
   * @throws RemoteException
   */
  public void unregister(Object bean) throws RemoteException;
  
  /**
   * Liefert die Dokumentation der REST-Beans.
   * @return Dokumentation der REST-Beans.
   * @throws RemoteException
   */
  public List<RestBeanDoc> getDoc() throws RemoteException;
}


/*********************************************************************
 * $Log: RestService.java,v $
 * Revision 1.5  2010/05/11 23:21:44  willuhn
 * @N Automatische Dokumentations-Seite fuer die REST-Beans basierend auf der Annotation "Doc"
 *
 * Revision 1.4  2008/10/21 22:33:47  willuhn
 * @N Markieren der zu registrierenden REST-Kommandos via Annotation
 *
 * Revision 1.3  2008/07/11 15:38:55  willuhn
 * @N Service-Deployment
 *
 * Revision 1.2  2008/06/16 14:22:11  willuhn
 * @N Mapping der REST-URLs via Property-Datei
 *
 * Revision 1.1  2008/06/13 14:11:04  willuhn
 * @N Mini REST-API
 *
 **********************************************************************/