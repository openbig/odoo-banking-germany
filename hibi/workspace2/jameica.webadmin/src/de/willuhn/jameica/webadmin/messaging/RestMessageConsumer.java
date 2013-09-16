/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/messaging/RestMessageConsumer.java,v $
 * $Revision: 1.2 $
 * $Date: 2010/09/13 12:45:44 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.webadmin.messaging;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.MessagingQueue;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.SystemMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.webadmin.rest.AutoRestBean;
import de.willuhn.logging.Logger;
import de.willuhn.util.ClassFinder;

/**
 * Registriert die AutoRestBeans.
 */
public class RestMessageConsumer implements MessageConsumer
{

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return true;
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{SystemMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    SystemMessage m = (SystemMessage) message;
    if (m.getStatusCode() != SystemMessage.SYSTEM_STARTED)
      return;
    
    try
    {
      Logger.info("searching auto rest beans");
      ClassFinder finder = Application.getClassLoader().getClassFinder();
      Class<AutoRestBean>[] classes = finder.findImplementors(AutoRestBean.class);
      MessagingQueue queue = Application.getMessagingFactory().getMessagingQueue("jameica.webadmin.rest.register");
      for (Class<AutoRestBean> c:classes)
      {
        try
        {
          AutoRestBean b = c.newInstance();
          queue.sendMessage(new QueryMessage(b));
        }
        catch (Exception e)
        {
          Logger.error("unable to init auto rest bean " + c.getName() + ", skipping",e);
        }
      }
    }
    catch (ClassNotFoundException e)
    {
      Logger.warn("no auto rest beans found");
    }
  }

}


/**********************************************************************
 * $
 **********************************************************************/
