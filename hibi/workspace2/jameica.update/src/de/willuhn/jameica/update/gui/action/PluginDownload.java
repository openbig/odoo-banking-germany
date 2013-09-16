/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/gui/action/PluginDownload.java,v $
 * $Revision: 1.5 $
 * $Date: 2009/10/28 17:00:58 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.update.gui.action;

import java.rmi.RemoteException;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.update.Plugin;
import de.willuhn.jameica.update.gui.dialogs.DependencyDialog;
import de.willuhn.jameica.update.rmi.PluginData;
import de.willuhn.jameica.update.rmi.Repository;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Laedt das uebergebene Plugin herunter und installiert es.
 */
public class PluginDownload implements Action
{

  /**
   * Erwartet ein Objekt vom Typ PluginData.
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();
    
    if (context == null)
      throw new ApplicationException(i18n.tr("Kein Plugin angegeben"));

    PluginData data = (PluginData) context;
    
    try
    {
      if (!data.isInstallable())
      {
        DependencyDialog d = new DependencyDialog(DependencyDialog.POSITION_CENTER,(PluginData)context);
        d.open();
        return;
      }
      
      if (!Application.getCallback().askUser(i18n.tr("Sind Sie sicher, daß Sie das Plugin\nherunterladen und installieren möchten?")))
        return;
    }
    catch (OperationCanceledException oce)
    {
      return;
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("unable to ask user",e);
      throw new ApplicationException(i18n.tr("Fehler beim Download des Plugins: {0}",e.getMessage()));
    }

    try
    {
      Repository repo = data.getPluginGroup().getRepository();
      repo.download(data,true);
    }
    catch (RemoteException re)
    {
      Logger.error("unable to download plugin",re);
      throw new ApplicationException(i18n.tr("Fehler beim Download des Plugins: {0}",re.getMessage()));
    }
  }
}


/**********************************************************************
 * $
 **********************************************************************/
