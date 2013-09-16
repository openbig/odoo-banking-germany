/**********************************************************************
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.scripting.messaging;

import java.io.File;
import java.util.List;

import de.willuhn.io.FileFinder;
import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.PluginMessage;
import de.willuhn.jameica.messaging.PluginMessage.Event;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.scripting.Settings;
import de.willuhn.jameica.system.Application;

/**
 * Wird benachrichtigt, wenn ein Plugin deinstalliert wird.
 * Wenn das Plugin ein Script registriert hatte, entfernen wir das hierbei.
 */
public class PluginUninstallMessageConsumer implements MessageConsumer
{

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#getExpectedMessageTypes()
   */
  public Class[] getExpectedMessageTypes()
  {
    return new Class[]{PluginMessage.class};
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#handleMessage(de.willuhn.jameica.messaging.Message)
   */
  public void handleMessage(Message message) throws Exception
  {
    PluginMessage msg = (PluginMessage) message;
    Event e = msg.getEvent();
    if (e == null || e != Event.UNINSTALLED)
      return;
    
    // Basis-Verzeichnis des Plugins ermitteln und nachschauen, ob in dem
    // Ordner eine Script-Datei liegt, die registriert ist.
    File dir = new File(msg.getManifest().getPluginDir());
    if (!dir.exists() || !dir.isDirectory())
      return;
    
    FileFinder finder = new FileFinder(dir);
    finder.extension(".js");
    File[] matches     = finder.findRecursive();
    List<File> scripts = Settings.getScripts();
    
    for (File f:matches)
    {
      if (scripts.contains(f))
      {
        Application.getMessagingFactory().getMessagingQueue("jameica.scripting.remove").sendMessage(new QueryMessage(f));
      }
    }
  }

  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    return true;
  }
}
