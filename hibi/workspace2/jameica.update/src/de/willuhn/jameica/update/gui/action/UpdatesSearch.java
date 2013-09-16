/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/gui/action/UpdatesSearch.java,v $
 * $Revision: 1.1 $
 * $Date: 2009/10/29 18:06:06 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.update.gui.action;

import java.util.List;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.gui.GUI;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.BackgroundTask;
import de.willuhn.jameica.update.Plugin;
import de.willuhn.jameica.update.gui.views.UpdatesView;
import de.willuhn.jameica.update.rmi.PluginData;
import de.willuhn.jameica.update.rmi.UpdateService;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;
import de.willuhn.util.ProgressMonitor;

/**
 * Sucht manuell nach Updates.
 */
public class UpdatesSearch implements Action
{
  private final static I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    Application.getController().start(new BackgroundTask() {
    
      /**
       * @see de.willuhn.jameica.system.BackgroundTask#run(de.willuhn.util.ProgressMonitor)
       */
      public void run(ProgressMonitor monitor) throws ApplicationException
      {
        UpdateService service = null;
        try
        {
          service = (UpdateService) Application.getServiceFactory().lookup(Plugin.class,"update");
        }
        catch (ApplicationException ae)
        {
          throw ae;
        }
        catch (Exception e)
        {
          Logger.error("error while loading update service",e);
          throw new ApplicationException(i18n.tr("Update-Service kann nicht geladen werden: {0}",e.getMessage()));
        }
        
        List<PluginData> updates = service.findUpdates(monitor);
        if (updates.size() > 0)
          GUI.startView(UpdatesView.class,updates);
      }
    
      /**
       * @see de.willuhn.jameica.system.BackgroundTask#isInterrupted()
       */
      public boolean isInterrupted()
      {
        return false;
      }
    
      /**
       * @see de.willuhn.jameica.system.BackgroundTask#interrupt()
       */
      public void interrupt()
      {
      }
    });
  }

}


/**********************************************************************
 * $
 **********************************************************************/
