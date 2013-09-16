/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.xmlrpc/src/de/willuhn/jameica/xmlrpc/server/XmlRpcServlet.java,v $
 * $Revision: 1.2 $
 * $Date: 2008/04/10 11:17:13 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.xmlrpc.server;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.XmlRpcServletServer;

import de.willuhn.logging.Logger;


/**
 * Unser XmlRpcServlet
 */
public class XmlRpcServlet extends HttpServlet
{
  private XmlRpcServletServer server;

  /**
   * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
   */
  public void init(ServletConfig config) throws ServletException
  {
    super.init(config);
    this.server = new XmlRpcServletServer();
    this.server.setHandlerMapping(new HandlerMappingImpl());
    
    ((XmlRpcServerConfigImpl) this.server.getConfig()).setEnabledForExtensions(true);
  }

  /**
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    response.setHeader("Allow","POST"); // http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
    response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED,"HTTP GET not supported, use POST instead");
  }

  /**
   * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
   */
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
  {
    try
    {
      this.server.execute(request,response);
    }
    catch (ServletException se)
    {
      Logger.error("unable to process request",se);
      throw se;
    }
    catch (IOException ioe)
    {
      Logger.error("unable to process request",ioe);
      throw ioe;
    }
    catch (Exception e)
    {
      Logger.error("unable to process request",e);
      throw new ServletException(e);
    }
  }


  /**
   * @see javax.servlet.GenericServlet#log(java.lang.String, java.lang.Throwable)
   */
  public void log(String text, Throwable t)
  {
    Logger.error(text,t);
  }

  /**
   * @see javax.servlet.GenericServlet#log(java.lang.String)
   */
  public void log(String text)
  {
    Logger.info(text);
  }

}


/**********************************************************************
 * $Log: XmlRpcServlet.java,v $
 * Revision 1.2  2008/04/10 11:17:13  willuhn
 * @C HTTP-Code via Konstante
 *
 * Revision 1.1  2008/04/04 00:17:13  willuhn
 * @N Apache XML-RPC von 3.0 auf 3.1 aktualisiert
 * @N jameica.xmlrpc ist jetzt von jameica.webadmin abhaengig
 * @N jameica.xmlrpc nutzt jetzt keinen eigenen embedded Webserver mehr sondern den Jetty von jameica.webadmin mittels Servlet. Damit kann nun XML-RPC ueber den gleichen TCP-Port (8080) gemacht werden, wo auch die restlichen Webfrontends laufen -> spart einen TCP-Port und skaliert besser wegen Multi-Threading-Support in Jetty
 *
 **********************************************************************/
