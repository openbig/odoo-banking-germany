/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.soap/src/de/willuhn/jameica/soap/Plugin.java,v $
 * $Revision: 1.9 $
 * $Date: 2010/01/19 00:33:59 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.soap;

import org.apache.commons.logging.LogFactory;

import de.willuhn.jameica.plugin.AbstractPlugin;
import de.willuhn.jameica.soap.messaging.PublishServiceMessageConsumer;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;


/**
 * Plugin-Klasse des SOAP-Konnektors
 */
public class Plugin extends AbstractPlugin
{
  /**
   * @see de.willuhn.jameica.plugin.AbstractPlugin#init()
   */
  public void init() throws ApplicationException
  {
    LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log",MyLogger.class.getName());
    try
    {
      Class.forName("de.willuhn.logging.JavaLoggingHandler");
    }
    catch (Exception e)
    {
      Logger.error("unable to register java-logging adapter",e);
    }

    Application.getMessagingFactory().getMessagingQueue("jameica.soap.publish").registerMessageConsumer(PublishServiceMessageConsumer.create());  
  }
}


/**********************************************************************
 * $Log: Plugin.java,v $
 * Revision 1.9  2010/01/19 00:33:59  willuhn
 * @C Publishing der Webservices aus MessageConsumer in dedizierten PublishService verschoben
 * @N Auto-Deployment von Services via AutoService
 *
 * Revision 1.8  2009/04/29 21:10:22  willuhn
 * @R removed unused/deprecated code
 *
 * Revision 1.7  2008/08/08 11:24:26  willuhn
 * @N Console-Logging von Java-Logging ausschalten. Da wir es auf den Jameica-Logger umbiegen, wuerde es sonst doppelt auf der Console erscheinen
 *
 * Revision 1.6  2008/07/11 16:50:13  willuhn
 * *** empty log message ***
 *
 * Revision 1.5  2008/07/11 15:38:52  willuhn
 * @N Service-Deployment
 *
 * Revision 1.4  2008/07/10 09:19:11  willuhn
 * *** empty log message ***
 *
 * Revision 1.3  2008/07/09 23:30:53  willuhn
 * @R Nicht benoetigte Jars (gemaess WHICH_JARS) entfernt
 * @N Deployment vereinfacht
 *
 * Revision 1.2  2008/07/08 14:27:37  willuhn
 * @C Konstruktor war deprecated
 *
 * Revision 1.1  2007/09/29 17:19:43  willuhn
 * @N initial import
 *
 **********************************************************************/
