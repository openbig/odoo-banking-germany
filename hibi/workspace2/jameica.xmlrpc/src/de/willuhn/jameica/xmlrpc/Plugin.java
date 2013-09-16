/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.xmlrpc/src/de/willuhn/jameica/xmlrpc/Plugin.java,v $
 * $Revision: 1.7 $
 * $Date: 2009/06/16 08:35:14 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.xmlrpc;

import org.apache.commons.logging.LogFactory;

import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.util.ApplicationException;

/**
 * @author willuhn
 */
public class Plugin extends AbstractPlugin
{
  /**
   * @see de.willuhn.jameica.plugin.AbstractPlugin#init()
   */
  public void init() throws ApplicationException
  {
    LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log",MyLogger.class.getName());
  }
}


/*********************************************************************
 * $Log: Plugin.java,v $
 * Revision 1.7  2009/06/16 08:35:14  willuhn
 * @R removed unused methods
 *
 * Revision 1.6  2008/01/18 14:39:32  willuhn
 * @D
 *
 * Revision 1.5  2007/12/06 09:32:01  willuhn
 * @D javadoc warnings
 *
 * Revision 1.4  2007/11/05 13:01:09  willuhn
 * @C Compiler-Warnings
 *
 * Revision 1.3  2006/10/31 01:43:08  willuhn
 * *** empty log message ***
 *
 * Revision 1.2  2006/10/19 16:08:30  willuhn
 * *** empty log message ***
 *
 * Revision 1.1  2006/10/19 15:27:01  willuhn
 * @N initial checkin
 *
 *********************************************************************/