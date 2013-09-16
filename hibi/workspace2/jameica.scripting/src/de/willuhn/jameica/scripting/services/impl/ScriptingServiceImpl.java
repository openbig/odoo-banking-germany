/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.scripting/src/de/willuhn/jameica/scripting/services/impl/ScriptingServiceImpl.java,v $
 * $Revision: 1.5 $
 * $Date: 2011/04/01 10:16:08 $
 * $Author: willuhn $
 *
 * Copyright (c) by willuhn - software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.scripting.services.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.script.ScriptEngine;

import de.willuhn.io.FileWatch;
import de.willuhn.jameica.messaging.MessageConsumer;
import de.willuhn.jameica.scripting.Plugin;
import de.willuhn.jameica.scripting.Settings;
import de.willuhn.jameica.scripting.messaging.InvokeMessageConsumer;
import de.willuhn.jameica.scripting.services.ScriptingService;
import de.willuhn.jameica.system.Application;
import de.willuhn.logging.Logger;
import de.willuhn.util.I18N;

/**
 * Implementierung des Scripting-Services.
 */
public class ScriptingServiceImpl implements ScriptingService
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
  
  private ScriptEngine engine            = null;
  private MessageConsumer mc             = null;
  private List<File> files               = null;
  private Events events                  = new Events();

  /**
   * @see de.willuhn.datasource.Service#getName()
   */
  public String getName() throws RemoteException
  {
    return "Scripting-Service";
  }

  /**
   * @see de.willuhn.datasource.Service#isStartable()
   */
  public boolean isStartable() throws RemoteException
  {
    return !this.isStarted();
  }

  /**
   * @see de.willuhn.datasource.Service#isStarted()
   */
  public boolean isStarted() throws RemoteException
  {
    return this.engine != null;
  }

  /**
   * @see de.willuhn.datasource.Service#start()
   */
  public void start() throws RemoteException
  {
    if (this.isStarted())
    {
      Logger.info("service allready started, skipping request");
      return;
    }
    
    this.events.clear();

    // 1. Script-Engine laden
    this.engine = Settings.getScriptEngine();
    if (this.engine == null)
    {
      Logger.warn("no script engine found");
      return;
    }
    
    this.engine.put("events",this.events);

    // 2. Vom Benutzer registrierte Scripts ausfuehren.
    this.files = Settings.getScripts();
    for (final File f:this.files)
    {
      if (!f.isFile() || !f.canRead())
      {
        Application.addWelcomeMessage(i18n.tr("Script {0} nicht lesbar",f.getAbsolutePath()));
        continue;
      }
      eval(f);
      FileWatch.addFile(f,new Observer() {
        public void update(Observable o, Object arg)
        {
          Logger.info("auto-reloading script " + f);
          eval(f);
        }
      });
    }
    
    // 3. Message-Consumer fuer Invoke-Aufrufe
    this.mc = new InvokeMessageConsumer();
    Application.getMessagingFactory().getMessagingQueue("jameica.scripting").registerMessageConsumer(this.mc);
  }
  
  /**
   * Fuehrt die Script-Datei aus.
   * @param f die auszufuehrende Script-Datei.
   */
  private void eval(File f)
  {
    Logger.info("executing script " + f);
    if (!f.exists() || !f.canRead() || !f.isFile())
    {
      Logger.warn("  not found or not readable, skipping");
      return;
    }
    Reader r = null;
    try
    {
      de.willuhn.jameica.system.Settings settings = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getSettings();
      String encoding = settings.getString("script.encoding",null);
      if (encoding != null)
        r = new BufferedReader(new InputStreamReader(new FileInputStream(f),encoding));
      else
        r = new BufferedReader(new InputStreamReader(new FileInputStream(f))); // Der Reader wirft leider eine NPE, wenn man ihm NULL als charset gibt
      this.getEngine().eval(r);
    }
    catch (Exception e)
    {
      Logger.error("error while loading script",e);
    }
    finally
    {
      if (r != null)
      {
        try
        {
          r.close();
        }
        catch (Exception e)
        {
          Logger.error("unable to close file " + f,e);
        }
      }
    }
  }

  /**
   * @see de.willuhn.datasource.Service#stop(boolean)
   */
  public void stop(boolean arg0) throws RemoteException
  {
    if (!this.isStarted())
    {
      Logger.info("service not started, skipping request");
      return;
    }
    try
    {
      if (this.files != null)
      {
        for (File f:this.files)
        {
          FileWatch.removeFile(f);
        }
      }
    }
    finally
    {
      this.engine = null;
      this.files = null;
      Application.getMessagingFactory().getMessagingQueue("jameica.scripting").unRegisterMessageConsumer(this.mc);
    }
  }

  /**
   * @see de.willuhn.jameica.scripting.services.ScriptingService#getEngine()
   */
  public ScriptEngine getEngine()
  {
    return this.engine;
  }

  /**
   * @see de.willuhn.jameica.scripting.services.ScriptingService#getFunction(java.lang.String)
   */
  public List<String> getFunction(String event)
  {
    return this.events.get(event);
  }
  
  
  /**
   * Hilfsklasse zum Mappen der Events auf die JS-Funktionen.
   */
  public class Events
  {
    private Map<String,List<String>> mapping = new HashMap<String,List<String>>();
    
    /**
     * Liefert eine Liste der JS-Funktionen fuer das Event.
     * @param event das Event.
     * @return Liste der Funktionen oder NULL, wenn keine definiert sind.
     */
    public List<String> get(String event)
    {
      return this.mapping.get(event);
    }
    
    /**
     * Fuegt die JS-Funktion dem Event hinzu.
     * @param event das Event.
     * @param function die Funktion.
     */
    public void add(String event, String function)
    {
      List<String> functions = get(event);
      if (functions == null)
      {
        functions = new ArrayList<String>();
        this.mapping.put(event,functions);
      }
      
      // Nur, wenn wir sie noch nicht haben. Andernfalls wuerden
      // wir die Funktion immer wieder erneut registrieren
      if (!functions.contains(function))
      {
        Logger.info("registering script function \"" + function + "\" for event \"" + event + "\"");
        functions.add(function);
      }
    }
    
    /**
     * Leert alle Mappings.
     */
    public void clear()
    {
      this.mapping.clear();
    }
  }
  
}



/**********************************************************************
 * $Log: ScriptingServiceImpl.java,v $
 * Revision 1.5  2011/04/01 10:16:08  willuhn
 * @N BUGZILLA 1011
 *
 * Revision 1.4  2010-07-29 23:43:38  willuhn
 * @N Script-Mapping. Damit ueberschreiben sich die Script-Funktionen nicht mehr gegenseitig, wenn mehrere Scripts registriert wurden
 *
 * Revision 1.3  2010-07-25 23:14:12  willuhn
 * @N Beispiel-Script
 *
 * Revision 1.2  2010-07-24 00:23:20  willuhn
 * @N Erste funktionierende Version
 *
 * Revision 1.1  2010-07-23 12:58:33  willuhn
 * @N initial import
 *
 **********************************************************************/