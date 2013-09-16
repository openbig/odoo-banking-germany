/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.xmlrpc/src/de/willuhn/jameica/xmlrpc/MyLogger.java,v $
 * $Revision: 1.3 $
 * $Date: 2007/07/06 13:20:31 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.xmlrpc;

import org.apache.commons.logging.Log;
import org.apache.xmlrpc.common.XmlRpcNotAuthorizedException;

import de.willuhn.logging.Level;
import de.willuhn.logging.Logger;

/**
 * Biegt die Commons-Logging-Ausgaben zu uns um.
 */
public class MyLogger implements Log
{
  private String name = null;
  
  /**
   * ct.
   * @param name der Name des Loggers.
   */
  public MyLogger(String name)
  {
    this.name = name;
  }
  
  /**
   * @see org.apache.commons.logging.Log#debug(java.lang.Object)
   */
  public void debug(Object arg0)
  {
    if (arg0 == null)
      return;
    Logger.debug(name + ": " + arg0.toString());
  }

  /**
   * @see org.apache.commons.logging.Log#debug(java.lang.Object, java.lang.Throwable)
   */
  public void debug(Object arg0, Throwable arg1)
  {
    if (arg0 == null)
      return;
    Logger.debug(name + ": " + arg0.toString() + (arg1 != null ? (": " + arg1.toString()) : ""));
  }

  /**
   * @see org.apache.commons.logging.Log#error(java.lang.Object)
   */
  public void error(Object arg0)
  {
    if (arg0 == null)
      return;
    Logger.error(name + ": " + arg0.toString());
  }
  
  /**
   * @see org.apache.commons.logging.Log#error(java.lang.Object, java.lang.Throwable)
   */
  public void error(Object arg0, Throwable arg1)
  {
    if (arg0 == null)
      return;

    if (arg1 instanceof XmlRpcNotAuthorizedException)
    {
      // Loggen wir nur in DEBUG-Level, weil
      // viele XML-RPC-Clients immer erstmal ein
      // anonymes Login versuchen
      
      Logger.debug("unauthorized request to xml-rpc service");
      return;
    }
    Logger.error(name + ": " + (arg0 != null ? arg0.toString() : ""),arg1);
  }

  /**
   * @see org.apache.commons.logging.Log#fatal(java.lang.Object)
   */
  public void fatal(Object arg0)
  {
    if (arg0 == null)
      return;
    Logger.error(name + ": " + arg0.toString());
  }

  /**
   * @see org.apache.commons.logging.Log#fatal(java.lang.Object, java.lang.Throwable)
   */
  public void fatal(Object arg0, Throwable arg1)
  {
    if (arg0 == null)
      return;
    Logger.error(name + ": " + arg0.toString(),arg1);
  }

  /**
   * @see org.apache.commons.logging.Log#info(java.lang.Object)
   */
  public void info(Object arg0)
  {
    if (arg0 == null)
      return;
    Logger.info(name + ": " + arg0.toString());
  }

  /**
   * @see org.apache.commons.logging.Log#info(java.lang.Object, java.lang.Throwable)
   */
  public void info(Object arg0, Throwable arg1)
  {
    if (arg0 == null)
      return;
    Logger.info(name + ": " + arg0.toString() + (arg1 != null ? (": " + arg1.toString()) : ""));
  }

  /**
   * @see org.apache.commons.logging.Log#isDebugEnabled()
   */
  public boolean isDebugEnabled()
  {
    return Logger.getLevel().getValue() == Level.DEBUG.getValue();
  }

  /**
   * @see org.apache.commons.logging.Log#isErrorEnabled()
   */
  public boolean isErrorEnabled()
  {
    return true;
  }

  /**
   * @see org.apache.commons.logging.Log#isFatalEnabled()
   */
  public boolean isFatalEnabled()
  {
    return true;
  }

  /**
   * @see org.apache.commons.logging.Log#isInfoEnabled()
   */
  public boolean isInfoEnabled()
  {
    return Logger.getLevel().getValue() >= Level.INFO.getValue();
  }

  /**
   * @see org.apache.commons.logging.Log#isTraceEnabled()
   */
  public boolean isTraceEnabled()
  {
    return Logger.getLevel().getValue() == Level.DEBUG.getValue();
  }

  /**
   * @see org.apache.commons.logging.Log#isWarnEnabled()
   */
  public boolean isWarnEnabled()
  {
    return Logger.getLevel().getValue() >= Level.WARN.getValue();
  }

  /**
   * @see org.apache.commons.logging.Log#trace(java.lang.Object)
   */
  public void trace(Object arg0)
  {
    if (arg0 == null)
      return;
    Logger.debug(name + ": " + arg0.toString());
  }

  /**
   * @see org.apache.commons.logging.Log#trace(java.lang.Object, java.lang.Throwable)
   */
  public void trace(Object arg0, Throwable arg1)
  {
    if (arg0 == null)
      return;
    Logger.debug(name + ": " + arg0.toString() + (arg1 != null ? (": " + arg1.toString()) : ""));
  }

  /**
   * @see org.apache.commons.logging.Log#warn(java.lang.Object)
   */
  public void warn(Object arg0)
  {
    if (arg0 == null)
      return;
    Logger.warn(name + ": " + arg0.toString());
  }

  /**
   * @see org.apache.commons.logging.Log#warn(java.lang.Object, java.lang.Throwable)
   */
  public void warn(Object arg0, Throwable arg1)
  {
    if (arg0 == null)
      return;
    Logger.warn(name + ": " + arg0.toString() + (arg1 != null ? (": " + arg1.toString()) : ""));
  }

}


/*********************************************************************
 * $Log: MyLogger.java,v $
 * Revision 1.3  2007/07/06 13:20:31  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2006/12/22 16:14:38  willuhn
 * @N Nicht autorisierte Requests erst beim zweiten Fehlversuch binnen 0,5 sec loggen
 *
 * Revision 1.1  2006/10/31 01:43:08  willuhn
 * *** empty log message ***
 *
 **********************************************************************/