/**********************************************************************
 * $Source: /cvsroot/jameica/jameica/src/de/willuhn/jameica/system/Server.java,v $
 * $Revision: 1.18 $
 * $Date: 2011/08/02 15:06:47 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn.webdesign
 * All rights reserved
 *
 **********************************************************************/
package de.willuhn.jameica.system;

import de.willuhn.jameica.messaging.SystemMessage;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.ProgressMonitor;

/**
 * Diese Klasse bildet den Serverloop der Anwendung ab.
 * @author willuhn
 */
public class Server implements ApplicationController
{
  private ApplicationCallback callback = null;
  
  /**
   * @see de.willuhn.jameica.system.ApplicationController#init()
   */
  public void init() throws ApplicationException
  {
    Application.getMessagingFactory().sendMessage(new SystemMessage(SystemMessage.SYSTEM_STARTED,"jameica up and running..."));

		String[] welcome = Application.getWelcomeMessages();
		if (welcome != null && welcome.length > 0)
		{
      try
      {
        Logger.flush();
      }
      catch (InterruptedException e)
      {
        // ignore
      }
      Logger.info("----------------------------------------------------------------------");
      Logger.info(Application.getI18n().tr("Startup-Messages:"));
			for (int i=0;i<welcome.length;++i)
			{
        Logger.info("  " + welcome[i]); 
			}
		}
    Logger.info("----------------------------------------------------------------------");
    if (!Application.inNonInteractiveMode())
      Logger.info(Application.getI18n().tr("press \"<CTRL><C>\" to shut down the server."));
    
    if (Application.inNonInteractiveMode())
    {
      try
      {
        Logger.flush();
        Logger.info("detatching terminal");
        Logger.flush();
        System.out.close();
        System.in.close();
        System.err.close();
        Logger.info("terminal detached");
      }
      catch (Exception e)
      {
        Logger.error("unable to detach terminal",e);
      }
    }
    
    // TODO: JameicaException muesste hier mal noch behandelt werden
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationController#shutDown()
   */
  public void shutDown()
  {
    Application.shutDown();
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationController#getApplicationCallback()
   */
  public ApplicationCallback getApplicationCallback()
  {
    if (callback == null)
      callback = new ApplicationCallbackConsole();
    return callback;
  }

  /**
   * @see de.willuhn.jameica.system.ApplicationController#start(de.willuhn.jameica.system.BackgroundTask)
   */
  public void start(final BackgroundTask task)
  {
    Thread t = new Thread("[Jameica Backgroundtask] " + task.getClass().getName())
    {
      public void run()
      {
        ProgressMonitor monitor = new ConsoleMonitor();
        try
        {
          task.run(monitor);
        }
        catch (OperationCanceledException oce)
        {
          if (monitor != null) monitor.setStatus(ProgressMonitor.STATUS_CANCEL);
        }
        catch (Throwable t)
        {
          Logger.error("error while executing background task",t);
          if (monitor != null) monitor.setStatus(ProgressMonitor.STATUS_ERROR);
        }
        finally
        {
          if (monitor != null) monitor.setStatus(ProgressMonitor.STATUS_DONE);
        }
      }
    };
    t.start();
  }
}

/*********************************************************************
 * $Log: Server.java,v $
 * Revision 1.18  2011/08/02 15:06:47  willuhn
 * @N ConsoleMonitor in extra Klasse ausgelagert
 *
 * Revision 1.17  2011-07-12 15:21:30  willuhn
 * @N JameicaException
 *
 * Revision 1.16  2011-04-26 12:09:18  willuhn
 * @B Potentielle Bugs gemaess Code-Checker
 **********************************************************************/