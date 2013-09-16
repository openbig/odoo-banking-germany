/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.webadmin/src/de/willuhn/jameica/webadmin/messaging/TrustMessageConsumer.java,v $
 * $Revision: 1.1 $
 * $Date: 2011/01/27 16:26:55 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.webadmin.messaging;

import de.willuhn.jameica.messaging.CheckTrustMessage;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.logging.Logger;

/**
 * Mit diesem Consumer koennen wir Zertifikaten vertrauen.
 */
public class TrustMessageConsumer implements MessageConsumer
{

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return false;
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{CheckTrustMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    if (message == null || !(message instanceof CheckTrustMessage))
      return;
    
    CheckTrustMessage msg = (CheckTrustMessage) message;
    Logger.info("applying trust state to certificate: " + msg.getCertificate().toString());
    msg.setTrusted(true,"jameica.webadmin");
  }
}


/*********************************************************************
 * $Log: TrustMessageConsumer.java,v $
 * Revision 1.1  2011/01/27 16:26:55  willuhn
 * @N Importieren und Loeschen von SSL-Zertifikaten
 *
 **********************************************************************/