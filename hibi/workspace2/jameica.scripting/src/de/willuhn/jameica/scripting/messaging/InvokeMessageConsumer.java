/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.scripting/src/de/willuhn/jameica/scripting/messaging/InvokeMessageConsumer.java,v $
 * $Revision: 1.9 $
 * $Date: 2012/01/09 22:20:27 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.scripting.messaging;

import java.util.ArrayList;
import java.util.List;

import javax.script.Invocable;
import javax.script.ScriptEngine;

import de.willuhn.jameica.messaging.Message;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.scripting.Plugin;
import de.willuhn.jameica.scripting.services.ScriptingService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Message-Consumer, der QueryMessages mit Script-Funktionen ausfuehrt.
 */
public class InvokeMessageConsumer implements MessageConsumer
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
  private final static String PREFIX_FX = "function.";
  
  /**
   * @see de.willuhn.jameica.messaging.MessageConsumer#autoRegister()
   */
  public boolean autoRegister()
  {
    // Wird manuell ueber den Service registriert.
    return false;
  }

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
    if (!(message instanceof QueryMessage))
      return;
    
    QueryMessage msg = (QueryMessage) message;

    ScriptingService service = (ScriptingService) Application.getServiceFactory().lookup(Plugin.class,"scripting");
    ScriptEngine engine = service.getEngine();
    if (engine == null)
    {
      Logger.warn("scripting service not started, skipping script execution");
      msg.setData(new ApplicationException(i18n.tr("Scripting-Service nicht gestartet")));
      return;
    }
    
    String event = msg.getName();
    if (event == null || event.length() == 0)
    {
      Logger.warn("no event name given for script execution");
      msg.setData(new ApplicationException(i18n.tr("Kein Event-Name angegeben")));
      return;
    }
    
    List<String> functions = null;

    // Wenn der Event-Name mit "function." beginnt, rufen wir
    // diese Funktion direkt auf - ohne Mapping ueber die Events
    if (event.startsWith(PREFIX_FX) && event.length() > PREFIX_FX.length())
    {
      functions = new ArrayList<String>();
      functions.add(event.substring(PREFIX_FX.length()));
    }
    else
    {
      // Checken, ob Funktionen fuer das Event registriert sind.
      functions = service.getFunction(event);
    }
    
    if (functions == null || functions.size() == 0)
    {
      Logger.debug("no script functions registered for event " + event);
      msg.setData(new ApplicationException(i18n.tr("Kein passendes Script gefunden")));
      return;
    }
    
    List returns = new ArrayList();
    Invocable i = (Invocable) engine;
    Object params = msg.getData();

    for (String method:functions)
    {
      try
      {
        Object value = null;
        
        if (params != null && params.getClass().isArray())
          value = i.invokeFunction(method,(Object[]) params);
        else
          value = i.invokeFunction(method,params);
        
        if (value != null)
          returns.add(value);
      }
      catch (NoSuchMethodException nme)
      {
        Logger.debug("script method not found: " + method);
        returns.add(new ApplicationException(i18n.tr("Funktion nicht in Script gefunden")));
      }
      catch (Exception e)
      {
        Logger.error("error while executing script method " + method + ", adding exception to return list",e);
        returns.add(e);
      }
    }
    
    // Rueckgabewert der Funktionen
    if (returns.size() == 0)
      msg.setData(null); // Rueckgabewert leeren
    else if (returns.size() == 1)
      msg.setData(returns.get(0)); // nur ein Wert, dann nehmen wir den direkt
    else
      msg.setData(returns); // mehrere Werte, dann liefern wir alle zurueck
  }

}
