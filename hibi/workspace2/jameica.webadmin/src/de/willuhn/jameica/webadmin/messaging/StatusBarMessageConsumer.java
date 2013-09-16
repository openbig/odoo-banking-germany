/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/messaging/StatusBarMessageConsumer.java,v $
 * $Revision: 1.1 $
 * $Date: 2010/11/02 00:56:31 $
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
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;

/**
 * Speichert die letzte empfangene Statusbar-Message.
 */
public class StatusBarMessageConsumer implements MessageConsumer
{
  private static StatusBarMessage last = null;

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
    return new Class[]{StatusBarMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    last = (StatusBarMessage) message;
  }

  /**
   * Liefert die letzte Nachricht.
   * @return die letzte Nachricht oder null.
   */
  public static StatusBarMessage getLastMessage()
  {
    // Wir warten ggf. noch einen kurzen Moment, bis alle Nachrichten zugestellt sind.
    int maxRetries = 0;
    while (Application.getMessagingFactory().getQueueSize() > 0 && maxRetries++ < 4)
    {
      try
      {
        Thread.sleep(100l);
      }
      catch (InterruptedException e)
      {
        break;
      }
    }
    return last;
  }
}


/*********************************************************************
 * $Log: StatusBarMessageConsumer.java,v $
 * Revision 1.1  2010/11/02 00:56:31  willuhn
 * @N Umstellung des Webfrontends auf Velocity/Webtools
 *
 **********************************************************************/