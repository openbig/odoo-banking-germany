/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webtools/RequestConfig.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/10/27 14:32:18 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.webtools;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Eine Bean, die die Properties eines Requests haelt.
 */
public class RequestConfig
{
  private ClassLoader classloader      = null;
  private HttpServletRequest request   = null;
  private HttpServletResponse response = null;
  
  /**
   * ct.
   * @param loader der Classloader.
   * @param request der Request.
   * @param response das Response.
   */
  RequestConfig(ClassLoader loader,
                HttpServletRequest request,
                HttpServletResponse response)
  {
    this.classloader = loader;
    this.request     = request;
    this.response    = response;
  }
  
  /**
   * Liefert den Classloader.
   * @return der Classloader.
   */
  public ClassLoader getClassloader()
  {
    return this.classloader;
  }
  
  /**
   * Liefert den Request.
   * @return der Request.
   */
  public HttpServletRequest getRequest()
  {
    return this.request;
  }

  /**
   * Liefert das Response.
   * @return das Response.
   */
  public HttpServletResponse getResponse()
  {
    return this.response;
  }
}


/**********************************************************************
 * $Log: RequestConfig.java,v $
 * Revision 1.1  2010/10/27 14:32:18  willuhn
 * @R jameica.webtools ist jetzt Bestandteil von jameica.webadmin. Das separate webtools-Plugin ist nicht mehr noetig
 *
 * Revision 1.1  2009/08/05 11:04:00  willuhn
 * @C Code cleanup
 * @N Webtools kennt jetzt die Lifecycle-Annotation
 *
 **********************************************************************/
