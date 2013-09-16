/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.scripting/src/de/willuhn/jameica/scripting/messaging/AddScriptMessageConsumer.java,v $
 * $Revision: 1.2 $
 * $Date: 2012/04/05 23:24:09 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.scripting.messaging;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.scripting.Settings;
import de.willuhn.logging.Logger;

/**
 * Registriert ein Script, welches via Messaging uebergeben wurde.
 */
public class AddScriptMessageConsumer implements MessageConsumer
{

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{QueryMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    QueryMessage msg = (QueryMessage) message;
    Object data = msg.getData();
    if (data == null)
    {
      Logger.warn("no message data given, ignoring message");
      return;
    }
    
    String s = StringUtils.trimToNull(data.toString());
    if (s == null)
    {
      Logger.warn("no message data given, ignoring message");
      return;
    }
    
    File file = new File(s);
    if (!file.exists() || !file.canRead())
    {
      Logger.warn(s + " is no valid file or does not exist");
      return;
    }
    
    if (Settings.contains(file))
    {
      // loggen wir mit debug-Level, damit das auch problemlos mehrfach aufgerufen werden kann.
      Logger.debug(file + " allready registered");
      return;
    }
    Settings.addScript(file);
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    // geschieht uebers Manifest.
    return false;
  }

}



/**********************************************************************
 * $Log: AddScriptMessageConsumer.java,v $
 * Revision 1.2  2012/04/05 23:24:09  willuhn
 * @B da fehlte ein return ;)
 *
 * Revision 1.1  2012/03/20 23:14:13  willuhn
 * @N Support zum Registrieren von Scripts via Messaging (erster Teil von BUGZILLA 1208)
 *
 **********************************************************************/