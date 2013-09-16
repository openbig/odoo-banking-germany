/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/servlets/RestServlet.java,v $
 * $Revision: 1.1 $
 * $Date: 2008/06/13 14:11:04 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.webadmin.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.webadmin.Plugin;
import de.willuhn.jameica.webadmin.rmi.RestService;
import de.willuhn.logging.Logger;

/**
 * Main-Servlet fuer die Mini REST-API.
 */
public class RestServlet extends HttpServlet
{
  /**
   * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    try
    {
      RestService service = (RestService) Application.getServiceFactory().lookup(Plugin.class,"rest");
      service.handleRequest(req,resp);
    }
    catch (IOException ioe)
    {
      // Explizite Exceptions lassen wir durch
      throw ioe;
    }
    catch (Exception e)
    {
      Logger.error("unable to load REST Service",e);
      
      // Wir werfen die urspruengliche Exception bewusst nicht weiter, weil sie
      // sensible Daten enthalten kann. Sie landet daher nur im Log.
      throw new ServletException("unable to load REST Service");
    }
  }

}


/*********************************************************************
 * $Log: RestServlet.java,v $
 * Revision 1.1  2008/06/13 14:11:04  willuhn
 * @N Mini REST-API
 *
 **********************************************************************/