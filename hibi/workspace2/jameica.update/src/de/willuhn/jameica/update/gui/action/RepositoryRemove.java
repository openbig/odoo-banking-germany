/**********************************************************************
 * $Source: /cvsroot/jameica/jameica.update/src/de/willuhn/jameica/update/gui/action/RepositoryRemove.java,v $
 * $Revision: 1.3 $
 * $Date: 2009/01/18 00:18:56 $
 * $Author: willuhn $
 * $Locker:  $
 * $State: Exp $
 *
 * Copyright (c) by willuhn software & services
 * All rights reserved
 *
 **********************************************************************/

package de.willuhn.jameica.update.gui.action;

import java.net.URL;

import de.willuhn.jameica.gui.Action;
import de.willuhn.jameica.messaging.QueryMessage;
import de.willuhn.jameica.messaging.StatusBarMessage;
import de.willuhn.jameica.system.Application;
import de.willuhn.jameica.system.OperationCanceledException;
import de.willuhn.jameica.update.Plugin;
import de.willuhn.jameica.update.Settings;
import de.willuhn.jameica.update.rmi.RepositoryService;
import de.willuhn.logging.Logger;
import de.willuhn.util.ApplicationException;
import de.willuhn.util.I18N;

/**
 * Aktion zum Loeschen eines Repository.
 */
public class RepositoryRemove implements Action
{

  /**
   * @see de.willuhn.jameica.gui.Action#handleAction(java.lang.Object)
   */
  public void handleAction(Object context) throws ApplicationException
  {
    if (context == null)
      return;
    
    I18N i18n = Application.getPluginLoader().getPlugin(Plugin.class).getResources().getI18N();

    String s = context.toString();
    
    if (Settings.SYSTEM_REPOSITORY.equalsIgnoreCase(s))
      throw new ApplicationException(i18n.tr("System-Repository darf nicht gelöscht werden"));
    
    URL url = null;
    try
    {
      url = new URL(s);
    }
    catch (Exception e)
    {
      Logger.error("invalid url: " + context,e);
      throw new ApplicationException(i18n.tr("Keine gültige Repository-URL angegeben"));
    }
    
    String q = i18n.tr("Sind Sie sicher, daß Sie diese URL löschen möchten?\n\n{0}",url.toString());
    
    try
    {
      if (!Application.getCallback().askUser(q))
        return;

      RepositoryService service = (RepositoryService) Application.getServiceFactory().lookup(Plugin.class,"repository");
      service.removeRepository(url);
      
      Application.getMessagingFactory().getMessagingQueue("jameica.update.repository.remove").sendMessage(new QueryMessage(url));
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Repository-URL gelöscht"),StatusBarMessage.TYPE_SUCCESS));
    }
    catch (OperationCanceledException oce)
    {
      // ignore
    }
    catch (ApplicationException ae)
    {
      throw ae;
    }
    catch (Exception e)
    {
      Logger.error("error while deleting url " + context,e);
      Application.getMessagingFactory().sendMessage(new StatusBarMessage(i18n.tr("Fehler beim Löschen der Repository-URL"),StatusBarMessage.TYPE_ERROR));
    }
  }

}


/**********************************************************************
 * $
 **********************************************************************/
